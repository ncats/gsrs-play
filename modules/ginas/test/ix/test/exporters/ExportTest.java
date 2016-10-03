package ix.test.exporters;

import ix.core.CacheStrategy;
import ix.core.plugins.IxCache;
import ix.test.ix.test.server.BrowserSession;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.SubstanceLoader;
import ix.test.ix.test.server.SubstanceSearcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
/**
 * Created by katzelda on 9/20/16.
 */
public class ExportTest {

    @Rule
    public GinasTestServer ts = new GinasTestServer(new HashMap<String,Object>(){
    	{
    		put("ix.cache.maxElementsNonEvictable",10);
    		
    	}
    });


    BrowserSession session;
    SubstanceSearcher searcher;
  


    @Before
    public void setup() throws Exception{
        session = ts.newBrowserSession(ts.createAdmin("admin4", "password"));
        SubstanceLoader loader = new SubstanceLoader(session);
        File f = new File("test/testdumps/rep90.ginas");
        loader.loadJson(f);

        searcher = new SubstanceSearcher(session);
    }
    @After
    public void tearDown(){
        session.close();
    }


    @Test 
    public void searchAll() throws IOException {
    
        SubstanceSearcher.SearchResult searchResult = searcher.all();
        try(InputStream in = searchResult.export("csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in))){

            List<String> lines = reader.lines().collect(Collectors.toList());
            assertEquals( 90, lines.size() -1 ); //1 line of header

            Set<String> uuids = parseUUids(lines);

            assertEquals(searchResult.getUuids(), uuids);
        }

    }

    private Set<String> parseUUids(List<String> lines) {
        Set<String> uuids = new HashSet<>(lines.size());
        Iterator<String> iter = lines.iterator();
        //skip header
        iter.next();
        Pattern p = Pattern.compile("\"([0-9a-f\\-]+)\"");
        while(iter.hasNext()){
            String line = iter.next();
            Matcher m = p.matcher(line);
            if(m.find()){
                //TODO the urls in the webpages only show the first 8 chars!
                uuids.add(m.group(1).substring(0,8));
               // System.out.println( m.group(1));
            }
        }
        return uuids;
    }

    @Test 
    public void searchOne() throws IOException {
  
        SubstanceSearcher.SearchResult searchResult = searcher.query("GUIZOTIA ABYSSINICA (L. F.) CASS.");
        try(InputStream in = searchResult.export("csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in))){

            List<String> lines = reader.lines().collect(Collectors.toList());
            assertEquals( 1, lines.size() -1 ); //1 line of header

            Set<String> uuids = parseUUids(lines);

            assertEquals(searchResult.getUuids(), uuids);
        }

    }
    @Test
    public void searchAllWithFullCache() throws IOException {
    
        searchAll(); 
        for(int i = 0; i<1000;i++)
        {
          //  System.out.println("cache Size = " + IxCache.getStatistics().size());
            IxCache.set(Integer.toString(i), new NonEvictable());
        }
        System.out.println("testing cache");
        searchAll();

    }
    
    @Test
    public void searchAllWithFullAfterLotsOfSearchesCache() throws IOException {
    
        searchAll(); 
        for(int i = 0; i<100;i++){
        	SubstanceSearcher.SearchResult searchResult = searcher.query(UUID.randomUUID());
        	//System.out.println("Key:" + searchResult.getKey());
        	
        }

        searchAll();

    }
    @CacheStrategy(evictable=false)
    public static class NonEvictable implements Serializable
    {
    	
    }

}
