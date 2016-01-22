package ix.ginas.models.v1;

import ix.ginas.models.utils.JSONEntity;

@JSONEntity(name = "site", title = "Site", isFinal = true)
public class Site {
    @JSONEntity(title = "Subunit Index")
    public Integer subunitIndex;
    
    @JSONEntity(title = "Residue Index")
    public Integer residueIndex;

    public Site () {}
    public Site (Integer si, Integer ri) {
    	this.subunitIndex=si;
    	this.residueIndex=ri;
    }
    
    public String toString(){
    	return this.subunitIndex + "_" + this.residueIndex;
    }
    public boolean equals(Object o){
    	if(o instanceof Site){
	    	if(this.subunitIndex == ((Site)o).subunitIndex){
	    		if(this.residueIndex == ((Site)o).residueIndex){
	    			return true;
	    		}
	    	}
    	}
    	
    	return false;
    }
    @Override
    public int hashCode(){
    	return toString().hashCode();
    }
}
