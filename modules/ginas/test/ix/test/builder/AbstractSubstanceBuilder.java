package ix.test.builder;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.controllers.EntityFactory;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;

public abstract class AbstractSubstanceBuilder<S extends Substance, T extends AbstractSubstanceBuilder<S,T>>{
	
	Function<S, S> andThen = (f->f);
	
	public abstract Supplier<S> getSupplier();

	protected abstract T getThis();
	
	public T andThen(Function<S, S> fun){
		andThen = andThen.andThen(fun);
		return getThis();
	}

    public T andThen(Consumer<S> fun){
        andThen = andThen.andThen(s ->{ fun.accept(s); return s;});
        return getThis();
    }
	
	public Supplier<S> asSupplier(){
		return (()->afterCreate().apply(getSupplier().get()));
	}
	
	public Function<S, S> afterCreate(){
		return andThen;
	}
	
	public T addName(String name){
		return andThen(s->{
			Name n=new Name(name);
			n.addReference(getOrAddFirstReference(s));
			s.names.add(n);
		});
	}
	
	public T addCode(String codeSystem, String code){
		return andThen(s->{
			Code c=new Code(codeSystem,code);
			c.addReference(getOrAddFirstReference(s));
			s.codes.add(c);
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
	
	public S build(){
		return afterCreate().apply(getSupplier().get());
	}
	
	public JsonNode buildJson(){
		return EntityFactory.EntityMapper.FULL_ENTITY_MAPPER().valueToTree(this.build());
	}
	
	public void buildJsonAnd(Consumer<JsonNode> c){
		c.accept(buildJson());
	}
	
	
}