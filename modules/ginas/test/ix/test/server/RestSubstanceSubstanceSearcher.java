package ix.test.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ix.core.search.text.TextIndexer;
import ix.ginas.controllers.GinasApp;
import ix.ginas.models.v1.Substance;
import ix.test.query.builder.SimpleQueryBuilder;
import ix.test.query.builder.SuppliedQueryBuilder;
import ix.utils.Tuple;
import ix.utils.Util;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RestSubstanceSubstanceSearcher implements SubstanceSearcher{

    private final RestSession restSession;

    private String defaultSearchOrder;

    public RestSubstanceSubstanceSearcher(RestSession restSession) {
        this.restSession = Objects.requireNonNull(restSession);
    }
    @Override
    public AbstractSession getSession() {
        return restSession;
    }
    @Override
    public void setSearchOrder(String term, SearchOrderDirection dir) {
        defaultSearchOrder = dir.formatQuery(term);
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchResultStatus{
        public String key;
        public String status;
        public String url;
        public String results;
        public boolean finished;

        public Optional<RestExportSupportSearchResult> getSomeResults(RestSubstanceSubstanceSearcher searcher, ObjectMapper mapper, int skip) throws IOException {
            return _getResults(searcher, mapper, "skip="+skip);
        }

        public Optional<RestExportSupportSearchResult> getAllResults(RestSubstanceSubstanceSearcher searcher, ObjectMapper mapper) throws IOException{
            return _getResults(searcher, mapper, null);
        }

        private Optional<RestExportSupportSearchResult> _getResults(RestSubstanceSubstanceSearcher searcher, ObjectMapper mapper, String additionalParams) throws IOException{
            if(finished){
                String url = this.results + (additionalParams==null? "" : "?"+ additionalParams);

                JsonNode results = searcher.getSession().get(url).asJson();

                RestExportSupportSearchResult searchResult = parseResultsJson(mapper, key, results, searcher);

                //get edit bins
                Map<String, String[]> m = new HashMap<>();
                for(Map.Entry<String, List<String>> e : splitQuery(url).entrySet()){
                    m.put(e.getKey(), e.getValue().toArray(new String[e.getValue().size()]));
                }


                List<TextIndexer.Facet> facets = GinasApp.getSubstanceFacets(30, m);
                for(TextIndexer.Facet f : facets){
                    Map<String, Integer> counts = new HashMap<>();
                    for(int i=0; i< f.size() ; i++){
                        counts.put(f.getLabel(i), f.getCount(i));
                    }
                    searchResult.setFacet(f.getName(), counts);
                }

                return Optional.of(searchResult);

            }
//        System.out.println("status did not finish? " + currentNode);
            return Optional.empty();
        }

        public Map<String, List<String>> splitQuery(String url) {
            if (url == null || url.trim().isEmpty()) {
                return Collections.emptyMap();
            }
            return Arrays.stream(url.split("&"))
                    .map(this::splitQueryParameter)
                    .collect(Collectors.groupingBy(AbstractMap.SimpleImmutableEntry::getKey, LinkedHashMap::new, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        }

        public AbstractMap.SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
            final int idx = it.indexOf("=");
            final String key = idx > 0 ? it.substring(0, idx) : it;
            final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
            return new AbstractMap.SimpleImmutableEntry<String, String>(key, value);
        }

    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FacetValue{
        public String label;
        public int count;

        @Override
        public String toString() {
            return "FacetValue{" +
                    "label='" + label + '\'' +
                    ", count=" + count +
                    '}';
        }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Facet{
        public String name;
        public List<FacetValue> values;

        public Map<String, Integer> countMap(){
            Map<String, Integer> map = new LinkedHashMap<>();
            for(FacetValue fv: values){
                map.put(fv.label, fv.count);
            }
            return map;
        }

        @Override
        public String toString() {
            return "Facet{" +
                    "name='" + name + '\'' +
                    ", values=" + values +
                    '}';
        }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class RestSearchResult{
        public String sha1;
        public String query;
        public int total;
        public int top;
        public int skip;
        public String uri;
        public String nextPageUri;
        public String id;
        public String path;
        public String method;
        public List<Facet> facets;
        public List<ContentUUID> content;

        @Override
        public String toString() {
            return "RestSearchResult{" +
                    "sha1='" + sha1 + '\'' +
                    ", query='" + query + '\'' +
                    ", total=" + total +
                    ", top=" + top +
                    ", skip=" + skip +
                    ", uri='" + uri + '\'' +
                    ", nextPageUri='" + nextPageUri + '\'' +
                    ", id='" + id + '\'' +
                    ", path='" + path + '\'' +
                    ", method='" + method + '\'' +
                    ", facets=" + facets +
                    ", content=" + content +
                    '}';
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ContentUUID{
        public String uuid;

        @Override
        public String toString() {
            return "ContentUUID{" +
                    "uuid='" + uuid + '\'' +
                    '}';
        }
    }

    private RestExportSupportSearchResult waitForResults(JsonNode node, long timeout) throws IOException{
//        String key = node.get("key").textValue();
//        String resultsUrl = node.get("results").textValue();
//        String status = node.get("status").textValue();
//
//        String url = node.get("url").textValue();
//
//        boolean finished = node.get("finished").asBoolean();

        ObjectMapper mapper = new ObjectMapper();
//    System.out.println(node);
//        SearchResultStatus status = mapper.treeToValue(node, SearchResultStatus.class);
//
//        long start = System.currentTimeMillis();
//        JsonNode currentNode = node;
//        while(!status.finished && System.currentTimeMillis() - start < timeout){
//            currentNode = restSession.createRequestHolder(status.url).get().get(timeout).asJson();
//            status = mapper.treeToValue(currentNode, SearchResultStatus.class);
//        }

        SearchResultStatus status = waitForFinished(node, timeout, mapper);
        if(status.finished){
            JsonNode results = restSession.getAsJson(status.results);

            return parseResultsJson(mapper, status.key, results, this);

        }
//        System.out.println("status did not finish? " + currentNode);
        return null;
    }

    private SearchResultStatus waitForFinished(JsonNode node, long timeout, ObjectMapper mapper) throws IOException{

//    System.out.println(node);
        SearchResultStatus status = mapper.treeToValue(node, SearchResultStatus.class);

        long start = System.currentTimeMillis();
        JsonNode currentNode = node;
        while(!status.finished && System.currentTimeMillis() - start < timeout){
            currentNode = restSession.createRequestHolder(status.url).get().get(timeout).asJson();
            status = mapper.treeToValue(currentNode, SearchResultStatus.class);
        }

        return status;
    }

    private static RestExportSupportSearchResult parseResultsJson(ObjectMapper mapper, String key, JsonNode results, RestSubstanceSubstanceSearcher searcher) throws com.fasterxml.jackson.core.JsonProcessingException {
//        System.out.println("parsing rest results for " + results);
        RestSearchResult restResult = mapper.treeToValue(results, RestSearchResult.class);

        Set<String> uuids = new LinkedHashSet<>(restResult.total);
        for(ContentUUID c :restResult.content){
            uuids.add(c.uuid);
        }
        RestSession restSession = (RestSession) searcher.getSession();

        String originalUri = restResult.uri;

        while(restResult.nextPageUri !=null){
            results = restSession.getAsJson(restResult.nextPageUri);

            restResult = mapper.treeToValue(results, RestSearchResult.class);


            for(ContentUUID c :restResult.content){
                uuids.add(c.uuid);
            }
        }

        RestExportSupportSearchResult ret = new RestExportSupportSearchResult(key, uuids, searcher, restSession.getUser(), restResult.total, originalUri);
//        System.out.println("found rest facets = " + restResult.facets);
        List<Facet> facets = restResult.facets;
        if(facets !=null) {
            for (Facet facet : facets) {

                ret.setFacet(facet.name, facet.countMap());
            }
        }
        return ret;
    }

    public RestExportSupportSearchResult exactSearch(String query) throws IOException{

        return (RestExportSupportSearchResult) query( new SimpleQueryBuilder()
                .where()
                .globalMatchesExact(query));
    }
    public static class RestExportSupportSearchResult extends SearchResult{
        private int total;

        private String baseUri;

        private RestSubstanceSubstanceSearcher searcher;
        public RestExportSupportSearchResult(String searchKey, Set<String> uuids, RestSubstanceSubstanceSearcher searcher, GinasTestServer.User user, int total,
                                             String baseUri) {
            super(searchKey, uuids, searcher, user);
            this.total = total;
            this.baseUri = baseUri;
            this.searcher = searcher;
        }

        @Override
        public WebExportRequest newExportRequest(String format) {
            //this is a hack because we don't really have a good key to use
            //for this export from the Rest API and the rest search doesn't do any caching either
            //so we use a special map used only in testing to store these results
            GinasApp.registerSpecialStream(this.getKey(), ()->getSubstances());
            return super.newExportRequest(format);
        }

        public int getTotal() {
            return total;
        }
        public RestSubstanceSearchRequest refiningRequest() throws IOException{
            WSRequestHolder requestHolder = searcher.getSession().getRestSession().createRequestHolder(baseUri);

            return searcher.newRequest(requestHolder);
        }
        public RestSubstanceSearchRequest refiningRequest(SuppliedQueryBuilder builder) throws IOException{
            return refiningRequest(builder.build());
        }
        public RestSubstanceSearchRequest refiningRequest(String subquery) throws IOException{


            return refiningRequest()
                    .setQuery(subquery);
        }

    }

    @Override
    public RestExportSupportSearchResult substructure(String smiles) throws IOException {
        return substructure(smiles, 0.8);
    }
    public RestExportSupportSearchResult substructure(String smiles, double cutoff) throws IOException {
//        String url = restSession.getHttpResolver().apiV1("substances/structureSearch?q="+ URLEncoder.encode(smiles, "UTF-8")+"");
        String url = restSession.getHttpResolver().apiV1("substances/structureSearch");

        JsonNode node = restSession.createRequestHolder(url)
                .setQueryParameter("q", smiles)
                .setQueryParameter("cutoff", Double.toString(cutoff))
                .get()
                .get(3000)
                .asJson();

        return waitForResults(node, 10_000);




//        JsonNode node = restSession.getAsJson(url+"?q="+smiles);
//        System.out.println(url);
////        Map<String, String[]> map = new LinkedHashMap<>();
////        map.put("q", new String[]{smiles});
////        String post = new Util.QueryStringManipulator(map).toQueryString();
//        String post = "q=\""+smiles+ "\"";
//        System.out.println("post = " + post);
//        JsonNode node = restSession.getRequest(url)
////                .setQueryParameter("q=",smiles)
//                .post(post)
//                .get(3000)
//                .asJson();

//        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(node));
//        return null;
    }

    @Override
    public SearchResult similarity(String smiles, double cutoff) throws IOException {
        SearchRequestOptions opts = new SearchRequestOptions();

        opts.setType(SearchRequestOptions.SearchType.SIMILARITY);
        opts.setQuery(getStructureAsUUID(smiles));
        opts.setCutoff(cutoff);


        return doStructureSearchResults(opts);
    }

    public RestExportSupportSearchResult flexMol(String mol) throws IOException {
        String url = restSession.getHttpResolver().apiV1("substances/search");
//        System.out.println(url);
        JsonNode node = restSession.createRequestHolder(url)

                .post("type=flex&q="+mol)
                .get(3000)
                .asJson();
//            System.out.println(node);
        return parseResultsJson(new ObjectMapper(), "key", node, this);
    }

    /**
     * In case of large structures, they can't be sent via GET requests.
     * The API does not currently support POST, but it does support searching
     * for a structure via UUID. This mechanism just registers the structure,
     * and fetches it via UUID.
     *
     *
     *
     * @param str
     * @return
     */
    public String getStructureAsUUID(String str){
        Map<String,String[]> originalParams = new LinkedHashMap<String,String[]>();
        String post=new Util.QueryStringManipulator(originalParams)
                .toggleInclusion("q", str)
                .toggleInclusion("type", "Flex")
                .toQueryString();
        WSResponse resp= restSession.createRequestHolder(restSession.getHttpResolver().apiV1("substances/structureSearch"))
                .setContentType("application/x-www-form-urlencoded")
                .post(post)
                .get(3000);

//        System.out.println(resp.getBody());
        String uuid=resp.getUri().toString().split("[?]q=")[1].split("&")[0];
        return uuid;
    }
    @Override
    public SearchResult flex(String smiles) throws IOException {

        return substructureSearch(smiles, "Flex");


    }

    private SearchResult substructureSearch(String smiles, String type) throws IOException {
        Map<String,String[]> originalParams = new LinkedHashMap<String,String[]>();
        String post=new Util.QueryStringManipulator(originalParams)
                .toggleInclusion("q", smiles)
                .toggleInclusion("type", type)
                .toQueryString();
        WSResponse resp= restSession.createRequestHolder(restSession.getHttpResolver().apiV1("substances/structureSearch"))
                .setContentType("application/x-www-form-urlencoded")
                .post(post)
                .get(3000);


        return waitForResults(resp.asJson(),10_000);
    }

    @Override
    public SearchResult exact(String smiles) throws IOException {
        return substructureSearch(smiles, "Exact");
    }

    @Override
    public RestExportSupportSearchResult query(String queryString) throws IOException{
        return query(queryString, (Consumer<WSRequestHolder> )null);
    }

    public RestExportSupportSearchResult query(SuppliedQueryBuilder queryBuilder, Consumer<WSRequestHolder> requestHolderConsumer) throws IOException{
        return query(queryBuilder.build(), requestHolderConsumer);
    }
    public RestFacetSearchResult queryFacets(SuppliedQueryBuilder queryBuilder, String facetname) throws IOException{
        return queryFacets(queryBuilder, facetname, null);
    }
    public RestFacetSearchResult queryFacets(SuppliedQueryBuilder queryBuilder, String facetname, Consumer<WSRequestHolder> requestHolderConsumer) throws IOException{
        String url = restSession.getHttpResolver().apiV1("substances/search/@facets");
        WSRequestHolder requestHolder = restSession.createRequestHolder(url)
                .setQueryParameter("q", queryBuilder.build())
                .setQueryParameter("field", facetname);

        if(requestHolderConsumer !=null){
            requestHolderConsumer.accept(requestHolder);
        }
        JsonNode results = requestHolder
                .get()
                .get(3000)
                .asJson();


        ObjectMapper mapper = new ObjectMapper();

        RestFacetSearchResultJson restResult = mapper.treeToValue(results, RestFacetSearchResultJson.class);



        return new RestFacetSearchResult(
                restResult.ftotal,
                restResult.content.stream()
                        .collect(Collectors.toMap(f->f.label, f-> f.count)),
                restResult.facetName,
                restResult.uri);

//
    }

    private RestExportSupportSearchResult query(WSRequestHolder requestHolder,  Consumer<WSRequestHolder> requestHolderConsumer)throws IOException{
        if(defaultSearchOrder !=null){
            requestHolder.setQueryParameter("order", defaultSearchOrder);
        }
        if(requestHolderConsumer !=null){
            requestHolderConsumer.accept(requestHolder);
        }
        String hashKey;

        try {
            hashKey =  Util.sha1(requestHolder);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        JsonNode node = requestHolder
                .get()
                .get(3000)
                .asJson();
        return parseResultsJson(new ObjectMapper(), hashKey, node, this);
    }
    public RestExportSupportSearchResult query(String queryString, Consumer<WSRequestHolder> requestHolderConsumer) throws IOException {
//substances/search";
        String url = restSession.getHttpResolver().apiV1("substances/search");
//        System.out.println(url);
        WSRequestHolder requestHolder = restSession.createRequestHolder(url)
                .setQueryParameter("q", queryString);


        return query(requestHolder, requestHolderConsumer);

    }


    private SearchResult query(String queryString, String type) throws IOException {
//substances/search";
        String url = restSession.getHttpResolver().apiV1("substances/search");
//        System.out.println(url);
        JsonNode node = restSession.createRequestHolder(url)
                .setQueryParameter("q", queryString)
                .setQueryParameter("type", type)
                .get()
                .get(3000)
                .asJson();
//            System.out.println(node);
        return parseResultsJson(new ObjectMapper(), "key", node, this);
    }

    @Override
    public SearchResult facet(String name, String value) throws IOException {
        return null;
    }

    @Override
    public RestSubstanceSearchRequest request() {
        return request(SimpleQueryBuilder.searchAll());
    }

    public RestSubstanceSearchRequest request(SuppliedQueryBuilder query) {
        return request(query.build());
    }

    private  RestSubstanceSearchRequest request(String query) {
        return new RestSubstanceSearchRequest(query);
    }


    public RestExportSupportSearchResult structureSimilaritySearch(SearchRequestOptions options) throws IOException{

        options.setType(SearchRequestOptions.SearchType.SIMILARITY);
        return doStructureSearchResults(options);
    }
    public SearchResultStatus structureSearch(SearchRequestOptions options) throws IOException{

        options.setType(SearchRequestOptions.SearchType.SUBSTRUCTURE);
        return doStructureSearch(options);
    }

    private SearchResultStatus doStructureSearch(SearchRequestOptions options) throws IOException {
        String url = restSession.getHttpResolver().apiV1("substances/structureSearch");
//        System.out.println(url);
        WSRequestHolder holder = restSession.createRequestHolder(url);
        options.populate(holder);


//                                System.out.println("query params = " + holder.getQueryParameters());
        JsonNode node = holder.get()
//                holder.post(options.generatePostBody())

                .get(3000)

                .asJson();

        return waitForFinished(node, 100_000, new ObjectMapper());
    }

    private RestExportSupportSearchResult doStructureSearchResults(SearchRequestOptions options) throws IOException {
        String url = restSession.getHttpResolver().apiV1("substances/structureSearch");
//        System.out.println(url);
        WSRequestHolder holder = restSession.createRequestHolder(url);
        options.populate(holder);


//                                System.out.println("query params = " + holder.getQueryParameters());
        JsonNode node = holder.get()
//                holder.post(options.generatePostBody())

                .get(3000)

                .asJson();

        return waitForResults(node, 100_000);
    }


    private RestSubstanceSearchRequest newRequest(WSRequestHolder holder){
        return new RestSubstanceSearchRequest( holder);
    }

    public class RestFacetSearchResult{

        private int total;
        private Map<String, Integer> facets;
        private String facetName;
        private String url;

        public RestFacetSearchResult(int total, Map<String, Integer> facets, String facetName, String url) {
            this.total = total;
            this.facets = facets;
            this.facetName = facetName;
            this.url = url;
        }

        public int getTotal() {
            return total;
        }

        public Map<String, Integer> getFacets() {
            return facets;
        }

        public String getFacetName() {
            return facetName;
        }

        public String getUrl() {
            return url;
        }

        public Map<String, Integer> getFilteredFacet(String keyContains){
            return facets.entrySet().stream()
                    .filter(e-> e.getKey().contains(keyContains))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }
    static class RestFacetSearchResultJson{
        public int ftotal;
        public int fdim;
        public int fskip;
        public int fcount;
        public String ffilter;

        public String uri;
        public String nextPageUri;
        public String facetName;

        public List<FacetValue> content;

        @Override
        public String toString() {
            return "RestFacetSearchResultJson{" +
                    "ftotal=" + ftotal +
                    ", fdim=" + fdim +
                    ", fskip=" + fskip +
                    ", fcount=" + fcount +
                    ", ffilter='" + ffilter + '\'' +
                    ", uri='" + uri + '\'' +
                    ", nextPageUri='" + nextPageUri + '\'' +
                    ", facetName='" + facetName + '\'' +
                    ", content=" + content +
                    '}';
        }
    }
    public class RestSubstanceSearchRequest implements SubstanceSearchRequest{


        private Set<Tuple<String,String>> facets = new HashSet<>();
        private String q;
        private FacetType ftype = FacetType.SIDEWAYS;

        Consumer<WSRequestHolder> consumers = req -> {};

        private WSRequestHolder holder;

        public RestSubstanceSearchRequest setQueryParameter(String key, String value){
            consumers = consumers.andThen(req -> req.setQueryParameter(key, value));
            return this;
        }
        private RestSubstanceSearchRequest(WSRequestHolder holder){
            this.holder = holder;
        }
        private RestSubstanceSearchRequest(String query){
            this.q = query;
        }
        @Override
        public String getQuery() {
            return q;
        }

        @Override
        public RestExportSupportSearchResult submit() throws IOException {
            Consumer<WSRequestHolder> consumer = req -> {

                facets.forEach(t -> req.setQueryParameter("facet", t.k() + "/" + t.v()));


                req.setQueryParameter(ftype.key(), ftype.value() + "");
            };

            if(holder ==null) {
                return RestSubstanceSubstanceSearcher.this.query(q, consumer.andThen(consumers));
            }else{
                if(q !=null){
                    holder.setQueryParameter("q", q);
                }
                return RestSubstanceSubstanceSearcher.this.query(holder,  consumer.andThen(consumers));
            }

        }

        @Override
        public RestSubstanceSearchRequest addFacet(String name, String value) {
            facets.add(Tuple.of(name, value));
            return this;
        }

        @Override
        public RestSubstanceSearchRequest setQuery(String q) {
            this.q=q;
            return this;
        }

        @Override
        public RestSubstanceSearchRequest setFacetType(FacetType ft) {
            ftype=ft;
            return this;
        }
    }

}
