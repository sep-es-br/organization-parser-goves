package br.gov.es.pmo.organization_parser.organograma_parser.model;

import br.gov.es.pmo.organization_parser.pmo_base.model.IWorkLocationParser;
import br.gov.es.pmo.organization_parser.pmo_base.model.WorkLocationDto;
import br.gov.es.pmo.organization_parser.pmo_base.utils.ApiClient;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Component
public class GovesWorkLocationParser implements IWorkLocationParser {

    private static final String BASE_URL = "https://api.organograma.es.gov.br";
    private static final String UNIT_PATH = "/unidades/%s/info";

    private final ApiClient apiClient;

    public GovesWorkLocationParser() {
        this(new ApiClient(BASE_URL));
    }

    GovesWorkLocationParser(final ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Optional<WorkLocationDto> findByGuid(
        final String guid,
        final String token
    ) {
        if(isBlank(guid)) {
            throw new IllegalArgumentException("O GUID da lotação é obrigatório.");
        }

        try {
            final String response = this.apiClient
                .doGetRequest(String.format(UNIT_PATH, guid), token)
                .block();

            if(isBlank(response)) {
                return Optional.empty();
            }
            return Optional.of(parse(response));
        }
        catch(final WebClientResponseException.NotFound ignored) {
            return Optional.empty();
        }
    }

    static WorkLocationDto parse(final String response) {
        final JSONObject json = new JSONObject(response);
        return new WorkLocationDto(
            nullableValue(json, "guid"),
            nullableValue(json, "nome"),
            nullableValue(json, "sigla"),
            nullableValue(json, "guidOrganizacao")
        );
    }

    private static String nullableValue(
        final JSONObject json,
        final String key
    ) {
        final String value = json.optString(key, null);
        return isBlank(value) ? null : value;
    }

    private static boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }
}
