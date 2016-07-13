package ix.ginas.processors;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import ix.core.EntityProcessor;
import ix.core.adapters.EntityPersistAdapter;
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
	
	public void addInverse(final Relationship obj){
		if(obj.isGenerator() && obj.isAutomaticInvertable()){
			final SubstanceReference oldSub=obj.fetchOwner().asSubstanceReference();
			final Substance newSub=SubstanceFactory.getFullSubstance(obj.relatedSubstance);
			EntityPersistAdapter.performChange(newSub,new Callable(){
				@Override
				public Object call() throws Exception {
					if(createAndAddInvertedRelationship(obj,oldSub,newSub)!=null){
						newSub.forceUpdate();	
					}
					return null;
				}
			});
			
			
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
			for(final Relationship r1 : rel){
				if(!r1.isGenerator()){
					r1.setOkToRemove();
					System.out.println("Removing:" + r1.uuid);
					final Substance osub=r1.fetchOwner();
					EntityPersistAdapter.performChange(osub,new Callable(){
						@Override
						public Object call() throws Exception {
							EntityPersistAdapter.storeEditForPossibleUpdate(osub);
							r1.delete();
							osub.forceUpdate();
							return null;
						}
					});
					
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
		RelationshipProcessor rp =RelationshipProcessor._instance;
		if(rp==null){
			rp=new RelationshipProcessor(); 
		}
		return rp;
	}

}
