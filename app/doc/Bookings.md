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
| `BookingValidator` | Validar período, horário, capacidade e sobreposições |
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
4. Consulta no Supabase os períodos confirmados ou pendentes do espaço.
5. Verifica sobreposições em Java.
6. Prepara `BookingDTO`, define `status` como `confirmed` e envia POST.
7. Valida a resposta, converte-a em `Booking` e entrega-a ao ViewModel.

O POST envia o próprio DTO, sem função RPC. O Supabase guarda a linha e gera o ID.
O ViewModel impede submissões repetidas. Datas/horas ficam no ViewModel e no estado
guardado da Activity; os campos do formulário são restaurados pelo Android.

## Regras das marcações

- A comodidade deve estar ativa; início no futuro e fim posterior ao início.
- Um dia de cada vez; fim às 00:00 corresponde à meia-noite seguinte.
- Pessoas: número positivo e dentro da capacidade conhecida.
- O período deve respeitar o horário de funcionamento conhecido.
- Reservas confirmadas/pendentes bloqueiam sobreposições; canceladas/rejeitadas não.
- Uma reserva pode começar exatamente quando outra termina.
- Datas interpretadas na hora do condomínio, `Europe/Lisbon`.

A consulta e o POST são pedidos separados. A restrição já existente na tabela
impede sobreposições em escritas simultâneas; o Repository trata HTTP 409 como
conflito de horário. A lógica de preparação e validação da app está em Java.

## Pedidos HTTP

A TheCondo é usada apenas para GET de prédios, comodidades e residentes.
No Supabase, `BookingApi` usa diretamente a tabela:

| Operação | Pedido |
| --- | --- |
| Reservas da casa | `GET /rest/v1/bookings?unit_id=eq.ID` |
| Disponibilidade | `GET /rest/v1/bookings?select=amenity_id,start_time,end_time,status&status=in.(confirmed,pending)&amenity_id=eq.ID` |
| Criar | `POST /rest/v1/bookings` |
| Apagar | `DELETE /rest/v1/bookings?select=id,unit_id&id=eq.ID&unit_id=eq.CASA` |

`eq.` significa igual a; `select` escolhe as colunas; `in.` filtra vários estados.
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
