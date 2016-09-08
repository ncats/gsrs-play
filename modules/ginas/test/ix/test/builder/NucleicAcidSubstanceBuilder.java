package ix.test.builder;


import ix.ginas.models.v1.NucleicAcid;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.Substance;

import java.util.function.Supplier;

/**
 * Created by katzelda on 9/2/16.
 */
public class NucleicAcidSubstanceBuilder extends AbstractSubstanceBuilder<NucleicAcidSubstance, NucleicAcidSubstanceBuilder>{


    @Override
    protected NucleicAcidSubstanceBuilder getThis() {
        return this;
    }

    @Override
    public Supplier<NucleicAcidSubstance> getSupplier(){
        return NucleicAcidSubstance::new;
    }

    public NucleicAcidSubstanceBuilder setNucleicAcid(NucleicAcid na){
        return andThen(s ->{ s.setNucleicAcid(na);});
    }

    public NucleicAcidSubstanceBuilder() {
    }

    public NucleicAcidSubstanceBuilder(Substance copy) {
        super(copy);
        if(copy instanceof  NucleicAcidSubstance){
            NucleicAcid na = ((NucleicAcidSubstance)copy).nucleicAcid;
            if(na !=null){
                setNucleicAcid(na);
            }
        }
    }
}
