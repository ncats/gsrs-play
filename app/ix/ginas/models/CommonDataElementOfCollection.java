package ix.ginas.models;

import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import ix.core.SingleParent;
import ix.ginas.models.v1.Substance;
import ix.utils.Util;

/**
 * This abstract class is meant as a convenience tool to allow ownership for
 * simple @OneToMany annotations.
 *
 * @author Tyler Peryea
 *
 */
@MappedSuperclass
@SingleParent
public abstract class CommonDataElementOfCollection<E extends CommonDataElementOfCollection> extends GinasCommonSubData implements Comparable<E>{
	@ManyToOne(cascade = CascadeType.PERSIST)
	private Substance owner;
	
	public Substance fetchOwner(){
		return this.owner;
	}

	public void assignOwner(Substance own){
		this.owner=own;
	}

	public int compareTo(E o) {
		return Util.getComparatorFor(this.getClass()).compare(this, o);
	}
}
