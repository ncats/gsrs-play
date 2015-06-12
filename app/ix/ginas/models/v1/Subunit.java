package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.List;

import ix.ginas.models.Ginas;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name="ix_ginas_subunit")
public class Subunit extends Ginas {
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String sequence;

    public Integer subunitIndex;

    public Subunit () {}
    
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
