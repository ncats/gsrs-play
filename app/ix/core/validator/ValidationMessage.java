package ix.core.validator;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import ix.core.util.InheritanceTypeIdResolver;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "_discriminator")
@JsonTypeIdResolver(InheritanceTypeIdResolver.class)
public interface ValidationMessage extends Comparable<ValidationMessage>{
	 enum MESSAGE_TYPE{
	    ERROR(0),
	    WARNING(1), 
	    SUCCESS(2), 
	    INFO(3)
	    ;
	    private int priority;
	    private MESSAGE_TYPE(int i){
	        priority=i;
	    }
	    public int getPriority(){
	        return this.priority;
	    }
	    public boolean isProblem(){
	        return (this == ERROR)||(this==WARNING);
	    }
	
	};
	default boolean isError(){
	    return this.getMessageType()==MESSAGE_TYPE.ERROR;
	}
	String getMessage();
	 ValidationMessage.MESSAGE_TYPE getMessageType();
	
	default int compareTo(ValidationMessage vm){
	    return Integer.compare(this.getMessageType().getPriority(), vm.getMessageType().getPriority());
	}
	
}
