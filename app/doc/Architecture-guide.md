# Organização da CondoSched

Aplicação Android em Java, API 26+. O ponto de entrada é `ui/AccessActivity`.

## Pastas e responsabilidades

Os packages estão em `app/src/main/java/com/example/condosched`.

| Pasta | O que contém |
| --- | --- |
| `ui` | Activities, navegação, interação e permissões do dispositivo |
| `ui/adapter` | Adapters que apresentam os objetos nas listas |
| `ui/display` | Formatação de datas, horas, taxas, estados e ícones |
| `viewmodel` | Coordenação e estado dos ecrãs através de ViewModel e LiveData |
| `repository` | Consultas, gravação, tratamento de respostas e conversão de DTOs |
| `network` | Interfaces Retrofit, clientes HTTP e configuração |
| `dto` | Contratos JSON: construtor vazio, getters/setters e anotações |
| `domain` | Modelos da app, validações e códigos de erro |
| `session` | Leitura e gravação da sessão em SharedPreferences |

Layouts em `res/layout`: `activity_...` para ecrãs, `item_...` para linhas e
`layout_...` para componentes partilhados. Textos em inglês (`values`) e português
(`values-b+pt`); estilos, cores e dimensões são recursos partilhados.

## Relação com as teóricas

| Aula | Conceitos utilizados |
| --- | --- |
| T3 | Activities, Manifest, Context, layouts XML e listeners |
| T4 | Intents/extras, Activity Result API, SharedPreferences, adapters e threads |
| T5, slides 34–37 | DTOs sem regras de negócio e conversão DTO → domínio |
| T6, slides 5–7 e 16–23 | MVVM, Repository, ViewModel e LiveData |
| T6, slides 42–51 | Retrofit, interfaces HTTP, Gson e `enqueue()` |

MVVM significa Model–View–ViewModel. A Activity apresenta os dados e recebe os
cliques; o ViewModel coordena o ecrã; o Repository trata o acesso aos dados.
Na T6, «UI controller» refere-se à Activity/Fragment que observa LiveData.
A SchoolApplication usa uma organização MVC de consola com Controller explícito;
a CondoSched segue a organização Android MVVM da T6.

Os exemplos foram adaptados: as reservas ficam no Supabase em vez de Room, e os
Repositories HTTP devolvem resultados por callback. O ViewModel atualiza LiveData
para a Activity observar. `enqueue()` com `onResponse`/`onFailure` também aparece
na T6; não é obrigatório usar RxJava para executar estes pedidos.

## Percurso de uma operação

```text
Clique na Activity → ViewModel → Repository → interface Retrofit → servidor
Resposta JSON → DTO → domínio → LiveData no ViewModel → Activity
```

As Activities não chamam Retrofit, DTOs ou SessionManager. Os ViewModels não
mantêm Activities ou Views. Quando precisam de armazenamento local, usam o
Context da aplicação através de um Repository.

`RepositoryCallback<T>` entrega sucesso ou um código `DataError`. O tipo `T` é o
resultado esperado, por exemplo uma lista de comodidades. `UiErrors`, na interface,
converte o código de erro num texto traduzido.

`UiState<T>` reúne dados e estado: IDLE, LOADING, SUCCESS ou ERROR. Todos os ecrãs
com operações usam esse formato. Uma lista vazia é um sucesso sem elementos.
O estado de eliminação é separado da lista para manter as reservas visíveis se
um DELETE falhar e impedir uma atualização enquanto se apaga uma reserva.

Os Mappers convertem horários ou estruturas JSON em modelos da app. O contacto
da habitação tem uma conversão curta, feita diretamente no seu Repository.

Nas marcações, `BookingValidator.validate()` verifica os dados da nova reserva;
`validateAvailability()` soma as pessoas das reservas ativas em cada momento do
período pedido. O Repository consulta também `number_of_people`, filtrando apenas
os períodos sobrepostos. A preparação da base existente e a limitação de pedidos
simultâneos estão explicadas no [guia de marcações](Bookings.md).

## Modelos e armazenamento

`Habitation` reúne IDs e nomes da casa, prédio e condomínio. `HabitationMapper`
percorre `CondominiumDTO → BuildingDTO → UnitDTO` e constrói esse objeto.
Os DTOs representam a API; não é preciso duplicar toda essa hierarquia no domínio.
`Habitation` é imutável e Serializable para passar do QR ao registo num Intent.
`HabitationProfile` contém o contacto e `HomeSession` junta a casa ao contacto.
Os restantes modelos principais são `Amenity`, `Resident` e `Booking`.

| Local | Dados |
| --- | --- |
| TheCondo | Consulta de condomínios, prédios, habitações, comodidades e residentes |
| Supabase | Tabelas `habitation_profiles` e `bookings`, por HTTP direto, sem RPC |
| SharedPreferences | Sessão local, através de SessionRepository → SessionManager |
| ViewModel | Estado temporário do ecrã, incluindo a fotografia capturada |
| MediaStore | Fotografias guardadas em `Pictures/CondoSched` |

`ApiClient` separa os dois clientes HTTP; a chave pública Supabase só segue para
esse serviço. O protótipo usa dados fictícios e acesso sem autenticação individual:
os filtros por casa organizam os pedidos, mas não são uma proteção entre utilizadores.

## Ciclo de vida e fotografia

ViewModelProvider mantém o ViewModel na rotação. `onCleared()` cancela pedidos que
já não são necessários. Os Repositories ignoram respostas atrasadas de consultas
canceladas. A sessão persiste em SharedPreferences; uma sessão sem IDs válidos
regressa ao QR. Se o processo terminar, o estado temporário pode ter de ser recarregado.

A Activity pede permissões e usa `TakePicturePreview` para obter uma miniatura.
`CameraViewModel` mantém a imagem e `PhotoRepository` grava-a numa thread de trabalho,
regressando à thread principal com Handler. API 26–28 pede permissão de escrita;
API 29+ usa MediaStore com `RELATIVE_PATH` e `IS_PENDING`.

## Guias e verificação

[QR e sessão](QR-access.md) explica o acesso e o contacto da casa.
[Marcações](Bookings.md) explica consultas, criação, validação e DELETE.
O [diagrama](DomainModel.puml) apresenta os modelos atuais; a
[proposta inicial](CondoSched.md) descreve o âmbito inicialmente pensado.

Os testes estão em `app/src/test`, por `domain`, `repository` e `viewmodel`.
MockWebServer simula pedidos HTTP; os testes locais não usam dados reais.
Compilar, executar testes e Android Lint não substitui testar os ecrãs num dispositivo.
