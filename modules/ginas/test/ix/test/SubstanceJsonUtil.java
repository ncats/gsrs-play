package ix.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import ix.ginas.models.v1.Reference;
import play.libs.ws.WSResponse;
import util.json.JsonUtil;
import util.json.JsonUtil.JsonNodeBuilder;

public final class SubstanceJsonUtil {

	private SubstanceJsonUtil(){
		//can not instantiate
	}
	
	/**
	 * Normalizes a substance JSON to be as expected for a typical submission.
	 * Specifically, removing approval information, changing status to pending,
	 * and ensuring that the public domain tag is added to the first
	 * reference.
	 * @param substance
	 * @return
	 */
	public static JsonNode prepareUnapprovedPublic(JsonNode substance){
		
		JsonNodeBuilder jnb=new JsonUtil.JsonNodeBuilder(substance)
				.remove("/approvalID")
				.remove("/approved")
				.remove("/approvedBy")
				
				.set("/status", "pending")
				.ignoreMissing();
		boolean hasReferences=false;
		if(substance.at("/references")!=null){
			for(JsonNode jsn:substance.at("/references")){
				hasReferences=true;break;
			}
		}
		
		if(hasReferences){
			jnb=jnb.add("/references/0/tags/-", Reference.PUBLIC_DOMAIN_REF);
			jnb=jnb.set("/references/0/publicDomain", true);
			jnb=jnb.set("/references/0/access", Collections.emptyList());
		}else{
			List<String> acc=new ArrayList<String>();
			acc.add("protected");
			jnb = jnb.set("/access", acc);
			
		}
		
		return jnb.build();
		
	}
	
	public static JsonNode prepareUnapproved(JsonNode substance){
		
		return new JsonUtil.JsonNodeBuilder(substance)
				.remove("/approvalID")
				.remove("/approved")
				.remove("/approvedBy")
				
				.set("/status", "pending")
				.ignoreMissing().build();
		
		
	}

	public static void ensureFailure(WSResponse response){
		int status = response.getStatus();
//		System.out.println("Response is:");
//		System.out.println(response.getBody());
		assertTrue("Expected failure code, got:" + status, status != 200 && status != 201);
	}

	public static void ensurePass(WSResponse response){
		int status = response.getStatus();
		try{
			assertTrue("Expected pass code, got:" + status, status == 200 || status == 201);
		}catch(Throwable e){
			System.err.println(response.getBody());
			throw e;
		}
	}


	public static boolean isLiteralNull( JsonNode js){
		return js.isNull();
	}

	public static void ensureIsValid(JsonNode js){
		try{
			assertTrue( isValid(js));
		}catch(Throwable e){
			System.err.println(js.toString());
			throw e;
		}
	}
	
	public static boolean isValid(JsonNode js){
		return js.get("valid").asBoolean();
	}

    public static String getApprovalStatus(JsonNode js){
        return js.get("status").asText().toLowerCase();
    }

    public static String getApprovalId(JsonNode js){
        return js.get("approvalID").asText();
    }

	public static String getRefUuidOnFirstRelationship(JsonNode js){
		JsonNode relations = js.get("relationships").get(0);
		JsonNode relatedSubs = relations.get("relatedSubstance");
		return relatedSubs.get("refuuid").asText();
	}
	public static String getTypeOnFirstRelationship(JsonNode js){
		return js.at("/relationships/0/type").asText();
	}


}
