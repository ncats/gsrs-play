package ix.ginas.utils;



public abstract class SequentialNumericIDGenerator<T> extends AbstractNoDependencyIDGenerator<T, String> implements NamedIdGenerator<T, String> {

	private int len;
	
	public String suffix;
	private boolean padding;
	
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
			return padd(len-suffix.length()-adapt.length(), adapt);
		}
		return adapt + suffix;
	}
	private String padd(int len, String s){
		StringBuilder sb = new StringBuilder(len+ s.length() + suffix.length());
		for(int i=0;i<len;i++){
			sb.append("0");
		}
		sb.append(s).append(suffix);
		return sb.toString();
	}


}