package ix.ginas.utils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import ix.ginas.controllers.v1.CodeFactory;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;
import ix.utils.Tuple;

public class CodeSequentialGenerator extends SequentialNumericIDGenerator{

	private long lastNum= 1L;
	private boolean fetched=false;
	private String codeSystem;
	
	public String getCodeSystem() {
		return codeSystem;
	}

	public void setCodeSystem(String codeSystem) {
		this.codeSystem = codeSystem;
	}

	public CodeSequentialGenerator(int len, String suffix, boolean padding, String codeSystem) {
		super(len, suffix, padding);
		this.codeSystem=codeSystem;
	}

	
	@Override
	public synchronized long getNextNumber() {
		if(!fetched){
			Optional<Tuple<Long,Code>> code=CodeFactory.getHighestValueCode(codeSystem, suffix);
			if(code.isPresent()){
				lastNum = code.get().k();
			}
			fetched=true;
		}
		return lastNum++;
	}
	
	public Code getCode(){
		Code c = new Code();
		c.codeSystem=this.codeSystem;
		c.code=this.generateID();
		c.type="PRIMARY";
		return c;
	}
	public Code addCode(Substance s){
		Code c=getCode();
		s.codes.add(c);
		Reference r = new Reference();
		r.docType="SYSTEM";
		r.citation="System Generated Code";
		c.addReference(r, s);
		return c;
	}

	@Override
	public boolean isValidId(String id) {
		if(id.endsWith(id)){
			return true;
		}
		return false;
	}
	
	

}
