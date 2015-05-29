package ix.qhts.models;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;
import play.db.ebean.Model;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name="ix_qhts_activity")
public class Activity extends Model {
    @Id public Long id;

    @OneToOne(cascade=CascadeType.ALL)
    @Column(nullable=false)
    public Sample sample;

    @OneToOne(cascade=CascadeType.ALL)
    @Column(nullable=false)
    public Assay assay;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_qhts_activity_replicate")
    public List<Replicate> replicates = new ArrayList<Replicate>();

    public Activity () {}
    public Activity (Assay assay) {
        this.assay = assay;
    }
    public Activity (Sample sample) {
        this.sample = sample;
    }
}
