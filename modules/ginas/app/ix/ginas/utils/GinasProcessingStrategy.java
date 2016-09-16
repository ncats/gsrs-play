package ix.ginas.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ix.core.GinasProcessingMessage;
import ix.core.models.Group;
import ix.ginas.models.v1.Substance;

public abstract class GinasProcessingStrategy {
	public static final String FAILED = "FAILED";
	public static final String WARNING = "WARNING";
	public static final String FAIL_REASON = "FAIL_REASON";
	
	//TODO: add messages directly here
	public List<GinasProcessingMessage> _localMessages = new ArrayList<GinasProcessingMessage>();
	
	public abstract void processMessage(GinasProcessingMessage gpm);
	
	public void addAndProcess(List<GinasProcessingMessage> source, List<GinasProcessingMessage> destination){
		for(GinasProcessingMessage gpm: source){
			this.processMessage(gpm);
			destination.add(gpm);
		}
	}

	public static enum HANDLING_TYPE {
		MARK, FAIL, FORCE_IGNORE
	};

	public HANDLING_TYPE failType = HANDLING_TYPE.MARK;
	public HANDLING_TYPE warningHandle = HANDLING_TYPE.MARK;

	public static GinasProcessingStrategy ACCEPT_APPLY_ALL() {
		return new GinasProcessingStrategy() {
			@Override
			public void processMessage(GinasProcessingMessage gpm) {
				if (gpm.suggestedChange){
					gpm.actionType = GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE;
				}else{
					if(gpm.isError()){
						gpm.actionType=GinasProcessingMessage.ACTION_TYPE.FAIL;
					}else{
						gpm.actionType = GinasProcessingMessage.ACTION_TYPE.IGNORE;
					}
				}
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
					if (gpm.suggestedChange){
						gpm.actionType = GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE;
					}else{
						gpm.actionType = GinasProcessingMessage.ACTION_TYPE.IGNORE;
					}
				}
			}
		};
	}
	

	public static GinasProcessingStrategy ACCEPT_APPLY_ALL_WARNINGS_MARK_FAILED() {
		return ACCEPT_APPLY_ALL_WARNINGS().markFailed();
	}
	
	public static GinasProcessingStrategy ACCEPT_APPLY_ALL_MARK_FAILED() {
		return ACCEPT_APPLY_ALL().markFailed();
	}
	
	public GinasProcessingStrategy markFailed() {
		this.failType = HANDLING_TYPE.MARK;
		return this;
	}

	public GinasProcessingStrategy failFailed() {
		this.failType = HANDLING_TYPE.FAIL;
		return this;
	}

	public GinasProcessingStrategy forceIgnoreFailed() {
		this.failType = HANDLING_TYPE.FORCE_IGNORE;
		return this;
	}
	
	
	public boolean handleMessages(Substance cs, List<GinasProcessingMessage> list) {
		boolean allow=true;
		for (GinasProcessingMessage gpm : list) {
			
			if(gpm.isError() && gpm.appliedChange){
				gpm.messageType=GinasProcessingMessage.MESSAGE_TYPE.WARNING;
			}
			
			if (gpm.actionType == GinasProcessingMessage.ACTION_TYPE.FAIL || gpm.isError()) {
				allow=false;
				if (failType == HANDLING_TYPE.FAIL) {
					throw new IllegalStateException(gpm.message);
				} else if (failType == HANDLING_TYPE.MARK) {
					cs.status = GinasProcessingStrategy.FAILED;
					cs.addRestrictGroup(Substance.GROUP_ADMIN);
				} else {

				}
			}
		}
		return allow;
	}

	public void addProblems(Substance cs, List<GinasProcessingMessage> list) {
		if (warningHandle == HANDLING_TYPE.MARK) {
			List<GinasProcessingMessage> problems = list.stream()
				.filter(f->f.isProblem())
				.collect(Collectors.toList());
			if(!problems.isEmpty()){
				cs.setValidationMessages(problems);
			}			
		}
	}
}
