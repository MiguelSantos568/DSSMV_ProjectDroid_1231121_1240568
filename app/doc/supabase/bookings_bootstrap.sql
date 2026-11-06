-- Opcional: dados fictícios para testar a lista e os conflitos na própria base.
-- Executar depois de bookings.sql e de registar a habitação 1 através do QR.
-- Os IDs de comodidade e residente são referências externas: ajustar ao ambiente de testes.
-- Não consulta nem altera a TheCondo. Não apaga nem substitui reservas existentes.
begin;

do $$
begin
    if not exists (select 1 from public.habitation_profiles where unit_id = 1) then
        raise exception 'Regista primeiro a habitacao 1 na app antes de executar este bootstrap';
    end if;
end;
$$;

with examples(marker, start_hour, end_hour, booking_status) as (
    values
        ('bootstrap-demo-1', time '10:00', time '11:00', 'confirmed'),
        ('bootstrap-demo-2', time '11:00', time '12:00', 'confirmed'),
        ('bootstrap-demo-3', time '14:00', time '15:00', 'cancelled')
)
insert into public.bookings (
    unit_id, amenity_id, resident_id, start_time, end_time,
    number_of_people, status, fee, notes
)
select 1, 1, 1,
    ((now() at time zone 'Europe/Lisbon')::date + 2) + e.start_hour,
    ((now() at time zone 'Europe/Lisbon')::date + 2) + e.end_hour,
    2, e.booking_status, 0, e.marker
from examples e
where not exists (
    select 1 from public.bookings b where b.unit_id = 1 and b.notes = e.marker
)
on conflict do nothing;

commit;
