package ix.ginas.utils;
public class GinasProcessingMessage {
	public enum MESSAGE_TYPE{WARNING, ERROR, INFO};
	public enum ACTION_TYPE{IGNORE, APPLY_CHANGE, FAIL, DO_NOTHING};
	
	public MESSAGE_TYPE messageType=MESSAGE_TYPE.INFO;
	public ACTION_TYPE actionType=ACTION_TYPE.DO_NOTHING;
	
	public String message;
	public boolean suggestedChange=false;
	public boolean appliedChange=false;
	
	public GinasProcessingMessage(MESSAGE_TYPE mtype, String msg){
		this.messageType=mtype;
		this.message=msg;
	}
	
	public GinasProcessingMessage appliableChange(boolean b){
		this.suggestedChange=b;
		return this;
	}
	
	public String toString(){
		return messageType + ":" + message;
	}
	
	public static GinasProcessingMessage ERROR_MESSAGE(String msg){
		return new GinasProcessingMessage(MESSAGE_TYPE.ERROR,msg);
	}
	public static GinasProcessingMessage WARNING_MESSAGE(String msg){
		return new GinasProcessingMessage(MESSAGE_TYPE.WARNING,msg);
	}
	public static GinasProcessingMessage INFO_MESSAGE(String msg){
		return new GinasProcessingMessage(MESSAGE_TYPE.INFO,msg);
	}
}
