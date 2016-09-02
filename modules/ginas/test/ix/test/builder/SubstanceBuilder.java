package ix.test.builder;

import java.util.function.Supplier;

import ix.ginas.models.v1.Substance;

//public SubstanceBuilder
public class SubstanceBuilder extends AbstractSubstanceBuilder<Substance>{
	@Override
	public Supplier<Substance> getSupplier() {
		return ()->new Substance();
	}
	
	public ChemicalSubstanceBuilder asChemical(){
		return new ChemicalSubstanceBuilder(this);
	}
}