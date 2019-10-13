package ix.ginas.processors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;

import ix.core.EntityProcessor;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.adapters.InxightTransaction;
import ix.core.models.Edit;
import ix.core.models.Keyword;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.SemaphoreCounter;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.SubstanceReference;
import ix.utils.Tuple;
import play.db.ebean.Model;

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



	//TODO:
	/*
	 * There are some issues remaining here, specifically the following:
	 *
	 * 1. The relationship inverse / updating code fails if a concept is being upgraded.
	 *    Almost all relationships that were added to the concept record will be removed from the opposite records.
	 *    This appears to only really be an issue if the relationships were pointing toward the concept originally, rather than being stored on the concept to begin with. [should work now]
	 * 2. Access settings are not copied over when a relationship is updated / added. [should work now]
	 * 3. If you change the type of a relationship to a one-way type, it won't do anything with the inverted relationship, which causes inconsistency [should work now]
	 * 4. If you change the related substance of a relationship, it neither deletes the inverse nor adds the new inverse [should work now]
	 * 5. If a relationship gets in an unstable state from any of the above, and is missing an inverse, no changes to that relationship will generate an inverse.
	 *    It would need to be removed and added again. [should work now]
	 *
	 *
	 * We need tests for each of the above.
	 *
	 *
	 *
	 *
	 *
	 */


	private static final String MENTION = "mention";

	public Model.Finder<UUID, Relationship> finder;

	private static RelationshipProcessor _instance = null;

	//These fields keep track of what UUIDs we are in the middle
	//of processing since several of these actions will trigger
	//other updates/creates/delete calls on this processor for the
	//other side of the relationship and we don't want to get trapped in a cycle.

	private SemaphoreCounter<String> relationshipUuidsBeingWorkedOn = new  SemaphoreCounter<String>();

	private SemaphoreCounter<String> relationshipUuidsBeingDeleted = new  SemaphoreCounter<String>();

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
//		System.out.println("Adding a relationship:" + obj.getOrGenerateUUID() + " to :" + obj.relatedSubstance.refPname);
		String uuid = obj.getOrGenerateUUID().toString();
		boolean isBeingDeleted = relationshipUuidsBeingDeleted.contains(uuid);
//		if(isBeingDeleted){
//			System.out.println("And that relationship is being delted already");
//		}else{
//			System.out.println("And that relationship is NOT being deleted");
//		}
		if(notWorkingOn(uuid) || isBeingDeleted){
			addInverse(obj);
		}
