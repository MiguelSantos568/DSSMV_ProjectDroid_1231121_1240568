# Organização da CondoSched

Aplicação Android em Java, compatível com API 26 ou superior. O acesso começa em
`AccessActivity`, através do QR de uma habitação.

## Estrutura do código

Todos os packages estão em `app/src/main/java/com/example/condosched`.

| Pasta | Responsabilidade |
| --- | --- |
| `ui` | Activities, navegação, interação com o utilizador e permissões do dispositivo |
| `ui/adapter` | Transformar listas de objetos de domínio em linhas dos ecrãs |
| `ui/display` | Formatar datas, horas, taxas e estados; escolher ícones |
| `viewmodel` | Coordenar ações e manter o estado dos ecrãs em LiveData |
| `repository` | Consultar/gravar dados, tratar respostas e converter DTOs para domínio |
| `network` | Interfaces Retrofit, clientes HTTP e configuração dos servidores |
| `dto` | Objetos correspondentes aos pedidos e respostas JSON |
| `domain` | Modelos da aplicação, validações e códigos de erro |
| `session` | Guardar e ler a sessão local através de SharedPreferences |

Os layouts estão em `app/src/main/res/layout`: `activity_...` para ecrãs,
`item_...` para linhas de listas e `layout_...` para componentes partilhados.
Os textos estão em `values` e `values-b+pt`. Cores, estilos e dimensões são
partilhados através dos recursos.

Para estudar o funcionamento atual, seguir os modelos de `domain`: `Habitation`,
`HabitationProfile`, `HomeSession`, `Amenity`, `Resident` e `Booking`.
As classes e os testes do desenho inicial sem utilização foram removidos.
O [diagrama do domínio](DomainModel.puml) representa os modelos que permanecem.

## Habitação e dados da API

`Habitation` é o modelo usado pela sessão e pelos ecrãs. É imutável: os campos são
`final` e recebem os valores no construtor. Implementa Serializable para poder ser
enviado num Intent, por exemplo do acesso por QR para o formulário de registo.

| Dados de `Habitation` | Origem |
| --- | --- |
| `unitId`, `floor`, `unit` | `UnitDTO`: ID, andar e número da porta |
| `buildingId`, `building` | `BuildingDTO`: ID e nome do prédio |
| `condominiumId`, `condominium` | `CondominiumDTO`: ID e nome do condomínio |

`HabitationMapper` percorre a hierarquia recebida da TheCondo e junta esses dados
num objeto `Habitation`. Por isso não existem modelos de domínio separados para
`Unit`, `Building` e `Condominium`; os DTOs correspondentes continuam necessários.
`HabitationProfile` guarda o contacto da casa e `HomeSession` junta esse perfil
à habitação. As marcações usam `Habitation.unitId` como referência à casa.

## Percurso dos dados

```text
Ação na Activity → ViewModel → Repository → API HTTP
Resposta JSON → DTO → Mapper → domínio → LiveData → Activity
```

A Activity recebe ações e apresenta o resultado. O ViewModel coordena a operação
e expõe LiveData. O Repository executa os pedidos Retrofit com `enqueue()` e
converte a resposta para os modelos utilizados pela app.

Os DTOs têm construtor vazio, getters e setters. Os Mappers fazem a conversão
para domínio. Os erros são devolvidos como `DataError`; os textos apresentados
são escolhidos na interface através dos recursos.

O `BookingRepository` valida IDs, datas, pessoas, estado e taxa nas respostas
completas antes de as apresentar ou confirmar uma alteração. `cancelBooking`
cancela uma marcação no Supabase; `cancel()` apenas interrompe um pedido HTTP
quando o ecrã deixa de ser necessário.

`UiState` representa uma operação em espera, carregamento, sucesso ou erro.
`AmenitiesState` distingue também uma lista vazia.

## Ecrãs e classes principais

