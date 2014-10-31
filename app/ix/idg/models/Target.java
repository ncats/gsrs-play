package ix.idg.models;

import ix.core.models.Indexable;
import javax.persistence.*;

@javax.persistence.Entity
@DiscriminatorValue("TAR")
public class Target extends Entity {
    @Column(length=128)
    @Indexable(facet=true,name="Target Family")
    public String family;

    @Column(length=10)
    @Indexable(facet=true,name="IDG Classification")
    public String idgClass;

    public Target () {}
}
