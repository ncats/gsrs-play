package ix.idg.models;

import ix.core.models.Indexable;
import javax.persistence.*;

@Entity
@DiscriminatorValue("TAR")
public class Target extends EntityModel {
    @Column(length=128)
    @Indexable(facet=true,name="IDG Target Family")
    public String idgFamily;

    @Column(length=10)
    @Indexable(facet=true,name="IDG Classification")
    public String idgClass;

    public Target () {}
}
