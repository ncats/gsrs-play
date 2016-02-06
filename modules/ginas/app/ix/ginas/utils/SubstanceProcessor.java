package ix.ginas.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ix.core.EntityProcessor;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.Substance.SubstanceDefinitionType;
import ix.ginas.models.v1.SubstanceReference;
import play.Logger;

public class SubstanceProcessor implements EntityProcessor<Substance>{

	
	private static final String INTERNAL_CODE_SYSTEM = "BDNUM";
	@Override
	public void postPersist(Substance obj) {
		//System.out.println("Post Persist Hook on:" + obj);
		//All of the logic for processing goes here
		
		//Grab BDNUM
		String internalCode=null;
		for(Code c: obj.codes){
			if(c.codeSystem.equals(INTERNAL_CODE_SYSTEM)){
				if(internalCode!=null){
					throw new IllegalStateException("Duplicate internal code");
				}
				internalCode=c.code;
			}
		}
		if(internalCode==null){
			for(Reference r: obj.references){
				if(r.docType.equals(INTERNAL_CODE_SYSTEM)){
					if(internalCode!=null){
						throw new IllegalStateException("Duplicate internal code");
					}
					internalCode=r.citation;
				}
			}
		}
		
		
		//Here is an example, which simply prints out the names
		for(Name n:obj.names){
			//System.out.println(obj.getApprovalIDDisplay() + "\t" + internalCode +"\t" + n.getName() + "\t" + n.type);
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
		System.out.println("Persisting substance:" + s);
		if (s.definitionType == SubstanceDefinitionType.ALTERNATIVE) {
			List<Substance> realPrimarysubs=SubstanceFactory.getSubstanceWithAlternativeDefinition(s);
			Set<String> oldprimary = new HashSet<String>();
			for(Substance pri:realPrimarysubs){
				
				oldprimary.add(pri.uuid.toString());
				
			}
			
			
			SubstanceReference sr = s.getPrimaryDefinitionReference();
			if (sr != null) {
				if(!oldprimary.contains(sr.refuuid)){
					//remove old references
					for(Substance oldPri: realPrimarysubs){
						oldPri.removeAlternativeSubstanceDefinitionRelationship(s);
					}
				
					Substance subPrimary = SubstanceFactory.getFullSubstance(sr);
					if (subPrimary != null) {
						if (subPrimary.definitionType == SubstanceDefinitionType.PRIMARY) {
							if(!subPrimary.addAlternativeSubstanceDefinitionRelationship(s)){
								
								Logger.info("Saving alt definition, now has:" + subPrimary.getAlternativeDefinitionReferences().size());
								subPrimary.save();
							}
						}
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
