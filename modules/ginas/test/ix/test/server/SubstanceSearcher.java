package ix.test.server;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.util.*;

import ix.test.query.builder.SimpleQueryBuilder;
import ix.test.query.builder.SubstanceCondition;
import ix.test.query.builder.SuppliedQueryBuilder;
import ix.utils.Tuple;
import ix.utils.Util;
import play.libs.ws.WSRequest;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;

public interface SubstanceSearcher {
    /**
     * Abstraction of how ginas encodes search orders.
     */
    enum SearchOrderDirection{
        /**
         * Order the search results in forward order.
         */
        FORWARD('^'){
            @Override
            public <T> void reorder(List<T> list) {
                //no-op
            }
            @Override
            public SearchOrderDirection getOtherDirection() {
                return REVERSE;
            }
        },
        /**
         * Order the search results in reverse order.
         */
        REVERSE('$'){
            @Override
            public <T> void reorder(List<T> list) {
                Collections.reverse(list);
            }

            @Override
            public SearchOrderDirection getOtherDirection() {
                return FORWARD;
            }
        };

        private final char c;
        private SearchOrderDirection(char c){
            this.c = c;
        }


        public String formatQuery(String query){
            return c + query;
        }

        /**
         * Take the input list and modify it in place to
         * re order the elements to match the search order.
         * For example calling this method for #REVERSE will reverse
         * the given list.
         *
         * @param forwardList the list of items in the original forward
         *                    search result order.
         * @param <T> the type being ordered.
         */
        public abstract <T> void reorder(List<T> forwardList);

        /**
         * Get the opposite search order direction programmatically.
         * @return the other SearchOrderDirection will never be null.
         */
        public abstract SearchOrderDirection getOtherDirection();

    }

    /**
     * Set the search order term sorted in FORWARD DIRECTION.
     * This is the same as {@link #setSearchOrder(String, SearchOrderDirection), setSearchOrder(term, SearchOrderDirection.FORWARD)}
     * @param term the search term to use; can not be null.
     *
     * @throws NullPointerException if either parameter is null.
     */
    default void setSearchOrder(String term){
        setSearchOrder(term, SearchOrderDirection.FORWARD);
    }

    /**
     * Set the search order term AND the direction results should be sorted by.
     * @param term the search term to use; can not be null.
     * @param dir the SearchOrderDirection to use can not be null.
     *
     * @throws NullPointerException if either parameter is null.
     */
    void setSearchOrder(String term, SearchOrderDirection dir);

    /**
     * Get the UUIDs of all the loaded substances that have this substructure.
     * Assumes the page size for searches is 16.
     * @param smiles a kekulized SMILES string of the substructure to search for.
     * @return a Set of UUIDs that match. will never be null but may be empty.
     *
     * @throws IOException if there is a problem parsing the results.
     */
    SearchResult substructure(String smiles) throws IOException;
    
    /**
     * Get the UUIDs of all the loaded substances that have similarity
     * above the specified cutoff
     * @param smiles a kekulized SMILES string of structure for similarity search
     * @return a Set of UUIDs that match. will never be null but may be empty.
     *
     * @throws IOException if there is a problem parsing the results.
     */
    SearchResult similarity(String smiles, double cutoff) throws IOException;

    /**
     * Get the UUIDs of all the loaded substances that have a defined moiety
     * which matches the given structure by a lychi level 3 match.
     * This is also called "flex match"
     * 
     * @param smiles a kekulized SMILES string of structure search
     * @return a Set of UUIDs that match. will never be null but may be empty.
     *
     * @throws IOException if there is a problem parsing the results.
     */
    SearchResult flex(String smiles) throws IOException;
    
    
    /**
     * Get the UUIDs of all the loaded substances that have a defined structure
     * which matches the given structure by a lychi level 4 match.
     * This is also called "exact match"
     * 
     * @param smiles a kekulized SMILES string of structure search
     * @return a Set of UUIDs that match. will never be null but may be empty.
     *
     * @throws IOException if there is a problem parsing the results.
     */
    SearchResult exact(String smiles) throws IOException;

    
    AbstractSession getSession();

