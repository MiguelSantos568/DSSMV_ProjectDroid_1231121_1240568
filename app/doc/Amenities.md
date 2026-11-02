# Consulta de comodidades por condomínio

## Percurso na app

- No início, tocar em **Comodidades**.
- Em alternativa, abrir **Condomínio > Ver comodidades**.
- O ecrã apresenta apenas as comodidades ativas de todos os prédios do condomínio associado à habitação do QR.
- Cada item mostra nome, prédio, descrição, horário, capacidade e necessidade de reserva.
- Tocar num cartão abre os detalhes, incluindo as regras de utilização e a taxa de reserva. A criação de reservas fica para a próxima etapa.

## Dados e organização

1. `Habitation` e `SessionManager` passam a guardar os IDs do condomínio e do prédio,
   além do ID da habitação e dos nomes. Os IDs vêm da TheCondo API.
2. Sessões antigas sem o ID do condomínio são completadas automaticamente por
   `HabitationService`, usando o ID da habitação. Não é necessário registar novamente o perfil.
3. `AmenityApi` declara `GET amenities?condominiumId=ID` e `GET buildings/condominium/ID` usando Retrofit.
4. `AmenityService` obtém os prédios do condomínio e mantém apenas as comodidades ativas desses prédios. Os nomes dos prédios são apresentados na lista.
5. `AmenityDTO` recebe os dados JSON. Na resposta real, os horários são textos como
   `08:00:00`, apesar de a especificação local os descrever como objetos `LocalTime`.
   O DTO acompanha a resposta real e permite taxas com valores decimais.
6. `AmenitiesActivity` gere carregamento, lista vazia e falhas com opção de tentar novamente.
7. `AmenityAdapter` associa cada DTO ao layout `item_amenity.xml`, usando um `ListView`
   e um `ArrayAdapter`, de acordo com os conceitos de adapters da T4.

O início abre a lista diretamente. O separador Condomínio apresenta o resumo da habitação
e outro acesso à lista. Não há alterações ao SQL nem às tabelas do Supabase nesta etapa.

## Testes

Exemplo confirmado na API em 2026-11-01:

- Habitação 1: Condo A, Building A, andar 1, porta 101.
- `GET /api/amenities?condominiumId=1`: Swimming Pool A, 08:00–20:00, capacidade 50,
  ativa e sem necessidade de reserva.
- A Fitness Center B pertence ao Building B do mesmo Condo A e também aparece nesta lista (06:00–22:00, capacidade 20).

Verificações manuais:

1. Com uma sessão existente, abrir Comodidades e confirmar o condomínio no cabeçalho.
2. Confirmar a piscina no Building A e o ginásio no Building B. Não devem aparecer comodidades de outros condomínios.
3. Abrir Condomínio pela barra inferior e depois Ver comodidades.
4. Navegar para Início e Perfil e usar o botão Voltar.
5. Sem internet, abrir a lista: deve mostrar erro e Tentar novamente.
6. Restabelecer a ligação e tocar em Tentar novamente: deve apresentar a lista.

Os testes automáticos cobrem o filtro HTTP, inclusão de vários prédios do mesmo condomínio, exclusão de comodidades inativas ou de outros condomínios, horários e taxas, lista vazia, falhas HTTP, JSON inválido e ausência de ID.

## Detalhes de uma comodidade

- `AmenitiesActivity` abre `AmenityDetailActivity` ao selecionar um cartão.
- O Intent transporta o `AmenityDTO` selecionado e o nome do prédio. O DTO implementa
  `Serializable`, para o Android poder transportá-lo e recriar o ecrã quando necessário.
- O detalhe mostra nome, descrição, localização, horários, capacidade, necessidade de
  reserva, taxa de reserva e regras. Reutiliza a resposta da lista, sem pedido HTTP adicional.
- `AmenityDisplay` concentra o ícone e a formatação de horários partilhados entre os dois
  ecrãs, além da formatação da taxa.
- A taxa é opcional: zero aparece como Sem taxa; um valor ausente aparece como Não indicada.
  Valores positivos não recebem um símbolo monetário inventado, porque a API não indica a moeda.
- O botão de voltar e o botão Voltar do Android regressam à mesma lista.
- Não há criação de reservas nem alterações à base de dados nesta etapa.

Teste manual: abrir piscina e ginásio, confirmar prédios e regras diferentes, voltar à lista
sem perder a posição, rodar o dispositivo e confirmar que o detalhe continua a apresentar
os dados selecionados. A seta e o texto Ver detalhes identificam os cartões clicáveis.