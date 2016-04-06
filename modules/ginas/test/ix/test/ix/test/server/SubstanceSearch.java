package ix.test.ix.test.server;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import scala.tools.nsc.backend.icode.Primitives;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by katzelda on 4/5/16.
 */
public class SubstanceSearch {

    private final BrowserSession session;

    private static final Pattern SUBSTANCE_LINK_PATTERN = Pattern.compile("<a href=\"/ginas/app/substance/([a-z0-9]+)\"");
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
    public Set<String> substructure(String smiles) throws IOException {

        //TODO have to kekulize


        String rootUrl = "ginas/app/substances?type=Substructure&q="+URLEncoder.encode(smiles, "UTF-8");
        int page=1;

        Set<String> substances = new LinkedHashSet<>();

        Set<String> temp;
        do {
            HtmlPage result = session.submit(session.newGetRequest(rootUrl + "&page=" + page));
            temp = getSubstancesFrom(result);
            page++;
            //we check the return value of the add() call
            //because when we get to the page beyond the end of the results
            //it returns the first page again
            //so we can check to see if we've already added these
            //records (so add() will return false)
            //which will break us out of the loop.
        }while(substances.addAll(temp));

        return substances;
    }

    private Set<String> getSubstancesFrom(HtmlPage page){
        Set<String> substances = new LinkedHashSet<>();

        Matcher matcher = SUBSTANCE_LINK_PATTERN.matcher(page.asXml());
        while(matcher.find()){
            substances.add(matcher.group(1));
        }

        return substances;
    }
}
