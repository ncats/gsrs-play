package ix.ginas.utils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gov.nih.ncats.common.util.CachedSupplier;
import ix.ginas.controllers.v1.CodeFactory;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;
import ix.utils.Tuple;

public class CodeSequentialGenerator extends SequentialNumericIDGenerator<Substance>{
	private final CachedSupplier<AtomicLong> lastNum;
	private String codeSystem;
	private String name;

	public String getCodeSystem() {
		return codeSystem;
	}

	public void setCodeSystem(String codeSystem) {
		this.codeSystem = codeSystem;
	}
	@JsonCreator
	public CodeSequentialGenerator(@JsonProperty("name") String name,
								   @JsonProperty("len") int len,
								   @JsonProperty("suffix") String suffix,
								   @JsonProperty("padding") boolean padding,
								   @JsonProperty("codeSystem") String codeSystem) {
		super(len, suffix, padding);
		this.name = name;
		this.codeSystem=codeSystem;
		this.lastNum = CachedSupplier.runOnce(()->{
			Optional<Tuple<Long,Code>> code=CodeFactory.getHighestValueCode(codeSystem, suffix);

			if(code.isPresent()){
				return new AtomicLong(code.get().k());
			}
			return new AtomicLong(0); // initialized to 0 so when  we first get we return 1.
		});
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public long getNextNumber() {
		return lastNum.getSync().incrementAndGet();
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
		r.addRestrictGroup("protected");
		c.addRestrictGroup("protected");
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
