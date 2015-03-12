package ix.tox21.models;

import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.utils.Global;
import ix.core.models.*;

@Entity
@Table(name="ix_tox21_sample")
public class Sample extends EntityModel {
    @Column(nullable=false,length=15)
    public String toxId;
    
    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_tox21_sample_synonym",
               joinColumns=@JoinColumn(name="ix_tox21_sample_synonym_id",
                                       referencedColumnName="id")
               )
    public List<Keyword> synonyms = new ArrayList<Keyword>();

    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_tox21_sample_property")
    public List<Value> properties = new ArrayList<Value>();

    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_tox21_sample_link")
    public List<XRef> links = new ArrayList<XRef>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_tox21_sample_publication")
    @JsonView(BeanViews.Full.class)
    public List<Publication> publications = new ArrayList<Publication>();

    public Sample () {}
    public String getName () { return toxId; }
    public String getDescription () { return null; }
    public List<Keyword> getSynonyms () { return synonyms; }
    public List<Value> getProperties () { return properties; }
    public List<XRef> getLinks () { return links; }
    public List<Publication> getPublications () { return publications; }
}
