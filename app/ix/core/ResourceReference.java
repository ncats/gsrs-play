package ix.core;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.util.EntityUtils.EntityWrapper;
import play.Logger;

@JsonSerialize(using = ResourceReference.ResourceReferenceSerializer.class)
public class ResourceReference <T>{
	private String resourceLink;
	private Supplier<T> raw;
	
	public ResourceReference(String uri, Supplier<T> sup){
		resourceLink=uri;
		raw=sup;
	}
	
	public String toString(){
		return resourceLink;
	}
	
	/**
	 * The direct serialized JsonNode expected for raw serialization ('$')
	 * @return
	 */
	public JsonNode rawJson(){
		T rawObj = raw.get();
		if(rawObj instanceof JsonNode){
			return (JsonNode)rawObj;
		}else{
			return EntityWrapper.of(raw.get()).toFullJsonNode();
		}
	}
	
	public static <T> ResourceReference<T> of(String uri,Supplier<T> sup){
		return new ResourceReference<T>(uri, sup);
	}
	
	public static <T> ResourceReference<T> ofRaw(String uri, T raw){
		return new ResourceReference<T>(uri, ()->raw);
	}
	public static ResourceReference<JsonNode> ofSerializedJson(String uri, Supplier<String> rawSupplier){
		Objects.requireNonNull(rawSupplier);
		return new ResourceReference<JsonNode>(uri, ()->{
			ObjectMapper om = new ObjectMapper();
			try {
				return om.readTree(rawSupplier.get());
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
			}
			return null;
		});
	}
	public static ResourceReference<JsonNode> ofSerializedJson(String uri, String raw){
		
		return new ResourceReference<JsonNode>(uri, ()->{
			ObjectMapper om = new ObjectMapper();
			try {
				return om.readTree(raw);
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
			}
			return null;
		});
	}
	
	/**
	 * Serialize the ResourceReference as a URI string by default
	 * @author peryeata
	 *
	 */
	public static class ResourceReferenceSerializer extends JsonSerializer<ResourceReference> {
	    public ResourceReferenceSerializer () {}

	    @Override
	    public void serialize (ResourceReference res, JsonGenerator jgen,
	                           SerializerProvider provider)
	        throws IOException, JsonProcessingException {
	        
	        provider.defaultSerializeValue(res.toString(), jgen);
	    }    
	}
		
	
}
