package ix.ginas.modelBuilders;


import ix.ginas.models.v1.*;

import java.util.Arrays;
import java.util.Collections;
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

    @Override
    protected Substance.SubstanceClass getSubstanceClass() {
        return Substance.SubstanceClass.nucleicAcid;
    }

    public NucleicAcidSubstanceBuilder setNucleicAcid(NucleicAcid na){
        return andThen(s ->{ s.setNucleicAcid(na);});
    }

    public NucleicAcidSubstanceBuilder() {
    }
    public NucleicAcidSubstanceBuilder addDnaSubunit(String dna) {
        return setNucleicAcid(dna, "dR");
    }
    public NucleicAcidSubstanceBuilder addRnaSubunit(String dna) {
        return setNucleicAcid(dna, "R");
    }

    private NucleicAcidSubstanceBuilder setNucleicAcid(String dna, String sugarType) {
        NucleicAcid na = new NucleicAcid();
        Subunit seq = new Subunit();

        seq.sequence = dna;
        na.setSubunits(Arrays.asList(seq));


//            Sets all sugars to ribose (should be simpler)
        Sugar sug= new Sugar();
        sug.setSugar(sugarType);
        sug.setSitesShorthand("1_1-1_" + seq.sequence.length());
        na.setSugars(Collections.singletonList(sug));

//            Sets all Linkages to phosphate (should be simpler)
        Linkage lin= new Linkage();
        lin.setLinkage("P");
        lin.setSitesShorthand("1_1-1_" + ( seq.sequence.length() -1));
        na.setLinkages(Collections.singletonList(lin));
        return setNucleicAcid(na);
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
