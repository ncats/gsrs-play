package ix.ginas.controllers.v1;

import ix.core.controllers.EntityFactory;
import ix.ginas.models.v1.VocabularyTerm;
import play.Logger;
import play.mvc.Result;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by sheilstk on 6/29/15.
 */
public class CVFactory extends EntityFactory {
    public static Map<String, ArrayList<VocabularyTerm>> map = new HashMap<String, ArrayList<VocabularyTerm>>();

    public static Result run() {
        Map<String, ArrayList<VocabularyTerm>> tmpmap = new HashMap<String, ArrayList<VocabularyTerm>>();
        String csvFile = "/ncats/users/sheilstk/Documents/CV.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = "\t";
        try {

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                String[] cvTerm = line.split(cvsSplitBy);
                String category = cvTerm[0];
                VocabularyTerm cv = new VocabularyTerm();
                int l = cvTerm.length;
                if (l >= 2) {
                    cv.valueShort = cvTerm[1];
                } else {
                    cv.valueShort = null;
                }
                if (l >= 3) {
                    cv.valueLong = cvTerm[2];
                } else {
                    cv.valueLong = null;
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
                ArrayList<VocabularyTerm> temp = tmpmap.get(category);
                if (temp == null) {
                    temp = new ArrayList<VocabularyTerm>();
                    tmpmap.put(category, temp);
                }
                temp.add(cv);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        Logger.info("Done");
        for(String domain: tmpmap.keySet()) {
            ArrayList<VocabularyTerm> toReturn = tmpmap.get(domain);
            Collections.sort(toReturn, new Comparator<VocabularyTerm>() {
                public int compare(VocabularyTerm t1, VocabularyTerm t2) {
                    return (t1.valueLong+"").compareTo(t2.valueLong+"");
                }
            });
        };
        map=tmpmap;
        return ok(ix.ginas.views.html.login.render());
    }

    public static ArrayList<VocabularyTerm> getField(String domain) {
        run();
        return map.get(domain);
    }

}



