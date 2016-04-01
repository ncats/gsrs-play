package ix.test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.ginas.models.v1.Relationship;
import play.libs.ws.WSResponse;
import util.json.JsonUtil;

import static org.junit.Assert.assertTrue;

public final class SubstanceJsonUtil {

	private SubstanceJsonUtil(){
		//can not instantiate
	}
	
	public static JsonNode toUnapproved(JsonNode substance){
		
		return new JsonUtil.JsonNodeBuilder(substance)
				.remove("/approvalID")
				.remove("/approved")
				.remove("/approvedBy")
				
				.set("/status", "pending")
				
				.ignoreMissing()
				.build();
	}

	public static void ensureFailure(WSResponse response){
		int status = response.getStatus();
//		System.out.println("Response is:");
//		System.out.println(response.getBody());
		assertTrue("Expected failure code, got:" + status, status != 200 && status != 201);
	}

	public static void ensurePass(WSResponse response){
		int status = response.getStatus();
		assertTrue("Expected pass code, got:" + status, status == 200 || status == 201);
	}


	public static boolean isLiteralNull( JsonNode js){
		return js.isNull();
	}

	public static void ensureIsValid(JsonNode js){
		assertTrue( isValid(js));
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

	public static String getRefUuid(JsonNode js){
		JsonNode relations = js.get("relationships").get(0);
		JsonNode relatedSubs = relations.get("relatedSubstance");
		return relatedSubs.get("refuuid").asText();
	}
}
