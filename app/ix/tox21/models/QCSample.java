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
@Inheritance
@DiscriminatorValue("QCSMPL")
public class QCSample extends Sample {
    public enum Grade {
        A ("MW Confirmed, Purity > 90%", "success"),
        Ac ("<p align='left'>CAUTION, Low Concentration"
            +"<p align='left'>Concentration 5-30% of expected value", "danger"),
        B ("MW Confirmed, Purity 75-90%", "success"),
        Bc ("<p align='left'>CAUTION, Low Concentration"
            +"<p align='left'>Concentration 5-30% of expected value", "danger"),
        C ("MW Confirmed, Purity 50-75%", "success"),
        Cc ("<p align='left'>CAUTION, Low Concentration"
            +"<p align='left'>Concentration 5-30% of expected value", "danger"),
        D ("CAUTION, Purity <50%", "warning"),
        F ("<p align='left'>CAUTION, Incorrect MW"
           +"<p align='left'>Biological Activity Unreliable", "danger"),
        Fc ("<p align='left'>CAUTION, Very Low Concentration"
            +"<p align='left'>Concentration <5% of expected value"
            +"<p align='left'>Biological Activity Unreliable", "danger"),
        Fns ("<p align='left'>CAUTION, No Sample Detected"
             +"<p align='left'>Biological Activity Unreliable", "danger"),
        I ("<p align='left'>ISOMERS"
           +"<p align='left'>Two or more isomers detected", "info"),
        M ("<p align='left'>DEFINED MIXTURE"
           +"<p align='left'>Two or more components", "info"),
        ND ("<p align='left'>Not Determined"
            +"<p align='left'>Analytical analysis is in progress", "default"),
        W ("Sample Withdrawn", "warning"),
        Z ("MW Confirmed, No Purity Info", "warning");
        
        public final String desc;
        public final String label;
        Grade (String desc, String label) {
            this.desc = desc;
            this.label = label;
        }
    }

    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String comments;

    @Indexable(facet=true,name="QC Grade")
    public Grade grade;

    public QCSample () {
    }
    public QCSample (String name) {
        super (name);
    }
}
