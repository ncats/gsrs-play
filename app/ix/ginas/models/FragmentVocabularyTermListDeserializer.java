package ix.ginas.models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import ix.core.controllers.AdminFactory;
import ix.core.models.Group;
import ix.ginas.models.v1.FragmentVocabularyTerm;
import ix.ginas.models.v1.VocabularyTerm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class FragmentVocabularyTermListDeserializer extends JsonDeserializer<List<VocabularyTerm>> {
    public FragmentVocabularyTermListDeserializer() {
    }

    public List<VocabularyTerm> deserialize
            (JsonParser parser, DeserializationContext ctx)
            throws IOException, JsonProcessingException {

    	List<VocabularyTerm> terms = new ArrayList<VocabularyTerm>();
        if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
            while (JsonToken.END_ARRAY != parser.nextToken()) {
                VocabularyTerm vt = parser.readValueAs(FragmentVocabularyTerm.class);
                terms.add(vt);
            }
        } else {}
        return terms;
    }
}


