package ix.ginas.utils;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.PrePersist;

import ix.core.EntityProcessor;
import ix.core.chem.Chem;
import ix.core.plugins.SequenceIndexerPlugin;
import ix.core.plugins.StructureIndexerPlugin;
import ix.ginas.controllers.v1.SubstanceFactory;
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

public class SubstanceProcessor implements EntityProcessor<Substance>{
	public static StructureIndexer _strucIndexer =
            Play.application().plugin(StructureIndexerPlugin.class).getIndexer();
    
	
	private static final String INTERNAL_CODE_SYSTEM = "BDNUM";
	@Override
	public void postPersist(Substance obj) {
			   //System.out.print(System.currentTimeMillis() + "\t");
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
	
	public void generateCodeIfNecessary(Substance s){
		CodeSequentialGenerator seqGen = Validation.getCodeGenerator();
		
		if(seqGen!=null && s.isPrimaryDefinition()){
	        boolean hasCode = false;
	        for(Code c:s.codes){
	        	if(c.codeSystem.equals(seqGen.getCodeSystem())){
	        		hasCode=true;
	        		break;
	        	}
	        }
	        if(!hasCode){
	        	try{
		        	Code c=seqGen.addCode(s);
		        	//System.out.println("Generating new code:" + c.code);
	        	}catch(Exception e){
	        		e.printStackTrace();
	        	}
	        }
        }
	}
	@Override
	public void prePersist(Substance s) {
		
		generateCodeIfNecessary(s);
		
		Logger.debug("Persisting substance:" + s);
		if (s.isAlternativeDefinition()) {
			
			Logger.debug("It's alternative");
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
						oldPri.save();
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
							subPrimary.save();
						}
					}
				
			}else{
				Logger.error("Persist error. Alternative definition has no primary relationship");
			}
		}
		//System.out.print("pp\t" + System.currentTimeMillis() + "\t");
	}
	@Override
	public void preUpdate(Substance obj) {
		prePersist(obj);
	}
	
	@Override
	public void preRemove(Substance obj) {}
	

	
	

}
