package ix.ginas.models.v1;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.GinasCommonSubData;

@Entity
@Table(name="ix_ginas_substanceref")
@JSONEntity(name = "substanceReference", isFinal = true)
public class SubstanceReference extends GinasCommonSubData {

    public static SubstanceReference newReferenceFor(Substance s){
        SubstanceReference ref = new SubstanceReference();
        ref.refuuid = s.getUuid().toString();
        ref.refPname = s.getName();
        ref.approvalID = s.approvalID;
        ref.substanceClass = Substance.SubstanceClass.reference.toString();

        return ref;
    }
    @JSONEntity(title = "Substance Name")
    @Column(length=1024)
    public String refPname;
    
    @JSONEntity(isRequired = true)
    @Column(length=128)
    public String refuuid;
    
    @JSONEntity(values = "JSONConstants.ENUM_REFERENCE")
    public String substanceClass;
    
    @Column(length=32)
    public String approvalID;

    public SubstanceReference () {}
    
    public String getLinkingID(){
        if(approvalID!=null){
                return approvalID;
        }
        if(refuuid!=null){
                return refuuid.split("-")[0];
        }
        return refPname;
    }
    
    public String getName(){
    	if(refPname!=null)
    		return refPname;
    	String rep= getLinkingID();
    	if(rep==null){
    		return "NO_NAME";
    	}
    	return rep;
    }

    @Override
    public String toString() {
        return "SubstanceReference{" +
                "refPname='" + refPname + '\'' +
                ", refuuid='" + refuuid + '\'' +
                ", substanceClass='" + substanceClass + '\'' +
                ", approvalID='" + approvalID + '\'' +
                '}';
    }
}
