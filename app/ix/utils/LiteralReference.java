package ix.utils;


public class LiteralReference<K>{
	public K o;
	public LiteralReference(K o){
		this.o=o;
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
}