package ix.ginas.utils;

import java.util.List;

import play.Logger;

public abstract class GinasProcessingStrategy {
	public abstract void processMessage(GinasProcessingMessage gpm);
	
	public static GinasProcessingStrategy ACCEPT_APPLY_ALL(){
		return new GinasProcessingStrategy(){
			@Override
			public void processMessage(GinasProcessingMessage gpm) {
				if(gpm.suggestedChange)
					gpm.actionType=GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE;
				else
					gpm.actionType=GinasProcessingMessage.ACTION_TYPE.IGNORE;
			}
		};
	}
	public static GinasProcessingStrategy ACCEPT_APPLY_ALL_WARNINGS(){
		return new GinasProcessingStrategy(){
			@Override
			public void processMessage(GinasProcessingMessage gpm) {
				if(gpm.messageType==GinasProcessingMessage.MESSAGE_TYPE.ERROR){
					gpm.actionType=GinasProcessingMessage.ACTION_TYPE.FAIL;
				}else{
					if(gpm.suggestedChange)
						gpm.actionType=GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE;
					else
						gpm.actionType=GinasProcessingMessage.ACTION_TYPE.IGNORE;
				}
			}
		};
	}
	public static void failIfNecessary(List<GinasProcessingMessage> list){
		for(GinasProcessingMessage gpm:list){
			Logger.debug("######### " + gpm.toString());
			if(gpm.actionType==GinasProcessingMessage.ACTION_TYPE.FAIL)
				throw new IllegalStateException(gpm.message);
		}
	}
}
