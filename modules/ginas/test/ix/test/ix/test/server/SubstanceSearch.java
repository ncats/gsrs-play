package ix.test.ix.test.server;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by katzelda on 4/5/16.
 */
public class SubstanceSearch {

    private final BrowserSession session;

    private static final Pattern SUBSTANCE_LINK_PATTERN = Pattern.compile("<a href=\"/ginas/app/substance/([a-z0-9]+)\"");

    /*
     <script>
	        filters['ec02c577e23436c4a3d5d9dd48272b41796ec06c'] = {
	           checked: false,
                   name: 'Molecular+Weight',
	           value: '200%3A400'
	        };
	     </script>


            <td>200:400</td>

	 <td>

              <span class="badge" style="float:right;">10</span>

	 </td>
        </tr>
     */
   // private static final Pattern FACET_PATTERN = Pattern.compile("<script>\\s+filters.+?name:\\s*'(.+)?'.+<td>(.+)?</td>\\s+<span class=\"badge\" style=\"float:right;\">(\\d+)</span>");

    /*
    Record Status

 unchecked 	Validated (UNII)	 17


 Substance Class

 unchecked 	Chemical	 17


 Molecular Weight

 unchecked 	200:400	 10
unchecked 	0:200	 9
unchecked 	400:600	 2
unchecked 	>1000	 2
unchecked 	800:1000	 1


 GInAS Tag

 unchecked 	NOMEN	 17
unchecked 	WARNING	 17
unchecked 	WHO-DD	 6
unchecked 	MI	 6
unchecked 	INCI	 2
unchecked 	INN	 1
unchecked 	HSDB	 1
unchecked 	MART.	 1
unchecked 	FCC	 1
unchecked 	FHFI	 1



 ×
Substructure Query:  C1=CC=CC=C1
     */
    private static final Pattern FACET_PATTERN = Pattern.compile("(Record\\s+Status.+?)Substructure Query:");

    private static final Pattern FACET_LINE_PATTERN = Pattern.compile("unchecked\\s+(.+)?\\s+(\\d+)");
    public SubstanceSearch(BrowserSession session) {
        Objects.requireNonNull(session);

        this.session = session;
    }

    /**
     * Get the UUIDs of all the loaded substances that have this substructure.
     * @param smiles a kekulized SMILES string of the substructure to search for.
     * @return a Set of UUIDs that match. will never be null but may be empty.
     *
     * @throws IOException if there is a problem parsing the results.
     */
    public SearchResult substructure(String smiles) throws IOException {

        //TODO have to kekulize


        String rootUrl = "ginas/app/substances?type=Substructure&q="+URLEncoder.encode(smiles, "UTF-8");
        int page=1;

        Set<String> substances = new LinkedHashSet<>();

        Set<String> temp;
        HtmlPage firstPage=null;
        do {

            HtmlPage htmlPage = session.submit(session.newGetRequest(rootUrl + "&page=" + page));
            temp = getSubstancesFrom(htmlPage);
            if(firstPage ==null){
                firstPage = htmlPage;
            }
            page++;
            //we check the return value of the add() call
            //because when we get to the page beyond the end of the results
            //it returns the first page again
            //so we can check to see if we've already added these
            //records (so add() will return false)
            //which will break us out of the loop.
        }while(substances.addAll(temp));

        SearchResult results = new SearchResult(substances);
        if(results.numberOfResults() >0){
            parseFacets(results, firstPage);
        }
        return results;
    }

