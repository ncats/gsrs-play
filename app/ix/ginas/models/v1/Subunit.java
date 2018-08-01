package ix.ginas.models.v1;

import ix.core.SingleParent;
import ix.core.models.Indexable;
import ix.ginas.models.GinasCommonSubData;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name="ix_ginas_subunit")
@SingleParent
public class Subunit extends GinasCommonSubData {
    @Lob
    @Basic(fetch=FetchType.EAGER)
    @Indexable(sequence=true)
    public String sequence;

    public Integer subunitIndex;

    public Subunit () {}
    
    public int getLength(){
    	if(sequence!=null){
    		return sequence.length();
    	}else{
    		return 0;
    	}
    }
    
    
//    public List<char[]> getCharArr(){
//    	List<char[]> returnArr = new ArrayList<char[]>();
//    	int index = 0;
//    	while (index < sequence.length()) {
//    	    returnArr.add(sequence.substring(index, Math.min(index + 10,sequence.length())).toCharArray());
//    	    index += 10;
//    	}
//		return returnArr;
//    }
//    public char[] subSeq(int start, int stop){
//    	char[] sub = new char[10];
//    	sequence.getChars(start, stop, sub, 0);
//    	return sub;
//    }
}
