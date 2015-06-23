package ix.ntd.models;

import play.db.ebean.Model;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by sheilstk on 6/22/15.
 */
public class Outcome extends Model {

    public enum summary {
        Indeterminate,
        Success,
        Failure
    }


    public List<String> clinical = new ArrayList<String>();
    public List<String> microbial = new ArrayList<String>();
    public List<String> imaging = new ArrayList<String>();
    public List<String> adverseEvents = new ArrayList<String>();

    public String followUp;

    public boolean relapse;


    public Outcome() {
    }
}
