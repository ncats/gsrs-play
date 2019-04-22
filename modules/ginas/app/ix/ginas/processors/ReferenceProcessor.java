package ix.ginas.processors;

import ix.core.EntityProcessor;
import ix.core.factories.EntityProcessorFactory;
import ix.core.util.CachedSupplier;
import ix.core.util.EntityUtils;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import play.Play;
import play.db.ebean.Model;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by katzelda on 12/2/18.
 */
public class ReferenceProcessor implements EntityProcessor<Reference>{

    //cachec supplier because entity processors might be registered later
    //after this processor is created but by the time we need to call this the first time
    //the set of entity processors should be unchanged the rest of the time this instance is running
    //so we can cache it.
    private CachedSupplier<Set<EntityProcessor>> registeredRelationships = CachedSupplier.of(()->
            EntityProcessorFactory.getInstance(Play.application())
                    .getRegisteredResourcesFor(Relationship.class));
    public ReferenceProcessor(){
    }
    @Override
    public void preUpdate(Reference obj) throws FailProcessingException {
//        System.out.println("updating reference");

        FailProcessingException[] holder = new FailProcessingException[1];
        //Relationships are often have an inverted relationship which
        //will have its own references but which are usually the
        // from an end user perspective, the same
        //but from the object model are 2 separate entities that happen to have
        //the same data.
        //so we need to keep them in sync.  The Relationship processor deals with that
        //but might not get fired if the reference is the only thing on the relationship
        //that changes.
        //so we manually fire the relationship processor on all affected relationships

        for(GinasAccessReferenceControlled referred : obj.getElementsReferencing()){
//            System.out.println("referred = " + referred);
            if(referred instanceof Relationship){
                registeredRelationships.get()
                        .forEach(processor ->{
//                            System.out.println("firing processor " + processor);
                            try{
                                processor.preUpdate(referred);
                            }catch(FailProcessingException e){
                                holder[0] = e;
                            }
                        });

                if(holder[0] !=null){
                    throw holder[0];
                }
            }
        }


    }

    @Override
    public void prePersist(Reference obj) throws FailProcessingException {
        //don't think we need to bother with pre -persist whatever
        //creates this reference should handle propagation and copying
//        System.out.println("prepersist reference");
    }

    @Override
    public void preRemove(Reference obj) throws FailProcessingException {
//        System.out.println("pre remove " + obj);
        //if it's been removed is it still in the getElements refercing ?
        //I don't think so...
        //GinasAccessReferenceControlled only link to the UUID of this reference
        //so removing a reference from that list without removing it from the substance
        //shouldn't fire this processor
        //and the validation shouldn't let a substance remove the reference
        //without also removing all the UUIDs in the corresponding GinasAccessReferenceControlled objects
        //so maybe we don't need this either...
        for(GinasAccessReferenceControlled referred : obj.getElementsReferencing()){
            System.out.println("still referred by " + referred);

        }
    }
}
