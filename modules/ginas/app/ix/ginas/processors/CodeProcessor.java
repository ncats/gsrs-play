package ix.ginas.processors;

import ix.core.EntityProcessor;
import ix.ginas.datasource.CodeSystemMeta;
import ix.ginas.datasource.CodeSystemURLGenerator;
import ix.ginas.models.v1.Code;


public class CodeProcessor implements EntityProcessor<Code>{

	public CodeSystemURLGenerator codeSystemData;
	
	public CodeProcessor(){
		try{
			codeSystemData= new CodeSystemURLGenerator("codeSystem.json");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void generateURL(Code code){
		if(codeSystemData==null)return;
		if(code.url==null || code.url.trim().length()<=0){
			CodeSystemMeta csm=codeSystemData.fetch(code.codeSystem);
			if(csm!=null){
				csm.addURL(code);
			}
		}
	}
	
	@Override
	public void prePersist(Code obj) throws ix.core.EntityProcessor.FailProcessingException {
		generateURL(obj);
	}

	@Override
	public void postPersist(Code obj) throws ix.core.EntityProcessor.FailProcessingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preRemove(Code obj) throws ix.core.EntityProcessor.FailProcessingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postRemove(Code obj) throws ix.core.EntityProcessor.FailProcessingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preUpdate(Code obj) throws ix.core.EntityProcessor.FailProcessingException {
		generateURL(obj);
	}

	@Override
	public void postUpdate(Code obj) throws ix.core.EntityProcessor.FailProcessingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postLoad(Code obj) throws ix.core.EntityProcessor.FailProcessingException {
		// TODO Auto-generated method stub
		
	}

}
