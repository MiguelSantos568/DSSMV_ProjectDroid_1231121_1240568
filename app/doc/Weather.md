# Meteorologia

O formulário de marcação e `WeatherActivity` usam o mesmo tipo `WeatherViewModel`,
com uma instância independente por Activity. Cada Activity observa
`LiveData<UiState<List<Forecast>>>`; o estado e o pedido sobrevivem à rotação.

```text
Activity → WeatherViewModel → WeatherRepository → WeatherApi (Retrofit)
JSON → WeatherDTO → Forecast → LiveData → Activity
```

`WeatherLocation`, em `ui`, obtém coordenadas depois de a Activity tratar das
permissões. Ao destruir o ecrã, cancela a obtenção de localização. O ViewModel
recebe apenas latitude, longitude, número de dias e idioma, sem Activity ou Context.
Sem coordenadas válidas, o Repository usa Porto. A previsão é para a localização
do dispositivo (ou Porto), não para coordenadas do condomínio.

`WeatherApi` declara GET `forecast.json` com `key`, `q`, `days` e `lang`.
`WeatherRepository` usa `enqueue()`, valida o JSON e converte `WeatherDTO` em
objetos `Forecast` com LocalDate, código de condição e descrição. Os DTOs internos
reproduzem a estrutura `forecast → forecastday → day → condition`.
O Repository devolve códigos DataError e cancela pedidos em curso quando necessário.

`ForecastAdapter` preenche `item_forecast.xml` no ecrã meteorológico.
`WeatherDisplay` escolhe os ícones nos dois ecrãs. O formulário apresenta a previsão
correspondente à data escolhida; uma falha ou data sem previsão não impede reservar.
Não há reflexão, HttpURLConnection, parsing de textos com `;` ou threads de rede
nas Activities. O Retrofit gere a execução assíncrona.

A chave é lida de `weather.api.key` no `local.properties` e incluída no BuildConfig
durante a compilação. Não deve ser versionada; noutro computador é necessário
configurar essa propriedade. Uma chave vazia torna a previsão indisponível sem
bloquear o resto da app. A API pode devolver menos dias do que os pedidos conforme
o plano: na verificação real devolveu 3 dias, mesmo pedindo 14.

## Configuração local

No `local.properties` da raiz do projeto, definir a chave da WeatherAPI:

```properties
weather.api.key=SUA_CHAVE
```

Não versionar o `local.properties`. A configuração já existente neste computador foi preservada.

## Testes

`WeatherRepositoryTest` verifica o contrato HTTP, conversão, localização alternativa,
respostas inválidas e cancelamento. `WeatherViewModelTest` verifica estado, rotação,
mudança de idioma e libertação de pedidos.

[Organização geral](Architecture-guide.md) · [Marcações](Bookings.md)
