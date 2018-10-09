package ix.ginas.processors;

import com.avaje.ebean.Ebean;
import ix.core.EntityProcessor;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.adapters.InxightTransaction;
import ix.core.util.EntityUtils;
import ix.core.util.SemaphoreCounter;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.SubstanceReference;
import play.db.ebean.Model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Processor to handle both sides of the Relationship.
 * Because GSRS Relationships are actually 2 separate objects,
 * each a member of a different Substance that's related;
 * we have to make sure any change or deletion of one
 * performs a "spooky action at a distance" to the other one.
 * This processor also handles creating the inverse relationship
 * on the other Substance if a user creates a new relationship on a Substance
 * to keep everything in sync.
 */
public class RelationshipProcessor implements EntityProcessor<Relationship>{
	private static final String MENTION = "mention";

	public Model.Finder<UUID, Relationship> finder;

	private static RelationshipProcessor _instance = null;

	//These fields keep track of what UUIDs we are in the middle
	//of processing since several of these actions will trigger
	//other updates/creates/delete calls on this processor for the
	//other side of the relationship and we don't want to get trapped in a cycle.

	private SemaphoreCounter<String> relationshipUuidsBeingWorkedOn = new  SemaphoreCounter<String>();

	public RelationshipProcessor(){
		finder = new Model.Finder(UUID.class, Relationship.class);
		_instance=this;

	}

	/**
	 * This is really "pre-create" only called when new object persisted for 1st time- not updates
	 * @param obj
	 */
	@Override
	public void prePersist(Relationship obj) {
		String uuid = obj.getOrGenerateUUID().toString();
		if(notWorkingOn(uuid)){

			addInverse(obj);
		}
	}

	private boolean notWorkingOn(String uuid){
//		System.out.println("working on list =" + relationshipUuidsBeingWorkedOn);
		if(relationshipUuidsBeingWorkedOn.add(uuid)){
			Optional<InxightTransaction> currentTransaction = InxightTransaction.getCurrentTransaction();
//			System.out.println("current transaction is " + currentTransaction);
			currentTransaction.ifPresent(tx -> tx.addFinallyRun(()-> relationshipUuidsBeingWorkedOn.removeCompletely(uuid)));
			return true;
		}
		return false;
	}

	@Override
	public void postPersist(Relationship obj) throws FailProcessingException {
		relationshipUuidsBeingWorkedOn.removeCompletely(obj.getOrGenerateUUID().toString());
	}

	@Override
	public void postUpdate(Relationship obj) throws FailProcessingException {
		relationshipUuidsBeingWorkedOn.removeCompletely(obj.getOrGenerateUUID().toString());
	}

