# Comodidades e marcações

## Utilização

Abrir Comodidades, escolher um espaço e consultar regras, horário, capacidade
e taxa. Em Marcar este espaço, escolher residente, data, horas e pessoas.
Depois de submeter, consultar a reserva em Marcações. Apagar marcação pede
confirmação e remove definitivamente a reserva.

## Ficheiros e percurso

| Ficheiro | Responsabilidade |
| --- | --- |
| `AmenitiesActivity`, `AmenitiesViewModel` | Lista e estado da consulta de espaços |
| `AmenityRepository`, `AmenityMapper` | Consultar, filtrar espaços ativos do condomínio e converter DTOs |
| `AmenityDetailActivity` | Apresentar a comodidade recebida no Intent |
| `CreateBookingActivity`, `CreateBookingViewModel` | Formulário, estado e submissão |
| `BookingValidator` | Validar período, horário e lotação total em cada momento |
| `BookingRepository` | Consultar regras/residentes, verificar disponibilidade, criar/listar/apagar |
| `BookingApi`, `BookingDTO`, `BookingMapper` | Contrato HTTP, JSON e conversão para domínio |
| `BookingsActivity`, `BookingsViewModel` | Lista e coordenação da eliminação |
| `ui/adapter`, `ui/display` | Linhas das listas e formatação |

As Activities apresentam dados e enviam ações aos ViewModels. Os Repositories
fazem pedidos Retrofit; os DTOs não chegam à interface. O detalhe recebe `Amenity`
Serializable num Intent e não precisa de outro pedido nem de um ViewModel vazio.

## Criação: o que o Java faz

A sessão identifica a casa por `Habitation.unitId`. O formulário apresenta os
residentes ativos dessa casa na TheCondo. O contacto Supabase não substitui o
residente responsável. Sem residente ativo, não é possível criar uma marcação.

`BookingRepository` segue estes passos:

1. Consulta a comodidade atual na TheCondo para obter horário, capacidade e taxa.
2. Usa `BookingValidator` para conferir as regras.
3. Confirma que o residente continua ativo e pertence à casa.
4. Consulta no Supabase os períodos confirmados/pendentes que cruzam o horário pedido e o número de pessoas de cada reserva.
5. Soma em Java as pessoas presentes em cada momento e verifica a capacidade do espaço.
6. Prepara `BookingDTO`, define `status` como `confirmed` e envia POST.
7. Valida a resposta, converte-a em `Booking` e entrega-a ao ViewModel.

O POST envia o próprio DTO, sem função RPC. O Supabase guarda a linha e gera o ID.
O ViewModel impede submissões repetidas. Datas/horas ficam no ViewModel e no estado
guardado da Activity; os campos do formulário são restaurados pelo Android.

## Previsão meteorológica no formulário

A Activity pede permissão de localização e usa `WeatherLocation` para obter
coordenadas. `WeatherViewModel` pede a previsão ao `WeatherRepository`, que chama
`WeatherApi` por Retrofit e converte o JSON em `List<Forecast>`. Sem coordenadas,
usa Porto. A Activity observa LiveData e mostra a descrição e o ícone do dia
selecionado, através de `WeatherDisplay`. Alterar a data reutiliza a previsão
carregada, sem repetir o pedido HTTP.

O número de dias disponíveis depende do plano da WeatherAPI. Uma data fora da
resposta ou uma falha mostra previsão indisponível, mas não bloqueia a marcação.
O ecrã meteorológico do menu usa o mesmo ViewModel e Repository, com
`ForecastAdapter` para apresentar as linhas. A chave local e o percurso dos dados
estão explicados no [guia de meteorologia](Weather.md).

## Regras das marcações

- A comodidade deve estar ativa; início no futuro e fim posterior ao início.
- Um dia de cada vez; fim às 00:00 corresponde à meia-noite seguinte.
- Pessoas: número positivo e dentro da capacidade conhecida.
- O período deve respeitar o horário de funcionamento conhecido.
- Reservas confirmadas/pendentes contam para a lotação; canceladas/rejeitadas não.
- Várias reservas podem partilhar o horário se a lotação total não exceder a capacidade.
- Sem capacidade conhecida, mantém-se o comportamento de reserva exclusiva.
- Uma reserva pode começar exatamente quando outra termina.
- Datas interpretadas na hora do condomínio, `Europe/Lisbon`.