| Fluxo | Activity | ViewModel | Repository |
| --- | --- | --- | --- |
| Leitura do QR | `AccessActivity` | `AccessViewModel` | `HabitationRepository`, `HabitationProfileRepository` |
| Registo do contacto | `RegisterActivity` | `RegisterViewModel` | `HabitationProfileRepository` |
| Sessão | `SessionActivity` | `SessionViewModel` | `SessionRepository` |
| Perfil | `ProfileActivity` | `ProfileViewModel` | `HabitationProfileRepository` |
| Lista de comodidades | `AmenitiesActivity` | `AmenitiesViewModel` | `AmenityRepository` |
| Criar marcação | `CreateBookingActivity` | `CreateBookingViewModel` | `BookingRepository` |
| Lista e cancelamento de marcações | `BookingsActivity` | `BookingsViewModel` | `BookingRepository` |
| Fotografia | `CameraActivity` | `CameraViewModel` | `PhotoRepository` |

`MainActivity` e `CondominiumActivity` apresentam dados da sessão.
`AmenityDetailActivity` apresenta a comodidade recebida no Intent.
`CreditsActivity` apresenta informação estática.

`SessionActivity` é a base dos ecrãs que exigem sessão. Observa a sessão e volta
para o acesso quando esta termina. `BottomNavigation` liga os separadores e
`ScreenInsets` ajusta o conteúdo às barras do sistema e ao teclado.

## Servidores e armazenamento

| Local | Dados |
| --- | --- |
| TheCondo | Consulta de condomínios, prédios, habitações, comodidades e residentes |
| Supabase | Perfis de contacto e marcações associadas à habitação |
| SharedPreferences | Cópia local da habitação e do contacto para manter a sessão |
| ViewModel | Estado temporário do ecrã, incluindo a fotografia capturada |
| MediaStore | Fotografias guardadas em `Pictures/CondoSched` |

`ApiClient` cria clientes independentes para TheCondo e Supabase. A chave do
Supabase só é acrescentada aos pedidos desse serviço. `SupabaseConfig` contém
os valores de configuração.

`BookingApi` usa o cliente Supabase. As restantes interfaces TheCondo são apenas
de consulta. A app não cria, altera ou apaga dados no servidor do professor.
`BookingDTO` representa o JSON da tabela de reservas, com campos como `unit_id`
e `amenity_id`. `BookingRequestDTO` envolve esse objeto no parâmetro `p_booking`
da função de criação. O Mapper converte a resposta para o modelo `Booking`.
`CancelBookingRequestDTO` transporta os IDs da habitação e da marcação para a
função `cancel_booking`. A Activity confirma a intenção do utilizador; o ViewModel
coordena o pedido e só atualiza a lista após confirmação do servidor.

A base é preparada com `supabase/habitation_profiles.sql` e depois
`supabase/bookings.sql`. O bootstrap de reservas é opcional; a ordem e os pedidos
HTTP estão descritos no [guia de marcações](Bookings.md).

## Ciclo de vida e fotografia

O ViewModel mantém o estado durante a rotação e liberta os pedidos em
`onCleared()`. Se o processo terminar, a sessão continua em SharedPreferences,
mas os dados em memória podem ter de ser carregados novamente.

A câmara devolve uma miniatura através de `TakePicturePreview`. A Activity trata
as permissões, o ViewModel mantém a imagem e o `PhotoRepository` grava-a numa
thread de trabalho. O resultado regressa à thread da interface através de um
Handler. Em API 26–28 é necessária permissão de escrita; em API 29+ a gravação
usa MediaStore com `RELATIVE_PATH` e `IS_PENDING`.

## Guias das funcionalidades

- [QR, registo e sessão](QR-access.md)
- [Comodidades e marcações](Bookings.md)

A proposta inicial está em [CondoSched.md](CondoSched.md) e a equipa em
[TeamComposition.md](TeamComposition.md).

## Verificação

Os testes estão em `app/src/test/java/com/example/condosched`, organizados em
`domain`, `repository` e `viewmodel`. Os testes dos Repositories usam MockWebServer
para verificar pedidos e respostas sem alterar dados dos servidores reais.

Com Gradle instalado:

```text
gradle :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

A organização utiliza os conceitos de Activities, Intents, SharedPreferences e
adapters da T4, DTOs da T5 e Repository, ViewModel, LiveData e Retrofit da T6.
