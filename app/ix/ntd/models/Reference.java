package ix.ntd.models;

import play.db.ebean.Model;
import javax.persistence.*;
import ix.core.models.Indexable;

import java.util.Date;

@Entity
@DiscriminatorValue("REF")
public class Reference extends Model {
    public enum RefType {
        Clinician_Report,
        Published_Reference
    }
    public String year;

    public Integer pmid;

    public String doi;
    public String title;
    public String url;
    @Lob
    public String refAbstract;

    public enum typeOfArticle{
       None, Original, Review}

    public enum typeOfStudy {
        Case_Report,
        Case_Series,
        Case_Study,
        Observational_Study,
        Clinical_Trial,
        Other
    }

    public enum AimofStudy{
        Safety,
        Effectiveness,
        Both
    }
    public enum language {
    }

    public boolean fullTextAvailable;
    public boolean  fullTextInRepository;

    public enum TreatmentOrPrevention{
        Treatment,
        Prevention,
        None
    }





    public Reference(){
    }

}