# Comodidades e marcações

## Percurso na aplicação

1. Abrir **Comodidades** no início ou **Condomínio → Ver comodidades**.
2. Selecionar um espaço para consultar os detalhes e regras.
3. Tocar em **Marcar este espaço**.
4. Escolher o residente, data, horas, número de pessoas e observações opcionais.
5. Submeter e consultar o ID e o estado devolvidos pelo servidor.
6. Abrir o separador **Marcações** para consultar as reservas da habitação.

## Consulta das comodidades

`AmenitiesActivity` observa `AmenitiesViewModel`. Este pede os dados ao
`AmenityRepository`, que consulta os prédios e as comodidades do condomínio.
O Repository mantém apenas as comodidades ativas dos prédios desse condomínio.

`AmenityMapper` converte `AmenityDTO` em `Amenity`. Os horários recebidos como
texto passam a `LocalTime`. Um horário ausente ou inválido fica `null`.
A taxa usa `Double` para distinguir um valor ausente de uma taxa igual a zero.

`ui/adapter/AmenityAdapter` apresenta a lista. Ao selecionar uma linha, a
Activity envia o objeto `Amenity`, que implementa Serializable, no Intent para
`AmenityDetailActivity`. O detalhe reutiliza esses dados.
`ui/display/AmenityDisplay` formata horas, taxas e ícones.

## Criação de uma marcação

| Ficheiro | Função |
| --- | --- |
| `ui/CreateBookingActivity` | Formulário, seletores de data/hora e apresentação do resultado |
| `viewmodel/CreateBookingViewModel` | Estado do formulário, validação inicial e submissão |
| `domain/BookingValidator` | Validar período, capacidade, horário de funcionamento e sobreposições |
| `repository/BookingRepository` | Coordenar consultas, preparar o pedido e guardar a marcação |
| `repository/BookingMapper` | Converter DTOs de reservas e residentes para domínio |
| `network/BookingApi` | Declarar as funções de consulta e criação de marcações no Supabase |
| `dto/BookingDTO` | Contrato JSON das reservas, com `unit_id`, `amenity_id` e horários |
| `dto/BookingRequestDTO` | Corpo do pedido à função `create_booking` |
| `network/ResidentApi` | Declarar consulta de residentes ativos |
| `network/AmenityApi` | Declarar consulta das comodidades e respetivos detalhes |

A sessão identifica uma habitação. O formulário apresenta os residentes ativos
da TheCondo cujo `unitId` corresponde a essa casa para escolher o responsável.
O perfil de contacto do Supabase não é automaticamente um residente da TheCondo.
Sem um residente ativo registado, a marcação não pode ser submetida.

Antes de guardar, o Repository:

1. Consulta a comodidade atual para obter atividade, horário, capacidade e taxa.
2. Confirma que o residente escolhido continua ativo e pertence à habitação.
3. Consulta no Supabase os períodos ocupados da comodidade e converte-os para domínio.
4. Usa `BookingValidator` para verificar se o horário está disponível.
5. Envia a marcação para `create_booking` no Supabase, incluindo o ID da habitação.
6. A base de dados verifica sobreposições e guarda o estado `confirmed`.
7. A app confere o ID, a habitação, a comodidade, o residente e o período da resposta.

A confirmação no ecrã utiliza o estado devolvido pelo servidor.
O ViewModel impede submissões repetidas enquanto o pedido decorre e depois
do sucesso. A data e as horas mantêm-se na rotação.

## Regras de validação

- A comodidade tem de estar ativa.
- O início tem de estar no futuro e o fim tem de ser posterior ao início.
- O formulário permite um dia de cada vez; fim às 00:00 significa a meia-noite seguinte.
- O número de pessoas tem de ser positivo e não pode ultrapassar a capacidade conhecida.
- O período tem de respeitar o horário de funcionamento conhecido.
- Reservas pendentes e confirmadas bloqueiam períodos sobrepostos.
- Reservas canceladas ou rejeitadas não bloqueiam o horário.
- Uma reserva pode começar exatamente quando outra termina.

A consulta inicial e o POST são separados, mas a tabela Supabase tem uma restrição
de exclusão por comodidade e intervalo de tempo. Assim, duas submissões simultâneas
não podem guardar períodos sobrepostos. A função converte esse conflito em HTTP 409.
Os horários são guardados como hora local do condomínio em `Europe/Lisbon`.

## Lista de marcações

