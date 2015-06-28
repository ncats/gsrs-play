package ix.ntd.models;

import play.db.ebean.Model;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by sheilstk on 6/22/15.
 */
public class Outcome extends Model {

    public enum Summary {
        Indeterminate,
        Success,
        Failure
    }

    public enum ClinicalTest {

    }

public Summary summary;
    public List<ClinicalTest> clinical = new ArrayList<ClinicalTest>();
    public List<String> microbial = new ArrayList<String>();
    public List<String> imaging = new ArrayList<String>();
    public List<String> adverseEvents = new ArrayList<String>();

    public String followUp;

    public boolean relapse;


    public Outcome() {
    }
}
