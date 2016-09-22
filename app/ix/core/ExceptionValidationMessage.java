package ix.core;

public class ExceptionValidationMessage implements ValidationMessage{

	private Throwable e;
	
	public ExceptionValidationMessage(Throwable e){
		this.e=e;
	}
	@Override
	public boolean isError() {
		return true;
	}

	@Override
	public String getMessage() {
		return e.getMessage();
	}

	@Override
	public MESSAGE_TYPE getMessageType() {
		
		return MESSAGE_TYPE.ERROR;
	}

}
