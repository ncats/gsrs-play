package ix.test.seqaln;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPreformattedText;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

import ix.AbstractGinasTest;
import ix.test.server.BrowserSession;

/**
 * Created by katzelda on 3/30/16.
 */
public class SequenceSearchAPI extends AbstractGinasTest{
	
	
    private static final Pattern IDENTITY_PRE_PATTERN = Pattern.compile("identity = (\\d+(\\.\\d+)?)");
///ginas/app/substance/4cf9ca84
    private static final Pattern PARTIAL_UUID_PATTERN = Pattern.compile("/ginas/app/substance/(\\S+)");
    private final BrowserSession session;

    public SequenceSearchAPI(BrowserSession session) {
        this.session = session;
    }

    public List<SearchResult> searchProteins(String querySequence, double percentIdentity){
        return search(querySequence, percentIdentity, true);
    }

    public List<SearchResult> searchNucleicAcids(String querySequence, double percentIdentity) {
        return search(querySequence, percentIdentity, false);
    }

        public List<SearchResult> search(String querySequence, double percentIdentity, boolean proteins){
        try{
            List<SearchResult> retList = new ArrayList<>();
            List<NameValuePair> params = new ArrayList<>();
            params.add(new NameValuePair("sequence", querySequence));
            params.add(new NameValuePair("identity", String.format("%.2f", percentIdentity)));

            if(proteins) {
                params.add(new NameValuePair("seqType", "Protein"));
            }else{
                params.add(new NameValuePair("seqType", "Nucleic Acid"));
            }


            WebRequest request = session.newPostRequest("ginas/app/sequence");
            request.setRequestParameters(params);

            HtmlPage page = session.submit(request);

            List<HtmlAnchor> results =  page.getByXPath("//div[@class='row']/div/h3/a");
            List<HtmlPreformattedText> identities = page.getByXPath("//div[@class='row']/div/pre");

            Iterator<HtmlAnchor> anchorIter = results.iterator();
            Iterator<HtmlPreformattedText> identitiesIter = identities.iterator();

            while(anchorIter.hasNext()){
                HtmlAnchor anchor = anchorIter.next();
                String uuid = parsePartialUuidFrom(anchor);
                String name = anchor.getTextContent();

                //TODO can we assume the order is the same as the order of the previous list?
                float identity = Float.NaN;
                if(identitiesIter.hasNext()) {
                    identity = parsePercentIdentity(identitiesIter.next());
                }
                retList.add( new SearchResult(uuid, name, identity));
            }

        return retList;
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
}
