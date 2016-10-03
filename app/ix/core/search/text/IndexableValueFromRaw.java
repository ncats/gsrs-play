package ix.core.search.text;

public class IndexableValueFromRaw implements IndexableValue{

	String path;
	String name;
	Object value;
	boolean dynamic=false;
	boolean facet=false;
	
	long[] range = new long[]{};
	double[] drange = new double[]{};
	
	
	
	@Override
	public String path() {
		return path;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Object value() {
		return value;
	}
	
	@Override
	public boolean isDynamicFacet(){
		return this.dynamic;
	}
	
	@Override
	public boolean facet(){
		return this.facet;
	}
	
	@Override
	public long[] ranges(){
		return range;
	}

	
	@Override
	public double[] dranges(){
		return this.drange;
	}
	
	public void setDynamic(){
		this.dynamic=true;
		this.facet=true;
	}
	
	
	public IndexableValueFromRaw dynamic(){
		setDynamic();
		return this;
	}
	
	public IndexableValueFromRaw withRange(long[] range){
		this.range=range;
		return this;
	}
	

	public IndexableValueFromRaw(String name, Object value,String path){
		this.path=path;
		this.name=name;
		this.value=value;
	}

	public IndexableValueFromRaw(String name, Object value) {
		this.name=name;
		this.value=value;
		this.path=name;
	}

	public IndexableValue withRange(double[] ranges) {
		this.drange=ranges;
		return this;
	}
}
