package ix.idg.models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

import ix.core.models.Indexable;
import ix.core.models.Value;
import ix.core.models.Keyword;
import ix.core.models.Publication;
import ix.core.models.Predicate;

@javax.persistence.Entity
@Table(name="ix_idg_entity")
@Inheritance
@DiscriminatorValue("ENT")
public class Entity extends Model {
    @Id
    public Long id;

    @Column(length=1024)
    @Indexable(facet=true,suggest=true,name="Entity")
    public String name;

    @Lob
    public String description;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_entity_synonym")
    public List<Keyword> synonyms = new ArrayList<Keyword>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_entity_property")
    public List<Value> properties = new ArrayList<Value>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_entity_annotation")
    public List<Value> annotations = new ArrayList<Value>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_entity_publication")
    public List<Publication> publications = new ArrayList<Publication>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_entity_predicate")
    public List<Predicate> predicates = new ArrayList<Predicate>();

    public Entity () {}
}
