# Organização da CondoSched

Manter estas regras nas alterações futuras. As teóricas do professor (T4/T5/T6)
e `app/doc/Architecture-guide.md` são a referência de organização e explicação.

- Código Java simples, com responsabilidades pequenas e nomes consistentes.
- `ui`: todas as Activities, adapters, apresentação e permissões do dispositivo.
  As Activities observam ViewModels; não chamam Retrofit, DTOs ou SessionManager.
- `viewmodel`: estado e coordenação dos ecrãs através de ViewModel e LiveData.
  Não guardar Activities ou Views. Usar Application apenas quando necessário
  para obter um Repository local que use o Context da aplicação.
- `repository`: acesso e preparação de dados, cancelamento e conversão DTO → domínio.
  Não devolver textos da interface: devolver os códigos de `domain/DataError`.
- `network`: interfaces Retrofit e configuração dos clientes; não colocar lógica de ecrãs.
- `dto`: apenas contratos JSON, com construtor vazio, getters/setters e anotações de serialização.
- `domain`: modelos e validação, sem dependências da interface ou de Retrofit.
- `session`: SharedPreferences; o acesso passa pelo SessionRepository.
- Ecrãs com dados estáticos ou recebidos num Intent não precisam de um ViewModel vazio.
- Layouts: `activity_<ecra>.xml`, `item_<elemento>.xml`, `layout_<componente>.xml`.
- Textos em recursos de português e inglês; estilos/cores partilhados em recursos.
- Se mudar uma responsabilidade, atualizar o guia com o percurso dos dados e a função
  dos ficheiros, para o aluno conseguir explicar a implementação ao professor.
- Executar compilação, testes relevantes e Android Lint. Preservar a compatibilidade com API 26.
- Não acrescentar caches, builds, configurações locais ou credenciais aos commits.
