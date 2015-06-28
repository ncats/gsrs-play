package ix.ntd.models;

import javax.persistence.Lob;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sheilstk on 6/25/15.
 */
public class TB extends Disease {
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
        Worms,// Chronic, //(Adult worms)
        Other
    }

    public enum OrganismStrain{
fdsdfsfdsdf,
        fgdfg,
        dsgsg
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

    public enum Symptom{

    }

    public enum Test{

    }


    public Stage stage;
    public OrganismStrain strain;
    public DiseaseLocation diseaseLocation;
    public DiseaseForm diseaseForm;
    public TransmissionMode transmission;
    @Lob
    public String resistanceOrFailures;

    List<Symptom> symptoms = new ArrayList<Symptom>();
    List<Test> labs = new ArrayList<Test>();
    List<Test> imaging = new ArrayList<Test>();

}
