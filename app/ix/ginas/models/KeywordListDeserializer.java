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

public class KeywordListDeserializer extends JsonDeserializer<List<Keyword>> {
    final String label;
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
}
