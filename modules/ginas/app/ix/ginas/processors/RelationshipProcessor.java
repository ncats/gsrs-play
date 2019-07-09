package ix.ginas.processors;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.core.JsonProcessingException;
import ix.core.EntityProcessor;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.adapters.InxightTransaction;
import ix.core.models.Keyword;
import ix.core.util.EntityUtils;
import ix.core.util.SemaphoreCounter;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.SubstanceReference;
import ix.utils.Tuple;
import play.db.ebean.Model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

				r.addReference(ref1, newSub);
				newSub.relationships.add(r);
//				newSub.references.add(ref1);

				//GSRS-736 copy over references
				//with new UUIDs

				for(Keyword kw : obj.getReferences()){
					Reference origRef =obj.fetchOwner().getReferenceByUUID(kw.getValue());
					try {
						Reference newRef = EntityUtils.EntityWrapper.of(origRef).getClone();
						newRef.uuid =null; //blank out UUID soit generates a new one on save
						r.addReference(newRef, newSub);
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
				}
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
//		System.out.println(EntityUtils.EntityWrapper.of(obj).toFullJsonNode());
		if(obj.isAutomaticInvertible()){
			List<Relationship> rel;

			if(obj.isGenerator()) {
				rel = new ArrayList<>(finder.where().eq("originatorUuid",
						obj.getOrGenerateUUID().toString()).findList());
			}else{
				rel = new ArrayList<>(finder.where().eq("originatorUuid",
						obj.originatorUuid).findList());
			}

			//This isn't complete, but handles the most common incomplete migration / old style
			//handling issues that happen from inverted relationships that don't use the originatorUUID
			if(rel.size()<2){
				Substance relatedSubstance = SubstanceFactory.getFullSubstance(obj.relatedSubstance);
				//GSRS-860 sometimes when grabbing substance json from public data
				//and loading it on local system and then making edits without pulling latest version from GSRS
				//we edit/ remove references that were system generated or point to other substances
				//that weren't also loaded so check to make sure we have the related substance
				if(relatedSubstance !=null && relatedSubstance.relationships !=null) {
				List<Relationship> candidates = relatedSubstance.relationships.stream()
								.filter(r->r.isAutomaticInvertible() && r.fetchInverseRelationship().isEquivalentBaseRelationship(obj))
								.collect(Collectors.toList());
				if(candidates.size()==1){
					rel.add(candidates.get(0));
				}
			}
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
				//there shouldn't be more than 1 anyway unless there's an error
				//so it's not much of a performance hit to do it inside the loop

				Relationship inverse = obj.fetchInverseRelationship();

				r1.setComments(inverse.comments);
				r1.type = new String(inverse.type);
				r1.amount = inverse.amount;

				//GSRS-684 and GSRS-730 copy over qualification and interactionType
				if(inverse.qualification !=null){
					//new String so ebean sees it's a new object
					//just in case...
					r1.qualification = new String(inverse.qualification);
				}
				if(inverse.interactionType !=null){
					//new String so ebean sees it's a new object
					//just in case...
					r1.interactionType = new String(inverse.interactionType);
				}

				//GSRS-736 completely remove and then re-add references withnew UUID?
				//TODO do this here or a new processor ?
				Substance r1Owner = r1.fetchOwner();
				Substance objOwner = obj.fetchOwner();
				if(r1Owner !=null) {

					List<Reference> refsToRemove = new ArrayList<Reference>();

					//TODO: fix this to remove the actual references from the substance
					Set<Keyword> keepRefs= r1.getReferences()
											 .stream()
										     .map(r->r1Owner.getReferenceByUUID(r.term))
										     .map(r->Tuple.of("SYSTEM".equals(r.docType),r))
										     .filter(t->{
										    	 if(!t.k()){
										    		 Reference toRemove=t.v();
										    		 long dependencies=toRemove.getElementsReferencing()
										    		 		 .stream()
										    		 		 .map(elm->EntityWrapper.of(elm))
										    		 		 .filter(ew->!r1.uuid.equals(ew.getId().orElse(null)))
										    		 		 .count();
										    		 if(dependencies<=0){
										    			 refsToRemove.add(toRemove);
										    		 }
										    	 }
										    	 return t.k();
										     })
										     .map(t->t.v())
											 .map(ref->ref.asKeyword())
											 .collect(Collectors.toSet());


					r1.setReferences(keepRefs);


					r1Owner.references.removeAll(refsToRemove);
					for(Reference ref1: refsToRemove){
						ref1.delete();
					}


					for (Keyword k : obj.getReferences()) {

						Reference ref = objOwner.getReferenceByUUID(k.getValue());
						if("SYSTEM".equals(ref.docType)){
							continue;
						}

						System.out.println("adding ref" +  ref);
						if(ref!=null){
							try {
								Reference newRef = EntityUtils.EntityWrapper.of(ref).getClone();
								newRef.uuid =null;
								r1.addReference(newRef, r1Owner);
							} catch (JsonProcessingException e) {
								e.printStackTrace();
							}
						}
					}
				}
//				r1.setReferences(new LinkedHashSet<>());
//				for(Keyword k : inverse.getReferences()){
////					Keyword copy = new Keyword(k.label, k.term);
//					r1.addReference(k.term);
//				}
//				r1.setReferences(inverse.getReferences());

				if(notWorkingOn(r1.getOrGenerateUUID().toString())) {
//					final Substance osub = r1.fetchOwner();
					if (r1Owner != null) {
						EntityPersistAdapter.performChangeOn(r1Owner, osub2 -> {
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
