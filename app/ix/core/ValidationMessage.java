package ix.core;


public interface ValidationMessage {
	public enum MESSAGE_TYPE{WARNING, ERROR, INFO, SUCCESS};
	public boolean isError();
	public String getMessage();
	public ValidationMessage.MESSAGE_TYPE getMessageType();
	
}
