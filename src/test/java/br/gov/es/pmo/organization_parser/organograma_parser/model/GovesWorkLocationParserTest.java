package br.gov.es.pmo.organization_parser.organograma_parser.model;

import br.gov.es.pmo.organization_parser.pmo_base.model.WorkLocationDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GovesWorkLocationParserTest {

    @Test
    void shouldMapWorkLocationResponse() {
        final WorkLocationDto result = GovesWorkLocationParser.parse(
            "{"
                + "\"guid\":\"unit-guid\","
                + "\"nome\":\"Gerência de Tecnologia\","
                + "\"sigla\":\"GTI\","
                + "\"guidOrganizacao\":\"organization-guid\""
                + "}"
        );

        assertEquals("unit-guid", result.getGuid());
        assertEquals("Gerência de Tecnologia", result.getName());
        assertEquals("GTI", result.getAbbreviation());
        assertEquals("organization-guid", result.getOrganizationGuid());
    }

    @Test
    void shouldKeepMissingOptionalFieldsNull() {
        final WorkLocationDto result = GovesWorkLocationParser.parse(
            "{\"guid\":\"unit-guid\"}"
        );

        assertEquals("unit-guid", result.getGuid());
        assertNull(result.getName());
        assertNull(result.getAbbreviation());
        assertNull(result.getOrganizationGuid());
    }
}
