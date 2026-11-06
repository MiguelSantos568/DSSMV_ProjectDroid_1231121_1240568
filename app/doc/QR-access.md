# Acesso por QR, registo e sessão

## Utilização

O QR identifica uma habitação da TheCondo. O seu conteúdo tem o formato
`condosched://unit/ID`, onde `ID` é o identificador da habitação e não o número
da porta. Existe um exemplo em [qr-unit-1.png](qr-unit-1.png).

1. O utilizador toca em ler QR e a biblioteca ZXing devolve o texto lido.
2. A app valida o formato e procura a habitação na TheCondo.
3. Consulta no Supabase o perfil associado ao ID dessa habitação.
4. Se o perfil existir, guarda a sessão e abre o início.
5. Se não existir, pede o nome e email de contacto da habitação.
6. Depois de o servidor devolver o perfil, guarda a sessão e abre o início.

Uma falha de rede apresenta um erro; não é tratada como ausência de perfil.

## Ficheiros e responsabilidades

| Ficheiro | Função |
| --- | --- |
| `ui/AccessActivity` | Abrir o leitor e apresentar o resultado do acesso |
| `viewmodel/AccessViewModel` | Coordenar a validação do QR, procura da casa e consulta do perfil |
| `domain/HabitationQr` | Validar o texto e extrair o ID |
| `repository/HabitationRepository` | Consultar os condomínios e encontrar a habitação |
| `repository/HabitationMapper` | Converter a hierarquia da API em `Habitation` |
| `ui/RegisterActivity` | Recolher nome e email |
| `viewmodel/RegisterViewModel` | Validar o contacto e pedir a criação do perfil |
| `domain/ProfileContactValidator` | Validar nome e email sem depender da interface |
| `repository/HabitationProfileRepository` | Consultar e criar o perfil no Supabase |
| `repository/SessionRepository` | Manter a sessão e notificar os observadores |
| `session/SessionManager` | Ler, gravar e limpar a sessão em SharedPreferences |

As Activities observam LiveData e navegam quando a operação termina.
`AccessViewModel.consumeResult()` marca a navegação como tratada para evitar
repeti-la numa recriação do ecrã.

## Pedidos HTTP

| Serviço | Pedido | Resultado |
| --- | --- | --- |
| TheCondo | `GET /api/condominiums` | Hierarquia de condomínios, prédios e habitações |
| Supabase | `POST /rest/v1/rpc/get_habitation_profile` | Perfil da habitação ou lista vazia |
| Supabase | `POST /rest/v1/rpc/create_habitation_profile` | Perfil criado ou perfil já existente |

`CondominiumApi` e `HabitationProfileApi` descrevem estes pedidos. Gson converte
as respostas em DTOs; os Repositories entregam modelos de domínio ao ViewModel.

## Dados do perfil

A tabela `habitation_profiles` tem `unit_id`, `full_name`, `email` e `created_at`.
`unit_id` é a chave primária: existe um perfil por habitação. Se dois dispositivos
registarem a mesma casa, a função de criação devolve o perfil que já existe.

Para preparar o Supabase, executar [habitation_profiles.sql](supabase/habitation_profiles.sql)
no SQL Editor do projeto configurado em `network/SupabaseConfig`.
O script ativa RLS, retira o acesso direto à tabela e disponibiliza as duas funções
utilizadas pela app.

O perfil é partilhado pela habitação. O QR e o email de contacto não constituem
autenticação individual. As funções recebem o ID da casa, sem verificar a identidade
da pessoa que o apresenta. Não são criados utilizadores no Supabase Auth.

## Sessão e perfil

`HomeSession` junta `Habitation` e `HabitationProfile`. O `SessionRepository`
guarda os dados através de `SessionManager` e expõe a sessão por LiveData.
Os ecrãs protegidos herdam de `SessionActivity` e observam essa sessão.

Ao abrir o perfil, `ProfileViewModel` procura o contacto atualizado no servidor.
Se a rede falhar, mantém a cópia local e apresenta uma mensagem. Se o perfil tiver
sido removido, a sessão termina. Uma resposta atrasada não reabre uma sessão terminada.

Terminar sessão limpa os dados de sessão do dispositivo, mas não apaga o perfil no Supabase.
A próxima leitura do mesmo QR volta a consultar esse perfil.

## Verificação manual

- Ler um QR válido e concluir o primeiro registo.
- Sair e ler o mesmo QR: deve entrar sem repetir o formulário.
- Usar um segundo dispositivo: deve recuperar o mesmo perfil.
- Fechar e reabrir a app: deve manter a sessão.
- Testar QR inválido, habitação inexistente e cancelamento da leitura.
- Sem internet, tentar um novo acesso: deve apresentar erro.
- Rodar o ecrã durante uma consulta e verificar que não repete a navegação.

[Organização geral](Architecture-guide.md)
