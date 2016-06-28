package ix.ginas.processors;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.PrePersist;

import ix.core.EntityProcessor;
import ix.core.chem.Chem;
import ix.core.plugins.SequenceIndexerPlugin;
import ix.core.plugins.StructureIndexerPlugin;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.datasource.KewControlledPlantDataSet;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Protein;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.Substance.SubstanceDefinitionType;
import ix.seqaln.SequenceIndexer;
import ix.ginas.models.v1.SubstanceReference;
import ix.ginas.models.v1.Subunit;
import ix.ginas.utils.validation.Validation;
import play.Logger;
import play.Play;
import tripod.chem.indexer.StructureIndexer;
import play.db.ebean.Model;

public class SubstanceProcessor implements EntityProcessor<Substance>{
	public static StructureIndexer _strucIndexer =
            Play.application().plugin(StructureIndexerPlugin.class).getIndexer();
	KewControlledPlantDataSet kewData;
	
	public Model.Finder<UUID, Relationship> finder;
	
	public SubstanceProcessor(){
		try{
			kewData= new KewControlledPlantDataSet("kew.json");
		}catch(Exception e){
			e.printStackTrace();
		}
		finder = new Model.Finder(UUID.class, Relationship.class);
		//System.out.println("Made processor");
	}
	
	
	@Override
	public void postPersist(Substance obj) {
			   //System.out.print(System.currentTimeMillis() + "\t");
		
//		if(changed){
//			obj.save();
//		}
	}
	
	public void addWaitingRelationships(Substance obj){
		List<Relationship> refrel = finder.where().eq("relatedSubstance.refuuid",
				obj.getOrGenerateUUID().toString()).findList();
		boolean changed=false;
		for(Relationship r:refrel){
			
			Relationship inv=RelationshipProcessor.getInstance().createAndAddInvertedRelationship(r,r.fetchOwner().asSubstanceReference(),obj);
			if(inv!=null){
				changed=true;
			}
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
	public void prePersist(Substance s) {
		
		
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
					for(Substance oldPri: realPrimarysubs){
						Logger.debug("Removing stale bidirectional relationships");
						List<Relationship> related=oldPri.removeAlternativeSubstanceDefinitionRelationship(s);
						for(Relationship r:related){
							r.delete();
						}
						oldPri.forceUpdate();
					}
					Logger.debug("Expanding reference");
					Substance subPrimary=null;	
					try{
						subPrimary = SubstanceFactory.getFullSubstance(sr);
					}catch(Exception e){
						e.printStackTrace();
					}
					Logger.debug("Got parent sub, which is:" + subPrimary.getName());
					if (subPrimary != null) {
						if (subPrimary.definitionType == SubstanceDefinitionType.PRIMARY) {
							Logger.debug("Going to save");
							if(!subPrimary.addAlternativeSubstanceDefinitionRelationship(s)){
								Logger.info("Saving alt definition, now has:" + subPrimary.getAlternativeDefinitionReferences().size());
							}
							subPrimary.forceUpdate();
						}
					}
				
			}else{
				Logger.error("Persist error. Alternative definition has no primary relationship");
			}
		}
		addKewIfPossible(s);
		addWaitingRelationships(s);
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
		prePersist(obj);
	}
	
	@Override
	public void preRemove(Substance obj) {}
	

	
	

}
