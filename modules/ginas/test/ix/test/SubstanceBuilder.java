package ix.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.controllers.EntityFactory;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.GinasChemicalStructure;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;

public class SubstanceBuilder{
	List<String> names = new ArrayList<String>();
	List<Reference> refs = new ArrayList<Reference>();
	
	Function<Void,Substance> _sup = (f->new Substance());
	
	public SubstanceBuilder(){}
	
	public SubstanceBuilder withName(String name){
		this.names.add(name);
		return this;
	}
	public SubstanceBuilder withDefaultReference(){
		Reference r=Reference.SYSTEM_GENERATED();
    	r.addTag(Reference.PUBLIC_DOMAIN_REF);
    	r.publicDomain=true;
    	refs.add(r);
		return this;
	}
	public SubstanceBuilder asChemical(String structure){
		_sup = (f->new ChemicalSubstance());
		_sup.andThen(s->{
					GinasChemicalStructure cs= new GinasChemicalStructure();
					cs.molfile=structure;
					((ChemicalSubstance)s).structure=cs;
					return s;
				});
		return this;
	}
	
	public Substance build(){
		Substance s= _sup.apply(null);
		Optional<Reference> dref=refs.stream()
				.peek(r->s.references.add(r))
				.findAny();
		
		this.names.stream()
			.map(n->new Name(n))
			.forEach(n->{
				n.addReference(dref.orElseGet(()->Reference.SYSTEM_ASSUMED()));
				s.names.add(n);
			});
		
		return s;
	}
	public JsonNode buildJson(){
		return EntityFactory.EntityMapper.FULL_ENTITY_MAPPER().valueToTree(this.build());
	}
	
	
	
	
}