package ix.test;

import com.fasterxml.jackson.databind.JsonNode;

import util.json.JsonUtil;

public class SubstanceJsonUtil {
	
	
	public static JsonNode toUnapproved(JsonNode substance){
		
		return new JsonUtil.JsonNodeBuilder(substance)
				.remove("/approvalID")
				.remove("/approved")
				.remove("/approvedBy")
				
				.set("/status", "pending")
				
				.ignoreMissing()
				.build();
	}
}
