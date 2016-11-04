package ix.core;

public interface ValidationMessage extends Comparable<ValidationMessage>{
	public enum MESSAGE_TYPE{
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
	public String getMessage();
	public ValidationMessage.MESSAGE_TYPE getMessageType();
	
	default int compareTo(ValidationMessage vm){
	    return Integer.compare(this.getMessageType().getPriority(), vm.getMessageType().getPriority());
	}
	
}
