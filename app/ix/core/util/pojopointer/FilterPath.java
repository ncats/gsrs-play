package ix.core.util.pojopointer;

public class FilterPath extends LambdaPath{
	private static final String VALUE_EQUALS = ":";
	//String filter;
	private PojoPointer field;
	private String value;

	//TODO: Should be a lucene thing?
	public FilterPath(final String filter) {

		final String[] fsplit=filter.split(FilterPath.VALUE_EQUALS);
		if(fsplit.length!=2){
			throw new IllegalStateException("Filters must specify one field, and one value");
		}
		setField(PojoPointer.fromURIPath(fsplit[0]));
		setValue(fsplit[1]);
	}

	public PojoPointer getField() {
		return this.field;
	}

	public PojoPointer getFieldPath(){
		return getField();
	}
	public String getFieldValue(){
		return getValue();
	}

	public String getValue() {
		return this.value;
	}

	public void setField(final PojoPointer field) {
		this.field = field;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	@Override
	protected String thisURIPath() {
		return "(" + getField().toURIpath() + FilterPath.VALUE_EQUALS + getValue()  + ")";
	}
}