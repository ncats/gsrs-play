package ix.ginas.models;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import play.db.ebean.Model;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import ix.core.models.Indexable;
import ix.core.models.Keyword;

@Entity
@Table(name="ix_ginas_name")
public class Name extends GinasModel {
    @Column(nullable=false,length=512)
    public String name;
    
    @Column(length=10)
    public String type;
    
    @Indexable(name="Public Domain", facet=true)
    public boolean publicDomain;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_name_domain")
    public List<Keyword> domains = new ArrayList<Keyword>();

    public boolean preferred;
    public Name () {}
}