    default WebExportRequest getExport(String format, String key){
        return new WebExportRequest(key,format,getSession());
    }
    /**
     * Performs an name search, where some name must contain
     * the "words" in the provided query string, in that order.
     * Words here are strings of non-whitespace/special characters
     * that lucene considers to be non-word. 
     * @param query
     * @return
     * @throws IOException
     */
    default SearchResult nameSearch(String query) throws IOException{
        String q = new SimpleQueryBuilder()
                .where()
                .condition(SubstanceCondition.name(query).phrase())
                .build();
        return query(q);
    }
    
    /**
     * Performs a name search, trusting that the query term is
     * formatted exactly as it needs to be, with any included
     * quotes or wildcard characters
     * @param query
     * @return
     * @throws IOException
     */
    default SearchResult nameRawSearch(String query) throws IOException{
        String q = new SimpleQueryBuilder()
                .where()
                .condition(SubstanceCondition.name(query).raw())
                .build();
        return query(q);
    }
    
    /**
     * Performs an exact name search, where some name must match
     * the provided query string, allowing for case-insensitive
     * and certain token/white space characters removed. 
     * @param query
     * @return
     * @throws IOException
     */
    default SearchResult exactNameSearch(String query) throws IOException{
        String q = new SimpleQueryBuilder()
                .where()
                .condition(SubstanceCondition.name(query).exact())
                .build();
        return query(q);
    }
    
    /**
     * Performs an code search, where some code must contain
     * the "words" in the provided query string, in that order.
     * Words here are strings of non-whitespace/special characters
     * that lucene considers to be non-word. 
     * @param query
     * @return
     * @throws IOException
     */
    default SearchResult codeSearch(String query) throws IOException{
        String q = new SimpleQueryBuilder()
                .where()
                .condition(SubstanceCondition.code(query).phrase())
                .build();
        return query(q);
    }
    /**
    Performs a term search using the given query builder.

    @apiNote this is the same as {@code query(queryBuilder.build())}.
     */
    default SearchResult query(SuppliedQueryBuilder queryBuilder) throws IOException{
        return query(queryBuilder.build());
    }

    /**
     * Performs an exact term search, where some value must match
     * the provided query string, allowing for case-insensitive
     * and certain token/white space characters removed. 
     * @param query
     * @return
     * @throws IOException
     */
    default SearchResult exactSearch(String query) throws IOException{

        return query( new SimpleQueryBuilder()
                .where()
                .globalMatchesExact(query));
    }
    

    /**
     * Get the UUIDs of all the loaded substances
     * @return a Set of UUIDs that match. will never be null but may be empty.
     * @throws IOException if there is a problem parsing the results.
     */
    default SearchResult query(UUID uuid) throws IOException{
        String q = new SimpleQueryBuilder()
                .where()
                .condition(SubstanceCondition.uuid(uuid.toString()).exact())
                .build();
        return query(q);
    };

    /**
     * Perform a text search of all the loaded substances
     * @return a {@link SearchResult} containing the UUIDs and other info
     *         on the search
     * @throws IOException if there is a problem parsing the results.
     */
    SearchResult query(String queryString) throws IOException;
    
    
    /**
     * Perform a facet filter of all the loaded substances
     * @return a {@link SearchResult} containing the UUIDs and other info
     *         on the search
     * @throws IOException if there is a problem parsing the results.
     */
    SearchResult facet(String name, String value) throws IOException;

    /**
     * Get the UUIDs of all the loaded substances
     * @return a Set of UUIDs that match. will never be null but may be empty.
     * @throws IOException if there is a problem parsing the results.
     */
    default SearchResult all() throws IOException{
        String q = new SimpleQueryBuilder()
                .where()
                .allDocsQuery()
                .build();
        return query(q);
    }
    
    public SubstanceSearchRequest request();
    
    public static enum FacetType{
        SIDEWAYS("sideway", true),
        DRILLDOWN("sideway", false);
        private String prop;
        private boolean bprop;
        
        private FacetType(String prop, boolean bb){
            this.prop=prop;
            this.bprop=bb;
        }
        public Tuple<String, String> getQueryParam(){
            return Tuple.of(prop, bprop+"");
        }
        
