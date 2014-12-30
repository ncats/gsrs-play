package ix.idg.models;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("DIS")
public class Disease extends EntityModel {
    public Disease () {}
    public Disease (String name) {
	this.name = name;
    }
}
