package ix.test.ix.test.server;

import com.fasterxml.jackson.databind.JsonNode;
import ix.test.SubstanceJsonUtil;

import java.util.Objects;

/**
 * Created by katzelda on 3/18/16.
 */
public class ControlledVocab {
    /*
      JsonNode vacabs = session.vocabulariesJSON();
            JsonNode content = vacabs.at("/content");
            assertTrue("There should be content found in CV response", !content.isNull());
            assertTrue("There should be more than 0 CVs loaded, found (" + content.size() + ")", content.size() >= 1);
            assertTrue("There should be more than 0 CVs listed in total, found (" + vacabs.at("/total").asText() + ")", vacabs.at("/total").asInt() >= 1);
     */

    private final JsonNode vocabs;

    public ControlledVocab(JsonNode vocabs) {
        Objects.requireNonNull(vocabs);
        this.vocabs = vocabs;
    }

    public int getTotalCount(){
        return vocabs.at("/total").asInt();
    }

    public int getLoadedCount(){
        JsonNode content = vocabs.at("/content");
        if(SubstanceJsonUtil.isLiteralNull(content)){
            throw new NullPointerException("content is 'null'");
        }
        return content.size();
    }
}
