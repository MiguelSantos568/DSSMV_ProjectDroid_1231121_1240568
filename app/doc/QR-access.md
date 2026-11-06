# QR, registo e sessão

## Funcionamento

O QR identifica uma habitação da TheCondo, no formato `condosched://unit/ID`.
O ID é o da habitação, não o número da porta. Exemplo: [qr-unit-1.png](qr-unit-1.png).

1. ZXing lê o QR e devolve o texto à `AccessActivity`.
2. `AccessViewModel` usa `HabitationQr` para validar o texto e obter o ID.
3. `HabitationRepository` consulta a TheCondo e encontra a casa.
4. `HabitationProfileRepository` consulta o contacto dessa casa no Supabase.
5. Se existir, guarda a sessão e abre o início; se faltar, abre o registo.
6. `RegisterViewModel` valida nome/email e pede ao Repository para guardar o perfil.

Falha de rede e ausência de perfil são resultados diferentes: uma falha não abre
um registo novo. As Activities observam LiveData e apresentam os resultados.

## Ficheiros principais

| Ficheiro | Função |
| --- | --- |
| `AccessActivity`, `AccessViewModel` | Leitura e coordenação do acesso |
| `HabitationQr` | Validar o formato do QR |
| `HabitationRepository`, `HabitationMapper` | Consultar a TheCondo e construir `Habitation` |
| `RegisterActivity`, `RegisterViewModel` | Formulário e registo do contacto |
| `ProfileContactValidator` | Validar nome e email em Java |
| `HabitationProfileRepository` | Consultar/criar o perfil e converter o DTO para domínio |
| `SessionRepository`, `SessionManager` | Sessão observável e persistência local |
| `SessionViewModel`, `SessionActivity` | Observar a sessão nos ecrãs e tratar a saída |

`HabitationMapper` percorre `CondominiumDTO → BuildingDTO → UnitDTO` e reúne os
IDs e nomes em `Habitation`. `HomeSession` junta essa casa a `HabitationProfile`.

## Pedidos e dados

| Serviço | Pedido | Resultado |
| --- | --- | --- |
| TheCondo | `GET /api/condominiums` | Hierarquia onde se procura a casa |
| Supabase | `GET /rest/v1/habitation_profiles?unit_id=eq.ID` | Perfil ou lista vazia |
| Supabase | `POST /rest/v1/habitation_profiles` | Perfil guardado |

`eq.` significa igual a. O POST envia `HabitationProfileDTO`, com `unit_id`,
`full_name` e `email`, e pede a linha criada com `Prefer: return=representation`.
A tabela também tem `created_at`, preenchido automaticamente.

`unit_id` é a chave primária: há um perfil por casa. Se outro dispositivo já o
registou, o POST recebe HTTP 409; o Repository consulta então o perfil existente.
Não o substitui. O nome e email são aparados e validados antes da submissão.

O projeto de demonstração usa dados fictícios e acesso público configurado nas
tabelas. O QR identifica a casa e o email é um contacto; nenhum deles é login.
A app não chama funções SQL nem cria utilizadores no Supabase Auth.

## Sessão e perfil

`SessionManager` guarda os IDs, nomes da casa e contacto em SharedPreferences
com `apply()`. Só `SessionRepository` acede a essa classe e expõe a sessão por
LiveData. `SessionActivity` volta ao QR quando a sessão termina.

Ao abrir o perfil, `ProfileViewModel` consulta o contacto atualizado. Se a rede
falhar, mantém a cópia local e mostra um aviso. Se o perfil tiver sido removido,
a sessão termina. Uma resposta atrasada não reabre uma sessão terminada.
Terminar sessão limpa apenas o dispositivo; o contacto continua no Supabase.

## Verificação manual

Testar o primeiro registo, repetir o QR noutro dispositivo, fechar/reabrir a app,
sair da sessão, recusar a leitura, usar um QR inválido, retirar a Internet e rodar
o ecrã durante a consulta. A rotação não deve repetir pedidos ou navegação.

[Organização geral](Architecture-guide.md)
