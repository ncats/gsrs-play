package crosstalk.core.models;

import javax.persistence.*;

@Entity
@Table(name="ct_core_mesh")
public class Mesh extends Keyword {
    public boolean majorTopic;

    public Mesh () {}
    public Mesh (boolean majorTopic) {
        this.majorTopic = majorTopic;
    }
    public Mesh (String term) {
        super (term);
    }
}
