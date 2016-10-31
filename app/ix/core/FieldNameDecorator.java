package ix.core;

public interface FieldNameDecorator {
	public String getDisplayName(String field);
	
	
	/**
	 * Generate a FieldNameDecorator that will fall back to the
	 * provided FieldNameDecorator if this one returns null.
	 * @param or
	 * @return
	 */
	default FieldNameDecorator orIfNull(FieldNameDecorator or){
		return (f)->{
			String ret=this.getDisplayName(f);
			if(ret!=null)return ret;
			return or.getDisplayName(f);
		};
	}
}