`validate()` continua a verificar os dados da nova reserva. Depois,
`validateAvailability(amenity, start, end, people, bookings)` verifica a lotação.
Um TreeMap ordena os instantes de entrada e saída: entradas somam pessoas e
saídas subtraem. Movimentos à mesma hora são agrupados, permitindo que uma
reserva comece exatamente quando outra termina. O cálculo usa `long` para evitar
overflow da soma de pessoas.

Exemplo: capacidade 10, reserva A de 6 pessoas das 10h às 11h e B de 6 das 11h
às 12h. Uma nova reserva de 4 pessoas das 10h às 12h é permitida: o máximo é
10 pessoas em simultâneo, não 16. A ordem da lista recebida não altera o resultado.

## Ajuste necessário na base existente

A configuração antiga criou `bookings_no_overlap`, que proíbe qualquer
sobreposição mesmo com lugares disponíveis. Foi confirmado no Supabase um erro
HTTP 400 / `23P01` ao tentar sobrepor duas reservas temporárias de uma pessoa;
a reserva de teste criada foi removida. Essa restrição tem de ser retirada para
o novo comportamento funcionar. No SQL Editor, executar apenas:

```sql
alter table public.bookings drop constraint if exists bookings_no_overlap;
```

Isto altera a estrutura da tabela, não apaga reservas nem cria funções.
Não voltar a executar os scripts históricos que repõem a restrição antiga.
Esta alteração ainda não foi aplicada remotamente: a app não tem permissões de
administração da base.

A lógica de lotação fica no Java. Como a consulta e o POST são pedidos separados,
duas submissões simultâneas podem ultrapassar a capacidade. Este protótipo não
garante a lotação de forma atómica no servidor.

## Pedidos HTTP

A TheCondo é usada apenas para GET de prédios, comodidades e residentes.
No Supabase, `BookingApi` usa diretamente a tabela:

| Operação | Pedido |
| --- | --- |
| Reservas da casa | `GET /rest/v1/bookings?unit_id=eq.ID` |
| Disponibilidade | `GET /rest/v1/bookings?select=amenity_id,start_time,end_time,status,number_of_people&status=in.(confirmed,pending)&amenity_id=eq.ID&start_time=lt.FIM&end_time=gt.INICIO` |
| Criar | `POST /rest/v1/bookings` |
| Apagar | `DELETE /rest/v1/bookings?select=id,unit_id&id=eq.ID&unit_id=eq.CASA` |

`eq.` significa igual a; `select` escolhe as colunas; `in.` filtra vários estados.
`lt.FIM` e `gt.INICIO` limitam a consulta às reservas que cruzam o período pedido.
POST e DELETE usam `Prefer: return=representation` para confirmar o resultado.
O acesso da demonstração é público e os dados são fictícios. Filtrar por casa no
Java não é autenticação nem impede pedidos feitos fora da app.

A tabela tem `id`, `unit_id`, `amenity_id`, `resident_id`, `start_time`, `end_time`,
`number_of_people`, `status`, `fee`, `notes` e `created_at`. `BookingDTO` mapeia
estes nomes JSON para atributos Java com `@SerializedName`.

## Lista e eliminação

O Repository consulta por casa, valida e ordena por início, da mais recente para
a mais antiga. Consulta ainda a TheCondo para os nomes dos espaços; uma falha
nessa consulta não impede apresentar as reservas. A lista atualiza ao regressar
ao ecrã e pelo botão Atualizar.

Apagar remove a linha da base, incluindo do histórico, e liberta o horário.
O ViewModel mantém a lista enquanto o pedido decorre, bloqueia repetições e só
remove a linha depois de confirmar os IDs devolvidos pelo servidor. Resposta vazia
significa que a reserva não foi encontrada. Em caso de erro, a lista mantém-se.
Se a resposta de escrita se perder, atualizar a lista antes de repetir o pedido.

## Verificação

Os testes locais simulam HTTP com MockWebServer. Na revisão foram também testados
GET de perfil/reservas/disponibilidade e POST, GET e DELETE de uma reserva temporária
no Supabase; a reserva criada para esse teste foi removida.

Falta confirmar visualmente num dispositivo o percurso completo: QR, formulário,
rotação, conflito de horário, apagar, recusar a confirmação e falhas de Internet.

[Organização geral](Architecture-guide.md)
