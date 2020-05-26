package ix.ginas.utils;


public abstract class SequentialNumericIDGenerator extends AbstractNoDependencyIDGenerator<Object, String> {

	private int len;
	
	public String suffix;
	private boolean padding = true;
	
	public SequentialNumericIDGenerator(int len, String suffix, boolean padding){
		this.len=len;
		this.suffix=suffix;
		this.padding=padding;
		
	}
	
	public abstract long getNextNumber();
	
	public String adapt(long num){
		return num+"";
	}
	
	public synchronized String generateID() {
		long next=getNextNumber();
		String adapt=adapt(next);
		if(padding){
			String pad = getPadding(len-suffix.length()-adapt.length());
			adapt=pad+adapt;
		}
		return adapt + suffix;
	}
	private String getPadding(int len){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<len;i++){
			sb.append("0");
		}
		return sb.toString();
	}


}