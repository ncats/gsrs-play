package ix.ginas.processors;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import ix.core.EntityProcessor;
import ix.core.adapters.EntityPersistAdapter;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.datasource.KewControlledPlantDataSet;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.Substance.SubstanceDefinitionType;
import ix.ginas.models.v1.SubstanceReference;
import play.Logger;
import play.db.ebean.Model;

/**
 * This Substance Processor makes the following changes when a Substance is saved:
 *
 * <ol>
 *     <li>If this Substance is an Alternate Definition: replace the reference to its PRIMARY definition
 *          to make sure it's up to date.</li>
 *      <li>Adds KEW tags if the approval ID is in the list of KEW records as specified in the KEW.json file</li>
 *
 * </ol>
 *
 * If the Substance is newly created/ just inserted, then
 * look for dangling Relationships where previously loaded substances refer to this new substance
 * and if it finds any, add the corresponding inverse relationship.  This is probably
 * only done during partial BATCH loads.
 *
 */
public class SubstanceProcessor implements EntityProcessor<Substance>{

    KewControlledPlantDataSet kewData;

    public Model.Finder<UUID, Relationship> finder;
    public Model.Finder<UUID, Substance> substanceFinder;
    public SubstanceProcessor(){
        try{
            kewData= new KewControlledPlantDataSet("kew.json");
        }catch(Exception e){
            e.printStackTrace();
        }
        finder = new Model.Finder(UUID.class, Relationship.class);
        substanceFinder = new Model.Finder(UUID.class, Substance.class);
        //System.out.println("Made processor");


    }


    @Override
    public void postPersist(Substance obj) {
//        System.out.println("Substance processor post persist for class " + obj.getClass());
    }

    public void addWaitingRelationships(Substance obj){

        List<Relationship> refrel = finder.where().eq("relatedSubstance.refuuid",
                obj.getOrGenerateUUID().toString()).findList();

        for(Relationship r:refrel){
            RelationshipProcessor.getInstance().createAndAddInvertedRelationship(r,
                    r.fetchOwner().asSubstanceReference(),
                    obj);

        }
    }

    @Override
    public void postLoad(Substance obj) {
        //Logic here may be needed at certain times for rebuilding indexes
        //This will require some external information, not yet present
    }

    @Override
    public void postRemove(Substance obj) {
        //Could have logic here to remove things
    }

    @Override
    public void postUpdate(Substance obj) {
        postPersist(obj);
    }

    @Override
    public void prePersist(final Substance s) {
        savingSubstance(s, true);
    }

    private void savingSubstance(final Substance s, boolean newInsert) {



        Logger.debug("Persisting substance:" + s);
        if (s.isAlternativeDefinition()) {

            Logger.debug("It's alternative");
            //If it's alternative, find the primary substance (there should only be 1, but this returns a list anyway)
            List<Substance> realPrimarysubs=SubstanceFactory.getSubstanceWithAlternativeDefinition(s);
            Logger.debug("Got some relationships:" + realPrimarysubs.size());
            Set<String> oldprimary = new HashSet<String>();
            for(Substance pri:realPrimarysubs){
                oldprimary.add(pri.getUuid().toString());
            }


            SubstanceReference sr = s.getPrimaryDefinitionReference();
            if (sr != null) {

                Logger.debug("Enforcing bidirectional relationship");
                //remove old references
                for(final Substance oldPri: realPrimarysubs){
                    if(oldPri ==null){
                        continue;
                    }
                    Logger.debug("Removing stale bidirectional relationships");


                    EntityPersistAdapter.performChangeOn(oldPri, obj->{
                        List<Relationship> related=oldPri.removeAlternativeSubstanceDefinitionRelationship(s);
                        for(Relationship r:related){
                            r.delete();
                        }
                        oldPri.forceUpdate();
                        return Optional.of(obj);
                    }
                            );


                }
                Logger.debug("Expanding reference");
                Substance subPrimary=null;	
                try{
                    subPrimary = SubstanceFactory.getFullSubstance(sr);
                }catch(Exception e){
                    e.printStackTrace();
                }

                if (subPrimary != null) {
                    Logger.debug("Got parent sub, which is:" + subPrimary.getName());
                    if (subPrimary.definitionType == SubstanceDefinitionType.PRIMARY) {

                        Logger.debug("Going to save");

                        EntityPersistAdapter.performChangeOn(subPrimary, obj -> {
                            if (!obj.addAlternativeSubstanceDefinitionRelationship(s)) {
                                Logger.info("Saving alt definition, now has:"
                                        + obj.getAlternativeDefinitionReferences().size());
                            }
                            obj.forceUpdate();
                            return Optional.of(obj);
                        });

                    }
                }

            }else{
                Logger.error("Persist error. Alternative definition has no primary relationship");
            }
        }
        addKewIfPossible(s);
        if(newInsert) {
            //depending on how this substance was created
            //it might have been from copying and pasting old json
            //of an already existing substance
            //which might have the change reason set so force it to be null for new inserts
            s.changeReason=null;

        addWaitingRelationships(s);
    }
    }

    /**
     * Adds/remove Kew tag for controlled kew substances
     * @param s
     */
    public void addKewIfPossible(Substance s){
        if(kewData!=null){
            if(s.approvalID!=null && s.isPrimaryDefinition()){
                if(kewData.contains(s.approvalID)){
                    s.addTagString("KEW");
                }else{
                    if(s.hasTagString("KEW")){
                        s.removeTagString("KEW");
                    }
                }
            }
        }
    }

    @Override
    public void preUpdate(Substance obj) {
        savingSubstance(obj, false);
    }

    @Override
    public void preRemove(Substance obj) {}





}
