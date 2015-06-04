package ix.qhts.models;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;
import play.db.ebean.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.core.models.Value;

/**
 * Concentration response curve
 */
@Entity
@Table(name="ix_qhts_curve")
public class Curve extends Model {
    @Id public Long id;

    @OneToOne(cascade=CascadeType.ALL)
    public Data conc;
    
    @OneToOne(cascade=CascadeType.ALL)
    public Data response;

    public Curve () {}
    public Curve (Data conc, Data response) {
        this.conc = conc;
        this.response = response;
    }
}
