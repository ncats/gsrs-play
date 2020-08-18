package ix.core.validator;


import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionValidationMessage implements ValidationMessage {

	private final Throwable e;
	
	public ExceptionValidationMessage(Throwable e){
		this.e=e;
	}
	@Override
	public boolean isError() {
		return true;
	}

	@Override
	public String getMessage() {
		String message = e.getMessage();
		//some throwables don't have messages (like NPE)
		//this causes problems because when converted to JSON we will lose the message
		if(message !=null && !message.isEmpty()){
			return message;
		}
		StringWriter sw = new StringWriter();
		try(
			PrintWriter pw = new PrintWriter(sw);) {
			e.printStackTrace(pw);
			return sw.toString();
		}
	}

	@Override
	public MESSAGE_TYPE getMessageType() {
		
		return MESSAGE_TYPE.ERROR;
	}

}
