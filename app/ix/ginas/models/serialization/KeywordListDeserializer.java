package ix.ginas.models.serialization;

import ix.core.models.Keyword;
import ix.ginas.models.GinasCommonData;
import ix.ginas.models.GinasCommonSubData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import play.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class KeywordListDeserializer extends JsonDeserializer<List<Keyword>> {
	
    private final String label;
    public KeywordListDeserializer (String label) {
    	
        this.label = label;
    }
    
    public KeywordListDeserializer () {
        this.label = null;
    }

    public List<Keyword> deserialize
        (JsonParser parser, DeserializationContext ctx)
        throws IOException, JsonProcessingException {
        List<Keyword> keywords = null;
        JsonToken token = parser.getCurrentToken();
        if (JsonToken.START_ARRAY == token) {
            keywords = new ArrayList<Keyword>();
            while ((token = parser.nextToken()) != JsonToken.END_ARRAY) {
                if (token == JsonToken.VALUE_STRING) {
                    keywords.add(new Keyword
                                 (label, parser.getValueAsString()));
                }
            }
        }
        return keywords;
    }
    
    public static List<Keyword> deserialize(List nonKeyword, String label){
    	List<Keyword> keywords = new ArrayList<Keyword>();
    	for(Object s:nonKeyword){
    		if(s instanceof Keyword){
    			keywords.add((Keyword)s);
    		}else{
	    		keywords.add(new Keyword(label, s.toString()));
    		}
    	}
    	return keywords;
    }
    
    //kept getter package-private to keep it
    //the same as when we used direct field access
    String getLabel() {
        return label;
    }
    
    public static class LanguageListDeserializer extends KeywordListDeserializer {
        public LanguageListDeserializer () {
            super (GinasCommonData.LANGUAGE);
        }
    }
    public static class DomainListDeserializer extends KeywordListDeserializer {
        public DomainListDeserializer () {
            super (GinasCommonData.DOMAIN);
        }
    }
    public static class ReferenceTagListDeserializer extends KeywordListDeserializer {
        public ReferenceTagListDeserializer () {
            super (GinasCommonData.REFERENCE_TAG);
        }
    }
    public static class TagListDeserializer extends KeywordListDeserializer {
        public TagListDeserializer () {
            super (GinasCommonSubData.TAG);
        }
    }
    public static class PartListDeserializer extends KeywordListDeserializer {
        public PartListDeserializer () {
            super ("Parts");
        }
    }
    public static class JurisdictionListDeserializer extends KeywordListDeserializer {
        public JurisdictionListDeserializer () {
            super (GinasCommonData.NAME_JURISDICTION);
        }
    }
    
}
