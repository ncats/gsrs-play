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
	
	public RelationshipProcessor(){
		finder = new Model.Finder(UUID.class, Relationship.class);
	}
	
	@Override
	public void prePersist(Relationship obj) {
		
		//If this is a brand new relationship, or it's a relationship between the same
		//substance
		if(obj.isGenerator() && obj.isInvertable()){
			SubstanceReference oldSub=obj.fetchOwner().asSubstanceReference();
			Substance newSub=SubstanceFactory.getFullSubstance(obj.relatedSubstance);
			Relationship r = obj.fetchInverseRelationship();
			r.originatorUuid=obj.getOrGenerateUUID().toString();
			r.relatedSubstance=oldSub;
			Reference ref1 = Reference.SYSTEM_GENERATED();
			ref1.citation="Generated from relationship on:'" + oldSub.refPname + "'"; 
			r.addReference(ref1.getOrGenerateUUID().toString());
			
			newSub.relationships.add(r);
			newSub.references.add(ref1);
			//r.save();
			newSub.save();
		}
	}
	
	@Override
	public void preUpdate(Relationship obj) {
		if(obj.isGenerator() && obj.isInvertable()){
			List<Relationship> rel = finder.where().eq("originatorUuid",
					obj.getOrGenerateUUID().toString()).findList();
			for(Relationship r1 : rel){
				if(!r1.isGenerator()){
					System.out.println("Removing:" + r1.uuid);
					r1.delete();
				}
			}
			prePersist(obj);
		}
	}
	
	@Override
	public void postPersist(Relationship obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preRemove(Relationship obj) {
		if(obj.isGenerator() && obj.isInvertable()){
			List<Relationship> rel = finder.where().eq("originatorUuid",
					obj.getOrGenerateUUID().toString()).findList();
			for(Relationship r1 : rel){
				if(!r1.isGenerator()){
					System.out.println("Removing:" + r1.uuid);
					r1.delete();
				}
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
	

}
