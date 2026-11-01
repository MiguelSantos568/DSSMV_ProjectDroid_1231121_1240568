# Um perfil por habitação: QR, Retrofit e Supabase

## Funcionamento

O QR contém `condosched://unit/ID`: ID é o identificador da habitação na TheCondo API,
não o número da porta. A biblioteca ZXing transforma a imagem da câmara neste texto.

1. A app valida o texto e extrai o ID (`HabitationQr`).
2. Consulta a TheCondo API por Retrofit e encontra a casa (`HabitationService`).
3. Consulta o perfil dessa casa no Supabase (`HabitationProfileService.find`).
4. Se existir, guarda a sessão local e abre o início, sem pedir nome ou email.
5. Se não existir, abre o formulário para o nome e email do contacto da habitação.
6. O formulário só conclui depois de o Supabase devolver o perfil guardado.

Um erro de ligação ou configuração não é interpretado como uma casa sem perfil.
Dois dispositivos podem entrar no mesmo perfil. O email não é verificado e não identifica
uma conta individual. Não são criados utilizadores no Supabase Auth nem residentes na
TheCondo API. Os antigos AuthService e ResidentService não são usados neste fluxo.

## Configurar o Supabase (passo necessário)

1. Abrir o projeto Supabase correspondente ao URL em `SupabaseClient`.
2. Abrir **SQL Editor > New query**.
3. Colar o conteúdo de `app/doc/supabase/habitation_profiles.sql` e executar em **Run**.
4. Confirmar a tabela `public.habitation_profiles` no Table Editor.
5. Executar a app e ler o QR de exemplo.

O script cria uma tabela separada e duas funções. Não altera os dados de `residents`
nem de `auth.users`. Perfis criados no fluxo antigo não são migrados automaticamente.
Não é preciso ativar o login anónimo do Supabase Auth.

Campos guardados:

| Campo | Significado |
| --- | --- |
| unit_id | ID da TheCondo API e chave primária: só um perfil por habitação |
| full_name | Nome do contacto responsável |
| email | Email de contacto |
| created_at | Data de criação no servidor |

A tabela tem RLS e não pode ser lida/escrita diretamente pela app.
A app chama duas funções através da API REST do Supabase:

- `get_habitation_profile`: devolve o perfil de um ID ou uma lista vazia.
- `create_habitation_profile`: insere o primeiro perfil; se o ID já existir,
  devolve o perfil existente sem o substituir.

A chave primária impede duplicados, mesmo se dois dispositivos registarem a mesma
casa ao mesmo tempo. A app usa o perfil devolvido pelo servidor, não assume que os
valores introduzidos foram os primeiros a ser guardados.

Modelo de acesso escolhido para o protótipo: conhecer o ID da habitação permite consultar
esse perfil. O ID no QR é previsível e não prova a identidade nem a residência da pessoa.
As funções limitam as operações disponíveis, mas não verificam a posse física do QR.
Não se deve descrever este modelo como autenticação individual segura.

## Retrofit e as teóricas

Retrofit descreve os pedidos HTTP em interfaces Java e Gson converte JSON em DTOs:

- `CondominiumApi`: GET dos condomínios, prédios e habitações.
- `HabitationProfileApi`: POST das duas funções do Supabase.
- `ApiClient`: URLs, tempos limite e chave pública do Supabase.
- Os serviços chamam `enqueue()` para não bloquear a interface.
- Os DTOs têm construtor vazio, getters e setters (T5).
- `SessionManager` usa SharedPreferences e `apply()` (T4).

A localização vem da TheCondo API. Nome e email vêm do Supabase. SharedPreferences
é apenas a cópia local para lembrar a casa entre aberturas. O perfil tenta atualizar
nome e email ao abrir; se a rede falhar, mostra a cópia guardada e avisa o utilizador.
Sessões locais da primeira implementação, que nunca foram guardadas no Supabase,
exigem uma nova leitura do QR após esta atualização.

## Exemplo e testes manuais

Abrir `qr-unit-1.png` noutro ecrã ou imprimir. Conteúdo: `condosched://unit/1`.
Habitação confirmada na API em 2026-11-01: Condo A, Building A, andar 1, porta 101.

1. Ler o QR e registar o nome e email. Confirmar a linha no Table Editor.
2. Sair no perfil. Ler o mesmo QR: deve entrar diretamente com os mesmos dados.
3. Repetir num segundo dispositivo: deve usar o mesmo perfil e continuar a existir só uma linha.
4. Fechar/reabrir a app: deve manter a sessão, mesmo sem internet.
5. Sem internet, sair e ler o QR: deve apresentar erro e não criar uma conta local.
6. Testar QR inválido, ID inexistente, cancelamento e permissão de câmara recusada.
7. Testar registos simultâneos em dois dispositivos: o primeiro perfil deve ser preservado.

Sair/mudar de habitação só limpa a sessão do dispositivo. O perfil da casa continua no
Supabase, pelo que a próxima leitura do mesmo QR o recupera.

Os testes automáticos usam um servidor HTTP local simulado para verificar os pedidos,
a interpretação dos DTOs e os erros. Não substituem a execução do SQL e os testes
num dispositivo real exigidos para a demonstração. Reservas e edição do perfil continuam
por implementar.

Referências:
- ZXing Android Embedded: https://github.com/journeyapps/zxing-android-embedded
- Retrofit: https://lysine.dev/retrofit/
- Funções Supabase: https://supabase.com/docs/guides/database/functions