package ix.ginas.processors;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import ix.core.EntityProcessor;
import ix.core.adapters.EntityPersistAdapter;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.SubstanceReference;
import play.db.ebean.Model;

public class RelationshipProcessor implements EntityProcessor<Relationship>{
	private static final String MENTION = "mention";

	public Model.Finder<UUID, Relationship> finder;
	
	private static RelationshipProcessor _instance = null;
	
	private Set<String> deletedUuidsInProgress = Collections.newSetFromMap(new ConcurrentHashMap<>());

	public RelationshipProcessor(){
		finder = new Model.Finder(UUID.class, Relationship.class);
		_instance=this;
	}
	
	@Override
	public void prePersist(Relationship obj) {
		addInverse(obj);
	}
	
	public void addInverse(final Relationship thisRelationship){
		if(thisRelationship.shouldBeInverted()){
			final Substance thisSubstance=thisRelationship.fetchOwner();
			final Substance otherSubstance=SubstanceFactory.getFullSubstance(thisRelationship.relatedSubstance); //db fetch
			
			
			if(otherSubstance ==null){ //probably warn
				return;
			}
			try{
				if(!canCreateInverstFor(thisRelationship, thisSubstance.asSubstanceReference(), otherSubstance)){
					return;
				}
			}catch(Exception e){
				e.printStackTrace();
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
		if(obj.shouldBeInverted()){
			if(newSub==null){
				//TODO: Look into this
				obj.relatedSubstance.substanceClass=MENTION;
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

	/**
	 * Test to see if the given relationship can be inverted and created for the
	 * other substance. Specifically, it tests that the relationship is:
	 * 
	 * <ol>
	 * <li>New</li>
	 * <li>Not otherwise automatically inverted</li>
	 * <li>Invertible</li>
	 * <li>That the inverted relationship doesn't already exist on target substance</li>
	 * </ol>
	 * 
	 * 
	 * @param obj
	 * @param oldSub
	 * @param newSub
	 * @return
	 */
	public boolean canCreateInverstFor(Relationship obj, SubstanceReference oldSub, Substance newSub){
		if(obj.shouldBeInverted()){
			if(newSub!=null){
				Relationship r = obj.fetchInverseRelationship();
				for(Relationship rOld:newSub.relationships){
					if(r.type.equals(rOld.type) && oldSub.refuuid.equals(rOld.relatedSubstance.refuuid)){
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void preUpdate(Relationship obj) {
		
		if(obj.shouldBeInverted()){
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
					if(deletedUuidsInProgress.remove(r1.uuid.toString())){
						continue;
					}
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
			if(obj.isOkToRemove()){
				return;
			}

			deletedUuidsInProgress.add(obj.uuid.toString());

					for(Relationship r : finder.where().eq("uuid", obj.originatorUuid)
														.findList()) {

						Substance s2 = r.fetchOwner();

						EntityPersistAdapter.performChangeOn(s2, osub2-> {
									System.out.println("deleting r from primary " + r);
									r.delete();
									osub2.forceUpdate();
							return Optional.of(osub2);
								});

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
