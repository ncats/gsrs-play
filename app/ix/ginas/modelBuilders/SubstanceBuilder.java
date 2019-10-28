package ix.ginas.modelBuilders;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.controllers.EntityFactory;
import ix.core.util.GinasPortalGun;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.MixtureSubstance;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.SpecifiedSubstanceGroup1Substance;
import ix.ginas.models.v1.StructurallyDiverseSubstance;
import ix.ginas.models.v1.Substance;

//public SubstanceBuilder
public class SubstanceBuilder extends AbstractSubstanceBuilder<Substance, SubstanceBuilder>{

	private static final ObjectMapper mapper = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();

	@Override
	protected Substance.SubstanceClass getSubstanceClass() {
		return Substance.SubstanceClass.concept;
	}

	public SubstanceBuilder() {
	}

	public SubstanceBuilder(Substance copy) {
		super(copy);
	}

	@Override
	public Supplier<Substance> getSupplier() {
		return Substance::new;
	}

	@Override
	protected SubstanceBuilder getThis() {
		return this;
	}

	public ChemicalSubstanceBuilder asChemical(){
		return new ChemicalSubstanceBuilder();
	}
	
	public ProteinSubstanceBuilder asProtein(){
		return new ProteinSubstanceBuilder();
	}

	public MixtureSubstanceBuilder asMixture() {
		return new MixtureSubstanceBuilder();
	}

	public NucleicAcidSubstanceBuilder asNucleicAcid(){
		return new NucleicAcidSubstanceBuilder();
	}
    public static <S extends Substance, B extends AbstractSubstanceBuilder<S,B>> B  from(String json) throws IOException{
        return from(mapper.readTree(json));
    }
    public static <S extends Substance, B extends AbstractSubstanceBuilder<S,B>> B  from(File json) throws IOException{
        return from(mapper.readTree(json));
    }
    public static <S extends Substance, B extends AbstractSubstanceBuilder<S,B>> B  from(InputStream json) throws IOException{
        return from(mapper.readTree(json));
    }
	public static <S extends Substance, B extends AbstractSubstanceBuilder<S,B>> B  from(JsonNode json){

		Substance substance = GinasPortalGun.createSubstanceFromJson(json);
		if(substance instanceof ChemicalSubstance){
			return (B) new ChemicalSubstanceBuilder((ChemicalSubstance) substance);
		}
		if(substance instanceof NucleicAcidSubstance){
			return (B) new NucleicAcidSubstanceBuilder((NucleicAcidSubstance) substance);
		}
		if(substance instanceof ProteinSubstance){
			return (B) new ProteinSubstanceBuilder((ProteinSubstance) substance);
		}
		if(substance instanceof MixtureSubstance){
			return (B) new MixtureSubstanceBuilder((MixtureSubstance) substance);
		}
		if(substance instanceof PolymerSubstance){
			return (B) new PolymerSubstanceBuilder((PolymerSubstance) substance);
		}
		if(substance instanceof StructurallyDiverseSubstance){
			return (B) new StructurallyDiverseSubstanceBuilder((StructurallyDiverseSubstance) substance);
		}
		if(substance instanceof SpecifiedSubstanceGroup1Substance){
			return (B) new SpecifiedSubstanceGroup1SubstanceBuilder((SpecifiedSubstanceGroup1Substance) substance);
		}

		return (B) new SubstanceBuilder(substance);
	}


}