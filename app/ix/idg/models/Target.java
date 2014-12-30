package ix.idg.models;

import java.util.List;
import java.util.ArrayList;

import ix.core.models.Indexable;
import ix.core.models.Keyword;
import javax.persistence.*;

@Entity
@DiscriminatorValue("TAR")
public class Target extends EntityModel {
    @OneToOne
    public Keyword organism;
        
    @Column(length=128)
    @Indexable(facet=true,name="IDG Target Family")
    public String idgFamily;

    @Column(length=10)
    @Indexable(facet=true,name="IDG Classification")
    public String idgClass;

    public Target () {}
}
