package ix.ginas.models.v1;

import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.utils.JSONConstants;

import java.util.List;
import java.util.Map;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.Ginas;

@JSONEntity(name = "amount", title = "Amount", isFinal = true)
@Entity
@Table(name="ix_ginas_amount")
public class Amount extends Ginas {
    @JSONEntity(title = "Amount Type", format = JSONConstants.CV_AMOUNT_TYPE, isRequired = true)
    public String type;
    
    @JSONEntity(title = "Average")
    public Double average;
    
    @JSONEntity(title = "High Limit")
    public Double highLimit;
    
    @JSONEntity(title = "High")
    public Double high;
    
    @JSONEntity(title = "Low Limit")
    public Double lowLimit;
    
    @JSONEntity(title = "Low")
    public Double low;
    
    @JSONEntity(title = "Units", format = JSONConstants.CV_AMOUNT_UNIT)
    public String units;
    
    @JSONEntity(title = "Non-numeric Value")
    public String nonNumericValue;
    
    @JSONEntity(title = "Referenced Material")
    @Column(length=10)
    public String approvalID;

    public Amount () {}
    
    
    public String toString(){
    	String val="";
    	if(highLimit!=null && lowLimit==null && average==null){
    		val=">"+highLimit;
    	}else if(highLimit==null && lowLimit==null && average!=null){
    		val=average+"";
    	}else if(highLimit==null && lowLimit!=null && average==null){
    		val="<" + lowLimit;
    	}else if(highLimit!=null && lowLimit!=null && average!=null){
    		val=average + "[" + lowLimit + " to " + highLimit + "]";
    	}else if(highLimit!=null && lowLimit==null && average!=null){
    		val=average + "[>" + highLimit + "]";
    	}else if(highLimit==null && lowLimit!=null && average!=null){
    		val=average + "[<" + lowLimit + "]";
    	}
    	if(nonNumericValue!=null){
    		
    	}
    	
    	return val+ " (" + units + ")"; 
    }
}
