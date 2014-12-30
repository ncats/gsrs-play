package ix.omim.models;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import play.db.ebean.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import ix.core.models.Publication;

@Entity
@Table(name="ix_omim_phenotype")
public class Phenotype extends Model {
    @Id
    public Long id;
    public Long mimId; // MIM number

    @Column(length=512)
    public String name;
    @Lob
    public String text;
    
    public final Date created = new Date ();
    public Date modified;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_omim_publication")
    public List<Publication> publications = new ArrayList<Publication>();

    public Phenotype () {}
    
    @PrePersist
    @PreUpdate
    public void modified () {
        this.modified = new Date ();
    }
}
