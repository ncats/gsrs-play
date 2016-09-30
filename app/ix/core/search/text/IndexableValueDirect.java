package ix.core.search.text;

import org.apache.lucene.index.IndexableField;

public class IndexableValueDirect implements IndexableValue {
	IndexableField ixf;
	public IndexableValueDirect(IndexableField ixf){
		this.ixf=ixf;
	}
	
	public IndexableField getDirectIndexableField(){
		return ixf;
	}
	

	public boolean isDirectIndexField(){
		return true;
	}
	
	
	
	@Override
	public String path() {
		return ixf.name();
	}

	@Override
	public String name() {
		return ixf.name();
	}

	@Override
	public Object value() {
		return ixf.stringValue();
	}

}
