package ix.test.builder;

import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.controllers.EntityFactory;
import ix.core.models.Keyword;
import ix.core.models.Principal;
import ix.ginas.models.v1.*;

import javax.management.relation.Relation;

public abstract class AbstractSubstanceBuilder<S extends Substance, T extends AbstractSubstanceBuilder<S,T>>{
	
	Function<S, S> andThen = (f->f);
	
	public abstract Supplier<S> getSupplier();

	protected abstract T getThis();


    public AbstractSubstanceBuilder(){
    }

    public AbstractSubstanceBuilder(Substance copy){

        UUID uuid = copy.getUuid();
        if(uuid !=null){
            setUUID(uuid);
        }

        for(Name name: copy.names){
            addName(name);
        }

        for(Code code : copy.codes){
            addCode(code);
        }

        for(Reference r : copy.references){
            addReference(r);
        }
        for(Note n : copy.notes){
            addNote(n);
        }

        for(Property p : copy.properties){
            addProperty(p);
        }
        for(Relationship r : copy.relationships){
            addRelationship(r);
        }

        for(Keyword k : copy.tags){
            addKeyword(k);
        }

        setDefinition(copy.definitionType, copy.definitionLevel);
        Substance.SubstanceClass substanceClass = copy.substanceClass;
        if(substanceClass !=null){
            setSubstanceClass(substanceClass);
        }

        setStatus(copy.status);

        setVersion(Integer.parseInt(copy.version));

        if(copy.approvalID !=null){
            setApproval(copy.approvedBy, copy.approved, copy.approvalID);
        }

        if(copy.changeReason !=null){
            setChangeReason(copy.changeReason);
        }

        if(copy.modifications !=null){
            setModifications(copy.modifications);
        }
    }

    private T setModifications(Modifications modifications) {
        return andThen( s->{
            s.modifications = modifications;
        });
    }

    private T setChangeReason(String changeReason) {
        return andThen( s->{
            s.changeReason = changeReason;
        });
    }

    public T setApproval(Principal approvedBy, Date approved, String approvalID) {
        return andThen(s ->{
           s.approvalID = approvalID;
            s.approved = approved;
            s.approvedBy = approvedBy;
        });
    }

    public T setVersion(int version){
        return andThen( s->{ s.version = Integer.toString(version);});
    }
    public T setStatus(String status){
        return andThen( s->{ s.status = status;});
    }
    public T setDefinition(Substance.SubstanceDefinitionType type, Substance.SubstanceDefinitionLevel level) {
        return andThen(s -> {
            s.definitionType = type;
            s.definitionLevel = level;
        });
    }
    public T setSubstanceClass(Substance.SubstanceClass c) {
        return andThen(s -> {s.substanceClass = c;});
    }
    public T addNote(Note n) {
        return andThen(s -> {s.notes.add(n);});
    }

    public T addReference(Reference r) {
        return andThen(s -> {s.references.add(r);});
    }

    public T addRelationship(Relationship r) {
        return andThen(s -> {s.relationships.add(r);});
    }

    public T addKeyword(Keyword k) {
        return andThen(s -> {s.tags.add(k);});
    }


    public T addProperty(Property p) {
        return andThen(s -> {s.properties.add(p);});
    }
    public T addName(Name name) {
        return andThen(s -> {s.names.add(name);});
    }

    public T addCode(Code code) {
        return andThen(s -> {s.codes.add(code);});
    }

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


    public T setUUID(UUID uuid) {
       return andThen(s -> {s.setUuid(uuid);});
    }
	
}