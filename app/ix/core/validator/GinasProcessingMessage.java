package ix.core.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ix.core.util.InheritanceTypeIdResolver;
import play.Logger;

@InheritanceTypeIdResolver.DefaultInstance
public class GinasProcessingMessage implements ValidationMessage {
	public enum ACTION_TYPE{IGNORE, APPLY_CHANGE, FAIL, DO_NOTHING};


	public MESSAGE_TYPE messageType=MESSAGE_TYPE.INFO;
	public ACTION_TYPE actionType=ACTION_TYPE.DO_NOTHING;

	public String message;
	public static class Link{
		public String href;
		public String text;
	}
	public boolean suggestedChange=false;
	public boolean appliedChange=false;
	public List<Link> links = new ArrayList<Link>();
	
	private boolean possibleDuplicate=false;

	public GinasProcessingMessage(){}

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
	public static GinasProcessingMessage SUCCESS_MESSAGE(String msg){
		return new GinasProcessingMessage(MESSAGE_TYPE.SUCCESS,msg);
	}
	
	public static boolean ALL_VALID(Collection<GinasProcessingMessage> messages){
		for(GinasProcessingMessage gpm:messages){
			Logger.info("Message:" + gpm.message);
			if(gpm.isProblem()){
				return false;
			}
		}
		return true;
	}
	@JsonIgnore
	public boolean isProblem(){
		return messageType == MESSAGE_TYPE.ERROR ||messageType == MESSAGE_TYPE.WARNING;
	}
	@JsonIgnore
	public boolean isError(){
		return messageType == MESSAGE_TYPE.ERROR;
	}


	public GinasProcessingMessage addLinks(Collection<? extends Link> links){
		this.links.addAll(links);
		return this;
	}
	public GinasProcessingMessage addLink(Link l){
		this.links.add(l);
		return this;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public GinasProcessingMessage setMessage(String s){
		this.message=s;
		return this;
	}

	@Override
	public MESSAGE_TYPE getMessageType() {
		return this.messageType;
	}
	
	public GinasProcessingMessage markPossibleDuplicate(){
		possibleDuplicate=true;
		return this;
	}

	@JsonIgnore
	public boolean isPossibleDuplicate(){
		return possibleDuplicate || !this.links.isEmpty();
	}

	public void makeError() {
		this.messageType=MESSAGE_TYPE.ERROR;
	}

	public boolean hasLinks() {
		return !this.links.isEmpty();
	}
}