`BookingsActivity` observa `BookingsViewModel`. O `BookingRepository` consulta no
Supabase as reservas pelo `unit_id` da sessão e confere esse ID na resposta.
Ordena-as por data de início, da mais recente para a mais antiga, e consulta a
TheCondo para associar os nomes dos espaços. Não é necessário voltar a consultar
os residentes para listar reservas antigas.

`ui/adapter/BookingAdapter` apresenta cada reserva.
`ui/display/BookingDisplay` formata as datas e traduz os estados.
A lista atualiza ao abrir ou regressar ao ecrã e tem um botão de atualização manual.

## Pedidos e armazenamento atual

| Pedido na TheCondo | Utilização |
| --- | --- |
| `GET /api/buildings/condominium/{id}` | Identificar os prédios do condomínio |
| `GET /api/amenities?condominiumId=ID` | Obter as comodidades a filtrar |
| `GET /api/amenities/{id}` | Atualizar as regras antes da marcação |
| `GET /api/residents/active` | Encontrar os residentes ativos da habitação |
| `GET /api/amenities` | Associar os nomes dos espaços às reservas |

| Pedido no Supabase | Utilização |
| --- | --- |
| `GET /rest/v1/rpc/get_home_bookings?p_unit_id=ID` | Listar reservas da habitação |
| `GET /rest/v1/rpc/get_amenity_bookings?p_amenity_id=ID` | Consultar períodos ocupados |
| `POST /rest/v1/rpc/create_booking` | Guardar a reserva e devolver uma lista com a linha criada |

O POST recebe `{"p_booking": {...}}`. O objeto tem `unit_id`, `amenity_id`,
`resident_id`, `start_time`, `end_time`, `number_of_people`, `fee` e `notes`.
O estado é definido pelo servidor. `id` é gerado pela base de dados.

As reservas da TheCondo não são importadas, consultadas nem usadas para bloquear
horários na app. As reservas criadas anteriormente nesse servidor continuam lá.
Não existe fallback de escrita: se o Supabase falhar, a app apresenta erro.

## Preparar a base e os dados de teste

Executar no SQL Editor do projeto configurado em `SupabaseConfig`:

1. [habitation_profiles.sql](supabase/habitation_profiles.sql), se a tabela de perfis ainda não existir.
2. [bookings.sql](supabase/bookings.sql), para criar a tabela, restrição e funções HTTP.
3. Opcionalmente, [bookings_bootstrap.sql](supabase/bookings_bootstrap.sql), depois de registar a habitação 1 na app.

O bootstrap insere dois horários confirmados adjacentes e um cancelado, para daqui
a dois dias. São exemplos fictícios com IDs de referência 1; ajustar esses IDs
se o ambiente usar outras habitações, comodidades ou residentes. Repetir o script
não substitui nem apaga reservas e não duplica os exemplos identificados pelas
observações `bootstrap-demo-...`. Se um horário já estiver ocupado, o exemplo é ignorado.

RLS está ativo e a app só acede às funções autorizadas, sem acesso direto à tabela.
A consulta de disponibilidade devolve apenas comodidade, horários e estado, sem
residentes nem observações. O acesso às reservas da casa continua a seguir o modelo
do QR: conhecer o ID permite consultá-las; não existe autenticação individual.

A app ainda não permite editar, cancelar ou apagar marcações. Também não existe
um fluxo de eventos ou pagamentos. Se a resposta ao POST falhar, deve consultar-se
a lista antes de repetir: o servidor pode ter guardado a reserva.

## Verificação manual

- Confirmar que a lista contém apenas espaços ativos do condomínio.
- Criar uma marcação válida e confirmar que aparece na lista.
- Tentar um horário já ocupado e verificar a mensagem de conflito.
- Testar início passado, fim anterior, horário fora do funcionamento e excesso de pessoas.
- Testar falhas de rede e atualização da lista.
- Rodar o formulário e verificar os valores e a ausência de submissões duplicadas.

Os testes automáticos de domínio, Repository e ViewModel usam respostas simuladas;
não criam reservas no servidor real. Os testes HTTP usam servidores separados para
TheCondo e Supabase e verificam os destinos e o contrato JSON.

Referências técnicas: [funções Supabase](https://supabase.com/docs/guides/database/functions),
[restrições de intervalos PostgreSQL](https://www.postgresql.org/docs/current/rangetypes.html#RANGETYPES-CONSTRAINT)
e [erros HTTP das funções PostgREST](https://docs.postgrest.org/en/stable/references/errors.html#custom-errors).

[Organização geral](Architecture-guide.md)
