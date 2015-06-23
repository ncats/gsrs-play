package ix.ntd.models;

import ix.core.models.Indexable;
import play.db.ebean.Model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by sheilstk on 6/22/15.
 */
public class Presentation extends Model {

    List<String> symptoms = new ArrayList<String>();
    List<String> labs = new ArrayList<String>();
    List<String> imaging = new ArrayList<String>();
    List<String> other = new ArrayList<String>();

}
