package ix.ginas.processors;

import ix.core.EntityProcessor;
import ix.core.util.ConfigHelper;
import ix.ginas.datasource.CodeSystemMeta;
import ix.ginas.datasource.DefaultCodeSystemUrlGenerator;
import ix.ginas.models.v1.Code;
import play.Play;

import java.util.Optional;


public class CodeProcessor implements EntityProcessor<Code>{

	public CodeSystemUrlGenerator codeSystemData;
	
	public CodeProcessor(){


		try{
			String key = "ix.codeSystemUrlGenerator.class";
			String classname = Play.application().configuration().getString(key);
			if(classname ==null){
				System.out.println("config =\n" + Play.application().configuration().asMap());
				throw new IllegalStateException("could not find " + key + " in config file");
			}
			Class<?> cls =  Class.forName(classname);
			codeSystemData = (CodeSystemUrlGenerator) ConfigHelper.readFromJson("ix.codeSystemUrlGenerator.json", cls);

		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	
	public void generateURL(Code code){
		if(codeSystemData==null){
			return;
		}
		if(code.url==null || code.url.trim().isEmpty()){
			Optional<String> csm=codeSystemData.generateUrlFor(code);
			if(csm.isPresent()){
				code.url = csm.get();
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
