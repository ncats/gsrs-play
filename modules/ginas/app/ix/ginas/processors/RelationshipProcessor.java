package ix.ginas.processors;

import java.util.List;
import java.util.UUID;

import ix.core.EntityProcessor;
import ix.core.models.Group;
import ix.ginas.controllers.v1.ControlledVocabularyFactory;
import play.db.ebean.Model;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.ControlledVocabulary;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.SubstanceReference;
import ix.ginas.models.v1.VocabularyTerm;
import play.Logger;

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
	
	public void addInverse(Relationship obj){
		if(obj.isGenerator() && obj.isAutomaticInvertable()){
			SubstanceReference oldSub=obj.fetchOwner().asSubstanceReference();
			Substance newSub=SubstanceFactory.getFullSubstance(obj.relatedSubstance);
			if(createAndAddInvertedRelationship(obj,oldSub,newSub)!=null){
				newSub.forceUpdate();	
			}
			
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
					if(r.type.equals(r.type) && r.relatedSubstance.refuuid.equals(rOld.relatedSubstance.refuuid)){
						return null;
					}
				}
				
				
				Reference ref1 = Reference.SYSTEM_GENERATED();
				ref1.citation="Generated from relationship on:'" + oldSub.refPname + "'"; 
				r.addReference(ref1.getOrGenerateUUID().toString());
				
				newSub.relationships.add(r);
				newSub.references.add(ref1);
				//r.save();
				//newSub.save();
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
	public void postPersist(Relationship obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preRemove(Relationship obj) {
		if(obj.isGenerator() && obj.isAutomaticInvertable()){
			List<Relationship> rel = finder.where().eq("originatorUuid",
					obj.getOrGenerateUUID().toString()).findList();
			for(Relationship r1 : rel){
				if(!r1.isGenerator()){
					r1.setOkToRemove();
					System.out.println("Removing:" + r1.uuid);
					r1.delete();
				}
			}
		}
		if(!obj.isGenerator()){
			if(!obj.isOkToRemove()){
				throw new IllegalStateException("This relationship cannot be deleted. The primary relationship must be deleted instead.");
			}
		}
	}

	@Override
	public void postRemove(Relationship obj) {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public void postUpdate(Relationship obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postLoad(Relationship obj) {
		// TODO Auto-generated method stub
		
	}
	
	public static RelationshipProcessor getInstance(){
		return RelationshipProcessor._instance;
	}

}
