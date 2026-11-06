-- Run this once in the Supabase SQL Editor for the project configured in SupabaseConfig.
-- Creates a shared habitation profile table, separate from Supabase Auth (auth.users).
-- Access model for this prototype: knowing the unit ID (the QR content) grants access.
begin;

create table if not exists public.habitation_profiles (
    unit_id bigint primary key check (unit_id > 0),
    full_name text not null check (length(btrim(full_name)) between 1 and 120),
    email text not null check (length(email) <= 254 and
        email ~ '^[^[:space:]@]+@[^[:space:]@]+[.][^[:space:]@]+$'),
    created_at timestamptz not null default now()
);

-- The table cannot be read or written directly by the mobile app.
alter table public.habitation_profiles enable row level security;
revoke all on table public.habitation_profiles from public, anon, authenticated;

create or replace function public.get_habitation_profile(p_unit_id bigint)
returns setof public.habitation_profiles
language sql stable security definer
set search_path = ''
as $$
    select * from public.habitation_profiles where unit_id = p_unit_id;
$$;

create or replace function public.create_habitation_profile(
    p_unit_id bigint, p_full_name text, p_email text
)
returns setof public.habitation_profiles
language plpgsql security definer
set search_path = ''
as $$
begin
    if p_unit_id is null or p_unit_id <= 0
        or p_full_name is null or length(btrim(p_full_name)) not between 1 and 120
        or p_email is null or length(btrim(p_email)) > 254
        or btrim(p_email) !~ '^[^[:space:]@]+@[^[:space:]@]+[.][^[:space:]@]+$'
    then
        raise exception 'Invalid home profile' using errcode = '22023';
    end if;

    -- Concurrent registrations cannot duplicate or overwrite the home profile.
    insert into public.habitation_profiles (unit_id, full_name, email)
    values (p_unit_id, btrim(p_full_name), btrim(p_email))
    on conflict (unit_id) do nothing;

    -- A separate statement sees the row committed by a concurrent first registration.
    return query select * from public.habitation_profiles where unit_id = p_unit_id;
end;
$$;

revoke all on function public.get_habitation_profile(bigint) from public;
revoke all on function public.create_habitation_profile(bigint, text, text) from public;
grant execute on function public.get_habitation_profile(bigint) to anon, authenticated;
grant execute on function public.create_habitation_profile(bigint, text, text) to anon, authenticated;

notify pgrst, 'reload schema';
commit;
