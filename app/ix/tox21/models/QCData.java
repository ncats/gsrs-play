package ix.tox21.models;

import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import play.db.ebean.Model;
import ix.utils.Global;
import ix.core.models.*;

@Entity
@Table(name="ix_tox21_qcdata")
public class QCData extends Model {
    public enum Grade {
        A ("MW Confirmed, Purity > 90%"),
        Ac ("CAUTION, Low Concentration\n"
            +"Concentration 5-30% of expected value"),
        B ("MW Confirmed, Purity 75-90%"),
        Bc ("CAUTION, Low Concentration\n"
            +"Concentration 5-30% of expected value"),
        C ("MW Confirmed, Purity 50-75%"),
        Cc ("CAUTION, Low Concentration\n"
            +"Concentration 5-30% of expected value"),
        D ("CAUTION, Purity <50%"),
        F ("CAUTION, Incorrect MW\n"
           +"Biological Activity Unreliable"),
        Fc ("CAUTION, Very Low Concentration\n"
            +"Concentration <5% of expected value\n"
            +"Biological Activity Unreliable"),
        Fns ("CAUTION, No Sample Detected\n"
             +"Biological Activity Unreliable"),
        I ("ISOMERS\n"
           +"Two or more isomers detected"),
        M ("DEFINED MIXTURE\n"
           +"Two or more components"),
        ND ("Not Determined\n"
            +"Analytical analysis is in progress"),
        W ("Sample Withdrawn"),
        Z ("MW Confirmed, No Purity Info");
        
        public final String desc;
        Grade (String desc) {
            this.desc = desc;
        }
    }
    
    @Id
    public Long id;
    @OneToOne
    public Sample sample;
    public Grade grade;

    public QCData () {
    }
}
