package ix.test.seqaln;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPreformattedText;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

import ix.AbstractGinasTest;
import ix.test.server.BrowserSession;
import ix.test.server.RestSession;
import ix.utils.Util;
import org.apache.http.Consts;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

/**
 * Created by katzelda on 3/30/16.
 */
public class SequenceSearchAPI {
	
	public static class SequenceSearchOptions{
	    public enum SearchType{
	        GLOBAL,
            LOCAL,
            CONTAINS;
        }
        public enum SequenceType{
            Protein,
            NucleicAcid
            ;

            public static SequenceType parseIsProtein(boolean isProtein){
                if(isProtein){
                    return Protein;
                }
                return NucleicAcid;
            }
        }
	    public SearchType searchType = SearchType.GLOBAL;
	    public String query;
	    public double percentIdentity;
	    public SequenceType type = SequenceType.Protein;

	    public Integer top;
	    public Integer skip;
	    public Integer fdim;
	    /*
	     int top,
            int skip,
            int fdim,
	     */

	    public SequenceSearchOptions(String sequence, double percentIdentity){
            validatePercentIdentity(percentIdentity);
            this.query = Objects.requireNonNull(sequence);
	        this.percentIdentity = percentIdentity;
        }

        private static void validatePercentIdentity(double percentIdentity) {
            if(percentIdentity <0 || percentIdentity >1){
                throw new IllegalArgumentException("percent identity must be between 0 and 1 inclusive");
}
        }

        public Integer getTop() {
            return top;
        }

        public SequenceSearchOptions setTop(Integer top) {
            this.top = top;
            return this;
        }

        public Integer getSkip() {
            return skip;
        }

        public SequenceSearchOptions setSkip(Integer skip) {
            this.skip = skip;
            return this;
        }

        public Integer getFdim() {
            return fdim;
        }

        public SequenceSearchOptions setFdim(Integer fdim) {
            this.fdim = fdim;
            return this;
        }

        public SearchType getSearchType() {
            return searchType;
        }

        public SequenceSearchOptions setSearchType(SearchType searchType) {
            this.searchType = searchType == null ? SearchType.GLOBAL : searchType;
            return this;
        }

        public String getQuery() {
            return query;
        }

        public SequenceSearchOptions setQuery(String query) {
            this.query = Objects.requireNonNull(query);
            return this;
        }

        public double getPercentIdentity() {
            return percentIdentity;
        }

        public SequenceSearchOptions setPercentIdentity(double percentIdentity) {
            validatePercentIdentity(percentIdentity);
            this.percentIdentity = percentIdentity;
            return this;
        }

        public SequenceType getType() {
            return type;
        }

        public SequenceSearchOptions setType(SequenceType type) {
            this.type = type == null ? SequenceType.Protein : type;
            return this;
        }
    }
    private static final Pattern IDENTITY_PRE_PATTERN = Pattern.compile("identity = (\\d+(\\.\\d+)?)");
///ginas/app/substance/4cf9ca84
    private static final Pattern PARTIAL_UUID_PATTERN = Pattern.compile("/ginas/app/substance/(\\S+)");
    private final RestSession session;

    public SequenceSearchAPI(RestSession session) {
        this.session = session;
    }

    public SearchResultActual searchProteins(String querySequence, double percentIdentity){
        return search(querySequence, percentIdentity, true);
    }

    public SearchResultActual searchNucleicAcids(String querySequence, double percentIdentity) {
        return search(querySequence, percentIdentity, false);
    }

