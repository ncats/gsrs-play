package ix.test.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.hazelcast.util.JsonUtil;
import ix.core.controllers.EntityFactory;
import ix.ginas.exporters.ExportMetaData;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Objects;

/**
 * Created by katzelda on 5/2/17.
 */
public class MyDownloadsAPI {
    private final RestSession session;
    private final GinasTestServer ts;

    public MyDownloadsAPI(RestSession session, GinasTestServer ts) {
        if(session.getUser() == null){
            throw new IllegalStateException("must be logged in");
        }
        this.session = session;
        this.ts = Objects.requireNonNull(ts);
    }

    public List<ExportMetaData> getAllDownloads() throws IOException{
        JsonNode node = session.getAsJson("ginas/app/downloads");


        return EntityFactory.EntityMapper.FULL_ENTITY_MAPPER().readValue(node.toString(), new TypeReference<List<ExportMetaData>>() {
        });

    }


    public File getExportDir() {
        return ts.getUserExportDir(session.getUser());
    }
}
