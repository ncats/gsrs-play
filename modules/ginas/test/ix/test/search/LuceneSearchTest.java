package ix.test.search;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.search.SearchResultContext;
import ix.core.util.ExpectFailureChecker.ExpectedToFail;
import ix.ginas.models.v1.Substance;
import ix.test.AbstractGinasServerTest;
import ix.test.builder.SubstanceBuilder;
import ix.test.server.BrowserSession;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import ix.test.server.SubstanceReIndexer;
import ix.test.server.SubstanceSearcher;
import ix.test.util.TestNamePrinter;

public class LuceneSearchTest extends AbstractGinasServerTest{
	
    
    @Test    
   	public void testTwoWordLuceneNameSearchShouldReturn() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
        	String theName = "ASPIRIN CACLIUM";
            SubstanceAPI api = new SubstanceAPI(session);
            
			new SubstanceBuilder()
				.addName(theName)
				.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
            
            String html=api.getTextSearchHTML(theName);
            assertRecordCount(html, 1);
        }
   	}
    
    @Test    
   	public void testSearchForWordPresentIn2RecordsNamesShouldReturnBoth() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
        	String aspirinCalcium = "ASPIRIN CACLIUM";
        	String aspirin = "ASPIRIN";
            SubstanceAPI api = new SubstanceAPI(session);
            
            
            new SubstanceBuilder()
			.addName(aspirinCalcium)
			.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
            
            new SubstanceBuilder()
			.addName(aspirin)
			.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
            
            String html=api.getTextSearchHTML(aspirin);
            assertRecordCount(html, 2);
        }
   	}
    
    @Test   
   	public void testExactSearchForWordPresentIn2RecordsNamesShouldReturnOnlyExact() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
        	String aspirinCalcium = "ASPIRIN CACLIUM";
        	String aspirin = "ASPIRIN";
        	String q = "\"^" + aspirin + "$\"";
            SubstanceAPI api = new SubstanceAPI(session);
            
            

            new SubstanceBuilder()
			.addName(aspirinCalcium)
			.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
            
            new SubstanceBuilder()
			.addName(aspirin)
			.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
            
            
            String html=api.getTextSearchHTML(q);
            assertRecordCount(html, 1);
			try(BrowserSession browserSession = ts.newBrowserSession(ts.getFakeUser1())){
				SubstanceSearcher searcher = new SubstanceSearcher(browserSession);

				SubstanceSearcher.SearchResult r = searcher.exactSearch(aspirin);
				assertEquals(1, r.getUuids().size());
			}

        }
    }


	@Test
	public void exactNormalNameSearchWhenlevosIndexedTooShouldNotReturnLevo() throws Exception {
		GinasTestServer.User user = ts.getFakeUser1();


		try( RestSession session = ts.newRestSession(user)) {

			String ibuprofen = "IBUPROFEN";
			SubstanceAPI api = new SubstanceAPI(session);



			new SubstanceBuilder()
					.addName(ibuprofen)
					.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));

			new SubstanceBuilder()
					.addName("(-)-"+ibuprofen)
					.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));

			try(BrowserSession browserSession = ts.newBrowserSession(user)){
				SubstanceSearcher searcher = new SubstanceSearcher(browserSession);

				SubstanceSearcher.SearchResult r = searcher.exactSearch(ibuprofen);
				assertEquals(1, r.getUuids().size());

				ts.doAsUser(user, () -> {
					SearchResultContext src = SearchResultContext.getSearchResultContextForKey(r.getKey());

					Substance s = r.getSubstances().findFirst().get();

					assertEquals(ibuprofen, s.getName());
				});
			}

		}
	}

	@Test
	public void exactLevoNameSearchWhenLevosIndexedTooShouldOnlyReturnLevo() throws Exception {
		GinasTestServer.User user = ts.getFakeUser1();


		try( RestSession session = ts.newRestSession(user)) {

			String ibuprofen = "IBUPROFEN";
			String levo = "(-)-"+ibuprofen;

			SubstanceAPI api = new SubstanceAPI(session);



			new SubstanceBuilder()
					.addName(ibuprofen)
					.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));

			new SubstanceBuilder()
					.addName(levo)
					.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));

			new SubstanceBuilder()
					.addName("(+)-"+ibuprofen)
					.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));

			try(BrowserSession browserSession = ts.newBrowserSession(user)){
				SubstanceSearcher searcher = new SubstanceSearcher(browserSession);

				SubstanceSearcher.SearchResult r = searcher.exactSearch(levo);
				assertEquals(1, r.getUuids().size());

				ts.doAsUser(user, () -> {
					Substance s = r.getSubstances().findFirst().get();

					assertEquals(levo, s.getName());
				});
			}

		}
	}

	@Test
	public void exactDextroNameSearchWhenLevosIndexedTooShouldOnlyReturnDextro() throws Exception {
		GinasTestServer.User user = ts.getFakeUser1();


		try (RestSession session = ts.newRestSession(user)) {

			String ibuprofen = "IBUPROFEN";
			String levo = "(-)-" + ibuprofen;
			String dextro = "(+)-" + ibuprofen;

			SubstanceAPI api = new SubstanceAPI(session);


			new SubstanceBuilder()
					.addName(ibuprofen)
					.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));

			new SubstanceBuilder()
					.addName(levo)
					.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));

			new SubstanceBuilder()
					.addName(dextro)
					.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));

			try (BrowserSession browserSession = ts.newBrowserSession(user)) {
				SubstanceSearcher searcher = new SubstanceSearcher(browserSession);

				SubstanceSearcher.SearchResult r = searcher.exactSearch(dextro);
				assertEquals(1, r.getUuids().size());

				ts.doAsUser(user, () -> {
					Substance s = r.getSubstances().findFirst().get();

					assertEquals(dextro, s.getName());
				});
			}

		}
	}

	@Test
	public void ensureSuggestFieldWorks() throws Exception {
		GinasTestServer.User user = ts.getFakeUser1();


		try (RestSession session = ts.newRestSession(user)) {

			String pre = "IBUP";
			String ib2 = "IBUPROFEN";

			SubstanceAPI api = new SubstanceAPI(session);


			new SubstanceBuilder()
					.addName(ib2)
					.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));

			JsonNode suggest = api.getSuggestPrefixJson(pre);
			assertEquals(1,suggest.at("/Name").size());
			assertEquals(ib2, suggest.at("/Name/0/key").asText());

		}
	}





	@Test @ExpectedToFail @Ignore
	public void ensureSuggestFieldDisappearsAfterNameRemoved() throws Exception {
		GinasTestServer.User user = ts.getFakeUser1();


		try (RestSession session = ts.newRestSession(user)) {

			String pre = "IBUP";
			String ib2 = "IBUPROFEN";
			String name2 = "ASPIRIN";

			SubstanceAPI api = new SubstanceAPI(session);

			JsonNode submit=new SubstanceBuilder()
					.addName(ib2)
					.generateNewUUID()
					.buildJson();
			ensurePass(api.submitSubstance(submit));


			JsonNode suggestBefore = api.getSuggestPrefixJson(pre);
			assertEquals(1,suggestBefore.at("/Name").size());
			assertEquals(ib2, suggestBefore.at("/Name/0/key").asText());

			JsonNode update= SubstanceBuilder
					.from(api.fetchSubstanceJsonByUuid(submit.at("/uuid").asText()))
					.andThenMutate(s->s.names.get(0).name=name2)
					.buildJson();

			ensurePass(api.updateSubstance(update));

			JsonNode suggestLater = api.getSuggestPrefixJson(pre);
			assertTrue(suggestLater.at("/Name").isMissingNode());


		}
	}


	@Test
	public void ensureSuggestFieldDisappearsAfterNameRemovedAndReindexed() throws Exception {
		GinasTestServer.User user = ts.getFakeUser1();


		try (RestSession session = ts.newRestSession(user)) {

			String pre1 = "IBUP";
			String pre2 = "ASP";
			String ib2 = "IBUPROFEN";
			String name2 = "ASPIRIN";

			SubstanceAPI api = new SubstanceAPI(session);

			JsonNode submit=new SubstanceBuilder()
					.addName(ib2)
					.generateNewUUID()
					.buildJson();
			ensurePass(api.submitSubstance(submit));


			JsonNode suggestBefore = api.getSuggestPrefixJson(pre1);
			assertEquals(1,suggestBefore.at("/Name").size());
			assertEquals(ib2, suggestBefore.at("/Name/0/key").asText());

			JsonNode update= SubstanceBuilder
					.from(api.fetchSubstanceJsonByUuid(submit.at("/uuid").asText()))
					.andThenMutate(s->s.names.get(0).name=name2)
					.buildJson();

			ensurePass(api.updateSubstance(update));

			try (BrowserSession browserSession = ts.newBrowserSession(ts.createAdmin("adminguy", "admin"))) {
				new SubstanceReIndexer(browserSession).reindex();
			}

			assertTrue(api.getSuggestPrefixJson(pre1).at("/Name").isMissingNode());

			JsonNode suggestLater = api.getSuggestPrefixJson(pre2);
			assertEquals(1,suggestLater.at("/Name").size());
			assertEquals(name2, suggestLater.at("/Name/0/key").asText());


		}
	}

	@Test
	public void ensureSuggestFieldDisappearsAfterNameRemovedAndNewSubstanceAddedAndReindexed() throws Exception {
		GinasTestServer.User user = ts.getFakeUser1();


		try (RestSession session = ts.newRestSession(user)) {

			String pre1 = "IBUP";
			String pre2 = "ASP";
			String ib2 = "IBUPROFEN";
			String name2 = "ASPIRIN";

			SubstanceAPI api = new SubstanceAPI(session);

			JsonNode submit=new SubstanceBuilder()
					.addName(ib2)
					.generateNewUUID()
					.buildJson();
			ensurePass(api.submitSubstance(submit));


			JsonNode suggestBefore = api.getSuggestPrefixJson(pre1);
			assertEquals(1,suggestBefore.at("/Name").size());
			assertEquals(ib2, suggestBefore.at("/Name/0/key").asText());

			JsonNode update= SubstanceBuilder
					.from(api.fetchSubstanceJsonByUuid(submit.at("/uuid").asText()))
					.andThenMutate(s->s.names.get(0).name=name2)
					.buildJson();

			ensurePass(api.updateSubstance(update));

			new SubstanceBuilder()
			.addName("Just another name")
			.generateNewUUID()
			.buildJsonAnd(s->ensurePass(api.submitSubstance(s)));

			try (BrowserSession browserSession = ts.newBrowserSession(ts.createAdmin("adminguy", "admin"))) {
				new SubstanceReIndexer(browserSession).reindex();
			}

			assertTrue(api.getSuggestPrefixJson(pre1).at("/Name").isMissingNode());


			JsonNode suggestLater = api.getSuggestPrefixJson(pre2);

			try (BrowserSession browserSession = ts.newBrowserSession(user)) {
				SubstanceSearcher searcher = new SubstanceSearcher(browserSession);
				SubstanceSearcher.SearchResult r = searcher.nameSearch(name2);
				assertEquals("Name search should return 1 result",1, r.getUuids().size());
			}

			assertEquals(1,suggestLater.at("/Name").size());
			assertEquals(name2, suggestLater.at("/Name/0/key").asText());


		}
	}


	@Test
	public void ensureUpdating2RecordsAndReindexingResultsIn2SubstancesInSearch() throws Exception {
		GinasTestServer.User user = ts.getFakeUser1();


		try (RestSession session = ts.newRestSession(user)) {

			List<String> toSearch = new ArrayList<>();

			SubstanceAPI api = new SubstanceAPI(session);
			for(int i=0;i<2;i++){
				String name = "ABC" + i;
				toSearch.add(name);
				JsonNode submit=new SubstanceBuilder()
					.addName(name)
					.generateNewUUID()
					.buildJson();
				ensurePass(api.submitSubstance(submit));
				SubstanceBuilder
				.from(api.fetchSubstanceJsonByUuid(submit.at("/uuid").asText()))
				.andThenMutate(s->s.names.get(0).name=name + " changed")
				.buildJsonAnd(s->ensurePass(api.updateSubstance(s)));
			}

			try (BrowserSession browserSession = ts.newBrowserSession(user)) {
				for(String search:toSearch){
					SubstanceSearcher searcher = new SubstanceSearcher(browserSession);
					SubstanceSearcher.SearchResult r = searcher.nameSearch(search);
					assertEquals("Pre-reindex Name search for " + search + " should return 1 result",1, r.getUuids().size());
				}
			}

			try (BrowserSession browserSession = ts.newBrowserSession(ts.createAdmin("adminguy", "admin"))) {
				new SubstanceReIndexer(browserSession).reindex();
			}


			try (BrowserSession browserSession = ts.newBrowserSession(user)) {
				for(String search:toSearch){
					SubstanceSearcher searcher = new SubstanceSearcher(browserSession);
					SubstanceSearcher.SearchResult r = searcher.nameSearch(search);
					assertEquals("Post-reindex Name search for " + search + " should return 1 result",1, r.getUuids().size());
				}
			}

		}
	}

	@Test
	public void ensureUpdatingARecordThreeTimesIsStillSearchable() throws Exception {
		GinasTestServer.User user = ts.getFakeUser1();


		try (RestSession session = ts.newRestSession(user)) {

			Consumer<String> searchFor = (s)->{
				try (BrowserSession browserSession = ts.newBrowserSession(user)) {

						SubstanceSearcher searcher = new SubstanceSearcher(browserSession);
						SubstanceSearcher.SearchResult r = searcher.nameSearch(s);
						assertEquals("Search for " + s + " should return 1 result",1, r.getUuids().size());
				}catch(Exception e){
					throw new IllegalStateException(e);
				}
			};

			SubstanceAPI api = new SubstanceAPI(session);
			JsonNode submit=new SubstanceBuilder()
					.addName("START1")
					.generateNewUUID()
					.buildJson();
				ensurePass(api.submitSubstance(submit));


			SubstanceBuilder
				.from(api.fetchSubstanceJsonByUuid(submit.at("/uuid").asText()))
				.andThenMutate(s->s.names.get(0).name="START2")
				.buildJsonAnd(s->{
					ensurePass(api.updateSubstance(s));
					searchFor.accept("START2");
				});

			SubstanceBuilder
				.from(api.fetchSubstanceJsonByUuid(submit.at("/uuid").asText()))
				.andThenMutate(s->s.names.get(0).name="START3")
				.buildJsonAnd(s->{
					ensurePass(api.updateSubstance(s));
					searchFor.accept("START3");
				});

			SubstanceBuilder
			.from(api.fetchSubstanceJsonByUuid(submit.at("/uuid").asText()))
			.andThenMutate(s->s.names.get(0).name="START4")
			.buildJsonAnd(s->{
				ensurePass(api.updateSubstance(s));
				searchFor.accept("START4");
			});

		}
	}



	@Test
	public void normalNameSearchWhenlevosAndDextrosIndexedTooShouldOnlyReturnAll3() throws Exception{
		GinasTestServer.User user = ts.getFakeUser1();


		try (RestSession session = ts.newRestSession(user)) {

			String ibuprofen = "IBUPROFEN";
			String levo = "(-)-" + ibuprofen;
			String dextro = "(+)-" + ibuprofen;

			SubstanceAPI api = new SubstanceAPI(session);


			new SubstanceBuilder()
					.addName(ibuprofen)
					.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));

			new SubstanceBuilder()
					.addName(levo)
					.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));

			new SubstanceBuilder()
					.addName(dextro)
					.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));

			try (BrowserSession browserSession = ts.newBrowserSession(user)) {
				SubstanceSearcher searcher = new SubstanceSearcher(browserSession);

				SubstanceSearcher.SearchResult r = searcher.nameSearch(dextro);
				assertEquals(3, r.getUuids().size());

				ts.doAsUser(user, () -> {
					Set<String> actual = r.getSubstances()
									.map(s -> s.getName())
									.collect(Collectors.toSet());

					Set<String> expected = setOf(ibuprofen, levo, dextro);
					assertEquals(expected, actual);
				});
			}

		}
	}

	private Set<String> setOf(String... values) {
		Set<String> set = new HashSet<>(values.length);
		for(String v : values){
			set.add(v);
		}
		return set;
	}


	@Test
   	public void testSearchForQuotedPhraseShouldReturnOnlyRecordWithThatOrder() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
        	String aspirinCalcium = "ASPIRIN CACLIUM";
        	String aspirin = "CALCIUM ASPIRIN";
        	String q = "\"" + aspirin + "\"";
            SubstanceAPI api = new SubstanceAPI(session);

            new SubstanceBuilder()
			.addName(aspirinCalcium)
			.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
            
            new SubstanceBuilder()
			.addName(aspirin)
			.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
            
            String html=api.getTextSearchHTML(q);
            assertRecordCount(html, 1);
        }catch(Throwable e){
        	e.printStackTrace();
        	throw e;
        }
   	}
    
    
    @Test   
   	public void testSearchForNameFieldWorks() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
        	String aspirin = "ASPIRIN";
        	String q = "root_names_name:\"" + aspirin + "\"";
            SubstanceAPI api = new SubstanceAPI(session);

            new SubstanceBuilder()
			.addName(aspirin)
			.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
            
            String html=api.getTextSearchHTML(q);
            assertRecordCount(html, 1);

			try(BrowserSession browserSession = ts.newBrowserSession(ts.getFakeUser1())){
				SubstanceSearcher searcher = new SubstanceSearcher(browserSession);

				SubstanceSearcher.SearchResult r = searcher.nameSearch(aspirin);
				assertEquals(1, r.getUuids().size());
			}

        }catch(Throwable e){
        	e.printStackTrace();
        	throw e;
        }
   	}
    
    @Test   
   	public void testSearchForNameInCodeFieldDoesntWork() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
        	String aspirin = "ASPIRIN";
        	String q = "root_codes_code:\"" + aspirin + "\"";
            SubstanceAPI api = new SubstanceAPI(session);

            new SubstanceBuilder()
			.addName(aspirin)
			.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
            
            String html=api.getTextSearchHTML(q);
            assertRecordCount(html, -1);
        }catch(Throwable e){
        	e.printStackTrace();
        	throw e;
        }
   	}
    
    @Test   
   	public void testSearchForNameInNameFieldDoesntReturnCodeMatches() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
        	String aspirin = "ASPIRIN";
        	String otherName = "GLEEVEC";
        	String q = "root_names_name:\"" + aspirin + "\"";
            SubstanceAPI api = new SubstanceAPI(session);

            new SubstanceBuilder()
			.addName(aspirin)
			.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
            
            new SubstanceBuilder()
			.addName(otherName)
			.addCode("CAS",aspirin)
			.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
            
            assertRecordCount(api.getTextSearchHTML(q), 1);
            assertRecordCount(api.getTextSearchHTML(aspirin), 2);
        }catch(Throwable e){
        	e.printStackTrace();
        	throw e;
        }
   	}
    
    @Test
   	public void testCodeSystemDynamicFieldMatches() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
        	String cas = "50-00-0";
        	String defName = "AAA";
        	
        	
        	String qCas = "root_codes_CAS:\"" + cas + "\"";
        	String qCasFake = "root_codes_CASFAKE:\"" + cas + "\"";
        	String qAll = "\"" + cas + "\"";
        	String qCode= "root_codes_code:\"" + cas + "\"";
        	
            SubstanceAPI api = new SubstanceAPI(session);

            new SubstanceBuilder()
			.addName(defName + "A")
			.addCode("CASFAKE",cas)
			.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
            
            new SubstanceBuilder()
			.addName(defName + "B")
			.addCode("CAS",cas)
			.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
            
            new SubstanceBuilder()
			.addName(cas)
			.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
            
            assertRecordCount(api.getTextSearchHTML(qCas), 1);
            assertRecordCount(api.getTextSearchHTML(qCasFake), 1);
            assertRecordCount(api.getTextSearchHTML(qAll), 3);
            assertRecordCount(api.getTextSearchHTML(qCode), 2);
        }catch(Throwable e){
        	e.printStackTrace();
        	throw e;
        }
   	}
    
    
    public static void assertRecordCount(String html, int expected){
    	int rc=getRecordCountFromHtml(html);
        assertEquals("Should have " + expected + " results, but found:" + rc, rc,expected);
    }
    // should be moved to some holder object
    public static int getRecordCountFromHtml(String html){
    	String recStart = "<span id=\"record-count\" class=\"label label-default\">";
    	int io=html.indexOf(recStart);
    	int ei=html.indexOf("<", io + 3);
    	if(ei>0 && io >0){
    		String c=html.substring(io + recStart.length(),ei);
    		try{
    		return Integer.parseInt(c.trim());
    		}catch(Exception e){}
    	}
    	return -1;
    }

    @Test  
   	public void testSearchForQuotedExactPhraseShouldReturnOnlyThatPhraseNotSuperString() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
        	String aspirinCalcium = "ASPIRIN CACLIUM";
        	String aspirinCalciumHydrate = "ASPIRIN CACLIUM HYDRATE";
        	String q = "\"^" + aspirinCalcium + "$\"";
            SubstanceAPI api = new SubstanceAPI(session);
            
            new SubstanceBuilder()
			.addName(aspirinCalcium)
			.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
            
            new SubstanceBuilder()
			.addName(aspirinCalciumHydrate)
			.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
            
            
            String html=api.getTextSearchHTML(q);
            assertRecordCount(html, 1);
        }catch(Throwable e){
        	e.printStackTrace();
        	throw e;
        }
   	}
    
    
    @Test   
   	public void testDefaultBrowseOrderShouldShowMostRecentlyEdittedFirst() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
        	SubstanceAPI api = new SubstanceAPI(session);
        	final String prefix="MYSPECIALSUFFIX";
        	List<String> addedName = new ArrayList<String>();
        	
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ".chars().mapToObj(i -> ((char) i) + prefix).forEach(n -> {
				addedName.add(n);
				new SubstanceBuilder()
					.addName(n)
					.buildJsonAnd(j->{
						ensurePass(api.submitSubstance(j));
					});
			});
            
            String html=api.fetchSubstancesUIBrowseHTML();
            assertFalse("First page shouldn't show oldest record by default. But found:" + addedName.get(0),html.contains(addedName.get(0)));
            int rows=16;
            Collections.reverse(addedName);
            addedName.stream().limit(rows).forEachOrdered(n->
            			assertTrue("First page should show newest 16 records by default.",html.contains(n))
            		);
            addedName.stream().skip(rows).forEachOrdered(n->
            	assertFalse("First page shouldn't show oldest records by default.",html.contains(n))
        	);
            
        }catch(Throwable e){
        	e.printStackTrace();
        	throw e;
        }
   	}
    
    
    @Test   
   	public void testBrowsingWithDisplayNameOrderingShouldOrderAlphabetically() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
        	SubstanceAPI api = new SubstanceAPI(session);
        	final String prefix="MYSPECIALSUFFIX";
        	List<String> addedName = new ArrayList<String>();
        	
        	
        	"ABCDEFGHIJKLMNOPQRSTUVWXYZ".chars()
        			 .mapToObj(i->((char)i)+prefix)
        			 .forEach(n->{
        				 addedName.add(n);
        				 new SubstanceBuilder()
	     					.addName(n)
	     					.buildJsonAnd(j->{
	     						ensurePass(api.submitSubstance(j));
	     					});
        			 });
            
        	
            String html=api.fetchSubstancesUISearchHTML(null,null,"^Display Name");
            String rhtml=api.fetchSubstancesUISearchHTML(null,null,"$Display Name");
            int rows=16;
            
            //Collections.reverse(addedName);
            addedName.stream().limit(rows).forEachOrdered(n->
            	assertTrue("Sorting alphabetical should show:" + n ,html.contains(n))
            	);
            addedName.stream().skip(rows).forEachOrdered(n->
            	assertFalse("Sorting alphabetical shouldn't show:" + n ,html.contains(n))
            	);
            
            Collections.reverse(addedName);
			addedName.stream().limit(16)
					.forEachOrdered(n -> assertTrue("Sorting rev alphabetical should show:" + n, rhtml.contains(n)));
			addedName.stream().skip(16)
					.forEachOrdered(n -> assertFalse("Sorting rev alphabetical shouldn't show:" + n, rhtml.contains(n)));
            
        }catch(Throwable e){
        	throw e;
        }
   	}
}
