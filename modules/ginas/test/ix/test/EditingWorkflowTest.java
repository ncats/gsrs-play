package ix.test;
import static ix.test.SubstanceJsonUtil.ensureFailure;
import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ix.core.models.Role;
import ix.ginas.models.v1.NameOrg;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.JsonHistoryResult;
import ix.test.ix.test.server.RestSession;
import ix.test.ix.test.server.SubstanceAPI;
import ix.test.util.TestNamePrinter;
import util.json.ChangeFilters;
import util.json.Changes;
import util.json.ChangesBuilder;
import util.json.JsonUtil;

/**
 * 
 * @author peryeata
 *
 * TODO: 
 * [done] add references (add/remove) check
 * [done] add checks for access control of edits for non-logged in users
 * [done] add names (add/remove) check
 * [done] add names reordering check
 * [done] add other editor changing something
 * add codes (add/remove) check
 * add chemical access (add/remove) check
 * add access reordering check
 * add what would look like a "copy" operation check 
 * [mostly done] refactor
 *
 */
public class EditingWorkflowTest {


    final File resource=new File("test/testJSON/toedit.json");
    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);

    @Rule
    public TestNamePrinter testNamePrinter = new TestNamePrinter();


    private GinasTestServer.User fakeUser1, fakeUser2;

    @Before
    public void getUsers(){
        fakeUser1 = ts.getFakeUser1();
        fakeUser2 = ts.getFakeUser2();
    }



    @Test
    public void testFailUpdateNoUserProtein() throws Exception {
        JsonNode entered = parseJsonFile(resource);
        RestSession session = ts.newRestSession(fakeUser1);


        SubstanceAPI api = new SubstanceAPI(session);
        api.submitSubstance(entered);
        session.logout();

        String uuid = entered.get("uuid").asText();

        ensurePass(api.fetchSubstanceByUuid(uuid));
        ensureFailure(api.updateSubstance(entered));
    }
    
   
    
    @Test
   	public void testSubmitProtein() throws Exception {
        JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(fakeUser1)) {

            SubstanceAPI api = new SubstanceAPI(session);
            ensurePass(api.submitSubstance(entered));
        }
   	}
    
    @Test
   	public void testCleanNewlinesInComments() throws Exception {
        JsonNode entered = parseJsonFile(resource);
        entered=(new JsonUtil.JsonNodeBuilder(entered))
        		.add("/relationships/-", JsonUtil.parseJsonString("{\n" + 
                		"		\"relatedSubstance\": {\n" + 
                		"			\"substanceClass\": \"reference\",\n" + 
                		"			\"approvalID\": \"R16CO5Y76E\",\n" + 
                		"			\"refPname\": \"ASPIRIN\",\n" + 
                		"			\"refuuid\": \"1868e373-1b6f-4dcc-8308-0d73ac865f3b\"\n" + 
                		"		},\n" + 
                		"		\"type\": \"PARENT-\\u003eSALT/SOLVATE\",\n" + 
                		"		\"comments\": \"Should remove the extra \\\\n pieces\",\n" + 
                		"		\"references\": [],\n" + 
                		"		\"access\": []\n" + 
                		"	}"))
        		
        		.build();
        try( RestSession session = ts.newRestSession(fakeUser1)) {

            SubstanceAPI api = new SubstanceAPI(session);
            ensurePass(api.submitSubstance(entered));
            String uuid=entered.at("/uuid").asText();
            JsonNode asstored=api.fetchSubstanceJsonByUuid(uuid);
            assertTrue("Entered comment should have a newline", asstored.at("/relationships/0/comments").asText().contains("\n"));
            assertFalse("Entered comment should not have '\\\\n'", asstored.at("/relationships/0/comments").asText().contains("\\n"));
        }
   	}
    
    
    @Test
   	public void testChangeProteinLocal() throws Exception {
        JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(fakeUser1)) {

            SubstanceAPI api = new SubstanceAPI(session);

            String uuid = entered.get("uuid").asText();
            ensurePass( api.submitSubstance(entered));
            renameLocal(api, uuid);
        }
   	}
    
    @Test
   	public void testUnicodeProblem() throws Exception {
        final File resource=new File("test/testJSON/racemic-unicode.json");
        try( RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();

            ensurePass(api.submitSubstance(entered));

            JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);
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
        JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            String uuid = entered.get("uuid").asText();
            ensurePass( api.submitSubstance(entered));
            renameLocal(api, uuid);
            renameServer(api, uuid);
        }
   	}
    @Test
   	public void testAddNameOrgProtein() throws Exception {
        try( RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);
    		
           	JsonNode entered= parseJsonFile(resource);
           	String uuid=entered.get("uuid").asText();              	
            ensurePass(api.submitSubstance(entered));
           	//renameLocal(uuid);
           	addNameOrgServer(api,uuid,  "INN");
        }
   	}
   
    @Test
   	public void testFailDoubleEditProtein() throws Exception {
        try( RestSession session1 = ts.newRestSession(fakeUser1);
        	 RestSession session2 = ts.newRestSession(fakeUser2);
        		) {
            SubstanceAPI api1 = new SubstanceAPI(session1);
            SubstanceAPI api2 = new SubstanceAPI(session2);
    		
           	JsonNode entered= parseJsonFile(resource);
           	String uuid=entered.get("uuid").asText();
            ensurePass(api1.submitSubstance(entered));
            JsonNode fetched=api1.fetchSubstanceJsonByUuid(uuid);
            
            
            JsonNode updated1 = new JsonUtil
                    .JsonNodeBuilder(fetched)
                    .set("/names/0/name", "MADE UP NAME 1")
                    .build();
            JsonNode updated2 = new JsonUtil
                    .JsonNodeBuilder(fetched)
                    .set("/names/0/name", "MADE UP NAME 2")
                    .build();
            
            ensurePass(api1.updateSubstance(updated1));
            ensureFailure(api2.updateSubstance(updated2));
            
            JsonNode fetchedagain=api1.fetchSubstanceJsonByUuid(uuid);
            assertEquals(fetchedagain.at("/names/0/name").asText(), "MADE UP NAME 1");
            
            
        }
   	}
    

    @Test
   	public void testAllowReferenceReverseAndGranularChange() throws Exception {

        String someNew="SOME_NEW_TAG";
        try( RestSession session1 = ts.newRestSession(fakeUser1);
        	 RestSession session2 = ts.newRestSession(fakeUser2);
        		) {
            SubstanceAPI api1 = new SubstanceAPI(session1);
            SubstanceAPI api2 = new SubstanceAPI(session2);
    		
           	JsonNode entered= parseJsonFile(resource);
           	String uuid=entered.get("uuid").asText();
            ensurePass(api1.submitSubstance(entered));
            JsonNode fetched=api1.fetchSubstanceJsonByUuid(uuid);
            JsonNode references = fetched.at("/references");
            JsonNode granularReference = fetched.at("/references/0");
            ArrayNode newReferences = (new ObjectMapper()).createArrayNode();
            for(int i=references.size()-1;i>=0;i--){
            	JsonNode ref=references.get(i);
            	if(granularReference == ref){
            		ref=new JsonUtil
                            .JsonNodeBuilder(ref)
                            .add("/tags/-", someNew)
                            .build();
            	}
            	newReferences.add(ref);
            }
            JsonNode updated1 = new JsonUtil
                    .JsonNodeBuilder(fetched)
                    .set("/references", newReferences)
                    .add("/references/0/tags/-", someNew)
                    .build();
            
            
            ensurePass(api1.updateSubstance(updated1));
            JsonNode fetchedagain=api1.fetchSubstanceJsonByUuid(uuid);
            
            
            Set<String> refuuidsnew = new LinkedHashSet<String>();
            Set<String> refuuidsold = new LinkedHashSet<String>();
            for(JsonNode ref: fetchedagain.at("/references")){
            	refuuidsnew.add(ref.at("/uuid").asText());
            	System.out.println(ref.at("/uuid"));
            	if(ref.at("/uuid").asText().equals(granularReference.at("/uuid").asText())){
            		assertTrue("New added tag should show up in the changed reference", ref.at("/tags").toString().contains(someNew));
            	}
            }
            System.out.println("----");
            for(JsonNode ref: updated1.at("/references")){
            	refuuidsold.add(ref.at("/uuid").asText());
            	System.out.println(ref.at("/uuid"));
            	
            }
            assertEquals(refuuidsold,refuuidsnew);
            System.out.println("These are the tags now");
            System.out.println(fetchedagain.at("/references/0"));
            
        }
   	}
    
    @Test
   	public void testAddUsers() throws Exception {

    	try(RestSession session1 = ts.newRestSession(ts.createUser(Role.Admin));
            RestSession session2 = ts.newRestSession(ts.createUser(Role.SuperUpdate))) {

            JsonNode whoAmI_1 = session1.whoAmIJson();
            JsonNode whoAmI_2 = session2.whoAmIJson();
            //System.out.println("User 1 is:" + session1.whoamiUsername());
            assertEquals(whoAmI_1.get("identifier").asText(), session1.getUserName());
            assertTrue(whoAmI_1.at("/roles").toString().contains(Role.Admin.toString()));
            //System.out.println("User 2 is:" + session2.whoamiUsername());
            assertEquals(whoAmI_2.get("identifier").asText(), session2.getUserName());
            assertTrue(whoAmI_2.at("/roles").toString().contains(Role.SuperUpdate.toString()));
        }
    	
   	}
    
    
    @Test
   	public void onlySuperUsersAllowedToLoadWithoutAccessRulesSet() throws Exception {
        /*
          private static final List<Role> superUserRoles = Role.roles(Role.SuperUpdate,Role.SuperDataEntry );
    private static final List<Role> normalUserRoles = Role.roles(Role.DataEntry,Role.Updater );

         */
        JsonNode entered = parseJsonFile(resource);
        try(RestSession normalUserSession = ts.newRestSession(ts.createUser(Role.DataEntry,Role.Updater));
            RestSession superUserSession = ts.newRestSession(ts.createUser(Role.SuperUpdate,Role.SuperDataEntry));) {

            SubstanceAPI normalAPI = new SubstanceAPI(normalUserSession);
            SubstanceAPI superAPI = new SubstanceAPI(superUserSession);

            String uuid = entered.get("uuid").asText();

            ensureFailure( normalAPI.submitSubstance(entered));


            JsonNode updated = new JsonUtil
                    .JsonNodeBuilder(entered)
                    .add("/access/-", "testGROUP")
                    .build();
            updated = normalAPI.submitSubstanceJson(updated);

            updated = new JsonUtil
                    .JsonNodeBuilder(updated)
                    .remove("/access/0")
                    .build();

            ensureFailure(normalAPI.updateSubstance(updated));
            ensurePass( superAPI.updateSubstance(updated));
        }
   	}
    //Can't submit an preapproved substance via this mechanism
    //Also, can't change an approvalID here, unless an admin
    //TODO: add the admin part
    @Test
   	public void testSubmitPreApprovedRemote() throws Exception {

        JsonNode entered = JsonUtil.parseJsonFile(resource);

        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            ensureFailure(api.submitSubstance(entered));

            entered = SubstanceJsonUtil.prepareUnapprovedPublic(entered);
            entered = api.submitSubstanceJson(entered);
        }

        try(RestSession session = ts.newRestSession(fakeUser2)){
            SubstanceAPI api = new SubstanceAPI(session);
            entered = api.approveSubstanceJson(entered.at("/uuid").asText());
            entered = new JsonUtil
                    .JsonNodeBuilder(entered)
                    .remove("/approvalID")
                    .build();
            api.updateSubstance(entered);

        }
   	}

    @Test
   	public void testAddNameRemote() throws Exception {
        JsonNode entered = parseJsonFile(resource);

        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            String uuid = entered.get("uuid").asText();
            ensurePass( api.submitSubstance(entered));
            addNameServer(api, uuid);
        }
   	}
    @Test
   	public void testAddRemoveNameRemote()  throws Exception {
        JsonNode entered = parseJsonFile(resource);
        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);


            String uuid = entered.get("uuid").asText();
            ensurePass( api.submitSubstance(entered));
            addNameServer(api, uuid);
            removeNameServer(api, uuid);
        }
   	}
    
    @Test
   	public void testChangeHistoryProteinRemote() throws Exception {
        JsonNode entered = parseJsonFile(resource);
        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            String uuid = entered.get("uuid").asText();

            ensurePass( api.submitSubstance(entered));
            renameLocal(api, uuid);
            JsonNode edited = renameServer(api, uuid);
            mostRecentEditHistory(api, uuid, edited);
        }
   	}
    
    @Test
   	public void testFacetUpdateRemote()  throws Exception {
        JsonNode entered= parseJsonFile(resource);

        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            String uuid=entered.get("uuid").asText();
            ensurePass(api.submitSubstance(entered));
            int oldProteinCount=getFacetCountFor(api, "Substance Class","protein");
            assertEquals(1,oldProteinCount);
            renameServer(api, uuid);
            int newProteinCount=getFacetCountFor(api, "Substance Class","protein");
            assertEquals(1,newProteinCount);

       }
   	}

    public int getFacetCountFor(SubstanceAPI session, String face, String label){
    	JsonNode jsn=session.searchJson();
       	JsonNode facets=jsn.at("/facets");
       	for(JsonNode facet:facets){
       		String name=facet.at("/name").asText();
       		if(face.equals(name)){
       			for(JsonNode val:facet.at("/values")){
       				if(label.equals(val.at("/label").asText())){
       					return val.at("/count").asInt();
       				}
       			}
       		}
       	}
       	return 0;
    }

    @Test
   	public void testChangeDisuflideProteinRemote() throws Exception {
        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();

            ensurePass(api.submitSubstance(entered));
            renameLocal(api, uuid);
            JsonNode edited = renameServer(api, uuid);
            mostRecentEditHistory(api, uuid, edited);
            edited = removeLastDisulfide(api, uuid);
        }
   	}
    
    
    @Test
   	public void testChangeDisuflideProteinHistoryRemote() throws Exception {
        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();

            ensurePass(api.submitSubstance(entered));
            renameLocal(api, uuid);
            JsonNode edited = renameServer(api, uuid);
            mostRecentEditHistory(api, uuid, edited);
            edited = removeLastDisulfide(api, uuid);
            mostRecentEditHistory(api, uuid, edited);
        }
   	}

    @Test
   	public void testRemoveAllDisuflidesProtein() throws Exception {
        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();

            ensurePass(api.submitSubstance(entered));
            iterativeRemovalOfDisulfides(api, uuid);
        }
   	}
    
    @Test
   	public void testAddAccessGroupToExistingProtein() throws Exception {
        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode entered = parseJsonFile("test/testJSON/toedit.json");
            String uuid = entered.get("uuid").asText();

            ensurePass(api.submitSubstance(entered));
            testAddAccessGroupServer(api, uuid);
        }
   	}
    
    @Test
   	public void testAddReferenceToExistingProtein() throws Exception {
        try(RestSession session = ts.newRestSession(fakeUser1)) {
               SubstanceAPI api = new SubstanceAPI(session);
               JsonNode entered = parseJsonFile("test/testJSON/toedit.json");
               
               String uuid = entered.get("uuid").asText();
               ensurePass(api.submitSubstance(entered));
               System.out.println("about to add reference");
               testAddReferenceNameServer(api, uuid);
           }
   	}
    
    @Test
   	public void testAddLanguageToExistingProtein() throws Exception {
        try(RestSession session = ts.newRestSession(fakeUser1)) {
               SubstanceAPI api = new SubstanceAPI(session);
               JsonNode entered = parseJsonFile("test/testJSON/toedit.json");
               
               String uuid = entered.get("uuid").asText();
               ensurePass(api.submitSubstance(entered));
               System.out.println("about to add reference");
               testAddLanguageNameServer(api, uuid);
           }
   	}
    
    @Test
   	public void testFailOnPublicMissingPublicReleaseTag() throws Exception {
        try(RestSession session = ts.newRestSession(fakeUser1)) {
               SubstanceAPI api = new SubstanceAPI(session);
			   JsonNode entered = SubstanceJsonUtil
						.prepareUnapproved(JsonUtil.parseJsonFile(new File("test/testJSON/toedit.json")));
			   
			   ensureFailure(api.submitSubstance(entered));
           }
   	}
    
    
    
    
    
    @Test
   	public void testAddAccessGroupToNewProtein() throws Exception {
        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode entered = parseJsonFile("test/testJSON/toedit.json");
            String uuid = entered.get("uuid").asText();
            addAccessGroupThenRegister(api, entered);
        }

   	}

    @Test
   	public void cantSubmitSubstanceTwice() throws Exception {
        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode entered = parseJsonFile("test/testJSON/toedit.json");
            String uuid = entered.get("uuid").asText();

            ensurePass( api.submitSubstance(entered));
            int pcount=getFacetCountFor(api, "Substance Class", "protein");
            System.out.println("#############");
            System.out.println(pcount + " for proteins before fail");
            ensureFailure(api.submitSubstance(entered));
        }
   	}
    
    @Test
   	public void failedRecordsDontChangeFacets() throws Exception {
        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode entered = parseJsonFile("test/testJSON/toedit.json");
            
            ensurePass( api.submitSubstance(entered));
            int pcount=getFacetCountFor(api, "Substance Class", "protein");
            JsonNode jsnnew=new JsonUtil.JsonNodeBuilder(entered).set("/uuid", "00c14fc6-fb23-405c-a3c9-11fb2ab633e2").build();
            //fail because of uuid collision with other references
            ensureFailure(api.submitSubstance(jsnnew));
            //TODO: Add test case to make sure no new facets are introduced
            int pcount2=getFacetCountFor(api, "Substance Class", "protein");
            assertEquals(pcount, pcount2);
        }
   	}
    
    

    @Test
   	public void lookupSubstanceBeforeRegisiteringItFails() throws Exception {
        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode entered = parseJsonFile("test/testJSON/toedit.json");
            String uuid = entered.get("uuid").asText();

            ensureFailure(api.fetchSubstanceByUuid(uuid));
        }

   	}

    @Test
   	public void testFailUpdateNewSubstance() throws Exception {
        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode entered = parseJsonFile("test/testJSON/toedit.json");
            String uuid = entered.get("uuid").asText();

            ensureFailure(api.updateSubstance(entered));
            ensureFailure(api.fetchSubstanceByUuid(uuid));
        }

   	}
    
    
    
    
    
    @Test
   	public void testHistoryViews() throws Exception{


        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode entered = parseJsonFile("test/testJSON/toedit.json");
            String uuid = entered.get("uuid").asText();

            ensurePass(api.submitSubstance(entered));

            JsonNode edited = renameServer(api, uuid);
            mostRecentEditHistory(api, uuid, edited);
            retrieveHistoryView(api, uuid, 1);
        }
   	}

    @Test
    public void revertChangeShouldMakeNewVersionWithOldValues() throws Exception {
        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();

            ensurePass(api.submitSubstance(entered));
            String oldName = "TRANSFERRIN ALDIFITOX S EPIMER";
            String newName = "foo";
            JsonNode oldNode = renameServer(api, uuid, newName);

            mostRecentEditHistory(api, uuid, oldNode);

            assertEquals(newName, api.fetchSubstanceJsonByUuid(uuid).at("/names/0/name").asText());

            renameServer(api, uuid, oldName);
            assertEquals(oldName, api.fetchSubstanceJsonByUuid(uuid).at("/names/0/name").asText());

            JsonNode originalNode = api.fetchSubstanceJsonByUuid(uuid, 1).getOldValue();
            JsonNode v2Node = api.fetchSubstanceJsonByUuid(uuid, 2).getOldValue();

            assertEquals("v1 name", oldName, originalNode.at("/names/0/name").asText());
            assertEquals("v2 name", newName, v2Node.at("/names/0/name").asText());

            JsonNode currentNode = api.fetchSubstanceJsonByUuid(uuid);
            assertEquals("current name", oldName, currentNode.at("/names/0/name").asText());

        }
    }

    @Test
    public void historyNewValueShouldBeOldValueOfNextVersion() throws Exception {
        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();

            ensurePass(api.submitSubstance(entered));
            String oldName = "TRANSFERRIN ALDIFITOX S EPIMER";
            String newName = "foo";
            JsonNode oldNode = renameServer(api, uuid, newName);


            JsonHistoryResult jsonHistoryResult = api.fetchSubstanceJsonByUuid(uuid, 1);
            JsonNode originalNode = jsonHistoryResult.getNewValue();

            assertEquals("v1 new value", jsonHistoryResult.getNewValue(), api.fetchSubstanceJsonByUuid(uuid));
            renameServer(api, uuid, oldName);

            JsonNode v2Node = api.fetchSubstanceJsonByUuid(uuid, 2).getOldValue();

            assertEquals("v2", originalNode, v2Node);
            
        }


    }
    @Test
    public void revertHistoryShouldBeTheSameAsOldValuesExceptMetaData() throws Exception {
        try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();

            ensurePass(api.submitSubstance(entered));
            String oldName = "TRANSFERRIN ALDIFITOX S EPIMER";
            String newName = "foo";
            renameServer(api, uuid, newName);

            JsonHistoryResult jsonHistoryResult = api.fetchSubstanceJsonByUuid(uuid, 1);
            JsonNode originalNode = jsonHistoryResult.getOldValue();
            originalNode = new JsonUtil.JsonNodeBuilder(originalNode).set("/version", "2").build();
            ensurePass(api.updateSubstance(originalNode));
            
            JsonNode reverted = api.fetchSubstanceJsonByUuid(uuid);
            
            Changes changes = JsonUtil.computeChanges(originalNode, reverted,ChangeFilters.keyMatches("last.*"));
            
            Changes expectedChangesDirect = new ChangesBuilder(originalNode,reverted)
                    .replace("/version")
                    .build();

            assertEquals(expectedChangesDirect, changes);
            
            
        }


    }

    public void retrieveHistoryView(SubstanceAPI api, String uuid, int version){
    	String newHTML=api.fetchSubstanceByUuid(uuid).getBody();
    	String oldHTML=api.fetchSubstance(uuid,version).getBody();

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
    private void renameLocal(SubstanceAPI api, String uuid, String oldName, String newName){
        JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);
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
    private void renameLocal(SubstanceAPI api, String uuid){

        renameLocal(api, uuid,"TRANSFERRIN ALDIFITOX S EPIMER", "TRANSFERRIN ALDIFITOX S EPIMER CHANGED");
    

    }
    
    public void mostRecentEditHistory(SubstanceAPI api, String uuid, JsonNode oldRecordExpected){
    		JsonNode newRecordFetched = api.fetchSubstanceJsonByUuid(uuid);
    		int oversion=Integer.parseInt(newRecordFetched.at("/version").textValue());
    		JsonNode edits = api.fetchSubstanceHistoryJson(uuid,oversion-1);

        int changecount=0;
    		for(JsonNode edit: edits){
    			//Ok, I'm just going to look at the null-path edits
    			//there should be 1
    			JsonNode oldv=api.getSession().urlJSON(edit.get("oldValue").asText());
    			JsonNode newv=api.getSession().urlJSON(edit.get("newValue").asText());
    				
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
    public JsonNode renameServer(SubstanceAPI api, String uuid){
        return renameServer(api, uuid, "TRANSFERRIN ALDIFITOX S EPIMER CHANGED");
    }
    public JsonNode renameServer(SubstanceAPI session, String uuid, String newName){
    	
    	JsonNode fetched = session.fetchSubstanceJsonByUuid(uuid);

    	String oldVersion=fetched.at("/version").asText();
    
    	JsonNode updated=new JsonUtil.JsonNodeBuilder(fetched).set("/names/0/name", newName).build();
		session.updateSubstanceJson(updated);
		JsonNode updateFetched = session.fetchSubstanceJsonByUuid(uuid);
		
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
    
    @Test
    public void validateGeneratedCodeAddition(){
    	try(RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode entered = parseJsonFile(resource);
            String uuid = entered.get("uuid").asText();

            ensurePass(api.submitSubstance(entered));
            JsonNode fetched=api.fetchSubstanceJsonByUuid(uuid);
            System.out.println("$$$$$$$$$$$");
            for(JsonNode code:fetched.at("/codes")){
            	System.out.println("CodeSystem:" + code.at("/codeSystem"));
            }
            System.out.println("done$$$$$$$$$$$");
            
        }
    }
    
    public JsonNode addNameOrgServer(SubstanceAPI api, String uuid, String nameorg){
    	
    	JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);

    	
    	NameOrg nameOrg = new NameOrg();
    	nameOrg.nameOrg=nameorg;
    	
    	JsonNode updated= new JsonUtil.JsonNodeBuilder(fetched)
    							.add("/names/0/nameOrgs/-", nameOrg)
    							.build();
    	
    	
    	//System.out.println("FIrst name org is:" + updated.at("/names/0/nameOrgs/0"));
    	api.updateSubstanceJson(updated);
		
		JsonNode updateFetched = api.fetchSubstanceJsonByUuid(uuid);
		assertEquals(
					  updated.at("/names/0/nameOrgs/0/nameOrg").toString(),
				updateFetched.at("/names/0/nameOrgs/0/nameOrg").toString()
				);
		//System.out.println("Now, it's:" + updateFetched.at("/names/0/nameOrgs/0"));
		
		
		return fetched;
    }
    
    public JsonNode addNameServer(SubstanceAPI api, String uuid){

    	
    	JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);

    	String oldVersion=fetched.at("/version").asText();
    	String newName="TRANSFERRIN ALDIFITOX S EPIMER CHANGED";
    
    	JsonNode updated=new JsonUtil.JsonNodeBuilder(fetched)
    			.copy("/names/-","/names/0")
    			.set("/names/1/name", newName)
    			.remove("/names/1/uuid")
    			.remove("/names/1/displayName")
    			.build();

        api.updateSubstanceJson(updated);
		JsonNode updateFetched = api.fetchSubstanceJsonByUuid(uuid);
		
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
    public JsonNode removeNameServer(SubstanceAPI session, String uuid){
    	
		JsonNode updateFetched = session.fetchSubstanceJsonByUuid(uuid);
		session.updateSubstanceJson(new JsonUtil.JsonNodeBuilder(updateFetched)
				.remove("/names/1")
				.build()
				);
		JsonNode afterRemove=session.fetchSubstanceJsonByUuid(uuid);
		
		assertTrue("After removing name, should have (1) name, found (" + afterRemove.at("/names").size() + ")",
				afterRemove.at("/names").size() == 1);
		
		return updateFetched;
    }
    
    public JsonNode testAddAccessGroupServer(SubstanceAPI api,String uuid){
    	
    	JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);
    
    	JsonNode updated=new JsonUtil.JsonNodeBuilder(fetched).add("/access/-", "testGROUP").build();
		api.updateSubstanceJson(updated);
		JsonNode updateFetched = api.fetchSubstanceJsonByUuid(uuid);
		JsonNode accessArray=updateFetched.at("/access");
		System.out.println("Got:" + accessArray.toString());
		assertTrue("Fetched access group should exist",accessArray!=null);
		assertTrue("Fetched access group should not be JSON-null",!accessArray.isNull());
		assertTrue("Fetched access group should not be empty, after being added",accessArray.size()>0);
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
    
    public JsonNode testAddReferenceNameServer(SubstanceAPI api, String uuid){
    	
    	JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);
    
    	String ref=fetched.at("/references/4/uuid").asText();
    	JsonNode updated=new JsonUtil.JsonNodeBuilder(fetched).add("/names/0/references/-", ref).build();
    	
    	System.out.println("updating...");
		JsonNode flashUpdate=api.updateSubstanceJson(updated);
		System.out.println("updated");
		JsonNode updateFetched = api.fetchSubstanceJsonByUuid(uuid);
		
		
		System.out.println("u:"+updated.at("/names/0/references/1"));
		
		System.out.println("fu:"+flashUpdate.at("/names/0/references/1"));
		System.out.println("uf:"+updateFetched.at("/names/0/references/1"));
		
		System.out.println(updated.at("/notes").toString());
		System.out.println(updateFetched.at("/notes").toString());
		
		Changes changes = JsonUtil.computeChanges(updated, updateFetched);
		Changes expectedChanges = new ChangesBuilder(updated,updateFetched)
				
								.replace("/version")
								.replace("/lastEdited")
								.replace("/names/0/lastEdited")
								
								
								.build();
		
		assertEquals(expectedChanges, changes);
		return fetched;
    }
    
    public JsonNode testAddLanguageNameServer(SubstanceAPI api, String uuid){
    	
    	JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);
    
    	JsonNode updated=new JsonUtil.JsonNodeBuilder(fetched).add("/names/0/languages/-", "sp").build();
    	
    	System.out.println("updating... language");
		JsonNode flashUpdate=api.updateSubstanceJson(updated);
		System.out.println("updated ");
		JsonNode updateFetched = api.fetchSubstanceJsonByUuid(uuid);
		
		
		System.out.println("u:"+updated.at("/names/0/languages/1"));
		
		System.out.println("fu:"+flashUpdate.at("/names/0/languages/1"));
		System.out.println("uf:"+updateFetched.at("/names/0/languages/1"));
		
		System.out.println(updated.at("/notes").toString());
		System.out.println(updateFetched.at("/notes").toString());
		
		Changes changes = JsonUtil.computeChanges(updated, updateFetched);
		Changes expectedChanges = new ChangesBuilder(updated,updateFetched)
				
								.replace("/version")
								.replace("/lastEdited")
								.replace("/names/0/lastEdited")
								
								
								.build();
		
		assertEquals(expectedChanges, changes);
		return fetched;
    }
    
    public JsonNode addAccessGroupThenRegister(SubstanceAPI api, JsonNode fetched){
    	
    	JsonNode updated=new JsonUtil.JsonNodeBuilder(fetched).add("/access/-", "testGROUP").build();
    	String uuid = updated.at("/uuid").textValue();
		api.submitSubstanceJson(updated);
		JsonNode fetchedBack = api.fetchSubstanceJsonByUuid(uuid);
		assertEquals("testGROUP",fetchedBack.at("/access/0").textValue());
		
		return fetchedBack;
    }


	public JsonNode parseJsonFile(String path){
		return parseJsonFile(new File(path));
	}
    public JsonNode parseJsonFile(File resource){
    	return SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
    }


    

	public void iterativeRemovalOfDisulfides(SubstanceAPI api, String uuid){
		while (removeLastDisulfide(api, uuid)!=null);
	}
	
	
	/*
	 * This works now.
	 * 
	 */
	public JsonNode removeLastDisulfide(SubstanceAPI api, String uuid){
		JsonNode updatedReturned = api.fetchSubstanceJsonByUuid(uuid);
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
		updatedReturnedb = api.updateSubstanceJson(updated2);
		updatedReturnedb = api.fetchSubstanceJsonByUuid(uuid);
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
