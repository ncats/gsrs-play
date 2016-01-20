package ix.ginas.models.v1;

import ix.ginas.models.utils.JSONEntity;

@JSONEntity(name = "site", title = "Site", isFinal = true)
public class Site {
    @JSONEntity(title = "Subunit Index")
    public Integer subunitIndex;
    
    @JSONEntity(title = "Residue Index")
    public Integer residueIndex;

    public Site () {}
    
    public String toString(){
    	return this.subunitIndex + "_" + this.residueIndex;
    }
}
