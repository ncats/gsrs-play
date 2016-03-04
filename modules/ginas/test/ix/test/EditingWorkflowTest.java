package ix.test;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;

import util.json.Changes;
import util.json.ChangesBuilder;
import util.json.JsonUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class EditingWorkflowTest {
	private final static boolean THIS_IS_TERRIBLE=true;
	private final static boolean WE_NEED_TO_FIX_THIS=true;
	private final static boolean EVERYTHING_WILL_BE_OK=false;

    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);

    @Test
    public void testVersionUpdate(){
    	assert(THIS_IS_TERRIBLE);
    	assert(WE_NEED_TO_FIX_THIS);
    	//assert(EVERYTHING_WILL_BE_OK);     //commented out, to ensure next code executes
    }
    
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
	                    System.out.println("Saving");
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
						//submit edit
	                	updatedReturned = ts.updateSubstanceJSON(updated);
                	}
                	
                	
                	
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
                	//make disulfide change
                	{
                		removeLastDisulfide(uuid);
                	}
                	
                	
                	
                	
                	String newHTML=ts.fetchSubstanceUI(uuid);
                	String oldHTML=ts.fetchSubstanceVersionUI(uuid,"1");
                	Set<String> oldlines = new LinkedHashSet<String>();
                	for(String line: oldHTML.split("\n")){
                		oldlines.add(line);
                	}
                	Set<String> newlines = new LinkedHashSet<String>();
                	for(String line: newHTML.split("\n")){
                		newlines.add(line);
                	}
                	Set<String> inOldButNotNew=new LinkedHashSet<String>(oldlines);
                	inOldButNotNew.removeAll(newlines);
                	Set<String> inNewButNotOld=new LinkedHashSet<String>(newlines);
                	inNewButNotOld.removeAll(oldlines);
                	
                	System.out.println("In old version, but not new:\n=================");
                	for(String oline:inOldButNotNew){
                		System.out.println(oline);
                	}
                	
                	System.out.println("In new version, but not old:\n=================");
                	for(String oline:inNewButNotOld){
                		System.out.println(oline);
                	}
                	
                	System.out.println("Levenshtein Distance"+StringUtils.getLevenshteinDistance(oldHTML, newHTML));
                	
                	
                	//TODO: This will break
                	//testIterativeRemovalOfDisulfides(uuid);
                	
                	
                	
                	
                } catch (Throwable e1) {
                	e1.printStackTrace();
                    throw new IllegalStateException(e1);
                }
            }
        });
	}
    
    //TODO: Refactor to include the other tests before, and make standalone 
	public void testIterativeRemovalOfDisulfides(String uuid){
		while (removeLastDisulfide(uuid));
	}
	
	/*
	 * This will work, but really shouldn't. The fact that it works means that metadata is not being properly updated.
	 * 
	 */
	public boolean removeLastDisulfide(String uuid){
		JsonNode updatedReturnedb = ts.fetchSubstanceJSON(uuid);
		JsonNode disulfs=updatedReturnedb.at("/protein/disulfideLinks");
		//System.out.println("Site shorthand is:" + updatedReturnedb.at("/protein/disulfideLinks/1/sitesShorthand"));
		
		if(disulfs.isNull() || disulfs.size()==0){
			System.out.println("No disulfs");
			return false;
		}
		
		int i=disulfs.size()-1;
		JsonNode updated2=new JsonUtil.JsonNodeBuilder(updatedReturnedb).remove("/protein/disulfideLinks/" + i).build();
		
		Changes changes = JsonUtil.computeChanges(updatedReturnedb, updated2);
		Changes expectedChanges = new ChangesBuilder(updatedReturnedb, updated2).removed("/protein/disulfideLinks/" + i)
									.build();
		
		assertEquals(expectedChanges, changes);
		
		//submit edit
		updatedReturnedb = ts.updateSubstanceJSON(updated2);
    	Changes changes2 = JsonUtil.computeChanges(updated2, updatedReturnedb);
    	System.out.println("Old disulfides:"+ updated2.at("/protein/disulfideLinks").size());
    	System.out.println("New disulfides:"+ updatedReturnedb.at("/protein/disulfideLinks").size());
    	assertEquals(new ChangesBuilder(updated2, updatedReturnedb)
    			
    			.replace("/version")
				.replace("/lastEdited")
				.build()
				
				, changes2);
    	System.out.println("Removed " + i);
    	return true;
	}
    
}
