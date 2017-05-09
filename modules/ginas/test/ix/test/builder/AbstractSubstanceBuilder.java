package ix.test.builder;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.controllers.EntityFactory;
import ix.core.models.Group;
import ix.core.models.Keyword;
import ix.core.models.Principal;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Modifications;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Note;
import ix.ginas.models.v1.Property;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import ix.test.server.GinasTestServer;

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
        
        
        if(copy.lastEdited !=null){
        	setLastEditedDate(copy.lastEdited);
        }
        if(copy.created !=null){
        	setCreatedDate(copy.created);
        }
        
        if(copy.createdBy !=null){
        	setCreatedBy(copy.createdBy);
        }
        if(copy.lastEditedBy !=null){
        	setLastEditedBy(copy.lastEditedBy);
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

    public T setCreatedBy(Principal p){
        return andThen( s->{s.setCreatedBy(p);});
    }

    public T setLastEditedBy(GinasTestServer.User user){
        return setLastEditedBy(user.asPrincipal());
    }
    public T setLastEditedBy(Principal p){
        return andThen( s->{s.setLastEditedBy(p);});
    }
    
    public T setCreatedDate(Date d){
        return andThen( s->{s.setCreated(d);});
    }
    
    public T setLastEditedDate(Date d){
        return andThen( s->{s.setLastEdited(d);});
    }
    
    public T addReflexiveActiveMoietyRelationship(){
    	return andThen( s->{ 
    		Relationship r = new Relationship();
    		r.type=Relationship.ACTIVE_MOIETY_RELATIONSHIP_TYPE;
    		r.relatedSubstance=s.asSubstanceReference();
    		s.relationships.add(r);
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
        return andThen(s -> {
            s.names =  addToNewList(s.names, name);
        });
    }

    private <T> List<T> addToNewList(List<T> oldList, T newElement){
        List<T> newList = new ArrayList<>(oldList);
        newList.add(newElement);
        return newList;
    }

    public T addCode(Code code) {
        return andThen(s ->{
            if(code.getReferences().isEmpty()) {
                code.addReference(getOrAddFirstReference(s));
            }
            s.codes.add(code);
        });
    }

    public T andThen(Function<S, S> fun){
		andThen = andThen.andThen(fun);
		return getThis();
	}

    public T andThen(Consumer<S> fun){
        andThen = andThen.andThen(s ->{ fun.accept(s); return s;});
        return getThis();
    }
    
    public T andThenMutate(Consumer<S> fun){
        andThen = andThen.andThen(s ->{ fun.accept(s); return s;});
        return getThis();
    }
	
	public Supplier<S> asSupplier(){
		return (()->afterCreate().apply(getSupplier().get()));
	}
	
	public Function<S, S> afterCreate(){
		return andThen;
	}

    public T addName(String name, Set<Group> access){
       return createAndAddBasicName(name, n-> n.setAccess(access));
    }


    private Name createName(Substance s, String name){
        Name n=new Name(name);
        n.addLanguage("en");
        n.addReference(getOrAddFirstReference(s));
        return n;
    }
    private T createAndAddBasicName(String name){
        return createAndAddBasicName(name, null);
    }
    private T createAndAddBasicName(String name, Consumer<Name> additionalNameOpperations){
        return andThen(s->{


            Name n = createName(s, name);
            if(additionalNameOpperations !=null){
                additionalNameOpperations.accept(n);
            }
            s.names =  addToNewList(s.names, n);
        });
    }
	public T addName(String name){
        return createAndAddBasicName(name);
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
       return andThen(s -> {s.uuid=uuid;});
    }

    public T setName(String name) {
       return andThen(s -> {s.names = new ArrayList<>();}).addName(name);

    }

    public T removeUUID(){
        return andThen( s-> {s.uuid =null;});
    }

    public T generateNewUUID(){
        return andThen( s-> {
            s.uuid =null;
            s.getOrGenerateUUID();});
    }
}