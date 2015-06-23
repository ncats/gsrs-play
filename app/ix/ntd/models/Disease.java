package ix.ntd.models;

import ix.core.models.Indexable;
import play.db.ebean.Model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;


@Entity
@DiscriminatorValue("DIS")
public class Disease extends Model {
    @Indexable(facet=true)
    public String diseaseName;

    public enum Stage{
        WHO_Category_I, //(small early lesion)
        WHO_Category_II, //(No ulcerative and ulcerative plaque and edematous forms)
        WHO_Category_III, // (Large ulcerative lesions (>5 cm in diameter))
        Preulcerative,
        Ulcerative,
        Acute,
        Chronic,
        Stage1, //(No CNS Involvement)
        Stage2, //(CNS Involvement)
        AcuteMicrofilaraemia,
        // Chronic, //(Adult worms)
        Other
    }

    public enum OrganismStrain{

    }

    //DO WE NEED THIS CATEGORY???
    public enum DiseaseLocation{
        Cardiologic,
        Gastrointestinal,
        Nervous_System,
        Other
    }

    public enum DiseaseForm {

    }

    public enum TransmissionMode{

    }

    public String ResistanceOrFailures;



    public List<Treatment> treatments = new ArrayList<Treatment>();

    public Disease(){
    }
}