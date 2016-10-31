package ix.ginas.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.models.Keyword;

/**
 * This is just a wrapper around a List of {@link Keyword}s with a fake equals method
 * to trick ebean into always updating the database.  Without this class,
 * if the contents of the list are changed, ebean doesn't always see that the data
 * is dirty and we get corrupted data in our database.
 */
public class EmbeddedKeywordList implements List<Keyword>, Serializable{
	@JsonIgnore
	private List<Keyword> keywords = new ArrayList<Keyword>();
	

	public EmbeddedKeywordList(Collection<Keyword> col){
		keywords= new ArrayList<Keyword>();
		this.addAll(col);
	}
	
	public EmbeddedKeywordList(){}
	
	public int size() {
		return keywords.size();
	}

	public boolean isEmpty() {
		return keywords.isEmpty();
	}

	public boolean contains(Object o) {
		return keywords.contains(o);
	}

	public Iterator<Keyword> iterator() {
		return keywords.iterator();
	}

	public Object[] toArray() {
		return keywords.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return keywords.toArray(a);
	}

	public boolean add(Keyword e) {
		return keywords.add(e);
	}

	public boolean remove(Object o) {
		return keywords.remove(o);
	}

	public boolean containsAll(Collection<?> c) {
		return keywords.containsAll(c);
	}

	public boolean addAll(Collection<? extends Keyword> c) {
		return keywords.addAll(c);
	}

	public boolean addAll(int index, Collection<? extends Keyword> c) {
		return keywords.addAll(index, c);
	}

	public boolean removeAll(Collection<?> c) {
		return keywords.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return keywords.retainAll(c);
	}


	public void clear() {
		keywords.clear();
	}

	/**
	 * This method always returns false. It does this to force EBean to think
	 * that it's a new immutable object, and persist the changes to the database.
	 * 
	 * 
	 * 
	 */
	public boolean equals(Object o) {
		//System.err.println("Equals method on EmbeddedKeywordList always returns false, to force EBean to think it's a new immutable object");
		//Test for immutable scalar tests
		return false;
		
		
		//return keywords.equals(o);
	}
	
	/**
	 * this is the actual equals method. 
	 * @param o
	 * @return
	 */
	public boolean realEquals(Object o){
		return keywords.equals(o);
	}

	public int hashCode() {
		return keywords.hashCode();
	}

	public Keyword get(int index) {
		return keywords.get(index);
	}

	public Keyword set(int index, Keyword element) {
		return keywords.set(index, element);
	}

	public void add(int index, Keyword element) {
		keywords.add(index, element);
	}

	public Keyword remove(int index) {
		return keywords.remove(index);
	}

	public int indexOf(Object o) {
		return keywords.indexOf(o);
	}

	public int lastIndexOf(Object o) {
		return keywords.lastIndexOf(o);
	}

	public ListIterator<Keyword> listIterator() {
		return keywords.listIterator();
	}

	public ListIterator<Keyword> listIterator(int index) {
		return keywords.listIterator(index);
	}

	public List<Keyword> subList(int fromIndex, int toIndex) {
		return keywords.subList(fromIndex, toIndex);
	}
	
	public String toString(){
		return keywords.toString();
	}

}
