package ix.test.builder;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.controllers.EntityFactory;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;

public abstract class AbstractSubstanceBuilder<K extends Substance>{
	
	Function<K,K> andThen = (f->f);
	
	public abstract Supplier<K> getSupplier();
	
	
	public AbstractSubstanceBuilder<K> andThen(Function<K,K> fun){
		andThen = andThen.andThen(fun);
		return this;
	}
	
	public Supplier<K> asSupplier(){
		return (()->afterCreate().apply(getSupplier().get()));
	}
	
	public Function<K,K> afterCreate(){
		return andThen;
	}
	
	public AbstractSubstanceBuilder<K> addName(String name){
		return andThen(s->{
			Name n=new Name(name);
			n.addReference(getOrAddFirstReference(s));
			s.names.add(n);
			return s;
		});
	}
	
	//Helper internal thing
	public static Reference getOrAddFirstReference(Substance s){
		if(s.references.size()>0){
			return s.references.get(0);
		}else{
			Reference rr= Reference.SYSTEM_GENERATED();
			rr.publicDomain=true;
			rr.addTag(Reference.PUBLIC_DOMAIN_REF);
			s.references.add(rr);
			return rr;
		}
	}
	
	public K build(){
		return afterCreate().apply(getSupplier().get());
	}
	
	public JsonNode buildJson(){
		return EntityFactory.EntityMapper.FULL_ENTITY_MAPPER().valueToTree(this.build());
	}
	
	public void buildJsonAnd(Consumer<JsonNode> c){
		c.accept(buildJson());
	}
	
	
}