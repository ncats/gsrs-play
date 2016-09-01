package ix.test.builder;

import java.util.function.Supplier;

import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.GinasChemicalStructure;
import ix.ginas.models.v1.Substance;

public class ChemicalSubstanceBuilder extends AbstractSubstanceBuilder<ChemicalSubstance>{
	public ChemicalSubstanceBuilder(AbstractSubstanceBuilder<Substance> sb){
		this.andThen(c->(ChemicalSubstance)sb.afterCreate().apply(c));
	}
	
	@Override
	public Supplier<ChemicalSubstance> getSupplier(){
		return (()->new ChemicalSubstance());
	}
	
	public ChemicalSubstanceBuilder setStructure(String smiles){
		this.andThen(cs->{
			cs.structure=new GinasChemicalStructure();
			cs.structure.smiles=smiles;
			cs.references.add(getOrAddFirstReference(cs));
			return cs;
		});
		return this;
	}
}