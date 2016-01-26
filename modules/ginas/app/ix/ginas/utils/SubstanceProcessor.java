package ix.ginas.utils;

import ix.core.EntityProcessor;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;

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
	public void prePersist(Substance obj) {}
	@Override
	public void preRemove(Substance obj) {}
	@Override
	public void preUpdate(Substance obj) {}

	

	

}
