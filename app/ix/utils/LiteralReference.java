package ix.utils;


public class LiteralReference<K>{
	private K o;
	public LiteralReference(K o){
		this.o=o;
	}
	
	public K get(){
		return o;
	}
	@Override
	public int hashCode(){
		return this.o.hashCode();
	}
	@Override
	public boolean equals(Object oref){
		if(oref==null)return false;
		if(oref instanceof LiteralReference){
			LiteralReference<?> or=(LiteralReference<?>)oref;
			return (this.o == or.o);
		}
		return false;
	}
	public static <K> LiteralReference<K> of(K k) {
		return new LiteralReference<K>(k);
	}
	
	public String toString(){
		return "Ref to:" + o.toString();
	}
}