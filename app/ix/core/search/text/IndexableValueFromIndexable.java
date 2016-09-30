package ix.core.search.text;

import java.util.regex.Pattern;

import ix.core.util.EntityUtils.InstantiatedIndexable;

public class IndexableValueFromIndexable implements IndexableValue{

	private String name;
	private String path;
	private Object value;
	
	InstantiatedIndexable ii;
	
	public IndexableValueFromIndexable(String name, String path, Object value, InstantiatedIndexable ii){
		this.name=ii.name().isEmpty() ? name : ii.name();
		this.path=path;
		this.value=value;
		this.ii=ii;
	}
	
	public static IndexableValueFromIndexable of(String name, Object value,String path,  InstantiatedIndexable ii){
		return new IndexableValueFromIndexable(name,path,value,ii);
	}
	
	public boolean indexed() {
		return ii.indexed();
	}

	public boolean sortable() {
		return ii.sortable();
	}

	public boolean taxonomy() {
		return ii.taxonomy();
	}

	public boolean facet() {
		return ii.facet();
	}

	public boolean suggest() {
		return ii.suggest();
	}

	public boolean sequence() {
		return ii.sequence();
	}

	public boolean structure() {
		return ii.structure();
	}

	public boolean fullText() {
		return ii.fullText();
	}

	public String pathsep() {
		return ii.pathsep();
	}

	public long[] ranges() {
		return ii.ranges();
	}

	public double[] dranges() {
		return ii.dranges();
	}

	public String format() {
		return ii.format();
	}

	public boolean recurse() {
		return ii.recurse();
	}

	public boolean indexEmpty() {
		return ii.indexEmpty();
	}

	public boolean equals(Object obj) {
		return ii.equals(obj);
	}

	public String emptyString() {
		return ii.emptyString();
	}

	public int hashCode() {
		return ii.hashCode();
	}

	public String toString() {
		return ii.toString();
	}

	public Pattern getPathSepPattern() {
		return ii.getPathSepPattern();
	}

	public String[] splitPath(String path) {
		return ii.splitPath(path);
	}

	
	
	@Override
	public String path() {
		return this.path;
	}

	@Override
	public String name() {
		return this.name;
	}

	@Override
	public Object value() {
		return this.value;
	}

}