        public String key(){
            return prop;
        }
        
        public boolean value(){
            return bprop;
        }
    }
    
    
    
    interface SubstanceSearchRequest{
        SearchResult submit() throws IOException;
        SubstanceSearchRequest addFacet(String name, String value);
        SubstanceSearchRequest setQuery(String q);
        SubstanceSearchRequest setFacetType(FacetType ft);

        String getQuery();
    }
    
    

    public static class SearchRequestOptions{
        /*
          .addQueryParameter("type", type)
            .addQueryParameter("q", smiles)
            .addQueryParameter("cutoff", cutoff +"")
            .addQueryParameter("wait", wait+"")
            .addQueryParameter("rows", rows+"");

        if(defaultSearchOrder!=null){
            root=root.addQueryParameter("order",defaultSearchOrder);
        }
         */

        private String query;

        private Boolean wait;
        private Double cutoff;
        private Integer rows;
        private String type;
        private String order;
        private Integer page;

        public enum SearchType{
            SUBSTRUCTURE("Substructure"),
            SIMILARITY("Similarity"),
            FLEX("Flex")
            ;

            private final String value;

            SearchType(String value) {
                this.value = value;
            }
        }

        public SearchRequestOptions(){
            this(null);
        }
        public SearchRequestOptions(String query) {
            this.query = query;
        }

        public boolean isWait() {
            return wait;

        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public void setWait(boolean wait) {
            this.wait = wait;
        }

        public double getCutoff() {
            return cutoff;
        }

        public void setCutoff(double cutoff) {
            this.cutoff = cutoff;
        }

        public int getRows() {
            return rows;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }

        public String getType() {
            return type;
        }

        public void setType(SearchType type){
            setType(type.value);
        }
        public void setType(String type) {
            this.type = type;
        }

        public String getOrder() {
            return order;
        }

        public void setOrder(String order) {
            this.order = order;
        }


        public void populate(WSRequestHolder holder) throws IOException{
            if(query !=null){
//                holder.setQueryParameter("q", URLEncoder.encode(query, "UTF-8"));
                holder.setQueryParameter("q", query);
            }
            if(type !=null) {
               holder.setQueryParameter("type", type);
            }
            if(order !=null) {
                holder.setQueryParameter("order", order);
            }
            if(rows !=null) {
                holder.setQueryParameter("rows", rows.toString());
            }
            if(cutoff !=null) {
                holder.setQueryParameter("cutoff", cutoff.toString());
            }
            if(wait !=null) {
                holder.setQueryParameter("wait", wait.toString());
            }
            if(page !=null){
                holder.setQueryParameter("page", page.toString());
            }


        }

        private String urlEncode(String s){
            try {
                return URLEncoder.encode(s, "UTF-8");
            }catch(IOException e){
               throw new UncheckedIOException(e);
            }
        }

        public String generatePostBody() throws IOException{
            return new Util.QueryStringManipulator(getQueryParams())

                    .toQueryString();
//            return getQueryParams().entrySet().stream()
//                                        .map(e-> urlEncode(e.getKey()) + "=" + urlEncode(e.getValue()))
//                                        .collect(Collectors.joining("&"));


        }

        private Map<String, String[]> getQueryParams(){

            Map<String, String[]> holder = new LinkedHashMap<>();
            if(query !=null){
//                holder.setQueryParameter("q", URLEncoder.encode(query, "UTF-8"));
                holder.put("q", new String[]{query});
            }
            if(type !=null) {
                holder.put("type", new String[]{type});
            }
            if(order !=null) {
                holder.put("order",  new String[]{order});
            }
            if(rows !=null) {
                holder.put("rows",  new String[]{rows.toString()});
            }
            if(cutoff !=null) {
                holder.put("cutoff", new String[]{ cutoff.toString()});
            }
            if(wait !=null) {
                holder.put("wait",  new String[]{wait.toString()});
            }
            if(page !=null){
                holder.put("page",  new String[]{page.toString()});
            }

            return holder;
        }
    }


}