	private void addInverse(final Relationship thisRelationship){
		if(thisRelationship.isAutomaticInvertible()){
			final Substance thisSubstance=thisRelationship.fetchOwner();
			final Substance otherSubstance=SubstanceFactory.getFullSubstance(thisRelationship.relatedSubstance); //db fetch


			if(otherSubstance ==null){ //probably warn
				return;
			}
			try{
				if(!canCreateInverseFor(thisRelationship, thisSubstance.asSubstanceReference(), otherSubstance)){
//					System.out.println("already exists ?");
					return;
				}
			}catch(Exception e){
				e.printStackTrace();
				return;
			}



			EntityPersistAdapter.performChangeOn(
					otherSubstance,
					s -> {
						Relationship r=createAndAddInvertedRelationship(thisRelationship, thisSubstance.asSubstanceReference(), s, true);

						if (r != null) {
							notWorkingOn(r.getOrGenerateUUID().toString());
							try {
								s.forceUpdate();
//								System.out.println("updated inverse " + r.getOrGenerateUUID());
							}catch(Throwable t){ t.printStackTrace();} finally{
								relationshipUuidsBeingWorkedOn.remove(r.getOrGenerateUUID().toString());
							}
						}else{
							return Optional.empty();
						}
						return Optional.of(s);
					}
			);
		}
	}
	public Relationship createAndAddInvertedRelationship(Relationship obj, SubstanceReference oldSub, Substance newSub){
		return createAndAddInvertedRelationship(obj, oldSub,newSub, false);
	}
	private Relationship createAndAddInvertedRelationship(Relationship obj, SubstanceReference oldSub, Substance newSub, boolean force){
		//doesn't exist yet
		if(obj.isAutomaticInvertible()){
			if(newSub==null){
				//TODO: Look into this
				obj.relatedSubstance.substanceClass=MENTION;
			}else{
				Relationship r = obj.fetchInverseRelationship();
				r.originatorUuid=obj.getOrGenerateUUID().toString();
				r.relatedSubstance=oldSub;
				if(!force) {
					for (Relationship rOld : newSub.relationships) {
						if (r.type.equals(rOld.type) && r.relatedSubstance.isEquivalentTo(rOld.relatedSubstance)) {
							return null;
						}
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
	public boolean canCreateInverseFor(Relationship obj, SubstanceReference oldSub, Substance newSub){
		if(obj.isAutomaticInvertible()){
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

		if(!notWorkingOn(obj.getOrGenerateUUID().toString())){
//			System.out.println("already worked on it!!");
			return;
		}

		//Do we need this?
		System.out.println(EntityUtils.EntityWrapper.of(obj).toFullJsonNode());
		if(obj.isAutomaticInvertible()){
			List<Relationship> rel;
			if(obj.isGenerator()) {
				rel = new ArrayList<>(finder.where().eq("originatorUuid",
						obj.getOrGenerateUUID().toString()).findList());
			}else{
				rel = new ArrayList<>(finder.where().eq("originatorUuid",
						obj.originatorUuid).findList());
			}

			for(Relationship r1 : rel){
				if(r1.getOrGenerateUUID().equals(obj.getOrGenerateUUID())){
					//found ourselves
					continue;
				}
				//we aren't going to delete it!! just update it
				//using the values

				//we make a new one each time because it does some clone stuff
				//and we don't want to reuse reference objects..I don't think
				//there shouldn't be more than 1 anyway unless here's an error
				//so it's not much of a performance hit to do it inside the loop

				Relationship inverse = obj.fetchInverseRelationship();

				r1.setComments(inverse.comments);
				r1.type = new String(inverse.type);
				r1.amount = inverse.amount;
				r1.setReferences(inverse.getReferences());

				if(notWorkingOn(r1.getOrGenerateUUID().toString())) {
					final Substance osub = r1.fetchOwner();
					if (osub != null) {
						EntityPersistAdapter.performChangeOn(osub, osub2 -> {
							r1.forceUpdate();
							osub2.forceUpdate();
							return Optional.of(osub2);
						});
					}
				}
			}
		}
	}


	@Override
	public void preRemove(Relationship obj) {
		if(obj.isGenerator() && obj.isAutomaticInvertible()){

			List<Relationship> rel = finder.where().eq("originatorUuid",
					obj.getOrGenerateUUID().toString()).findList();

			for(final Relationship r1 : rel){
				//The logic here has me scratching my head ... not sure about it.
				//Whenever we use the isGenerator method, I think we might be doing
				//something wrong.
				if(!r1.isGenerator()){
					if(relationshipUuidsBeingWorkedOn.remove(r1.uuid.toString())){
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

			notWorkingOn(obj.uuid.toString());

			for(Relationship r : finder.where().eq("uuid", obj.originatorUuid)
					.findList()) {
				Substance s2 = r.fetchOwner();
				EntityPersistAdapter.performChangeOn(s2, osub2-> {
					r.delete();
					osub2.forceUpdate();
					return Optional.of(osub2);
				});
			}

		}
	}

	public static RelationshipProcessor getInstance(){
		RelationshipProcessor rp = RelationshipProcessor._instance;
		if(rp==null){
			rp=new RelationshipProcessor();
		}
		return rp;
	}

}
