package ix.ginas.utils;

import ix.ginas.controllers.v1.CodeFactory;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;

public class CodeSequentialGenerator extends SequentialNumericIDGenerator{
	private long lastNum=1;
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
	public long getNextNumber() {
		if(!fetched){
			String code=CodeFactory.getMostRecentCode(codeSystem, "%" + suffix);
			if(code!=null){
				lastNum=Long.parseLong(code.replace(suffix, ""));
				lastNum++;
			}
			fetched=true;
		}
		return lastNum++;
	}
	
	public Code getCode(){
		Code c = new Code();
		c.codeSystem=this.codeSystem;
		c.code=this.generateID();
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
	
	

}
