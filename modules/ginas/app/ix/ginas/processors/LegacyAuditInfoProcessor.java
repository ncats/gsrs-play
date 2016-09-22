package ix.ginas.processors;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ix.core.EntityProcessor;
import ix.core.controllers.PrincipalFactory;
import ix.core.models.Principal;
import ix.ginas.models.v1.Note;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;


import play.Play;
public class LegacyAuditInfoProcessor implements EntityProcessor<Substance>{
	public static final String START_LEGACY_REF="Legacy Info:";
	
	private static final Pattern CREATED_BY_REGEX = Pattern.compile("<CREATED_BY>([^<]*)<");
	private static final Pattern CREATED_DATE_REGEX = Pattern.compile("<CREATED_DATE>([^<]*)<");
	private static final Pattern MODIFIED_BY_REGEX = Pattern.compile("<MODIFIED_BY>([^<]*)<");
	private static final Pattern MODIFIED_DATE_REGEX = Pattern.compile("<MODIFIED_DATE>([^<]*)<");
	private static final Pattern APPROVED_BY_REGEX = Pattern.compile("<APPROVED_BY>([^<]*)<");
	private static final Pattern APPROVED_DATE_REGEX = Pattern.compile("<APPROVED_DATE>([^<]*)<");
	public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Override
	public void prePersist(Substance obj) throws ix.core.EntityProcessor.FailProcessingException {
		preFlightFormat(obj);
	}
	
	private static String getIf(Pattern p, String s){
		Matcher m = p.matcher(s);
		if(m.find()){
			return m.group(1);
		}
		return null;
	}
	
	private static void applyPrincipalIf(Pattern p, String s, Consumer<Principal> pconsumer){
		String pname=getIf(p ,s);
		if(pname!=null){	
			Principal prince= PrincipalFactory.registerIfAbsent(new Principal(pname,null));
			pconsumer.accept(prince);
		}
	}
	
	private static void applyDateIf(Pattern p, String s, Consumer<Date> dconsumer){
		String date=getIf(p ,s);
		if(date!=null && date.length()>0){
			Date d;
			try {
				d = FORMATTER.parse(date);
				dconsumer.accept(d);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	public void preFlightFormat(Substance obj){
		//String legacy_audit_ref=null;
		
		obj.notes.stream()
			.filter(n->(n.note!=null && n.note.startsWith(START_LEGACY_REF)))
			.map(n->n.note)
			.findFirst()
			.ifPresent(legacy_audit_ref->{
				
				applyPrincipalIf(CREATED_BY_REGEX,legacy_audit_ref, p -> obj.createdBy=p );
				applyPrincipalIf(MODIFIED_BY_REGEX,legacy_audit_ref, p -> obj.lastEditedBy=p );
				applyPrincipalIf(APPROVED_BY_REGEX,legacy_audit_ref, p -> obj.approvedBy=p );
				applyDateIf(CREATED_DATE_REGEX,legacy_audit_ref, p -> obj.created=p );
				applyDateIf(MODIFIED_DATE_REGEX,legacy_audit_ref, p -> obj.lastEdited=p );
				applyDateIf(APPROVED_DATE_REGEX,legacy_audit_ref, p -> obj.approved=p );
				
			});
		//TODO: Refactor somewhere else
		if(Play.isTest()){
			if(obj.hasTagString("FORCE_FAIL")){
				throw new IllegalStateException("FAIL ME");
			}
		}
	}

}
