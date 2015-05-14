package ix.srs.models;

import java.util.*;
import play.db.ebean.*;
import javax.persistence.*;

import ix.core.models.IxModel;
import ix.core.models.Indexable;

@Entity
@Table(name="ix_srs_product")
public class Product extends IxModel {
    @Indexable(facet=true,suggest=true,name="Product")
    public String name;
    public Product () {}
}
