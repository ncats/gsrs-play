package ix.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ix.core.models.Role;
import ix.ginas.models.v1.NameOrg;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;

import util.json.Changes;
import util.json.ChangesBuilder;
import util.json.JsonUtil;

import com.fasterxml.jackson.databind.JsonNode;

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
   	public void testFailUpdateNoUserProtein() throws Exception {
        GinasTestServer.UserSession session = ts.loginFakeUser1();
        JsonNode entered= parseJsonFile(resource);
        session.submitSubstanceJSON(entered);

        session.logout();

        String uuid=entered.get("uuid").asText();

        ts.getNotLoggedInSession().fetchSubstance(uuid);
        session.updateSubstanceFail(entered);
   	}
   
    
    @Test
   	public void testSubmitProtein() throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {
            JsonNode entered = parseJsonFile(resource);
            session.submitSubstance(entered);
        }
   	}
    
    
    @Test
   	public void testChangeProteinLocal() throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {
            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();
            session.submitSubstance(entered);
            renameLocal(session, uuid);
        }
   	}
    
    @Test
   	public void testUnicodeProblem() throws Exception {
        final File resource=new File("test/testJSON/racemic-unicode.json");
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {
            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();
            session.submitSubstance(entered);
            JsonNode fetched = session.fetchSubstanceJSON(uuid);
            String lookingfor = "(±)-1-CHLORO-2,3-EPOXYPROPANE";

            Set<String> names = new HashSet<String>();

            for (JsonNode name : fetched.at("/names")) {
                //System.out.println("The name:" + name.at("/name"));
                names.add(name.at("/name").asText());
            }
            assertTrue("Special unicode names should still be found after round trip", names.contains(lookingfor));
        }
   	}
    

    
    
    @Test
   	public void testChangeProteinRemote() throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {
            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();
            session.submitSubstance(entered);
            renameLocal(session, uuid);
            JsonNode edited = renameServer(session, uuid);
        }
   	}
    @Test
   	public void testAddNameOrgProtein() throws Exception {
    	try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {
    		
           	JsonNode entered= parseJsonFile(resource);
           	String uuid=entered.get("uuid").asText();              	
            session.submitSubstance(entered);
           	//renameLocal(uuid);
           	addNameOrgServer(session,uuid,  "INN");
        }
   	}
    
    @Test
   	public void testAddUsers() throws Exception {
    	GinasTestServer.UserSession session1 = ts.createNewUserAndLogin(Role.Admin);
    	GinasTestServer.UserSession session2 = ts.createNewUserAndLogin(Role.SuperUpdate);
    			
    	//System.out.println("User 1 is:" + session1.whoamiUsername());
    	assertEquals(session1.whoamiUsername(), session1.getUserName());
    	assertTrue(session1.whoamiJSON().at("/roles").toString().contains(Role.Admin.toString()));
    	//System.out.println("User 2 is:" + session2.whoamiUsername());
    	assertEquals(session2.whoamiUsername(), session2.getUserName());
    	assertTrue(session2.whoamiJSON().at("/roles").toString().contains(Role.SuperUpdate.toString()));
    	
    	
    	session1.close();
    	session2.close();
    	
   	}
    
    
    @Test
   	public void testSubmitPublicRemote() throws Exception {
                   	try(GinasTestServer.UserSession session = ts.loginFakeUser3();
                        GinasTestServer.UserSession session2 = ts.loginFakeUser2()) {
                        JsonNode entered = parseJsonFile(resource);

                        String uuid = entered.get("uuid").asText();

                        session.submitSubstanceFail(entered);

                        JsonNode updated = new JsonUtil
                                .JsonNodeBuilder(entered)
                                .add("/access/-", "testGROUP")
                                .build();
                        updated = session.submitSubstanceJSON(updated);

                        updated = new JsonUtil
                                .JsonNodeBuilder(updated)
                                .remove("/access/0")
                                .build();
                        session.updateSubstanceFail(updated);
                        session2.updateSubstanceJSON(updated);
                    }
   	}
    //Can't submit an preapproved substance via this mechanism
    //Also, can't change an approvalID here, unless an admin
    //TODO: add the admin part
    @Test
   	public void testSubmitPreApprovedRemote() throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1();
            GinasTestServer.UserSession session2 = ts.loginFakeUser2()) {
            JsonNode entered = JsonUtil.parseJsonFile(resource);
            session.submitSubstanceFail(entered);
            entered = SubstanceJsonUtil.toUnapproved(entered);
            entered = session.submitSubstanceJSON(entered);

            entered = session2.approveSubstanceJSON(entered.at("/uuid").asText());
            entered = new JsonUtil
                    .JsonNodeBuilder(entered)
                    .remove("/approvalID")
                    .build();
            session2.updateSubstanceFail(entered);

        }
   	}

    @Test
   	public void testAddNameRemote() throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {
            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();
            session.submitSubstance(entered);
            addNameServer(session, uuid);
        }
   	}
    @Test
   	public void testAddRemoveNameRemote()  throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {
            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();
            session.submitSubstance(entered);
            addNameServer(session, uuid);
            removeNameServer(session, uuid);
        }
   	}
    
    @Test
   	public void testChangeHistoryProteinRemote() throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {

            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();

            session.submitSubstance(entered);
            renameLocal(session, uuid);
            JsonNode edited = renameServer(session, uuid);
            mostRecentEditHistory(session, uuid, edited);
        }
   	}
    
    @Test
   	public void testFacetUpdateRemote()  throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()){

                   	JsonNode entered= parseJsonFile(resource);
                   	String uuid=entered.get("uuid").asText();
                   	session.submitSubstance(entered);
                   	int oldProteinCount=getFacetCountFor(session, "Substance Class","protein");
                   	assertEquals(1,oldProteinCount);
                   	renameServer(session, uuid);
                   	int newProteinCount=getFacetCountFor(session, "Substance Class","protein");
                   	assertEquals(1,newProteinCount);

               }
   	}

    public int getFacetCountFor(GinasTestServer.UserSession session, String face, String label){
    	JsonNode jsn=session.fetchSubstancesSearchJSON();
       	JsonNode facets=jsn.at("/facets");
       	for(JsonNode facet:facets){
       		String name=facet.at("/name").asText();
       		if("Substance Class".equals(name)){
       			for(JsonNode val:facet.at("/values")){
       				if("protein".equals(val.at("/label").asText())){
       					return val.at("/count").asInt();
       				}
       			}
       		}
       	}
       	return 0;
    }

    @Test
   	public void testChangeDisuflideProteinRemote() throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {

            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();

            session.submitSubstance(entered);
            renameLocal(session, uuid);
            JsonNode edited = renameServer(session, uuid);
            mostRecentEditHistory(session, uuid, edited);
            edited = removeLastDisulfide(session, uuid);
        }
   	}
    
    
    @Test
   	public void testChangeDisuflideProteinHistoryRemote() throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {

            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();

            session.submitSubstance(entered);
            renameLocal(session, uuid);
            JsonNode edited = renameServer(session, uuid);
            mostRecentEditHistory(session, uuid, edited);
            edited = removeLastDisulfide(session, uuid);
            mostRecentEditHistory(session, uuid, edited);
        }
   	}
    
    //@Ignore("This test will fail, because we can't completely empty an array")
    @Test
   	public void testRemoveAllDisuflidesProtein() throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {

            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();

            session.submitSubstance(entered);
            iterativeRemovalOfDisulfides(session, uuid);
        }
   	}
    
    //@Ignore("This test will fail, because there is a non-trivial mapping from the JSON to the old substance record. The recursive strategy can't discover the right properties")
    @Test
   	public void testAddAccessGroupToExistingProtein() throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {

            JsonNode entered = parseJsonFile("test/testJSON/toedit.json");
            String uuid = entered.get("uuid").asText();

            session.submitSubstance(entered);
            testAddAccessGroupServer(session, uuid);
        }
   	}
    
    @Test
   	public void testAddReferenceToExistingProtein() throws Exception {
           try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {

               JsonNode entered = parseJsonFile("test/testJSON/toedit.json");
               String uuid = entered.get("uuid").asText();

               session.submitSubstance(entered);
               testAddReferenceNameServer(session, uuid);
           }
   	}
    
    //This test, however, passes. It also checks for the new access group
    @Test
   	public void testAddAccessGroupToNewProtein() throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {
            JsonNode entered = parseJsonFile("test/testJSON/toedit.json");
            String uuid = entered.get("uuid").asText();
            addAccessGroupThenRegister(session, entered);
        }

   	}
    
   //This test makes sure that double submitting a substance fails
    @Test
   	public void testFailDoubleSubmission() throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {
            JsonNode entered = parseJsonFile("test/testJSON/toedit.json");
            String uuid = entered.get("uuid").asText();

            session.submitSubstance(entered);
            session.submitSubstanceFail(entered);
        }
   	}
    
    //This test makes sure that looking up a substance before registering it fails
    @Test
   	public void testFailInvalidLookup() throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {
            JsonNode entered = parseJsonFile("test/testJSON/toedit.json");
            String uuid = entered.get("uuid").asText();
            session.fetchSubstanceFail(uuid);
        }

   	}
    
   //This test makes sure that updating a substance before registering it fails
    //@Ignore("I believe this fails now, because the PUT method allows for non-existent substances. It shouldn't")
    @Test
   	public void testFailUpdateNewSubstance() throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {
            JsonNode entered = parseJsonFile("test/testJSON/toedit.json");
            String uuid = entered.get("uuid").asText();
            session.updateSubstanceFail(entered);
            session.fetchSubstanceFail(uuid);
        }

   	}
    
    
    
    
    
    @Test
   	public void testHistoryViews() throws Exception{


        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {
            JsonNode entered = parseJsonFile("test/testJSON/toedit.json");
            String uuid = entered.get("uuid").asText();

            session.submitSubstance(entered);
            JsonNode edited = renameServer(session, uuid);
            mostRecentEditHistory(session, uuid, edited);
            retrieveHistoryView(session, uuid, 1);
        }
   	}

    @Test
    public void revertChangeShouldMakeNewVersionWithOldValues() throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {
            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();

            session.submitSubstance(entered);
            String oldName = "TRANSFERRIN ALDIFITOX S EPIMER";
            String newName = "foo";
            JsonNode oldNode = renameServer(session, uuid, newName);

            mostRecentEditHistory(session, uuid, oldNode);

            assertEquals(newName, session.fetchSubstanceJSON(uuid).at("/names/0/name").asText());

            renameServer(session, uuid, oldName);
            assertEquals(oldName, session.fetchSubstanceJSON(uuid).at("/names/0/name").asText());

            JsonNode originalNode = session.fetchSubstanceJSON(uuid, 1).getOldValue();

            JsonNode v2Node = session.fetchSubstanceJSON(uuid, 2).getOldValue();

            assertEquals("v1 name", oldName, originalNode.at("/names/0/name").asText());
            assertEquals("v2 name", newName, v2Node.at("/names/0/name").asText());

            JsonNode currentNode = session.fetchSubstanceJSON(uuid);
            assertEquals("current name", oldName, currentNode.at("/names/0/name").asText());

        }
    }

    @Test
    public void historyNewValueShouldBeOldValueOfNextVersion() throws Exception {
        try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {
            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();

            session.submitSubstance(entered);
            String oldName = "TRANSFERRIN ALDIFITOX S EPIMER";
            String newName = "foo";
            JsonNode oldNode = renameServer(session, uuid, newName);


            GinasTestServer.JsonHistoryResult jsonHistoryResult = session.fetchSubstanceJSON(uuid, 1);
            JsonNode originalNode = jsonHistoryResult.getNewValue();

            assertEquals("v1 new value", jsonHistoryResult.getNewValue(), session.fetchSubstanceJSON(uuid));
            renameServer(session, uuid, oldName);


            JsonNode v2Node = session.fetchSubstanceJSON(uuid, 2).getOldValue();

            assertEquals("v2", originalNode, v2Node);
        }


    }

    public void retrieveHistoryView(GinasTestServer.UserSession session, String uuid, int version){
    	String newHTML=session.fetchSubstanceUI(uuid);
    	String oldHTML=session.fetchSubstanceVersionUI(uuid,version);

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
    private void renameLocal(GinasTestServer.UserSession session, String uuid, String oldName, String newName){
        JsonNode fetched = session.fetchSubstanceJSON(uuid);
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
    private void renameLocal(GinasTestServer.UserSession session, String uuid){

        renameLocal(session, uuid,"TRANSFERRIN ALDIFITOX S EPIMER", "TRANSFERRIN ALDIFITOX S EPIMER CHANGED");
    

    }
    
    public void mostRecentEditHistory(GinasTestServer.UserSession session, String uuid, JsonNode oldRecordExpected){
    		JsonNode newRecordFetched = session.fetchSubstanceJSON(uuid);
    		int oversion=Integer.parseInt(newRecordFetched.at("/version").textValue());
    		JsonNode edits = session.fetchSubstanceHistoryJSON(uuid,oversion-1);

        int changecount=0;
    		for(JsonNode edit: edits){
    			//Ok, I'm just going to look at the null-path edits
    			//there should be 1
    			JsonNode oldv=session.urlJSON(edit.get("oldValue").asText());
    			JsonNode newv=session.urlJSON(edit.get("newValue").asText());
    				
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
    public JsonNode renameServer(GinasTestServer.UserSession session, String uuid){
        return renameServer(session, uuid, "TRANSFERRIN ALDIFITOX S EPIMER CHANGED");
    }
    public JsonNode renameServer(GinasTestServer.UserSession session, String uuid, String newName){
    	
    	JsonNode fetched = session.fetchSubstanceJSON(uuid);

    	String oldVersion=fetched.at("/version").asText();
    
    	JsonNode updated=new JsonUtil.JsonNodeBuilder(fetched).set("/names/0/name", newName).build();
		session.updateSubstanceJSON(updated);
		JsonNode updateFetched = session.fetchSubstanceJSON(uuid);
		
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
    
    public JsonNode addNameOrgServer(GinasTestServer.UserSession session, String uuid, String nameorg){
    	
    	JsonNode fetched = session.fetchSubstanceJSON(uuid);

    	
    	NameOrg nameOrg = new NameOrg();
    	nameOrg.nameOrg=nameorg;
    	
    	JsonNode updated= new JsonUtil.JsonNodeBuilder(fetched)
    							.add("/names/0/nameOrgs/-", nameOrg)
    							.build();
    	
    	
    	//System.out.println("FIrst name org is:" + updated.at("/names/0/nameOrgs/0"));
    	session.updateSubstanceJSON(updated);
		
		JsonNode updateFetched = session.fetchSubstanceJSON(uuid);
		assertEquals(
					  updated.at("/names/0/nameOrgs/0/nameOrg").toString(),
				updateFetched.at("/names/0/nameOrgs/0/nameOrg").toString()
				);
		//System.out.println("Now, it's:" + updateFetched.at("/names/0/nameOrgs/0"));
		
		
		return fetched;
    }
    
    public JsonNode addNameServer(GinasTestServer.UserSession session, String uuid){

    	
    	JsonNode fetched = session.fetchSubstanceJSON(uuid);

    	String oldVersion=fetched.at("/version").asText();
    	String newName="TRANSFERRIN ALDIFITOX S EPIMER CHANGED";
    
    	JsonNode updated=new JsonUtil.JsonNodeBuilder(fetched)
    			.copy("/names/-","/names/0")
    			.set("/names/1/name", newName)
    			.remove("/names/1/uuid")
    			.remove("/names/1/displayName")
    			.build();

        session.updateSubstanceJSON(updated);
		JsonNode updateFetched = session.fetchSubstanceJSON(uuid);
		
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
    public JsonNode removeNameServer(GinasTestServer.UserSession session, String uuid){
    	
		JsonNode updateFetched = session.fetchSubstanceJSON(uuid);
		session.updateSubstanceJSON(new JsonUtil.JsonNodeBuilder(updateFetched)
				.remove("/names/1")
				.build()
				);
		JsonNode afterRemove=session.fetchSubstanceJSON(uuid);
		
		assertTrue("After removing name, should have (1) name, found (" + afterRemove.at("/names").size() + ")",
				afterRemove.at("/names").size() == 1);
		
		return updateFetched;
    }
    
    public JsonNode testAddAccessGroupServer(GinasTestServer.UserSession session,String uuid){
    	
    	JsonNode fetched = session.fetchSubstanceJSON(uuid);
    
    	JsonNode updated=new JsonUtil.JsonNodeBuilder(fetched).add("/access/-", "testGROUP").build();
		session.updateSubstanceJSON(updated);
		JsonNode updateFetched = session.fetchSubstanceJSON(uuid);
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
    
    public JsonNode testAddReferenceNameServer(GinasTestServer.UserSession session, String uuid){
    	
    	JsonNode fetched = session.fetchSubstanceJSON(uuid);
    
    	String ref=fetched.at("/references/5/uuid").asText();
    	JsonNode updated=new JsonUtil.JsonNodeBuilder(fetched).add("/names/0/references/-", ref).build();
		session.updateSubstanceJSON(updated);
		JsonNode updateFetched = session.fetchSubstanceJSON(uuid);
		
		
		Changes changes = JsonUtil.computeChanges(updated, updateFetched);
		Changes expectedChanges = new ChangesBuilder(updated,updateFetched)
				
								.replace("/version")
								.replace("/lastEdited")
								.replace("/names/0/lastEdited")
								
								.build();
		
		assertEquals(expectedChanges, changes);
		return fetched;
    }
    
    public JsonNode addAccessGroupThenRegister(GinasTestServer.UserSession session, JsonNode fetched){
    	
    	JsonNode updated=new JsonUtil.JsonNodeBuilder(fetched).add("/access/-", "testGROUP").build();
		//ts.updateSubstanceJSON(updated);
		JsonNode updateFetched = session.submitSubstanceJSON(updated);
		assertEquals("testGROUP",updateFetched.at("/access/0").textValue());
		
		return updateFetched;
    }


	public JsonNode parseJsonFile(String path){
		return parseJsonFile(new File(path));
	}
    public JsonNode parseJsonFile(File resource){
    	return SubstanceJsonUtil.toUnapproved(JsonUtil.parseJsonFile(resource));
    }


    

	public void iterativeRemovalOfDisulfides(GinasTestServer.UserSession session, String uuid){
		while (removeLastDisulfide(session, uuid)!=null);
	}
	
	
	/*
	 * This works now.
	 * 
	 */
	public JsonNode removeLastDisulfide(GinasTestServer.UserSession session, String uuid){
		JsonNode updatedReturned = session.fetchSubstanceJSON(uuid);
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
		updatedReturnedb = session.updateSubstanceJSON(updated2);
		updatedReturnedb = session.fetchSubstanceJSON(uuid);
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
