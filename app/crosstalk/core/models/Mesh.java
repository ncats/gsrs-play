package crosstalk.core.models;

import javax.persistence.*;

@Entity
@Table(name="ct_core_mesh")
public class Mesh extends Value {
    public boolean majorTopic;

    @Indexable(taxonomy=true, name="MeSH")
    @Column(length=1024)
    public String term;

    public Mesh () {}
    public Mesh (boolean majorTopic) {
        this.majorTopic = majorTopic;
    }
    public Mesh (String term) {
        this.term = term;
    }
}
