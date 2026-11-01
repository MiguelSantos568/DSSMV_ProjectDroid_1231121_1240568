# Interface e limpeza do projeto

## Interface

Os ecrãs de início, QR, formulário, condomínio, comodidades e perfil partilham agora:

- Cores verde e neutras em `colors.xml`, com variantes para modo escuro.
- Estilos de texto e botões em `themes.xml`.
- Cartões arredondados e ícones vetoriais leves, desenhados nos recursos XML.
- Conteúdo com largura máxima de 720dp e margens maiores em tablets.
- Navegação inferior com destaque do separador selecionado.
- Botões principais com altura mínima de 56dp e controlos de cabeçalho de 48dp.
- Textos em recursos de português e inglês.

O ecrã de comodidades mantém o `ListView` e o `ArrayAdapter`. Cada cartão identifica
o prédio, o tipo de espaço, o horário, a capacidade e a necessidade de reserva.
O perfil apresenta o contacto partilhado e a habitação, sem dados de exemplo nem o
antigo botão de edição que apenas mostrava uma mensagem de desenvolvimento.

As alterações visuais não alteram o SQL, os perfis existentes ou o conteúdo dos QR.

## Ficheiros removidos

As referências em Java, XML, Manifest, testes e documentação foram verificadas antes
da remoção. A compilação, os testes e o Android Lint verificam as dependências restantes.

| Ficheiros | Motivo |
| --- | --- |
| `network/AuthService.java` | Fluxo antigo por email e palavra-passe, sem chamadas no acesso por QR. |
| `network/ResidentService.java` | Serviço antigo sem chamadas no perfil partilhado por casa. |
| `ui/BuildingsActivity.java` | Activity vazia, sem navegação nem declaração no Manifest. |
| `domain/DTO/BookingDTO.java`, `DocumentDTO.java`, `ResidentDTO.java` | DTOs sem consumidores, construtores ou conversores usados. Serão definidos de acordo com os endpoints quando essas operações forem implementadas. |
| `res/layout/buildinglist.xml`, `optionmenu_main.xml` | Layout vazio e menu antigo que nenhum ecrã carrega. |
| 16 imagens de `res/mipmap-hdpi` | Ícones e imagens sem referências; os ícones atuais são vetoriais. Os dois ícones launcher foram preservados. |

Também foram removidos textos e cores sem utilização e a constante do endereço de
autenticação antigo. O `.gitignore` evita acrescentar novos caches, builds, APKs e
configurações locais ao Git. Não retira automaticamente ficheiros já versionados.

## Modelos mantidos deliberadamente

`Amenity`, `Resident`, `Document` e `Unit` ainda não têm consumidores no fluxo atual.
Foram mantidos como parte do modelo de domínio documentado, para as próximas
funcionalidades de reservas e integração. `Booking`, `Event`, `Condominium`, `Building`
e `Coordinates` também participam no modelo e/ou nos testes existentes.

Documentação das APIs, modelo UML e testes existentes foram preservados.

## Referências de design

- Android, estrutura de layouts e adaptação ao tamanho do ecrã:
  https://developer.android.com/design/ui/mobile/guides/layout-and-content/layout-basics
- Android, contraste e áreas de toque:
  https://developer.android.com/guide/topics/ui/accessibility/apps
- Material Design, cartões:
  https://m3.material.io/components/cards/overview
