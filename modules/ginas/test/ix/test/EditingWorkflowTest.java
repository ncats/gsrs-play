package ix.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import util.json.ChangeFilters;
import util.json.Changes;
import util.json.ChangesBuilder;
import util.json.JsonUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author peryeata
 *
 * TODO: 
 * [done] add references (add/remove) check
 * [done] add checks for access control of edits for non-logged in users
 * [done] add names (add/remove) check
 * add codes (add/remove) check
 * add other editor changing something
 * add chemical access (add/remove) check
 * add names reordering check
 * add access reordering check
 * add what would look like a "copy" operation check 
 * refactor
 *
 */
public class EditingWorkflowTest {


    final File resource=new File("test/testJSON/toedit.json");
    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);


    
       
    @Test
   	public void testFailUpdateNoUserProtein() {
   		ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                   	ts.loginFakeUser1();
                   	JsonNode entered= parseJsonFile(resource);
                   	submitSubstance(entered);
                   	ts.logout();
                   	String uuid=entered.get("uuid").asText();              	
                   	
                   	ts.fetchSubstance(uuid);
                   	ts.updateSubstanceFail(entered);

               }
           });
   	}
    
    @Test
   	public void testSubmitProtein() {
   		ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                   	ts.loginFakeUser1();
                   	JsonNode entered= parseJsonFile(resource);
                   	submitSubstance(entered);

               }
           });
   	}
    
    
    @Test
   	public void testChangeProteinLocal() {
   		ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                   	ts.loginFakeUser1();
                   	JsonNode entered= parseJsonFile(resource);
                   	String uuid=entered.get("uuid").asText();              	
                   	submitSubstance(entered);
                   	renameLocal(uuid);

               }
           });
   	}
    
    @Test
   	public void testUnicodeProblem() {
    	final File resource=new File("test/testJSON/racemic-unicode.json");
    	
   		ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                   	ts.loginFakeUser1();
                   	JsonNode entered= parseJsonFile(resource);
                   	String uuid=entered.get("uuid").asText();              	
                   	submitSubstance(entered);
                   	JsonNode fetched=ts.fetchSubstanceJSON(uuid);
                   	String lookingfor="(±)-1-CHLORO-2,3-EPOXYPROPANE";
                   	
                   	Set<String> names= new HashSet<String>();
                   	
                   	for(JsonNode name: fetched.at("/names")){
                   		//System.out.println("The name:" + name.at("/name"));
                   		names.add(name.at("/name").asText());
                   	}
                   assertTrue("Special unicode names should still be found after round trip",names.contains(lookingfor));

               }
           });
   	}
    

    
    
    @Test
   	public void testChangeProteinRemote() {
   		ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                   	ts.loginFakeUser1();
                   	JsonNode entered= parseJsonFile(resource);
                   	String uuid=entered.get("uuid").asText();              	
                   	submitSubstance(entered);
                   	renameLocal(uuid);
                   	JsonNode edited=renameServer(uuid);

               }
           });
   	}
    
    @Test
   	public void testAddNameRemote() {
   		ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                   	ts.loginFakeUser1();
                   	JsonNode entered= parseJsonFile(resource);
                   	String uuid=entered.get("uuid").asText();              	
                   	submitSubstance(entered);
                   	addNameServer(uuid);
                   	//JsonNode edited=testRenameServer(uuid);
               }
           });
   	}
    @Test
   	public void testAddRemoveNameRemote() {
   		ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                   	ts.loginFakeUser1();
                   	JsonNode entered= parseJsonFile(resource);
                   	String uuid=entered.get("uuid").asText();              	
                   	submitSubstance(entered);
                   	addNameServer(uuid);
                   	removeNameServer(uuid);
               }
           });
   	}
    
    @Test
   	public void testChangeHistoryProteinRemote() {
   		ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                	ts.loginFakeUser1();
                   	
                   	JsonNode entered= parseJsonFile(resource);
                   	String uuid=entered.get("uuid").asText();              	
                   	
                   	submitSubstance(entered);
                   	renameLocal(uuid);
                   	JsonNode edited=renameServer(uuid);
   					mostRecentEditHistory(uuid,edited);

               }
           });
   	}
    
    @Test
   	public void testChangeDisuflideProteinRemote() {
   		ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                	ts.loginFakeUser1();
                   	
                   	JsonNode entered= parseJsonFile(resource);
                   	String uuid=entered.get("uuid").asText();              	
                   	
                   	submitSubstance(entered);
                   	renameLocal(uuid);
                   	JsonNode edited=renameServer(uuid);
   					mostRecentEditHistory(uuid,edited);
   					edited=removeLastDisulfide(uuid);

               }
           });
   	}
    
    
    @Test
   	public void testChangeDisuflideProteinHistoryRemote() {
   		ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                	ts.loginFakeUser1();
                   	
                   	JsonNode entered= parseJsonFile(resource);
                   	String uuid=entered.get("uuid").asText();              	
                   	
                   	submitSubstance(entered);
                   	renameLocal(uuid);
                   	JsonNode edited=renameServer(uuid);
   					mostRecentEditHistory(uuid,edited);
   					edited=removeLastDisulfide(uuid);
   					mostRecentEditHistory(uuid,edited);

               }
           });
   	}
    
    //@Ignore("This test will fail, because we can't completely empty an array")
    @Test
   	public void testRemoveAllDisuflidesProtein() {
   		ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                	ts.loginFakeUser1();
                   	
                   	JsonNode entered= parseJsonFile(resource);
                   	String uuid=entered.get("uuid").asText();              	
                   	
                   	submitSubstance(entered);
   					iterativeRemovalOfDisulfides(uuid);
               }
           });
   	}
    
    //@Ignore("This test will fail, because there is a non-trivial mapping from the JSON to the old substance record. The recursive strategy can't discover the right properties")
    @Test
   	public void testAddAccessGroupToExistingProtein() {
   		ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                	ts.loginFakeUser1();
                   	
                   	JsonNode entered= parseJsonFile("test/testJSON/toedit.json");
                   	String uuid=entered.get("uuid").asText();              	
                   	
                   	submitSubstance(entered);
                   	testAddAccessGroupServer(uuid);
               }
           });
   	}
    
    @Test
   	public void testAddReferenceToExistingProtein() {
   		ts.run(new GinasTestServer.ServerWorker() {
               public void doWork() throws Exception {
                	ts.loginFakeUser1();
                   	
                   	JsonNode entered= parseJsonFile("test/testJSON/toedit.json");
                   	String uuid=entered.get("uuid").asText();              	
                   	
                   	submitSubstance(entered);
                   	testAddReferenceNameServer(uuid);
               }
           });
   	}
    
    //This test, however, passes. It also checks for the new access group
    @Test
   	public void testAddAccessGroupToNewProtein() {
   		ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                	ts.loginFakeUser1();
                   	
                   	JsonNode entered= parseJsonFile("test/testJSON/toedit.json");
                   	String uuid=entered.get("uuid").asText();              	
                   	addAccessGroupThenRegister(entered);
               }
           });
   	}
    
   //This test makes sure that double submitting a substance fails
    @Test
   	public void testFailDoubleSubmission() {
   		ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                	ts.loginFakeUser1();
                   	
                   	JsonNode entered= parseJsonFile("test/testJSON/toedit.json");
                   	String uuid=entered.get("uuid").asText();       
                   	submitSubstance(entered);
                   	ts.submitSubstanceFail(entered);

               }
           });
   	}
    
    //This test makes sure that looking up a substance before registering it fails
    @Test
   	public void testFailInvalidLookup() {
   		ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                	ts.loginFakeUser1();
                   	
                   	JsonNode entered= parseJsonFile("test/testJSON/toedit.json");
                   	String uuid=entered.get("uuid").asText();       
                   	ts.fetchSubstanceFail(uuid);

               }
           });
   	}
    
   //This test makes sure that updating a substance before registering it fails
    //@Ignore("I believe this fails now, because the PUT method allows for non-existent substances. It shouldn't")
    @Test
   	public void testFailUpdateNewSubstance() {
   		ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                	ts.loginFakeUser1();
                   	
                   	JsonNode entered= parseJsonFile("test/testJSON/toedit.json");
                   	String uuid=entered.get("uuid").asText();
                   	ts.updateSubstanceFail(entered);
                   	ts.fetchSubstanceFail(uuid);

               }
           });
   	}
    
    
    
    
    
    @Test
   	public void testHistoryViews() {
   		ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                	ts.loginFakeUser1();
                   	
                   	JsonNode entered= parseJsonFile("test/testJSON/toedit.json");
                   	String uuid=entered.get("uuid").asText();              	
                   	
                   	submitSubstance(entered);
                   	JsonNode edited=renameServer(uuid);
   					mostRecentEditHistory(uuid,edited);
   					retrieveHistoryView(uuid,1);

               }
           });
   	}

    @Test
    public void revertChangeShouldMakeNewVersionWithOldValues() {
        ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                ts.loginFakeUser1();

                JsonNode entered= parseJsonFile(resource);
                String uuid=entered.get("uuid").asText();

                submitSubstance(entered);
                String oldName = "TRANSFERRIN ALDIFITOX S EPIMER";
                String newName = "foo";
                JsonNode oldNode= renameServer(uuid,newName );

                mostRecentEditHistory(uuid,oldNode);

                assertEquals(newName, ts.fetchSubstanceJSON(uuid).at("/names/0/name").asText());

                renameServer(uuid,oldName );
                 assertEquals(oldName, ts.fetchSubstanceJSON(uuid).at("/names/0/name").asText());

                JsonNode originalNode = ts.fetchSubstanceJSON(uuid,1).getOldValue();

                JsonNode v2Node = ts.fetchSubstanceJSON(uuid,2).getOldValue();

                assertEquals("v1 name", oldName, originalNode.at("/names/0/name").asText());
                assertEquals("v2 name", newName, v2Node.at("/names/0/name").asText());

                JsonNode currentNode = ts.fetchSubstanceJSON(uuid);
                assertEquals("current name", oldName, currentNode.at("/names/0/name").asText());


            }
        });
    }

    @Test
    public void historyNewValueShouldBeOldValueOfNextVersion() {
        ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
                ts.loginFakeUser1();

                JsonNode entered= parseJsonFile(resource);
                String uuid=entered.get("uuid").asText();

                submitSubstance(entered);
                String oldName = "TRANSFERRIN ALDIFITOX S EPIMER";
                String newName = "foo";
                JsonNode oldNode= renameServer(uuid,newName );


                GinasTestServer.JsonHistoryResult jsonHistoryResult = ts.fetchSubstanceJSON(uuid, 1);
                JsonNode originalNode = jsonHistoryResult.getNewValue();

                assertEquals("v1 new value", jsonHistoryResult.getNewValue(), ts.fetchSubstanceJSON(uuid));
                renameServer(uuid,oldName );


                JsonNode v2Node = ts.fetchSubstanceJSON(uuid,2).getOldValue();

                assertEquals("v2", originalNode, v2Node);


            }
        });
    }

    private static String unquote(String s){
        return StringEscapeUtils.unescapeCsv(s);
    }
    
    public void retrieveHistoryView(String uuid, int version){
    	String newHTML=ts.fetchSubstanceUI(uuid);
    	String oldHTML=ts.fetchSubstanceVersionUI(uuid,version);

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
    	
//    	System.out.println("Lines in old version view, but not new:" + inOldButNotNew.size());
//    	for(String oline:inOldButNotNew){
//    		//System.out.println(oline);
//    	}
//
//    	System.out.println("Lines in new version view, but not old:" + inNewButNotOld.size());
//    	for(String oline:inNewButNotOld){
//    		//System.out.println(oline);
//    	}
    	int distance=StringUtils.getLevenshteinDistance(oldHTML, newHTML);

    	assertTrue("Levenshtein Distance (" + distance + ") of page HTML should be greater than (0)",distance>0);
    	assertTrue("New lines (" + inNewButNotOld.size() + ") of page HTML should be greater than (0)",inNewButNotOld.size()>0);
    	assertTrue("Removed lines (" + inOldButNotNew.size() + ") of page HTML should be greater than (0)",inOldButNotNew.size()>0);
    	
    }
    private void renameLocal(String uuid, String oldName, String newName){
        JsonNode fetched = ts.fetchSubstanceJSON(uuid);
        JsonNode updated=new JsonUtil.JsonNodeBuilder(fetched).set("/names/0/name", newName).build();

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
    private void renameLocal(String uuid){

        renameLocal(uuid,"TRANSFERRIN ALDIFITOX S EPIMER", "TRANSFERRIN ALDIFITOX S EPIMER CHANGED");
    

    }
    
    public void mostRecentEditHistory(String uuid, JsonNode oldRecordExpected){
    		JsonNode newRecordFetched = ts.fetchSubstanceJSON(uuid);
    		int oversion=Integer.parseInt(newRecordFetched.at("/version").textValue());
    		JsonNode edits = ts.fetchSubstanceHistoryJSON(uuid,oversion-1);

        int changecount=0;
    		for(JsonNode edit: edits){
    			//Ok, I'm just going to look at the null-path edits
    			//there should be 1
    			JsonNode oldv=ts.urlJSON(edit.get("oldValue").asText());
    			JsonNode newv=ts.urlJSON(edit.get("newValue").asText());
    				
    			Changes changes = JsonUtil.computeChanges(newRecordFetched, newv);
				Changes expectedChanges = new ChangesBuilder(newRecordFetched,newv)
											.build();
				assertEquals(expectedChanges, changes);
					
				changes = JsonUtil.computeChanges(oldRecordExpected, oldv);
				expectedChanges = new ChangesBuilder(oldRecordExpected,oldv)
										.build();
				assertEquals(expectedChanges, changes);
				changecount++;  
				break;
            }
    		assertEquals(1,changecount);
    		
    }
    public JsonNode renameServer(String uuid){
        return renameServer(uuid, "TRANSFERRIN ALDIFITOX S EPIMER CHANGED");
    }
    public JsonNode renameServer(String uuid, String newName){
    	
    	JsonNode fetched = ts.fetchSubstanceJSON(uuid);

    	String oldVersion=fetched.at("/version").asText();
    
    	JsonNode updated=new JsonUtil.JsonNodeBuilder(fetched).set("/names/0/name", newName).build();
		ts.updateSubstanceJSON(updated);
		JsonNode updateFetched = ts.fetchSubstanceJSON(uuid);
		
		assertEquals(Integer.parseInt(oldVersion) + 1, Integer.parseInt(updateFetched.at("/version").asText()));
		assertEquals(newName, updateFetched.at("/_name").asText());
		Changes changes = JsonUtil.computeChanges(updated, updateFetched);
		Changes expectedChanges = new ChangesBuilder(updated,updateFetched)
		
								.replace("/version")
								.replace("/_name")
								.replace("/lastEdited")
								.replace("/names/0/lastEdited")
								
								.build();
		
		assertEquals(expectedChanges, changes);
		return fetched;
    }
    
    public JsonNode addNameServer(String uuid){
    	
    	JsonNode fetched = ts.fetchSubstanceJSON(uuid);

    	String oldVersion=fetched.at("/version").asText();
    	String newName="TRANSFERRIN ALDIFITOX S EPIMER CHANGED";
    
    	JsonNode updated=new JsonUtil.JsonNodeBuilder(fetched)
    			.copy("/names/-","/names/0")
    			.set("/names/1/name", newName)
    			.remove("/names/1/uuid")
    			.remove("/names/1/displayName")
    			.build();
    	
		ts.updateSubstanceJSON(updated);
		JsonNode updateFetched = ts.fetchSubstanceJSON(uuid);
		
		assertEquals(Integer.parseInt(oldVersion) + 1, Integer.parseInt(updateFetched.at("/version").asText()));
		Changes changes = JsonUtil.computeChanges(updated, updateFetched);
		Changes expectedChanges = new ChangesBuilder(updated,updateFetched)
		
								.replace("/version")
								.replace("/lastEdited")
								.replace("/names/1/lastEdited")
								.replace("/names/1/_self")
								.added("/names/1/uuid")
								
								.build();
		
		assertEquals(expectedChanges, changes);
		
		return fetched;
    }
    public JsonNode removeNameServer(String uuid){
    	
		JsonNode updateFetched = ts.fetchSubstanceJSON(uuid);
		ts.updateSubstanceJSON(new JsonUtil.JsonNodeBuilder(updateFetched)
				.remove("/names/1")
				.build()
				);
		JsonNode afterRemove=ts.fetchSubstanceJSON(uuid);
		
		assertTrue("After removing name, should have (1) name, found (" + afterRemove.at("/names").size() + ")",
				afterRemove.at("/names").size() == 1);
		
		return updateFetched;
    }
    
    public JsonNode testAddAccessGroupServer(String uuid){
    	
    	JsonNode fetched = ts.fetchSubstanceJSON(uuid);
    
    	JsonNode updated=new JsonUtil.JsonNodeBuilder(fetched).add("/access/-", "testGROUP").build();
		ts.updateSubstanceJSON(updated);
		JsonNode updateFetched = ts.fetchSubstanceJSON(uuid);
		JsonNode accessArray=updateFetched.at("/access");
		assertTrue("Fetched access group exists",accessArray!=null);
		assertTrue("Fetched access group is not JSON-null",!accessArray.isNull());
		assertTrue("Fetched access group is not Empty, afer being added",accessArray.size()>0);
		assertEquals("testGROUP",updateFetched.at("/access/0").textValue());
		
		
		//System.out.println("This is the group now" + updateFetched.at("/access/0").textValue());
		Changes changes = JsonUtil.computeChanges(updated, updateFetched);
		Changes expectedChanges = new ChangesBuilder(updated,updateFetched)
								.replace("/version")
								.replace("/lastEdited")
								
								.build();
		
		assertEquals(expectedChanges, changes);
		return fetched;
    }
    
    public JsonNode testAddReferenceNameServer(String uuid){
    	
    	JsonNode fetched = ts.fetchSubstanceJSON(uuid);
    
    	String ref=fetched.at("/references/5/uuid").asText();
    	JsonNode updated=new JsonUtil.JsonNodeBuilder(fetched).add("/names/0/references/-", ref).build();
		ts.updateSubstanceJSON(updated);
		JsonNode updateFetched = ts.fetchSubstanceJSON(uuid);
		
		
		Changes changes = JsonUtil.computeChanges(updated, updateFetched);
		Changes expectedChanges = new ChangesBuilder(updated,updateFetched)
				
								.replace("/version")
								.replace("/lastEdited")
								.replace("/names/0/lastEdited")
								
								.build();
		
		assertEquals(expectedChanges, changes);
		return fetched;
    }
    
    public JsonNode addAccessGroupThenRegister(JsonNode fetched){
    	
    	JsonNode updated=new JsonUtil.JsonNodeBuilder(fetched).add("/access/-", "testGROUP").build();
		//ts.updateSubstanceJSON(updated);
		JsonNode updateFetched = ts.submitSubstanceJSON(updated);
		assertEquals("testGROUP",updateFetched.at("/access/0").textValue());
		
		return updateFetched;
    }


	public JsonNode parseJsonFile(String path){
		return parseJsonFile(new File(path));
	}
    public JsonNode parseJsonFile(File resource){
    	try(InputStream is=new FileInputStream(resource)){
        	return new ObjectMapper().readTree(is);
    	}catch(Throwable t){
    		throw new RuntimeException(t);
    	}
    }
    public void testLogin(){
    	ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1);
    }
    
    public void submitSubstance(JsonNode jsn){
    	ts.submitSubstanceJSON(jsn);
    }
    
    
	public void iterativeRemovalOfDisulfides(String uuid){
		while (removeLastDisulfide(uuid)!=null);
	}
	
	
	/*
	 * This works now.
	 * 
	 */
	public JsonNode removeLastDisulfide(String uuid){
		JsonNode updatedReturned = ts.fetchSubstanceJSON(uuid);
		JsonNode disulfs=updatedReturned.at("/protein/disulfideLinks");
		//System.out.println("Site shorthand is:" + updatedReturnedb.at("/protein/disulfideLinks/1/sitesShorthand"));
		
		if(disulfs.isNull() || disulfs.size()==0){
			return null;
		}
		
		int i=disulfs.size()-1;
		JsonNode updated2=new JsonUtil.JsonNodeBuilder(updatedReturned).remove("/protein/disulfideLinks/" + i).build();
		
		Changes changes = JsonUtil.computeChanges(updatedReturned, updated2);
		Changes expectedChanges = new ChangesBuilder(updatedReturned, updated2).removed("/protein/disulfideLinks/" + i)
									.build();
		
		assertEquals(expectedChanges, changes);

		JsonNode updatedReturnedb;
		//submit edit
		updatedReturnedb = ts.updateSubstanceJSON(updated2);
		updatedReturnedb = ts.fetchSubstanceJSON(uuid);
    	Changes changes2 = JsonUtil.computeChanges(updated2, updatedReturnedb);
    	
    	
    	ChangesBuilder changeBuilder=new ChangesBuilder(updated2, updatedReturnedb)
		.replace("/version")
		.replace("/lastEdited")
    	.replace("/protein/lastEdited");
    	
    	assertEquals(
    			changeBuilder.build()
				
				, changes2);
    	return updatedReturned;
	}
}
