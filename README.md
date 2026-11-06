# CondoSched

Projeto Android em Java da unidade curricular DSSMV 2026/27. Compatível com API 26+.

O ponto de entrada é `ui/AccessActivity`. O código está separado em `ui`, `viewmodel`,
`repository`, `network`, `dto`, `domain` e `session`.

Documentação técnica:

- [Organização geral da app](app/doc/Architecture-guide.md)
- [Acesso por QR, registo e sessão](app/doc/QR-access.md)
- [Comodidades e marcações](app/doc/Bookings.md)
- [Diagrama dos modelos de domínio](app/doc/DomainModel.puml)

`Habitation` representa a casa selecionada e o seu condomínio/prédio. Os DTOs
`UnitDTO`, `BuildingDTO` e `CondominiumDTO` são os contratos da API, convertidos
para esse modelo. Os testes estão em `app/src/test`, separados do código da app.

Perfis e reservas são guardados no Supabase. A TheCondo é usada apenas para
consultar os dados do condomínio, habitações, comodidades e residentes.

No SQL Editor do Supabase, executar primeiro `app/doc/supabase/habitation_profiles.sql`
e depois `app/doc/supabase/bookings.sql`. Para dados de teste opcionais, consultar
`app/doc/supabase/bookings_bootstrap.sql`.

Com Gradle instalado, executar:

```text
gradle :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```
