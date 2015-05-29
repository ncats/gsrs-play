package ix.qhts.models;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;
import play.db.ebean.Model;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Value;

@Entity
@Table(name="ix_qhts_replicate")
public class Replicate extends Model {    
    @Id public Long id;
    public Integer replicate; // replicate index

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_qhts_replicate_hill")
    public List<HillModel> models = new ArrayList<HillModel>();
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_qhts_replicate_property")
    public List<Value> properties = new ArrayList<Value>();

    public Replicate () {}
    public Replicate (int replicate) { this.replicate = replicate; }
}
