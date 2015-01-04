package ix.idg.models;

import java.util.List;
import java.util.ArrayList;

import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.core.models.BeanViews;
import ix.utils.Global;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@DiscriminatorValue("TAR")
public class Target extends EntityModel {
    @JsonView(BeanViews.Full.class)
    @OneToOne
    public Keyword organism;

    @Column(length=128)
    @Indexable(facet=true,name="IDG Target Family")
    public String idgFamily;

    @Column(length=10)
    @Indexable(facet=true,name="IDG Classification")
    public String idgClass;

    public Target () {}

    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_organism")
    public String getJsonOrganism () {
	return Global.getRef(organism);
    }
}
