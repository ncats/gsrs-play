package ix.qhts.models;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;
import play.db.ebean.Model;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Value;
import ix.core.models.Indexable;

/**
 * 4-p hill model for concentration response curve
 */
@Entity
@Table(name="ix_qhts_hillmodel")
public class HillModel extends Model {
    static final double ln10 = 2.30258509299404568401;
    
    @Id public Long id;
    public Double ac50;
    public Double hillCoef;
    public Double infAct; // inf activity
    public Double zeroAct;

    @Indexable(name="Curve Class 1", facet=true)
    @Column(length=10)
    public String curveClass1;
    
    @Indexable(name="Curve Class 2", facet=true)
    @Column(length=10)
    public String curveClass2;
    
    @OneToOne(cascade=CascadeType.ALL)
    public Curve crc; // reference concentration response curve

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_qhts_hillmodel_property")
    public List<Value> properties = new ArrayList<Value>();
    
    public HillModel () {}

    public Double getLogAC50 () {
        return Math.log10(ac50 * 1.e-6);
    }

    public double eval (double x) {
        return zeroAct + ((infAct - zeroAct)
                          / (1. + Math.exp(ln10 * hillCoef
                                           * (getLogAC50 () - x))));
    }
}