//		else{
//			System.out.println("But we're already doing something with that one, so don't trigger anything");
//		}
	}

	private boolean notWorkingOn(String uuid){
		return notWorkingOn(uuid,false);
	}

	private boolean notWorkingOn(String uuid, boolean isRemove){
//		System.out.println("working on list =" + relationshipUuidsBeingWorkedOn);
		if(relationshipUuidsBeingWorkedOn.add(uuid)){
			if(isRemove){
//				System.out.println("Adding delete flag for uuid:" + uuid);
				relationshipUuidsBeingDeleted.add(uuid);
			}
			Optional<InxightTransaction> currentTransaction = InxightTransaction.getCurrentTransaction();
//			System.out.println("current transaction is " + currentTransaction);
			currentTransaction.ifPresent(tx -> tx.addFinallyRun(()-> {
				relationshipUuidsBeingWorkedOn.removeCompletely(uuid);
				relationshipUuidsBeingDeleted.removeCompletely(uuid);
//				System.out.println("Transaction is done for:" + uuid);

			}));
			return true;
		}
		return false;
	}

	@Override
	public void postPersist(Relationship obj) throws FailProcessingException {
		relationshipUuidsBeingWorkedOn.removeCompletely(obj.getOrGenerateUUID().toString());
		relationshipUuidsBeingDeleted.removeCompletely(obj.getOrGenerateUUID().toString());
//		System.out.println("Persist done for:" + obj.uuid.toString());
	}

	@Override
	public void postUpdate(Relationship obj) throws FailProcessingException {
		relationshipUuidsBeingWorkedOn.removeCompletely(obj.getOrGenerateUUID().toString());
		relationshipUuidsBeingDeleted.removeCompletely(obj.getOrGenerateUUID().toString());
//		System.out.println("Update done for:" + obj.uuid.toString());
	}

	@Override
	public void postRemove(Relationship obj) throws FailProcessingException {
		relationshipUuidsBeingWorkedOn.removeCompletely(obj.getOrGenerateUUID().toString());
		relationshipUuidsBeingDeleted.removeCompletely(obj.getOrGenerateUUID().toString());
//		System.out.println("Removal done for:" + obj.uuid.toString());
	}

	private void addInverse(final Relationship thisRelationship){
//		System.out.println("Adding an inverse for:" + thisRelationship.toSimpleString());
		if(thisRelationship.isAutomaticInvertible()){
			final Substance thisSubstance=thisRelationship.fetchOwner();
			final Substance otherSubstance=SubstanceFactory.getFullSubstance(thisRelationship.relatedSubstance); //db fetch


			if(otherSubstance ==null){ //probably warn
//				System.out.println("Related substance for inverse relationship doesn't exist!");
				return;
			}
			try{
				//this still needs to be fixes, since there are cases where a new relationship should be added, but it won't be currently

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

//						System.out.println("Adding directly now");
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
//		else{
//			System.out.println("But it's not invertible");
//		}
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

	private Optional<Relationship> findCurrent(Relationship rr){
		EntityWrapper<Substance> owner = EntityWrapper.of(rr.fetchOwner());
		Optional<Edit> edit =EntityPersistAdapter.getInstance().getEditFor(owner.getKey());

		try{
			if(edit.isPresent()){
				Substance beforeS=edit.map(e->e.oldValue)
						.map(s->{
							try{
								return owner.getEntityInfo().fromJson(s);
							}catch(Exception e){
								e.printStackTrace();
								return null;
							}
						})
						.filter(s->s!=null)
						.get();

				if(beforeS!=null){
					return beforeS.relationships.stream().filter(rel->rel.uuid.equals(rr.uuid)).findFirst()
							.map(rr1->{
								rr1.assignOwner(beforeS);
								return rr1;
							});
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return Optional.empty();
	}

	private Optional<Relationship> getRealInvertedRelationshipToRealRelationship(Relationship obj){
		List<Relationship> rel;
		if(obj.isGenerator()) {
//			System.out.println("Finding relationship that is child of this generator");
			rel = new ArrayList<>(finder.where().eq("originatorUuid",
					obj.getOrGenerateUUID().toString()).findList());
		}else{
//			System.out.println("Finding relationship that parent of this child non-generator");
			rel = new ArrayList<>(finder.where().eq("originatorUuid",
					obj.originatorUuid).findList());
		}

		return rel.stream()
				.filter(rr->!rr.getOrGenerateUUID().equals(obj.getOrGenerateUUID()))
				.findAny();
	}

	private static enum InverseMethod{
		EXPLICIT,
		SAME_TYPE_ISOLATED,
		SAME_TYPE_BEST_MATCH
	}

	public Optional<Tuple<Relationship,InverseMethod>> findRealExplicitOrImplicitInvertedRelationship(Relationship obj1){
		Optional<Relationship> opR=getRealInvertedRelationshipToRealRelationship(obj1);
		if(opR.isPresent())return opR.map(rr->Tuple.of(rr,InverseMethod.EXPLICIT));
		SubstanceReference parentRef=obj1.fetchOwner().asSubstanceReference();
		if(obj1.isAutomaticInvertible()){
//			System.out.println("Looking at old related substance for possible inversions");
			Substance relatedSubstance = SubstanceFactory.getFullSubstance(obj1.relatedSubstance);
			//GSRS-860 sometimes when grabbing substance json from public data
			//and loading it on local system and then making edits without pulling latest version from GSRS
			//we edit/ remove references that were system generated or point to other substances
			//that weren't also loaded so check to make sure we have the related substance
			if(relatedSubstance !=null && relatedSubstance.relationships !=null) {
//					System.out.println("Found old referenced substance:" + relatedSubstance.getName());

				List<Relationship> candidates = relatedSubstance.relationships.stream()
						.filter(r->r.relatedSubstance.isEquivalentTo(parentRef))
						.filter(r -> r.isAutomaticInvertible() && r.fetchInverseRelationship().type.equals(obj1.type))
						.collect(Collectors.toList());

				if (candidates.size() == 1) {

//						System.out.println("Inference successful");
					return Optional.of(Tuple.of(candidates.get(0),InverseMethod.SAME_TYPE_ISOLATED));
				}else if (candidates.size() == 0) {
//						System.out.println("Found no suitable relationships");
					return Optional.empty();
				}else{
//						System.out.println("Found too many possible inverse relationships:" + candidates.size());

					RelationshipHash rex=RelationshipHash.of(obj1);
					Relationship r2=candidates.stream()
							.map(r->Tuple.of(r,RelationshipHash.of(r)))
							.map(Tuple.vmap(hh->hh.matchLevel(rex)))
							.sorted(Comparator.comparing(t->-t.v()))
							.findFirst()
							.map(t->{
//						        	  System.out.println("Best one matches at level:" + t.v());
								return t.k();
							})
							.orElse(null)
							;

					return Optional.of(Tuple.of(r2,InverseMethod.SAME_TYPE_BEST_MATCH));

				}
			}
		}
		return Optional.empty();
	}
	private static class RelationshipHash{
		String[] levels = new String[5];

		public RelationshipHash(Relationship r){
			levels[0]=r.relatedSubstance.refuuid;
			levels[1]=r.type;
			levels[2]=r.qualification + ":" + r.interactionType;
			levels[3]=(r.mediatorSubstance!=null)?r.mediatorSubstance.refuuid:"null";
			levels[4]=(r.amount!=null)?r.amount.toString():"null";
		}

		public int matchLevel(RelationshipHash rhash){
			for(int i=0;i<levels.length;i++){
				if(!rhash.levels[i].equals(this.levels[i])){
					return i-1;
				}
			}
			return levels.length-1;
		}
		static RelationshipHash of(Relationship r){
			return new RelationshipHash(r);
		}
	}


	@Override
	public void preUpdate(Relationship obj) {

//		System.out.println("Going to update:" + obj.uuid);

		if(!notWorkingOn(obj.getOrGenerateUUID().toString())){
//			System.out.println("already worked on it!!");
			return;
		}
		SubstanceReference parentRef = obj.fetchOwner().asSubstanceReference();

		AtomicBoolean forceToBeGenerator = new AtomicBoolean(false);




		Optional<Relationship> before = findCurrent(obj);

		//Do we need this?
//		System.out.println(EntityUtils.EntityWrapper.of(obj).toFullJsonNode());
		if(before.isPresent()){
//			System.out.println("Found the old form of this one:" + before.get().relatedSubstance.refPname);
//			System.out.println("The new one is to:" + obj.relatedSubstance.refPname);


			Relationship beforeR = before.get();

			Optional<Tuple<Relationship,InverseMethod>> beforeInverted = findRealExplicitOrImplicitInvertedRelationship(beforeR);

			if(beforeInverted.isPresent()){
				if(beforeInverted.get().v() != InverseMethod.EXPLICIT){
					//This means it found a good fit, which also means that the originator info should be updated
					//we will mark this as its own originator from now on
					forceToBeGenerator.set(true);
				}
			}
//			else{
//				System.out.println("Inference not successful");
//			}


			boolean addInverse=false;
			if(forceToBeGenerator.get()){
				obj.originatorUuid=obj.getOrGenerateUUID().toString();
			}

			if(beforeInverted.isPresent()){

				Relationship r1a =beforeInverted.get().k();
				Substance r1Owner = r1a.fetchOwner();

				if(!r1a.getOrGenerateUUID().equals(obj.getOrGenerateUUID())){
//					System.out.println("This isn't a self-reference relationship");
					//If it's the same related substance, we update it. If it's different, we need to actually delete the old
					//one and add a new one.
					//TODO
//					if(beforeR==obj){
//						System.out.println("The previous record and this one are literally the same java object");
//					}else{
//						System.out.println("The previous record and this one are different java objects");
//					}

					if(obj.relatedSubstance.isEquivalentTo(beforeR.relatedSubstance) && obj.isAutomaticInvertible() && beforeR.isAutomaticInvertible()){
//						System.out.println("This is an update with the same subject/object");
						//we aren't going to delete it!! just update it
						//using the values



						//we make a new one each time because it does some clone stuff
						//and we don't want to reuse reference objects..I don't think
						//there shouldn't be more than 1 anyway unless there's an error
						//so it's not much of a performance hit to do it inside the loop

						Relationship inverse = obj.fetchInverseRelationship();



						//GSRS-736 completely remove and then re-add references withnew UUID?
						//TODO do this here or a new processor ?

						Substance objOwner = obj.fetchOwner();
						if(r1Owner !=null) {

							if(notWorkingOn(r1a.getOrGenerateUUID().toString())) {
								//							final Substance osub = r1.fetchOwner();
								if (r1Owner != null) {
									EntityPersistAdapter.performChangeOn(r1Owner, osub2 -> {
//										System.out.println("Going to perform real change");
										Relationship r1=osub2.relationships.stream().filter(rr->rr.uuid.equals(r1a.uuid)).findFirst().orElse(null);
										if(r1!=null){

											List<Reference> refsToRemove = new ArrayList<Reference>();

											//TODO: fix this to remove the actual references from the substance
											Set<Keyword> keepRefs= r1.getReferences()
													.stream()
													.map(r->osub2.getReferenceByUUID(r.term))
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
											if(inverse.mediatorSubstance !=null){
												r1.mediatorSubstance = inverse.mediatorSubstance.copyWithNullUUID();
											}


											r1.setReferences(keepRefs);
											r1.setAccess(inverse.getAccess()); //Should take care of access problem


											osub2.references.removeAll(refsToRemove);
											//										for(Reference ref1: refsToRemove){
											//											ref1.delete();
											//										}isn't


											for (Keyword k : obj.getReferences()) {

												Reference ref = objOwner.getReferenceByUUID(k.getValue());
												if("SYSTEM".equals(ref.docType)){
													continue;
												}

//												System.out.println("adding ref" +  ref);
												if(ref!=null){
													try {
														Reference newRef = EntityUtils.EntityWrapper.of(ref).getClone();
														newRef.uuid =null;
														r1.addReference(newRef, osub2);
													} catch (JsonProcessingException e) {
														e.printStackTrace();
													}
												}
											}
											if(forceToBeGenerator.get()){
												r1.originatorUuid=obj.getOrGenerateUUID().toString();
											}
											r1.forceUpdate();
											osub2.forceUpdate();
											return Optional.of(osub2);
										}else{
//											System.err.println("Couldn't find the inverted relationship");
											return Optional.empty();
										}
									});
								}
							}

						}
					}else{ //The related substance changed: delete old inverse, add new one
//						System.out.println("The subject/object are different than before: delete and add");
						if(notWorkingOn(r1a.getOrGenerateUUID().toString())) {
							EntityPersistAdapter.performChangeOn(r1Owner, osub2 -> {
//								System.out.println("Deleting old ...");
								Relationship r1=osub2.relationships.stream().filter(rr->rr.uuid.equals(r1a.uuid)).findFirst().orElse(null);
								if(r1!=null){
									r1.delete();
									osub2.forceUpdate();
									return Optional.of(osub2);
								}else{
									return Optional.empty();
								}
							});
							addInverse=true;
						}

					}
				}
//				else{
//					System.out.println("This IS a self-reference relationship");
//				}
			}else{
				addInverse=true;
			}
			if(addInverse){
				//If we add one explicitly, we should also make it the originator
				obj.originatorUuid=obj.getOrGenerateUUID().toString();
//				System.out.println("Adding new inverse");
				addInverse(obj);
			}
		}
	}


	@Override
	public void preRemove(Relationship obj) {
//		System.out.println("Removing a relationship:" + obj.getOrGenerateUUID() + " to :" + obj.relatedSubstance.refPname);

		Relationship before = findCurrent(obj).orElse(obj);


		if(notWorkingOn(before.getOrGenerateUUID().toString(),true)) {
//			System.out.println("We're not doing anything with it, let's trigger inverse stuff");
			if(before.isAutomaticInvertible()){
//				System.out.println("It's invertible");

				Optional<Tuple<Relationship, InverseMethod>> opInv= findRealExplicitOrImplicitInvertedRelationship(before);

				if(opInv.isPresent()){
					Relationship r1=opInv.get().k();

					//It's a bigger deal to accidentally delete a relationship you're not sure about, so don't do it if
					//there's some ambiguity
					if(!opInv.get().v().equals(InverseMethod.SAME_TYPE_BEST_MATCH)){
//						System.out.println("Found a corresponding relationship inverse");
						if(relationshipUuidsBeingWorkedOn.remove(r1.uuid.toString())){
//							System.out.println("Oh, we're already working on that one, don't do anything");
							relationshipUuidsBeingDeleted.remove(r1.uuid.toString());
						}else{

							//What does this do?
							r1.setOkToRemove();
							final Substance osub=r1.fetchOwner();
							if(osub !=null) {
								EntityPersistAdapter.performChangeOn(osub, osub2->{
//									System.out.println("Okay, going to delete the inverse");

									Relationship rem = null;
									for(Relationship r:osub2.relationships){
										if(r.uuid.equals(r1.uuid)){
											rem=r;
										}
									}
									if(rem!=null){
										rem.delete();
										osub2.relationships.remove(rem);
									}
									osub2.forceUpdate();
//									System.out.println("Inverse should be deleted now");
									return Optional.of(osub2);
								});
							}
//							else{
//								System.out.println("Can't find the owner of that relationship");
//							}
						}
					}
//					else{
//						System.out.println("There is ambiguity on the relationship inverse. Probably shouldn't delete.");
//					}
				}
//				else{
//					System.out.println("There's no inverted relationship that can be found");
//				}
			}
//			else{
//				System.out.println("It's not invertible anyway, no need to propagate anything");
//			}

		}
//		else{
//			System.out.println("But we're already doing something with that one, so don't trigger anything");
//		}
	}

	public static RelationshipProcessor getInstance(){
		RelationshipProcessor rp = RelationshipProcessor._instance;
		if(rp==null){
			rp=new RelationshipProcessor();
		}
		return rp;
	}

}
