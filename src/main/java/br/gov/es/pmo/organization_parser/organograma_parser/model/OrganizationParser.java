package br.gov.es.pmo.organization_parser.organograma_parser.model;

import br.gov.es.pmo.organization_parser.pmo_base.model.IOrganizationParser;
import br.gov.es.pmo.organization_parser.pmo_base.model.OrganizationDto;
import br.gov.es.pmo.organization_parser.pmo_base.utils.ApiClient;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component("org")
public class OrganizationParser
    implements IOrganizationParser<String> {

    private static final String INTEGRATION = "GOVES";
    private static final String SUFFIX = "ES";
    private static final String SECTOR = "PUBLIC";
    private static final String GUID = "guid";
    private static final String SIGLA = "sigla";
    private static final String RAZAO_SOCIAL = "razaoSocial";

    private static final String GOVES_GUID =
        "fe88eb2a-a1f3-4cb1-a684-87317baf5a57";

    private final ApiClient apiClient =
        new ApiClient("https://api.organograma.es.gov.br");

    private final ConcurrentMap<String, Optional<String>> abbreviationByUnit =
        new ConcurrentHashMap<>();

    public List<OrganizationDto> getOrganizations(String token) {

        String json =
            apiClient
                .doGetRequest(
                    "/organizacoes/"
                        + GOVES_GUID
                        + "/filhas",
                    token
                )
                .block();

        try {
              JSONArray array =
                (JSONArray) new JSONParser(
                    JSONParser.DEFAULT_PERMISSIVE_MODE
                ).parse(json);

            List<OrganizationDto> result = new ArrayList<>();

            for (Object obj : array) {

                JSONObject o = (JSONObject) obj;

                if (GOVES_GUID.equals((String) o.get(GUID))) {
                    continue;
                }

                OrganizationDto dto = new OrganizationDto(
                    (String) o.get(GUID),
                    (String) o.get(SIGLA),
                    (String) o.get(RAZAO_SOCIAL),
                    INTEGRATION,
                    SUFFIX,
                    SECTOR
                );

                result.add(dto);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException(
                "Erro ao parsear organizações",
                e
            );
        }
    }

    @Override
    public Optional<String> findAbbreviationByUnit(
        final String unitId,
        final String token
    ) {
        return abbreviationByUnit.computeIfAbsent(
            unitId,
            id -> loadAbbreviationByUnit(id, token)
        );
    }

    @Override
    public void clearCache() {
        abbreviationByUnit.clear();
    }

    private Optional<String> loadAbbreviationByUnit(
        final String unitId,
        final String token
    ) {
        try {
            final String json = apiClient
                .doGetRequest("/unidades/" + unitId, token)
                .block();
            final JSONObject unit = (JSONObject) new JSONParser(
                JSONParser.DEFAULT_PERMISSIVE_MODE
            ).parse(json);
            final JSONObject organization =
                (JSONObject) unit.get("organizacao");
            if (organization == null) {
                return Optional.empty();
            }
            final String abbreviation = (String) organization.get(SIGLA);
            return abbreviation == null || abbreviation.trim().isEmpty()
                ? Optional.empty()
                : Optional.of(abbreviation);
        } catch (final WebClientResponseException.NotFound exception) {
            return Optional.empty();
        } catch (final Exception exception) {
            throw new RuntimeException(
                "Erro ao consultar unidade no Organograma",
                exception
            );
        }
    }

}
