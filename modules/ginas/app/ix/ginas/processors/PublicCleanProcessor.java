package ix.ginas.processors;

import ix.core.EntityProcessor;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;

public class PublicCleanProcessor implements EntityProcessor<Substance>{

	@Override
	public void prePersist(Substance obj) throws ix.core.EntityProcessor.FailProcessingException {
		preFlightFormat(obj);
	}
	@Override
	public void preUpdate(Substance obj) throws ix.core.EntityProcessor.FailProcessingException {
		preFlightFormat(obj);
	}
	
	public void preFlightFormat(Substance obj){
		for(Reference r:obj.references){
			if(r.citation!=null && r.citation.contains(":<SRS_LEGACY_DATA>")){
				r.citation=r.citation.substring(0,r.citation.indexOf(":<SRS_LEGACY_DATA>"));
			}
		}
	}

	@Override
	public void postPersist(Substance obj) throws ix.core.EntityProcessor.FailProcessingException {
		
	}

	@Override
	public void preRemove(Substance obj) throws ix.core.EntityProcessor.FailProcessingException {
		
	}

	@Override
	public void postRemove(Substance obj) throws ix.core.EntityProcessor.FailProcessingException {
		
	}

	

	@Override
	public void postUpdate(Substance obj) throws ix.core.EntityProcessor.FailProcessingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postLoad(Substance obj) throws ix.core.EntityProcessor.FailProcessingException {
		// TODO Auto-generated method stub
		
	}

}
