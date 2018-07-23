package ix.ginas.models.v1;

import ix.ginas.models.utils.JSONEntity;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JSONEntity(name = "site", title = "Site", isFinal = true)
public class Site {
    private static Pattern INDEX_PATTERN = Pattern.compile("(\\d+)_(\\d+)");
    @JSONEntity(title = "Subunit Index")
    public int subunitIndex;
    
    @JSONEntity(title = "Residue Index")
    public int residueIndex;

    /**
     * Create a new Site object by parsing the formatted String
     * @param formattedString
     * @return
     */
    public static Site of(String formattedString){
        Matcher m = INDEX_PATTERN.matcher(formattedString);
        if(m.find()){
            int s = Integer.parseInt(m.group(1));
            int r = Integer.parseInt(m.group(1));
            return new Site(s,r);
        }
        throw new IllegalArgumentException("could not parse indexes for site from '"+formattedString + "'");
    }
    public Site () {}
    public Site (int subunitIndex, int residueIndex) {
    	this.subunitIndex=subunitIndex;
    	this.residueIndex=residueIndex;
    }
    
    public String toString(){
    	return this.subunitIndex + "_" + this.residueIndex;
    }
    public boolean equals(Object o){
    	if(o instanceof Site){
    	    Site s  = (Site)o;
    	    if(subunitIndex != s.subunitIndex){
    	        return false;
            }
            if(residueIndex != s.residueIndex){
                return false;
            }
	    			return true;
	    		}
    	
    	return false;
    }
    @Override
    public int hashCode(){
    	return toString().hashCode();
    }
}
