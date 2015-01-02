package ix.idg.models;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

@Entity
@DiscriminatorValue("GEN")
public class Gene extends EntityModel {
    public Gene () {}
}
