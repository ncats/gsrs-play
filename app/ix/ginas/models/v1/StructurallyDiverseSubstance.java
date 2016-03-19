package ix.ginas.models.v1;

import javax.persistence.*;

@Entity
@Inheritance
@DiscriminatorValue("DIV")
public class StructurallyDiverseSubstance extends Substance {
    @OneToOne(cascade=CascadeType.ALL)
    public StructurallyDiverse structurallyDiverse;

    public StructurallyDiverseSubstance () {}
    
    @Override
    public void delete(){
    	super.delete();
    	
    }
}
