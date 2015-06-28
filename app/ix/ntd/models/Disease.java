package ix.ntd.models;

import ix.core.models.Indexable;
import play.db.ebean.Model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.util.ArrayList;
import java.util.List;


@Entity
@DiscriminatorValue("DIS")
public class Disease extends Model {
    @Indexable(facet=true)

    public enum DiseaseName{
        XDR_TB,
        Leishmaniasis,
        Lymphatic_filariasis,
        artemesinin_resistant_malaria
    }

    public DiseaseName diseaseName;


public Presentation presentation;

    public List<Treatment> treatments = new ArrayList<Treatment>();

    public Disease(){
    }

}