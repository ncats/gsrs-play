package ix.ginas.utils;

import ix.ginas.models.v1.Substance;

import java.util.List;

import play.Logger;

public abstract class GinasProcessingStrategy {
	public abstract void processMessage(GinasProcessingMessage gpm);

	public static enum FAIL_TYPE {
		MARK, FAIL, FORCE_IGNORE
	};

	public FAIL_TYPE failType = FAIL_TYPE.MARK;

	public static GinasProcessingStrategy ACCEPT_APPLY_ALL() {
		return new GinasProcessingStrategy() {
			@Override
			public void processMessage(GinasProcessingMessage gpm) {
				if (gpm.suggestedChange)
					gpm.actionType = GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE;
				else
					gpm.actionType = GinasProcessingMessage.ACTION_TYPE.IGNORE;
			}
		};
	}

	public static GinasProcessingStrategy ACCEPT_APPLY_ALL_WARNINGS() {
		return new GinasProcessingStrategy() {
			@Override
			public void processMessage(GinasProcessingMessage gpm) {
				if (gpm.messageType == GinasProcessingMessage.MESSAGE_TYPE.ERROR) {
					gpm.actionType = GinasProcessingMessage.ACTION_TYPE.FAIL;
				} else {
					if (gpm.suggestedChange)
						gpm.actionType = GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE;
					else
						gpm.actionType = GinasProcessingMessage.ACTION_TYPE.IGNORE;
				}
			}
		};
	}
	
	public static GinasProcessingStrategy ACCEPT_APPLY_ALL_WARNINGS_MARK_FAILED() {
		return ACCEPT_APPLY_ALL_WARNINGS().markFailed();
	}

	public GinasProcessingStrategy markFailed() {
		this.failType = FAIL_TYPE.MARK;
		return this;
	}

	public GinasProcessingStrategy failFailed() {
		this.failType = FAIL_TYPE.FAIL;
		return this;
	}
	
	public GinasProcessingStrategy forceIgnoreFailed() {
		this.failType = FAIL_TYPE.FORCE_IGNORE;
		return this;
	}

	public void failIfNecessary(Substance cs, List<GinasProcessingMessage> list) {
		for (GinasProcessingMessage gpm : list) {
			Logger.debug("######### " + gpm.toString());
			if (gpm.actionType == GinasProcessingMessage.ACTION_TYPE.FAIL) {
				if (failType == FAIL_TYPE.FAIL) {
					throw new IllegalStateException(gpm.message);
				}else if (failType == FAIL_TYPE.MARK) {
					cs.status="FAILED";
					cs.addPropertyNote(gpm.message, "FAIL_REASON");
					cs.addRestrictGroup("admin");					
				}else{
					
				}
			}
		}
	}
}
