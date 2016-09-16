package ix.test.builder;

import java.util.function.Supplier;

import ix.ginas.models.v1.Protein;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.Substance;

public class ProteinSubstanceBuilder  extends AbstractSubstanceBuilder<ProteinSubstance, ProteinSubstanceBuilder>{


    @Override
    protected ProteinSubstanceBuilder getThis() {
        return this;
    }

    @Override
    public Supplier<ProteinSubstance> getSupplier(){
        return ProteinSubstance::new;
    }

    public ProteinSubstanceBuilder setProtein(Protein prot){
        return andThen(s ->{ s.setProtein(prot);});
    }

    public ProteinSubstanceBuilder() {
    }

    public ProteinSubstanceBuilder(Substance copy) {
        super(copy);
        if(copy instanceof  ProteinSubstance){
            Protein prot = ((ProteinSubstance)copy).protein;
            if(prot !=null){
                setProtein(prot);
            }
        }
    }
}
