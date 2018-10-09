package ix.ginas.modelBuilders;

import ix.ginas.models.v1.*;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Created by peryeat on 8/31/18.
 */
public class StructurallyDiverseSubstanceBuilder extends AbstractSubstanceBuilder<StructurallyDiverseSubstance, StructurallyDiverseSubstanceBuilder>{


    public StructurallyDiverseSubstanceBuilder(Substance copy) {
        super(copy);
        if(copy instanceof  StructurallyDiverseSubstance){
            StructurallyDiverse sd = ((StructurallyDiverseSubstance)copy).structurallyDiverse;
            if(sd !=null){
                setStructurallyDiverse(sd);
            }
        }
    }

    @Override
    public Supplier<StructurallyDiverseSubstance> getSupplier() {
        return () ->{
            StructurallyDiverseSubstance s = new StructurallyDiverseSubstance();
            s.structurallyDiverse = new StructurallyDiverse();
            return s;
        };
    }

    @Override
    protected Substance.SubstanceClass getSubstanceClass() {
        return Substance.SubstanceClass.structurallyDiverse;
    }

    @Override
    protected StructurallyDiverseSubstanceBuilder getThis() {
        return this;
    }

    public StructurallyDiverseSubstanceBuilder setStructurallyDiverse(StructurallyDiverse stdiv){
        andThen( s ->{
            s.structurallyDiverse=stdiv;
        });
        return getThis();
    }

}
