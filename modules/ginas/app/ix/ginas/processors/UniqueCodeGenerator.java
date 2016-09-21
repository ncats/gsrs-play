package ix.ginas.processors;

import java.util.Map;

import ix.core.EntityProcessor;
import ix.core.adapters.EntityPersistAdapter;
import ix.ginas.controllers.v1.ControlledVocabularyFactory;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.CodeSystemVocabularyTerm;
import ix.ginas.models.v1.ControlledVocabulary;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.VocabularyTerm;
import ix.ginas.utils.CodeSequentialGenerator;
import ix.ginas.utils.GinasGlobal;

public class UniqueCodeGenerator implements EntityProcessor<Substance> {
	private CodeSequentialGenerator seqGen=null;
	private String codeSystem;
	public UniqueCodeGenerator(){
		
	}
	
	
	public UniqueCodeGenerator(Map m){
		codeSystem = 				(String)  m.get("codesystem");
        String codeSystemSuffix = 	(String)  m.get("suffix");
        int length = 				(Integer) m.get("length");
        boolean padding = 			(Boolean) m.get("padding");
        if(codeSystem!=null){
        	seqGen=new CodeSequentialGenerator(length,codeSystemSuffix,padding,codeSystem);
        }
        addCodeSystem();
	}
	
	private void addCodeSystem(){
		Runnable r = new Runnable(){

			@Override
			public void run() {
				//System.out.println("Adding code system");
				if(codeSystem!= null){
					ControlledVocabulary cvv = ControlledVocabularyFactory.getControlledVocabulary("CODE_SYSTEM");
					boolean addNew=true;
					for(VocabularyTerm vt1 : cvv.terms){
						if(vt1.value.equals(codeSystem)){
							addNew=false;
							break;
						}
					}
					if(addNew){
						CodeSystemVocabularyTerm vt = new CodeSystemVocabularyTerm();
						vt.display=codeSystem;
						vt.value=codeSystem;
						vt.hidden=true;
						
						//*************************************
						// This causes problems if done first
						// may have ramifications elsewhere
						//*************************************
						//vt.save();
						
						
						cvv.addTerms(vt);
						cvv.update();
						
						//Needed because update doesn't necessarily
						//trigger the update hooks
						
						EntityPersistAdapter.getInstance().reindex(cvv);
						
					}
				}
				//System.out.println("Done adding code system");
			}};
		GinasGlobal.runAfterStart(r);
		
	}
	
	public void generateCodeIfNecessary(Substance s){
		
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
		        	seqGen.addCode(s);
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
	}

	@Override
	public void postPersist(Substance obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preRemove(Substance obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postRemove(Substance obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preUpdate(Substance obj) {
		prePersist(obj);
	}

	@Override
	public void postUpdate(Substance obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postLoad(Substance obj) {
		// TODO Auto-generated method stub
		
	}

}
