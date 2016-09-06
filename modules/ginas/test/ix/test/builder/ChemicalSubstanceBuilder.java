package ix.test.builder;

import java.util.function.Supplier;

import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.GinasChemicalStructure;
import ix.ginas.models.v1.Substance;

public class ChemicalSubstanceBuilder extends AbstractSubstanceBuilder<ChemicalSubstance, ChemicalSubstanceBuilder>{


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
			cs.structure.molfile=smiles;
			cs.references.add(getOrAddFirstReference(cs));
		});
	}
}