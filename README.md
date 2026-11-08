# CondoSched

Aplicação Android em Java para DSSMV 2026/27, compatível com API 26+.
O acesso é feito por QR de uma habitação. A app consulta comodidades e permite
criar, consultar e apagar marcações, com previsão meteorológica para a data escolhida.

## Guias para estudar

1. [Organização e relação com as teóricas](app/doc/Architecture-guide.md)
2. [QR, registo e sessão](app/doc/QR-access.md)
3. [Comodidades e marcações](app/doc/Bookings.md)
4. [Meteorologia](app/doc/Weather.md)

[Diagrama do domínio](app/doc/DomainModel.puml) ·
[Proposta inicial](app/doc/CondoSched.md) · [Equipa](app/doc/TeamComposition.md)

A TheCondo é usada para consulta; o Supabase guarda contactos e reservas por
GET, POST e DELETE. O acesso da demonstração usa dados fictícios e não tem
login individual. Os scripts em `app/doc/supabase` são da configuração antiga;
não são usados pela app atual.

No Windows, compilar e verificar com o Gradle Wrapper do projeto:

```text
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```