    private void parseFacets(SearchResult results, HtmlPage html) {

        Matcher matcher = FACET_PATTERN.matcher(html.asText());

        Map<String, Map<String,Integer>> map = new LinkedHashMap<>();
        System.out.println("parsing facets");
       // System.out.println(html.asText());
        if(matcher.find()) {

            System.out.println("facet block is " + matcher.group());
            String facetBlock = matcher.group(1);

            Scanner scanner = new Scanner(facetBlock);
            String facetName = null;
            Map<String, Integer> facetMap = null;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line == null) {
                    continue;
                }
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                Matcher facetMatcher = FACET_LINE_PATTERN.matcher(trimmed);
                if (facetMatcher.find()) {

                    facetMap.put(facetMatcher.group(1), Integer.parseInt(facetMatcher.group(2)));
                } else {
                    if (facetName != null) {
                        System.out.println("adding facets : " + facetName + " => " + facetMap);
                        //add parsed facets
                        results.setFacet(facetName, facetMap);
                    }
                    //new facet block
                    facetName = trimmed;
                    facetMap = new HashMap<>();
                }

            }
        }


    }

    private Set<String> getSubstancesFrom(HtmlPage page){
        Set<String> substances = new LinkedHashSet<>();

        Matcher matcher = SUBSTANCE_LINK_PATTERN.matcher(page.asXml());
        while(matcher.find()){
            substances.add(matcher.group(1));
        }

        return substances;
    }


    public static class SearchResult{
        private final Set<String> uuids;

        private final Map<String, Map<String, Integer>> facetMap = new LinkedHashMap<>();

        public SearchResult(Set<String> uuids){
            Objects.requireNonNull(uuids);
            this.uuids = Collections.unmodifiableSet(new LinkedHashSet<>(uuids));

        }

        public Set<String> getUuids(){
            return uuids;
        }

        public int numberOfResults(){
            return uuids.size();
        }


        public Map<String, Integer> getFacet(String facetName){
            return facetMap.get(facetName);
        }

        public void setFacet(String facetName, Map<String, Integer> countMap){
            Objects.requireNonNull(facetName);
            Objects.requireNonNull(countMap);

            Map<String, Integer> copy = new SortByValueMap<>(Order.DECREASING);
            copy.putAll(countMap);

            facetMap.put(facetName, copy);


        }

        enum Order implements Comparator<Integer>{
            INCREASING{
                @Override
                public int compare(Integer o1, Integer o2) {
                    return Integer.compare(o1,o2);
                }
            },
            DECREASING{
                @Override
                public int compare(Integer o1, Integer o2) {
                    return Integer.compare(o2,o1);
                }
            };


        }

        public static class SortByValueMap<K, V> extends AbstractMap<K,V>
        implements NavigableMap<K,V>{

            private final TreeMap<K,V> map;
            //All this code just so we can reference "this" in the
            //conststructor.
            //if we extend TreeMap, we can't use "this"
            //in our call to super()
            //and we can't set the comparator
            //except from the call to super
            //...
            //I guess we could have changed our comparator to be mutable...

            public SortByValueMap(Comparator<V> valueComparator){
                map = new TreeMap<K, V>(new SortByValueComparator<>(this, valueComparator));
            }

            @Override
            public Set<Entry<K, V>> entrySet() {
                return map.entrySet();
            }

            @Override
            public Entry<K, V> lowerEntry(K key) {
                return map.lowerEntry(key);
            }

            @Override
            public K lowerKey(K key) {
                return map.lowerKey(key);
            }

            @Override
            public Entry<K, V> floorEntry(K key) {
                return map.floorEntry(key);
            }

            @Override
            public K floorKey(K key) {
                return map.floorKey(key);
            }

            @Override
            public Entry<K, V> ceilingEntry(K key) {
                return map.ceilingEntry(key);
            }

            @Override
            public K ceilingKey(K key) {
                return map.ceilingKey(key);
            }

            @Override
            public Entry<K, V> higherEntry(K key) {
                return map.higherEntry(key);
            }

            @Override
            public K higherKey(K key) {
                return map.higherKey(key);
            }

            @Override
            public Entry<K, V> firstEntry() {
                return map.firstEntry();
            }

            @Override
            public Entry<K, V> lastEntry() {
                return map.lastEntry();
            }

            @Override
            public Entry<K, V> pollFirstEntry() {
                return map.pollFirstEntry();
            }

            @Override
            public Entry<K, V> pollLastEntry() {
                return map.pollLastEntry();
            }

            @Override
            public NavigableMap<K, V> descendingMap() {
                return map.descendingMap();
            }

            @Override
            public NavigableSet<K> navigableKeySet() {
                return map.navigableKeySet();
            }

            @Override
            public NavigableSet<K> descendingKeySet() {
                return null;
            }

            @Override
            public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
                return map.subMap(fromKey, fromInclusive, toKey, toInclusive);
            }

            @Override
            public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
                return map.headMap(toKey, inclusive);
            }

            @Override
            public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
                return tailMap(fromKey, inclusive);
            }

            @Override
            public SortedMap<K, V> subMap(K fromKey, K toKey) {
                return map.subMap(fromKey, toKey);
            }

            @Override
            public SortedMap<K, V> headMap(K toKey) {
                return map.headMap(toKey);
            }

            @Override
            public SortedMap<K, V> tailMap(K fromKey) {
                return map.tailMap(fromKey);
            }

            @Override
            public Comparator<? super K> comparator() {
                return map.comparator();
            }

            @Override
            public K firstKey() {
                return map.firstKey();
            }

            @Override
            public K lastKey() {
                return map.lastKey();
            }
        }


        public static class SortByValueComparator<T, V> implements Comparator<T>{
            private final Map<T, V> countMap;

            private Comparator<V> order;

            public SortByValueComparator(Map<T, V> countMap, Comparator<V> order) {
                this.countMap = countMap;
                this.order = order;
            }

            @Override
            public int compare(T s1, T s2) {
                return order.compare(countMap.get(s1), countMap.get(s2));
            }
        }
    }


}
