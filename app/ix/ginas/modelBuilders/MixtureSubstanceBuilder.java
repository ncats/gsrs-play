package ix.ginas.modelBuilders;

import ix.ginas.models.v1.*;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Created by katzelda on 7/20/18.
 */
public class MixtureSubstanceBuilder extends AbstractSubstanceBuilder<MixtureSubstance, MixtureSubstanceBuilder>{



    @Override
    public Supplier<MixtureSubstance> getSupplier() {
        return () ->{
            MixtureSubstance s = new MixtureSubstance();
            s.mixture = new Mixture();
            return s;
        };
    }

    @Override
    protected Substance.SubstanceClass getSubstanceClass() {
        return Substance.SubstanceClass.mixture;
    }

    @Override
    protected MixtureSubstanceBuilder getThis() {
        return this;
    }

    public MixtureSubstanceBuilder setMixture(Mixture mixture){
        andThen( s ->{
            s.mixture = mixture;
        });
        return getThis();
    }

    public MixtureSubstanceBuilder addComponent(Component c){
        return andThen( s-> {
            s.mixture.components.add(Objects.requireNonNull(c));
        });

    }

    public MixtureSubstanceBuilder addComponents(String type, Substance... refs){
        for(Substance ref : refs) {
            Component c = new Component();
            c.type = type;
            c.substance = SubstanceReference.newReferenceFor(ref);
            addComponent(c);
        }
        return getThis();
    }
}
