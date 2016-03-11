package ix.ginas.utils;

import ix.core.ValidationMessage;
import ix.ginas.models.v1.Substance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import play.Logger;
import ix.core.ValidationMessage.MESSAGE_TYPE;

public class GinasProcessingMessage implements ValidationMessage{
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
		boolean valid=true;
		for(GinasProcessingMessage gpm:messages){
			Logger.info("Message:" + gpm.message);
			if(gpm.isProblem()){
				valid=false;
				return valid;
			}
		}
		return valid;
	}
	
	public boolean isProblem(){
		return messageType == MESSAGE_TYPE.ERROR ||messageType == MESSAGE_TYPE.WARNING;
	}
	public boolean isError(){
		return messageType == MESSAGE_TYPE.ERROR;
	}
	
	public GinasProcessingMessage addSubstanceLink(Substance s){
		Link l = new Link();
		l.href=ix.ginas.controllers.routes.GinasApp.substance(s.getLinkingID())+"";
		l.text="[" + s.getApprovalIDDisplay() + "]" + s.getName();
		this.links.add(l);
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

	@Override
	public MESSAGE_TYPE getMessageType() {
		return this.messageType;
	}
}
