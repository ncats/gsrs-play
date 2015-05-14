package ix.srs.models;

import java.util.*;
import play.db.ebean.*;
import javax.persistence.*;

import ix.core.models.IxModel;
import ix.core.models.Indexable;
import ix.core.models.Structure;

@Entity
@Table(name="ix_srs_ingredient")
public class Ingredient extends IxModel {
    @Indexable(facet=true,suggest=true,name="Ingredient")
    public String name;

    @OneToOne(cascade=CascadeType.ALL)
    public Structure structure;
    public Ingredient () {}
}
