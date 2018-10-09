package ix.ginas.modelBuilders;

import ix.ginas.models.v1.*;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Created by peryeat on 8/31/18.
 */
public class PolymerSubstanceBuilder extends AbstractSubstanceBuilder<PolymerSubstance, PolymerSubstanceBuilder>{


    public PolymerSubstanceBuilder(Substance copy) {
        super(copy);
        if(copy instanceof  PolymerSubstance){
            Polymer poly = ((PolymerSubstance)copy).polymer;
            if(poly !=null){
                setPolymer(poly);
            }
        }
    }

    @Override
    public Supplier<PolymerSubstance> getSupplier() {
        return () ->{
            PolymerSubstance s = new PolymerSubstance();
            s.polymer = new Polymer();
            return s;
        };
    }

    @Override
    protected Substance.SubstanceClass getSubstanceClass() {
        return Substance.SubstanceClass.polymer;
    }

    @Override
    protected PolymerSubstanceBuilder getThis() {
        return this;
    }

    public PolymerSubstanceBuilder setPolymer(Polymer poly){
        andThen( s ->{
            s.polymer=poly;
        });
        return getThis();
    }

}
