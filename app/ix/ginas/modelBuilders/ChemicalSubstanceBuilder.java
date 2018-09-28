package ix.ginas.modelBuilders;

import java.util.Arrays;
import java.util.function.Supplier;

import ix.ginas.models.v1.*;

public class ChemicalSubstanceBuilder extends AbstractSubstanceBuilder<ChemicalSubstance, ChemicalSubstanceBuilder>{

    @Override
    protected Substance.SubstanceClass getSubstanceClass() {
        return Substance.SubstanceClass.chemical;
    }

    @Override
	protected ChemicalSubstanceBuilder getThis() {
		return this;
	}
	
	@Override
	public Supplier<ChemicalSubstance> getSupplier(){
		return ChemicalSubstance::new;
	}
	
	public ChemicalSubstanceBuilder setStructure(String smiles){
		return andThen(cs->{
			cs.structure=new GinasChemicalStructure();
			cs.structure.molfile=smiles;//not really right, but we know it works
            Reference orAddFirstReference = getOrAddFirstReference(cs);
            cs.structure.addReference( orAddFirstReference,cs);
		});
	}

    public ChemicalSubstanceBuilder() {
    }

    public ChemicalSubstanceBuilder(Substance copy) {
        super(copy);

        ChemicalSubstance cs = (ChemicalSubstance)copy;
        setStructure(cs.structure);
        for(Moiety m : cs.moieties){
            addMoiety(m);
        }
        if(cs.getAtomMaps().length !=0){
            setAtomMap(cs.getAtomMaps());
        }
    }
    public ChemicalSubstanceBuilder setAtomMap(int[] atoms){
        //make defensive copy
        int[] copy = Arrays.copyOf(atoms, atoms.length);
        return andThen( s-> { s.setAtomMaps(copy);});
    }
    public ChemicalSubstanceBuilder addMoiety(Moiety m){
        return andThen( s-> { s.moieties.add(m);});
    }
    public ChemicalSubstanceBuilder setStructure(GinasChemicalStructure structure){
        return andThen(s-> { s.structure = structure;});
    }
}