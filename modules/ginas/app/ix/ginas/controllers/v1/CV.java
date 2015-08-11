package ix.ginas.controllers.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ix.core.controllers.EntityFactory;
import ix.ginas.models.v1.VocabularyTerm;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import scala.util.parsing.json.JSON;

import java.io.*;
import java.util.*;

/**
 * Created by sheilstk on 6/29/15.
 */
public class CV {
    Map<String, List<VocabularyTerm>> map =
        new TreeMap<String, List<VocabularyTerm>>();

    public CV (InputStream is) throws IOException {
        String line = "";
        String cvsSplitBy = "\t";

        BufferedReader br = new BufferedReader(new InputStreamReader (is));
        while ((line = br.readLine()) != null) {
            String[] cvTerm = line.split(cvsSplitBy);
            String category = cvTerm[0];
            VocabularyTerm cv = new VocabularyTerm();
            int l = cvTerm.length;
            if (l >= 2) {
                cv.value = cvTerm[1];
            } else {
                cv.value = null;
            }
            if (l >= 3) {
                cv.display = cvTerm[2];
            } else {
                cv.display = null;
            }
            if (l >= 4) {
                cv.description = cvTerm[3];
            } else {
                cv.description = null;
            }
            if (l >= 5) {
                cv.origin = cvTerm[4];
            } else {
                cv.origin = null;
            }
            List<VocabularyTerm> temp = map.get(category);
            if (temp == null) {
                temp = new ArrayList<VocabularyTerm>();
                map.put(category, temp);
            }
            temp.add(cv);
        }
    }

    public String getField (String domain) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode j = mapper.valueToTree(map.get(domain));
        return j.toString();
    }

    public String getDisplay (String domain, String value) {
        String ret= null;
        List<VocabularyTerm> domainList = map.get(domain);
        for(VocabularyTerm v : domainList){
            if(v.value.equals(value)){
                ret = v.display;
            }
        }
        return ret;
    }

    public int size () { return map.size(); }
}



