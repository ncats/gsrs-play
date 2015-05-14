package ix.srs.models;

import java.util.*;
import play.db.ebean.*;
import javax.persistence.*;

import ix.core.models.IxModel;
import ix.core.models.Indexable;

@Entity
@Table(name="ix_srs_application")
public class Application extends IxModel {
    public String title;
    
    @Indexable(facet=true,suggest=true,name="Application Type")
    public String apptype; // application type

    @Indexable(facet=true,suggest=true,name="Application Number")
    public String appnumber; // application number

    @OneToOne(cascade=CascadeType.ALL)
    @Basic(fetch=FetchType.EAGER)
    public Product product;
    
    @OneToOne(cascade=CascadeType.ALL)
    @Basic(fetch=FetchType.EAGER)
    public Ingredient ingredient;
    
    @Column(length=9)
    @Indexable(facet=true,suggest=true,name="BDNUM")
    public String bdnum;

    // other columns can be added here...
    
    public Application () {}
}
