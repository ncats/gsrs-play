package ix.test.ix.test.server;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.ws.WSResponse;

import java.util.Objects;

import static org.junit.Assert.assertTrue;

/**
 * Created by katzelda on 3/17/16.
 */
public class SubstanceAPI {

    private static final String API_URL_VALIDATE = "ginas/app/api/v1/substances/@validate";
    private static final String API_URL_SUBMIT = "ginas/app/api/v1/substances";
    private static final String API_URL_FETCH = "ginas/app/api/v1/substances($UUID$)?view=full";
    private static final String API_URL_HISTORY = "ginas/app/api/v1/substances($UUID$)/@edits";

    private static final String API_URL_APPROVE = "ginas/app/api/v1/substances($UUID$)/@approve";
    private static final String API_URL_UPDATE = "ginas/app/api/v1/substances";

    private static final String API_URL_SUBSTANCES_SEARCH="ginas/app/api/v1/substances/search";



    private static final String UI_URL_SUBSTANCE="http://localhost:9001/ginas/app/substance/$ID$";
    private static final String UI_URL_SUBSTANCE_VERSION="ginas/app/substance/$ID$/v/$VERSION$";

    private final RestSession session;

    private final long timeout = 10_000L;
    public SubstanceAPI(RestSession session) {
        Objects.requireNonNull(session);
        this.session = session;
    }

    public RestSession getSession(){
        return session;
    }
    public JsonNode search(){
        return session.getAsJson(API_URL_SUBSTANCES_SEARCH);
    }

    public WSResponse submitSubstance(JsonNode js){
        return session.createRequestHolder(API_URL_SUBMIT).post(js).get(timeout);
    }
    public JsonNode submitSubstanceJson(JsonNode js){
        return session.exctractJSON(submitSubstance(js));
    }

    public WSResponse updateSubstance(JsonNode js){
        return session.createRequestHolder(API_URL_UPDATE).put(js).get(timeout);

    }
    public JsonNode updateSubstanceJson(JsonNode js){
        return session.exctractJSON(updateSubstance(js));

    }

    public WSResponse validateSubstance(JsonNode js){
        return session.createRequestHolder(API_URL_VALIDATE).post(js).get(timeout);
    }
    public JsonNode validateSubstanceJson(JsonNode js){
        return session.exctractJSON( validateSubstance(js));
    }

    public WSResponse fetchSubstance(String uuid){
        return session.createRequestHolder(API_URL_FETCH.replace("$UUID$", uuid)).get().get(timeout);
    }
    public JsonNode fetchSubstanceJson(String uuid){
        return session.exctractJSON(fetchSubstance(uuid));
    }

    public WSResponse fetchSubstance(String id, int version){
        return session.createRequestHolder(UI_URL_SUBSTANCE_VERSION.replace("$ID$", id).replace("$VERSION$", Integer.toString(version))).get().get(timeout);
    }

    public WSResponse approveSubstance(String uuid){
        return session.createRequestHolder(API_URL_APPROVE.replace("$UUID$", uuid)).get().get(timeout);
    }

    public JsonNode approveSubstanceJson(String uuid){
        return session.exctractJSON( approveSubstance(uuid));
    }

    public WSResponse fetchSubstanceHistory(String uuid, int version){
        return session.createRequestHolder(API_URL_HISTORY.replace("$UUID$", uuid))
                .setQueryParameter("filter","path=null AND version=\'" + version + "\'")
                .get().get(timeout);
    }


    public JsonNode fetchSubstancesSearchJSON() {
        return session.exctractJSON(fetchSubstancesSearch());
    }
    public WSResponse fetchSubstancesSearch() {
        return session.createRequestHolder(API_URL_SUBSTANCES_SEARCH).get().get(timeout);
    }

    /**
     * Get the summary JSON which contains the oldValue and newValue URLs
     * for this version change.
     * @param uuid the UUID of the substance to fetch.
     *
     * @param version the version to of the substance to fetch.
     * @return the JsonNode , should not be null.
     */
    public JsonNode fetchSubstanceHistoryJson(String uuid, int version){
        return session.exctractJSON(fetchSubstanceHistory(uuid, version));
    }

    public JsonHistoryResult fetchSubstanceJson(String uuid, int version){
        JsonNode edits = fetchSubstanceHistoryJson(uuid,version);
        //should only have 1 edit...so this should be safe
        JsonNode edit = edits.iterator().next();
        JsonNode oldv= session.urlJSON(edit.get("oldValue").asText());
        JsonNode newv= session.urlJSON(edit.get("newValue").asText());



        return new JsonHistoryResult(edit, oldv, newv);
    }


}
