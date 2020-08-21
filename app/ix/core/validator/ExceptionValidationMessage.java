package ix.core.validator;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionValidationMessage implements ValidationMessage {

	private final String message;

	/**
	 * Only used for JSON deserialization.
	 * @param message the message from the validation message JSON.
	 * @return a new instance.
	 */
	@JsonCreator
	public static ExceptionValidationMessage deserializeFrom(@JsonProperty("message") String message){
		return new ExceptionValidationMessage(message);
	}
	private ExceptionValidationMessage(String message){
		this.message = message;
	}
	public ExceptionValidationMessage(Throwable e){
		this.message = extractMessageFrom(e);
	}
	private String extractMessageFrom(Throwable e){
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
	public boolean isError() {
		return true;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public MESSAGE_TYPE getMessageType() {
		
		return MESSAGE_TYPE.ERROR;
	}

}
