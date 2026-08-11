# organization-parser-goves

Implementação dos contratos de Organograma para os serviços do Governo do Estado do Espírito Santo.

## Objetivo

Este plugin consulta a API `api.organograma.es.gov.br` e converte organizações e lotações para os tipos definidos em `pmo-core-organization-parser`.

Ele fornece dois beans Spring:

- `OrganizationParser`, implementa `IOrganizationParser<String>`;
- `GovesWorkLocationParser`, implementa `IWorkLocationParser`.

## Funcionalidades

- listar organizações filhas do GOVES;
- resolver a sigla da organização responsável por uma unidade;
- consultar os dados de uma lotação pelo GUID;
- armazenar em cache a resolução de siglas por unidade;
- tratar unidades e lotações inexistentes como `Optional.empty()`.

## Requisitos

- Java 8 ou superior;
- Spring Boot 2.2.12;
- token Bearer com acesso à API de Organograma;
- JitPack configurado no projeto consumidor.

## Instalação

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.sep-es-br:pmo-core-organization-parser:1.1.2'
    implementation 'com.github.sep-es-br:organization-parser-goves:1.1.1'
}
```

## Uso no OpenPMO API

O parser pode ser carregado por coordenada no `application.properties`:

```properties
app.organization.parser.repository=com.github.sep-es-br:organization-parser-goves:1.1.1
```

O token não é criado pelo plugin. O projeto consumidor deve obtê-lo e informá-lo em cada chamada.

## Consulta de organizações

```java
@Service
public class OrganizationService {

    private final IOrganizationParser<String> parser;

    public OrganizationService(final IOrganizationParser<String> parser) {
        this.parser = parser;
    }

    public List<OrganizationDto> findAll(final String token) {
        return parser.getOrganizations(token);
    }
}
```

`OrganizationParser.getOrganizations(token)` consulta:

```text
GET /organizacoes/fe88eb2a-a1f3-4cb1-a684-87317baf5a57/filhas
```

A organização raiz é ignorada. Cada item é normalizado com:

- `integration = GOVES`;
- `suffix = ES`;
- `sector = PUBLIC`.

Falhas de leitura ou interpretação da resposta geram `RuntimeException`.

## Resolução de sigla por unidade

```java
Optional<String> abbreviation = parser.findAbbreviationByUnit(
    unitGuid,
    token
);
```

A consulta utiliza `GET /unidades/{unitId}`. Os resultados, inclusive ausências, são armazenados em um `ConcurrentHashMap`.

- resposta encontrada: retorna `organizacao.sigla`;
- HTTP 404: retorna `Optional.empty()`;
- organização ou sigla ausente: retorna `Optional.empty()`;
- outros erros: gera `RuntimeException`.

Para invalidar o cache:

```java
parser.clearCache();
```

## Consulta de lotação

```java
Optional<WorkLocationDto> location = workLocationParser.findByGuid(
    locationGuid,
    token
);
```

`GovesWorkLocationParser` utiliza `GET /unidades/{guid}/info` e mapeia:

| Campo da resposta | Campo do DTO |
| --- | --- |
| `guid` | `guid` |
| `nome` | `name` |
| `sigla` | `abbreviation` |
| `guidOrganizacao` | `organizationGuid` |

GUID nulo ou vazio gera `IllegalArgumentException`. HTTP 404 ou corpo vazio resultam em `Optional.empty()`; campos opcionais ausentes permanecem nulos.

## Registro dos beans

O core registra `OrganizationParserAutoConfig` por `spring.factories` e executa component scan no pacote `br.gov.es.pmo.organization_parser`. Por isso, basta manter este plugin no classpath para que seus componentes sejam descobertos.

## Build e testes

No Windows:

```powershell
.\gradlew.bat clean test build
```

Em Linux ou macOS:

```bash
./gradlew clean test build
```
