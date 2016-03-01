package ix.test;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;
import util.json.Change;
import util.json.Changes;
import util.json.ChangesBuilder;
import util.json.JsonUtil;

import static org.junit.Assert.*;


public class EditingWorkflowTest {
	

    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);

    
    @Test
	public void testEditRoundTrip() {
		final File resource=new File("test/testJSON/toedit.json");
		ts.run(new Runnable() {
            public void run() {
                try {
                	JsonNode entered;
                	JsonNode returned;
                	JsonNode fetched;
                	JsonNode updated;
                	JsonNode updatedReturned;
                	JsonNode updateFetched;
                	String uuid;
                	
                	ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1);
                	
                	//register and store returned
                	try(InputStream is=new FileInputStream(resource)){
	                	entered= new ObjectMapper().readTree(is);
	                    uuid=entered.get("uuid").asText();
	                    returned = ts.submitSubstanceJSON(entered);
                	}
                	
                	//fetch registered

					fetched = ts.fetchSubstanceJSON(uuid);

                	String oldName="TRANSFERRIN ALDIFITOX S EPIMER";
                	String newName="TRANSFERRIN ALDIFITOX S EPIMER CHANGED";
                	String oldVersion="1";
                	String newVersion="2";
                	//make name change
                	{

                		Map edited = (new ObjectMapper()).treeToValue(fetched, Map.class);
                		((Map)((List)(edited.get("names"))).get(0)).put("name", newName);
                		updated=(new ObjectMapper()).valueToTree(edited);

						Changes changes = JsonUtil.getDestructiveChanges(fetched, updated);


						Map<String, Change> changesMap = new HashMap<String, Change>();
						changesMap.put("/names/0/name", new Change("/names/0/name", oldName, newName, Change.ChangeType.REPLACED));

						Changes expectedChanges = new Changes(changesMap);

						assertEquals(expectedChanges, changes);
						/*JsonNode jp = JsonDiff.asJson(fetched,updated);
	                    int changes=0;
	                    
	                    for(JsonNode jschange: jp){
	                    	changes++;
	                    	assertEquals(fetched.at(jschange.get("path").asText()).asText(),oldName);
	                    	assertEquals(jschange.get("op").asText(),"replace");
	                    	assertEquals(jschange.get("path").asText(),"/names/0/name");
	                    	assertEquals(jschange.get("value").asText(),newName);
	                    }
	                    assertTrue(changes==1);
	                    */
                	}
                	
                	//submit edit
                	{
                		updatedReturned = ts.updateSubstanceJSON(updated);
                		
                	}
                	
                	//refetch editted
                	{
                		updateFetched = ts.fetchSubstanceJSON(uuid);
                		assertEquals(Integer.parseInt(newVersion),Integer.parseInt(updateFetched.at("/version").asText()));
                		assertEquals(newName,updateFetched.at("/_name").asText());
                		
                		JsonNode jp = JsonDiff.asJson(updated,updateFetched);
                		int changes=0;
                		for(JsonNode jschange: jp){
	                    	
	                    	String path=jschange.get("path").asText();
	                    	if(path.equals("/version") ||
	                    	   path.equals("/_name") ||
	                    	   path.equals("/lastEdited") ||
	                    	   path.equals("/names/0/lastEdited")
	                    			){
	                    	}else{
	                    		System.out.println("CHANGED:" + jschange + " old: " + updated.at(jschange.get("path").asText()));
	                    		changes++;
	                    	}
	                    	
	                    }
                		assertTrue(changes==0);
                	}
                	
                	{
                		JsonNode edits = ts.fetchSubstanceHistoryJSON(uuid);
                		for(JsonNode edit: edits){
                			//Ok, I'm just going to look at the null-path edits
                			//there should be 1
                			if(
                			   !edit.get("oldValue").isNull() && 
                			   !edit.get("newValue").isNull() &&
                			   edit.get("path").isNull()
                					){
                				JsonNode oldv=ts.urlJSON(edit.get("oldValue").asText());
                				JsonNode newv=ts.urlJSON(edit.get("newValue").asText());
                				JsonNode jp = JsonDiff.asJson(updateFetched,newv);
                				//Expecting nothin'
                				//System.out.println("Hopefully, there's nothing here:");
                				int chcount=0;
                				for(JsonNode jschange: jp){
        	                    		System.out.println("CHANGES FROM NEW:" + jschange + " old: " + updateFetched.at(jschange.get("path").asText()));
        	                    		chcount++;
        	                    }
                				assertTrue(chcount==0);
                				jp = JsonDiff.asJson(fetched,oldv);
                				//Expecting nothin'
                				//System.out.println("Hopefully, there's nothing here too:");
                				chcount=0;
                				for(JsonNode jschange: jp){
        	                    		System.out.println("CHANGES FROM OLD:" + jschange + " old: " + fetched.at(jschange.get("path").asText()));
        	                    		chcount++;
        	                    }
                				assertTrue(chcount==0);
                			}
	                    }
                	}
                	
                	
                } catch (Throwable e1) {
                	e1.printStackTrace();
                    throw new IllegalStateException(e1);
                }
            }
        });
	}
	
    
}
