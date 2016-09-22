package ix.test.processor;

import static ix.test.SubstanceJsonUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.UserFetcher;
import ix.core.util.StopWatch;
import ix.ginas.models.v1.Note;
import ix.ginas.processors.LegacyAuditInfoProcessor;
import ix.test.builder.SubstanceBuilder;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import ix.test.ix.test.server.SubstanceAPI;
import play.Configuration;

import play.Play;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class LegacyAuditInfoParserTest {
	 @Rule
	 public GinasTestServer ts = new GinasTestServer(()->{
		 	Config additionalConfig = ConfigFactory.parseFile(new File("conf/ginas-legacy-audit.conf"))
		 				.resolve()
		 				.withOnlyPath("ix.core.entityprocessors");
		 	return new Configuration(additionalConfig).asMap();
	 });

	 
	 public static class AuditNoteBuilder{
		 
		 String createdBy=null;
		 String modifiedBy=null;
		 String approvedBy=null;
		 Date approvedDate = null;
		 
		 public AuditNoteBuilder(){
			 
		 }
		 
		 public AuditNoteBuilder withCreatedBy(String createdBy){
			 this.createdBy=createdBy;
			 return this;
		 }
		 public AuditNoteBuilder withModifiedBy(String modifiedBy){
			 this.modifiedBy=modifiedBy;
			 return this;
		 }
		 public AuditNoteBuilder withApprovedBy(String approvedBy){
			 this.approvedBy=approvedBy;
			 return this;
		 }
		 
		 public AuditNoteBuilder withApprovedDate(Date approvedDate){
			 this.approvedDate=approvedDate;
			 return this;
		 }
		 
		 
		 public String buildNoteText(){
			 StringBuilder sb = new StringBuilder();
			 sb.append(LegacyAuditInfoProcessor.START_LEGACY_REF);
			 if(this.createdBy!=null){
				 sb.append("<CREATED_BY>" + this.createdBy + "</CREATED_BY>");
			 }
			 if(this.modifiedBy!=null){
				 sb.append("<MODIFIED_BY>" + this.modifiedBy + "</MODIFIED_BY>");
			 }
			 if(this.approvedBy!=null){
				 sb.append("<APPROVED_BY>" + this.approvedBy + "</APPROVED_BY>");
			 }
			 if(this.approvedDate!=null){
				 sb.append("<APPROVED_DATE>" + LegacyAuditInfoProcessor.FORMATTER.format(this.approvedDate) + "</APPROVED_DATE>");
			 }
			 return sb.toString();
		 }
		 
	 }
	 
	 
	 @Test
	 public void tryForcingApprovedByInLegacyNote() {
		 
		 	try( RestSession session = ts.newRestSession(ts.createAdmin("whatever" + UUID.randomUUID(), "whenever"))) {
	        	String theName = "Simple Named Concept";
	        	String forcedApprovedBy = "SOME_BLOKE";
	            SubstanceAPI api = new SubstanceAPI(session);
	            JsonNode jsn2 = new SubstanceBuilder()
						.addName(theName)
						.generateNewUUID()
						.addNote(new Note(new AuditNoteBuilder()
											.withApprovedBy(forcedApprovedBy)
											.buildNoteText()))
						.andThenMutate(s->s.addTagString("FORCE_FAIL"))
						.buildJson();
					ensureFailure(api.submitSubstance(jsn2));
						JsonNode jsn = new SubstanceBuilder()
							.addName(theName)
							.generateNewUUID()
							.addNote(new Note(new AuditNoteBuilder()
												.withApprovedBy(forcedApprovedBy)
												.buildNoteText()))
							.buildJson();
						ensurePass(api.submitSubstance(jsn));
						assertEquals(forcedApprovedBy, api.fetchSubstanceJsonByUuid(jsn.at("/uuid").asText()).at("/approvedBy").asText());
	        }catch(Throwable t){
	        	t.printStackTrace();
	        	throw t;
	        }
	 }
	 @Test
	 public void tryForcingModifiedByInLegacyNote() {
		 
		 	try( RestSession session = ts.newRestSession(ts.createAdmin("whatever" + UUID.randomUUID(), "whenever"))) {
	        	String theName = "Simple Named Concept";
	        	String forcedApprovedBy = "tylertest";
	            SubstanceAPI api = new SubstanceAPI(session);
	           
	            
	            
						JsonNode jsn = new SubstanceBuilder()
							.addName(theName)
							.generateNewUUID()
							.addNote(new Note(new AuditNoteBuilder()
									.withModifiedBy(forcedApprovedBy)
									.buildNoteText()))
							.buildJson();
						ensurePass(api.submitSubstance(jsn));
						assertEquals(forcedApprovedBy, api.fetchSubstanceJsonByUuid(jsn.at("/uuid").asText()).at("/lastEditedBy").asText());
	        }catch(Throwable t){
	        	t.printStackTrace();
	        	throw t;
	        }
	 }
	 @Test
	 public void tryForcingApprovalDateAndApproverInLegacyNote() {
		 
		 	try( RestSession session = ts.newRestSession(ts.createAdmin("whatever" + UUID.randomUUID(), "whenever"))) {
	        	String theName = "Simple Named Concept";
	        	String forcedApprovedBy = "tylertest";
	        	Date d = new Date();
	        	
	            SubstanceAPI api = new SubstanceAPI(session);
	           
						JsonNode jsn = new SubstanceBuilder()
							.addName(theName)
							.generateNewUUID()
							.addNote(new Note(new AuditNoteBuilder()
									.withApprovedBy(forcedApprovedBy)
									.withApprovedDate(d)
									.buildNoteText()))
							.buildJson();
						ensurePass(api.submitSubstance(jsn));
						assertEquals(forcedApprovedBy, api.fetchSubstanceJsonByUuid(jsn.at("/uuid").asText()).at("/approvedBy").asText());
						Date approvedDate = new Date(api.fetchSubstanceJsonByUuid(jsn.at("/uuid").asText()).at("/approved").asLong());
						
						//Less than a second difference between set approved Date
						//and actual approved date
						assertTrue(Math.abs(d.getTime()-approvedDate.getTime())<1000);
	        }catch(Throwable t){
	        	t.printStackTrace();
	        	throw t;
	        }
	 }
}
