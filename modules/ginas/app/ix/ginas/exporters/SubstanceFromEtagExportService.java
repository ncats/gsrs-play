package ix.ginas.exporters;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import ix.core.exporters.AbstractEtagExportService;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Substance;
import play.mvc.Http;

import java.util.UUID;

public class SubstanceFromEtagExportService extends AbstractEtagExportService<UUID, Substance> {

    private static final JsonPointer UUID_JSON_POINTER = JsonPointer.compile("/uuid");


    public SubstanceFromEtagExportService(Http.Request currentRequest){
        this(currentRequest,25);
    }

    public SubstanceFromEtagExportService(Http.Request currentRequest, int fetchPageSize) {
        super(currentRequest, fetchPageSize,
                jsonNode -> UUID.fromString(jsonNode.at(UUID_JSON_POINTER).asText()),
                uuid->SubstanceFactory.finder.get().byId(uuid));
    }
}
