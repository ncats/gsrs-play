package ix.core.models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;

import ix.ginas.models.GinasCommonData;

@Entity
@DiscriminatorValue("KEY")
@DynamicFacet(label="label", value="term")
public class Keyword extends Value {
    @Column(length=255)
    public String term;
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String href;

    public Keyword () {}
    public Keyword (String term) {
        this.term = term;
    }
    public Keyword (String label, String term) {
        super (label);
        this.term = term;
    }

    @Override
    public String getValue () { return term; }

    public int hashCode(){
    	return (label + ":" + term).hashCode();
    }
    public boolean equals (Object obj) {
        if (obj instanceof Keyword) {
            Keyword kw = (Keyword)obj;
            if (label != null && term != null)
                return label.equals(kw.label) && term.equals(kw.term);
            if(label==null && kw.label==null){
            	return term.equals(kw.term);
            }
        }
        return false;
    }
    
    public String toString(){
    	String ret="Keyword ";
    	if(this.label==null){
    		return ret +  "NO_LABEL:" + this.term;
    	}else{
    		return ret +  this.label  + ":" + this.term;
    	}
    }
}
