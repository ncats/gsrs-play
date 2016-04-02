package ix.ginas.models;

import ix.core.models.Keyword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import play.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class KeywordDeserializer extends JsonDeserializer<Keyword> {
	
    private final String label;
    public KeywordDeserializer (String label) {
    	
        this.label = label;
    }
    
    public KeywordDeserializer () {
        this.label = null;
    }

    public Keyword deserialize
        (JsonParser parser, DeserializationContext ctx)
        throws IOException, JsonProcessingException {
        Keyword kw=null;
        JsonToken token = parser.getCurrentToken();
        if (token == JsonToken.VALUE_STRING) {
        	kw= new Keyword(label, parser.getValueAsString());
        }
        return kw;
    }
    
    //kept getter package-private to keep it
    //the same as when we used direct field access
    String getLabel() {
        return label;
    }
    
    public static class LanguageDeserializer extends KeywordDeserializer {
        public LanguageDeserializer () {
            super (GinasCommonData.LANGUAGE);
        }
    }
    public static class DomainDeserializer extends KeywordDeserializer {
        public DomainDeserializer () {
            super (GinasCommonData.DOMAIN);
        }
    }
    public static class ReferenceTagDeserializer extends KeywordDeserializer {
        public ReferenceTagDeserializer () {
            super (GinasCommonData.REFERENCE_TAG);
        }
    }
    public static class TagDeserializer extends KeywordDeserializer {
        public TagDeserializer () {
            super (GinasCommonSubData.TAG);
        }
    }
    public static class PartDeserializer extends KeywordDeserializer {
        public PartDeserializer () {
            super ("Parts");
        }
    }
    public static class JurisdictionDeserializer extends KeywordDeserializer {
        public JurisdictionDeserializer () {
            super (GinasCommonData.NAME_JURISDICTION);
        }
    }
    public static class SubClassDeserializer extends KeywordDeserializer {
        public SubClassDeserializer () {
            super (GinasCommonData.SUB_CLASS);
        }
    }
    
}
