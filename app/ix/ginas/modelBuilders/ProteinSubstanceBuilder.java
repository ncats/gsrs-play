package ix.ginas.modelBuilders;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import ix.core.models.Keyword;
import ix.ginas.models.v1.*;

public class ProteinSubstanceBuilder  extends AbstractSubstanceBuilder<ProteinSubstance, ProteinSubstanceBuilder>{

    @Override
    protected Substance.SubstanceClass getSubstanceClass() {
        return Substance.SubstanceClass.protein;
    }

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

    /**
     * Add a new subunit with the given sequence and
     * add/set all the needed additonal fields like subunit index
     * and adding a public reference etc to pass validation.
     * @param sequence the sequence to use as a new subunit
     *                 can not be null.
     * @return this builder
     * @throws NullPointerException if sequence is null
     */
    public ProteinSubstanceBuilder addSubUnit(String sequence){
        Objects.requireNonNull(sequence);
        return andThen( s->{
            if(s.protein ==null){
                s.protein = new Protein();
            }
            int index = s.protein.getSubunits().size() +1;
            Subunit subunit = new Subunit();
            subunit.sequence = sequence;
            subunit.subunitIndex = index;

            Reference r = AbstractSubstanceBuilder.createNewPublicDomainRef();
            s.protein.addReference(r, s);

            s.protein.getSubunits().add(subunit);


        });
    }
    public ProteinSubstanceBuilder addSubUnit(Subunit subunit){
        Objects.requireNonNull(subunit);
        return andThen( s->{
            if(s.protein ==null){
                s.protein = new Protein();
            }
            s.protein.getSubunits().add(subunit);

        });
    }
}
