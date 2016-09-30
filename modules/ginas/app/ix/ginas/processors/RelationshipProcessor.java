package ix.ginas.processors;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ix.core.EntityProcessor;
import ix.core.adapters.EntityPersistAdapter;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.SubstanceReference;
import play.db.ebean.Model;

public class RelationshipProcessor implements EntityProcessor<Relationship>{
	public Model.Finder<UUID, Relationship> finder;
	
	private static RelationshipProcessor _instance = null;
	
	public RelationshipProcessor(){
		finder = new Model.Finder(UUID.class, Relationship.class);
		_instance=this;
	}
	
	@Override
	public void prePersist(Relationship obj) {
		addInverse(obj);
	}
	
	public void addInverse(final Relationship thisRelationship){
		if(thisRelationship.isGenerator() && thisRelationship.isAutomaticInvertable()){
			final Substance thisSubstance=thisRelationship.fetchOwner();
			final Substance otherSubstance=SubstanceFactory.getFullSubstance(thisRelationship.relatedSubstance); //db fetch
			
			
			if(otherSubstance ==null){ //probably warn
				return;
			}
			
			Relationship preChangeReference=createAndAddInvertedRelationship(thisRelationship, thisSubstance.asSubstanceReference(), otherSubstance);
			
			if(preChangeReference==null){
				return;
			}
			
			
			EntityPersistAdapter.performChangeOn(
					otherSubstance,
					s -> {
						Relationship r=createAndAddInvertedRelationship(thisRelationship, thisSubstance.asSubstanceReference(), s);
						if (r != null) {
							s.forceUpdate();
						}else{
							return Optional.empty();
						}
						return Optional.of(s);
					}
			);
		}
	}
	
	public Relationship createAndAddInvertedRelationship(Relationship obj, SubstanceReference oldSub, Substance newSub){
		//doesn't exist yet
		if(obj.isGenerator() && obj.isAutomaticInvertable()){
			if(newSub==null){
				//TODO: Look into this
				obj.relatedSubstance.substanceClass="mention";
			}else{
				Relationship r = obj.fetchInverseRelationship();
				r.originatorUuid=obj.getOrGenerateUUID().toString();
				r.relatedSubstance=oldSub;
				for(Relationship rOld:newSub.relationships){
					if(r.type.equals(rOld.type) && r.relatedSubstance.refuuid.equals(rOld.relatedSubstance.refuuid)){
						//System.out.println("Inverted already exists?");
						return null;
					}
				}
				Reference ref1 = Reference.SYSTEM_GENERATED();
				ref1.citation="Generated from relationship on:'" + oldSub.refPname + "'"; 
				r.addReference(ref1.getOrGenerateUUID().toString());
				
				newSub.relationships.add(r);
				newSub.references.add(ref1);
				return r;
			}
		}
		return null;
	}
	
	@Override
	public void preUpdate(Relationship obj) {
		
		if(obj.isGenerator() && obj.isAutomaticInvertable()){
			List<Relationship> rel = finder.where().eq("originatorUuid",
					obj.getOrGenerateUUID().toString()).findList();
			for(Relationship r1 : rel){
				if(!r1.isGenerator()){
					r1.setOkToRemove();
					r1.delete();
				}
			}
			addInverse(obj);
		}
	}
	
	@Override
	public void preRemove(Relationship obj) {
		if(obj.isGenerator() && obj.isAutomaticInvertable()){
			List<Relationship> rel = finder.where().eq("originatorUuid",
					obj.getOrGenerateUUID().toString()).findList();
			for(final Relationship r1 : rel){
				if(!r1.isGenerator()){
					r1.setOkToRemove();
					final Substance osub=r1.fetchOwner();
					if(osub !=null) {
						EntityPersistAdapter.performChangeOn(osub, osub2->{
							r1.delete(); //Does this actually work? Not sure
							osub2.forceUpdate();
							return Optional.of(osub2);
						}); 
					}
					
				}
			}
		}
		if(!obj.isGenerator()){
			if(!obj.isOkToRemove()){
				throw new IllegalStateException("This relationship cannot be deleted. The primary relationship must be deleted instead.");
			}
		}
	}

	public static RelationshipProcessor getInstance(){
		RelationshipProcessor rp =RelationshipProcessor._instance;
		if(rp==null){
			rp=new RelationshipProcessor(); 
		}
		return rp;
	}

}
