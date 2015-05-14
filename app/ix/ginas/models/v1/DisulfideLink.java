package ix.ginas.models.v1;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.Ginas;


@Entity
@Table(name="ix_ginas_disulfide")
public class DisulfideLink extends Ginas {
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_disulfide_site")
    public List<Site> sites = new ArrayList<Site>();

    public DisulfideLink () {}
}