        public SearchResultActual search(String querySequence, double percentIdentity, boolean proteins){
            return search(new SequenceSearchOptions(querySequence, percentIdentity)
                                        .setType(SequenceSearchOptions.SequenceType.parseIsProtein(proteins)));
        }
    public SearchResultActual search(SequenceSearchOptions sequenceSearchOptions){
        try{
            List<SearchResult> retList = new ArrayList<>();
            List<NameValuePair> params = new ArrayList<>();

            List<org.apache.http.NameValuePair> form = new ArrayList<>();
            form.add(new BasicNameValuePair("q", sequenceSearchOptions.query));
            form.add(new BasicNameValuePair("cutoff",  String.format("%.2f", sequenceSearchOptions.percentIdentity)));

            form.add(new BasicNameValuePair("seqType", sequenceSearchOptions.type.name()));
            form.add(new BasicNameValuePair("type", sequenceSearchOptions.searchType.name()));

            if(sequenceSearchOptions.top !=null){
                form.add(new BasicNameValuePair("top", Integer.toString(sequenceSearchOptions.top)));
            }
            if(sequenceSearchOptions.skip !=null){
                form.add(new BasicNameValuePair("skip", Integer.toString(sequenceSearchOptions.skip)));
            }
            if(sequenceSearchOptions.fdim !=null){
                form.add(new BasicNameValuePair("fdim", Integer.toString(sequenceSearchOptions.fdim)));
            }
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            entity.writeTo(out);

            System.out.println(new String(out.toByteArray()));
            JsonNode statusNode = session.createRequestHolder(session.getHttpResolver().apiV1("substances/sequenceSearch"))
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")

                    .post(new String(out.toByteArray()))
                    .get(3_000)
                    .asJson();


            ObjectMapper mapper = new ObjectMapper();
            SearchResultJson status = mapper.treeToValue(statusNode, SearchResultJson.class);
            int tries = 1;
            while(!status.determined && tries < 5){
                Thread.sleep(1_000);
                tries++;
                JsonNode sess=session.get(status.url).asJson();
                try{
                	status = mapper.treeToValue(sess, SearchResultJson.class);
                }catch(Exception e){
                	System.err.println(sess.toString());
                	throw e;
                }

                }

            if(status.finished){
                JsonNode resultsNode= session.get(status.results).asJson();
//                System.out.println(resultsNode);
                try{
                return mapper.treeToValue(resultsNode, SearchResultActual.class);
                }catch(Exception e){
                	System.err.println(resultsNode.toString());
                	throw e;
                }
            }

//            System.out.println("search result = " + node);
            return null;
//            params.add(new NameValuePair("sequence", querySequence));
//            params.add(new NameValuePair("identity", String.format("%.2f", percentIdentity)));
//
//
//
//
//            WebRequest request = session.newPostRequest("ginas/app/sequence");
//            request.setRequestParameters(params);
//
//            HtmlPage page = session.submit(request);
//
////            System.out.println(page.asXml());
//            List<HtmlAnchor> results =  page.getByXPath("//div[@class='row']/div/h4[@class='title-name']/a");
//            List<HtmlPreformattedText> identities = page.getByXPath("//div[@class='row']/div/pre");
//
//            Iterator<HtmlAnchor> anchorIter = results.iterator();
//            Iterator<HtmlPreformattedText> identitiesIter = identities.iterator();
//
//            while(anchorIter.hasNext()){
//                HtmlAnchor anchor = anchorIter.next();
//                String uuid = parsePartialUuidFrom(anchor);
//                String name = anchor.getTextContent();
////
////                System.out.println("found " + name);
//                //TODO can we assume the order is the same as the order of the previous list?
//                float identity = Float.NaN;
//                if(identitiesIter.hasNext()) {
//                    identity = parsePercentIdentity(identitiesIter.next());
//                }
//                retList.add( new SearchResult(uuid, name, identity));
//            }
//
//        return retList;
        }catch(Exception e){
            throw new IllegalStateException(e);
        }
    }

    private String parsePartialUuidFrom(HtmlAnchor anchor) throws IOException{
        Matcher matcher = PARTIAL_UUID_PATTERN.matcher(anchor.getHrefAttribute());
        if(!matcher.find()){
            throw new IOException("could not parse uuid from " + anchor.getHrefAttribute());
        }

        return matcher.group(1);
    }
    private float parsePercentIdentity(HtmlPreformattedText identityPreTag) throws IOException{
        Matcher matcher = IDENTITY_PRE_PATTERN.matcher( identityPreTag.getTextContent());
        if(!matcher.find()){
            throw new IOException("could not parse percent identity from " + identityPreTag.getTextContent());
        }

        return Float.parseFloat(matcher.group(1));
    }

    public static class SearchResult{
        private final String uuid;
        private final String name;
        private final float percentIdentity;

        public SearchResult(String uuid, String name, float percentIdentity) {
            Objects.requireNonNull(uuid);
            Objects.requireNonNull(name);

            if(percentIdentity <0){
                throw new IllegalArgumentException("percent identity can not be negative");
            }
            if(percentIdentity > 1){
                throw new IllegalArgumentException("percent identity can not be more than 1");
            }
            this.uuid = uuid;
            this.name = name;
            this.percentIdentity = percentIdentity;
        }

        public String getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        public float getPercentIdentity() {
            return percentIdentity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SearchResult that = (SearchResult) o;

            if (Float.compare(that.percentIdentity, percentIdentity) != 0) return false;
            if (!uuid.equals(that.uuid)) return false;
            return name.equals(that.name);

        }

        @Override
        public int hashCode() {
            int result = uuid.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + (percentIdentity != +0.0f ? Float.floatToIntBits(percentIdentity) : 0);
            return result;
        }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SearchResultActual{
        public int id;
        public int version;
        public String etag;
        public String path;
        public String uri;
        public String sha1;
        public String message;
        public int total, count, skip, top;
        public String query;
        //TODO include facets
        @JsonProperty("content")
        public List<SubstanceHit> hits;

    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SubstanceHit{
            public String uuid;
            public String substanceClass;
            @JsonProperty("_name")
            public String name;
            @JsonProperty("_matchContext")
            public MatchContext matchContext;
    }

    static class MatchContext{
        public List<Alignment> alignments;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Alignment{
        @JsonProperty("query")
            public String querySeq;
        @JsonProperty("target")
            public String targetSeq;
            public String id;
            public String scoreType;
            public int score;
            @JsonProperty("alignments")
            public List<Hsp> hsps;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hsp{
        @JsonProperty("query")
        public String querySeq;
        @JsonProperty("target")
        public String targetSeq;
        public String alignment;

        public int score, global,sub;
        public double iden;
    }

    static class SearchResultJson{
            public long start, stop;
            public String id;
            public int total;
            public String key;
            public String status;
            public boolean determined;
            public boolean finished;
            public int count;
            public String message;

            public String generatingUrl;
            public String url;
            public String results;

    }
}
