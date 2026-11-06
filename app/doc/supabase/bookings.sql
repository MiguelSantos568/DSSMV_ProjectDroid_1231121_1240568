-- Executar depois de habitation_profiles.sql no SQL Editor do Supabase.
-- Os horários representam a hora local do condomínio em Europe/Lisbon.
begin;

create schema if not exists extensions;
create extension if not exists btree_gist with schema extensions;
set local search_path = public, extensions;

create table if not exists public.bookings (
    id bigint generated always as identity primary key,
    unit_id bigint not null references public.habitation_profiles(unit_id),
    amenity_id bigint not null check (amenity_id > 0),
    resident_id bigint not null check (resident_id > 0),
    start_time timestamp without time zone not null,
    end_time timestamp without time zone not null,
    number_of_people integer not null check (number_of_people > 0),
    status text not null default 'confirmed' check (status in ('confirmed', 'pending', 'cancelled', 'rejected')),
    fee numeric check (fee >= 0),
    notes text not null default '' check (length(notes) <= 500),
    created_at timestamptz not null default now(),
    constraint booking_valid_period check (end_time > start_time),
    constraint booking_single_day check (
        end_time::date = start_time::date or
        (end_time::date = start_time::date + 1 and end_time::time = time '00:00')
    ),
    -- A base de dados impede sobreposições, mesmo em pedidos simultâneos.
    -- [) inclui o início e exclui o fim: horários adjacentes são permitidos.
    constraint bookings_no_overlap exclude using gist (
        amenity_id with =,
        tsrange(start_time, end_time, '[)') with &&
    ) where (status in ('confirmed', 'pending'))
);

create index if not exists bookings_unit_start_idx on public.bookings(unit_id, start_time desc);

alter table public.bookings enable row level security;
revoke all on table public.bookings from public, anon, authenticated;
revoke all on sequence public.bookings_id_seq from public, anon, authenticated;

create or replace function public.get_home_bookings(p_unit_id bigint)
returns setof public.bookings
language sql stable security definer
set search_path = ''
as $$
    select * from public.bookings
    where unit_id = p_unit_id
    order by start_time desc, id desc;
$$;

-- A disponibilidade não expõe o contacto, residente, observações ou ID da casa.
create or replace function public.get_amenity_bookings(p_amenity_id bigint)
returns table (amenity_id bigint, start_time timestamp, end_time timestamp, status text)
language sql stable security definer
set search_path = ''
as $$
    select b.amenity_id, b.start_time, b.end_time, b.status
    from public.bookings b
    where b.amenity_id = p_amenity_id
      and b.status in ('confirmed', 'pending')
      and b.end_time > (now() at time zone 'Europe/Lisbon')
    order by b.start_time;
$$;

create or replace function public.create_booking(p_booking jsonb)
returns setof public.bookings
language plpgsql security definer
set search_path = ''
as $$
declare
    home_id bigint := (p_booking->>'unit_id')::bigint;
    space_id bigint := (p_booking->>'amenity_id')::bigint;
    person_id bigint := (p_booking->>'resident_id')::bigint;
    starts timestamp := (p_booking->>'start_time')::timestamp;
    ends timestamp := (p_booking->>'end_time')::timestamp;
    people integer := (p_booking->>'number_of_people')::integer;
    amount numeric := (p_booking->>'fee')::numeric;
    description text := btrim(coalesce(p_booking->>'notes', ''));
begin
    if home_id is null or home_id <= 0 or space_id is null or space_id <= 0
        or person_id is null or person_id <= 0
        or starts is null or ends is null or ends <= starts
        or starts <= (now() at time zone 'Europe/Lisbon')
        or people is null or people <= 0
        or length(description) > 500
        or (amount is not null and (amount < 0 or amount = 'NaN'::numeric or amount = 'Infinity'::numeric))
    then
        raise exception 'Invalid booking' using errcode = '22023';
    end if;

    if not exists (select 1 from public.habitation_profiles where unit_id = home_id) then
        raise exception 'Home profile not found' using errcode = '22023';
    end if;

    -- O servidor decide o estado; o cliente não pode forçar estados arbitrários.
    -- O conflito é tratado abaixo e devolvido à app como HTTP 409.
    return query
    insert into public.bookings (
        unit_id, amenity_id, resident_id, start_time, end_time,
        number_of_people, status, fee, notes
    ) values (
        home_id, space_id, person_id, starts, ends,
        people, 'confirmed', amount, description
    ) returning *;
exception
    when exclusion_violation then
        raise sqlstate 'PT409' using message = 'Booking period already occupied';
end;
$$;

create or replace function public.cancel_booking(p_unit_id bigint, p_booking_id bigint)
returns setof public.bookings
language sql volatile security definer
set search_path = ''
as $$
    update public.bookings
    set status = 'cancelled'
    where id = p_booking_id and unit_id = p_unit_id
      and status in ('confirmed', 'pending', 'cancelled')
    returning *;
$$;

-- Modelo do protótipo: o ID da casa do QR é usado para consultar as suas reservas.
-- Estas funções não constituem autenticação individual de residentes.
revoke all on function public.get_home_bookings(bigint) from public;
revoke all on function public.get_amenity_bookings(bigint) from public;
revoke all on function public.create_booking(jsonb) from public;
revoke all on function public.cancel_booking(bigint, bigint) from public;
grant execute on function public.get_home_bookings(bigint) to anon, authenticated;
grant execute on function public.get_amenity_bookings(bigint) to anon, authenticated;
grant execute on function public.create_booking(jsonb) to anon, authenticated;
grant execute on function public.cancel_booking(bigint, bigint) to anon, authenticated;

notify pgrst, 'reload schema';
commit;
