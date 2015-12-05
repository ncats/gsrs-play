package ix.ginas.models.v1;


import ix.ginas.models.GinasSubData;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;


@SuppressWarnings("serial")
@Entity
@Table(name="ix_ginas_disulfide")
public class DisulfideLink extends GinasSubData {
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_disulfide_site")
    public List<Site> sites = new ArrayList<Site>();

    public DisulfideLink () {}
}
