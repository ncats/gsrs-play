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
                		updated=new JsonUtil.JsonNodeBuilder(fetched).set("/names/0/name", newName).build();
						
                		Changes changes = JsonUtil.computeChanges(fetched, updated);
						Changes expectedChanges = new ChangesBuilder()
													.replaced("/names/0/name", oldName, newName)
													.build();
						Changes expectedChangesDirect = new ChangesBuilder(fetched,updated)
													.replace("/names/0/name")
													.build();
						
						assertEquals(expectedChanges, changes);
						assertEquals(expectedChangesDirect, changes);
                	}
                	
                	//submit edit
                	updatedReturned = ts.updateSubstanceJSON(updated);
                	
                	//refetch editted
                	{
						updateFetched = ts.fetchSubstanceJSON(uuid);
						assertEquals(Integer.parseInt(newVersion), Integer
								.parseInt(updateFetched.at("/version").asText()));
						assertEquals(newName, updateFetched.at("/_name").asText());
	
						Changes changes = JsonUtil.computeChanges(updated, updateFetched);
						Changes expectedChanges = new ChangesBuilder(updated,updateFetched)
						
												.replace("/version")
												.replace("/_name")
												.replace("/lastEdited")
												.replace("/names/0/lastEdited")
												
												.build();
						assertEquals(expectedChanges, changes);
                	}
                	
                	{
                		JsonNode edits = ts.fetchSubstanceHistoryJSON(uuid);
                		int changecount=0;
                		for(JsonNode edit: edits){
                			//Ok, I'm just going to look at the null-path edits
                			//there should be 1
                			JsonNode oldv=ts.urlJSON(edit.get("oldValue").asText());
                			JsonNode newv=ts.urlJSON(edit.get("newValue").asText());
                				
                			Changes changes = JsonUtil.computeChanges(updateFetched, newv);
        					Changes expectedChanges = new ChangesBuilder(updateFetched,newv)
        												.build();
        					assertEquals(expectedChanges, changes);
        						
        					changes = JsonUtil.computeChanges(fetched, oldv);
        					expectedChanges = new ChangesBuilder(fetched,oldv)
        											.build();
        					assertEquals(expectedChanges, changes);
        					changecount++;                			
	                    }
                		assertEquals(1,changecount);
                	}
                	
                	
                } catch (Throwable e1) {
                	e1.printStackTrace();
                    throw new IllegalStateException(e1);
                }
            }
        });
	}
	
    
}
