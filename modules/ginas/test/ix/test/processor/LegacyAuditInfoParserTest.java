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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.UserFetcher;
import ix.core.controllers.EntityFactory;
import ix.core.plugins.IxCache;
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
	private static final String FORCE_FAIL_TAG = "FORCE_FAIL";
	@Rule
	public GinasTestServer ts = new GinasTestServer(()->{
		Config additionalConfig = ConfigFactory.parseFile(new File("conf/ginas-legacy-audit.conf"))
				.resolve()
				.withOnlyPath("ix.core.entityprocessors");
		return new Configuration(additionalConfig).asMap();
	});


	RestSession session;
	SubstanceAPI api;

	@Before
	public void allowForcedAudit(){
		UserFetcher.globalDisableForceAuditUpdate();
		session = ts.newRestSession(ts.createAdmin("madeUp", "SomePassword"));
		api= new SubstanceAPI(session);
	}

	@After
	public void disableForcedAudit(){
		UserFetcher.globalEnableForceAuditUpdate();
		session.close();

	}

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

	/**
	 * Swallows EntityFactory stdErr, useful when a stackTrace is expected, 
	 * but would be alarming on the log
	 * @param r
	 */
	public static void runWithSwallowedStdErrForPersist(Runnable r){
		ix.core.plugins.ConsoleFilterPlugin.runWithSwallowedStdErrFor(r,EntityFactory.class.getName());
	}

	@Test
	public void tryForcingApprovedByInLegacyNoteAfterFailing() {
		String theName = "Simple Named Concept";
		String forcedApprovedBy = "SOME_BLOKE";
		//Don't print stacktrace
		runWithSwallowedStdErrForPersist(()->{
			JsonNode jsn2 = new SubstanceBuilder()
					.addName(theName)
					.generateNewUUID()
					.addNote(new Note(new AuditNoteBuilder()
							.withApprovedBy(forcedApprovedBy)
							.buildNoteText()))
					.andThenMutate(s->s.addTagString(FORCE_FAIL_TAG))
					.buildJson();
			ensureFailure(api.submitSubstance(jsn2));
		});

		JsonNode jsn = new SubstanceBuilder()
				.addName(theName)
				.generateNewUUID()
				.addNote(new Note(new AuditNoteBuilder()
						.withApprovedBy(forcedApprovedBy)
						.buildNoteText()))
				.buildJson();
		ensurePass(api.submitSubstance(jsn));
		assertEquals(forcedApprovedBy, api.fetchSubstanceJsonByUuid(jsn.at("/uuid").asText()).at("/approvedBy").asText());
	}
	@Test
	public void tryForcingModifiedByInLegacyNote() {
		String theName = "Simple Named Concept";
		String forcedApprovedBy = "tylertest";

		JsonNode jsn = new SubstanceBuilder()
				.addName(theName)
				.generateNewUUID()
				.addNote(new Note(new AuditNoteBuilder()
						.withModifiedBy(forcedApprovedBy)
						.buildNoteText()))
				.buildJson();
		ensurePass(api.submitSubstance(jsn));
		assertEquals(forcedApprovedBy, api.fetchSubstanceJsonByUuid(jsn.at("/uuid").asText()).at("/lastEditedBy").asText());

	}
	@Test
	public void tryForcingApprovalDateAndApproverInLegacyNote() {

		String theName = "Simple Named Concept";
		String forcedApprovedBy = "tylertest";
		Date d = new Date();


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

	}
	@Test
	public void tryForcingApprovalDateAndApproverAndLastEditedInLegacyNoteAfterFailingOnce() {

		String theName = "Simple Named Concept";
		String forcedApprovedBy = "tylertest";
		Date d = new Date();

		runWithSwallowedStdErrForPersist(()->{
			new SubstanceBuilder()
			.addName(theName)
			.generateNewUUID()
			.addNote(new Note(new AuditNoteBuilder()
					.withApprovedBy(forcedApprovedBy)
					.withModifiedBy(forcedApprovedBy)
					.withApprovedDate(d)
					.buildNoteText()))
			        .andThenMutate(s->s.addTagString(FORCE_FAIL_TAG))
			.buildJsonAnd(jsn->{
				ensureFailure(api.submitSubstance(jsn));
			});
		});

		JsonNode jsn = new SubstanceBuilder()
				.addName(theName)
				.generateNewUUID()
				.addNote(new Note(new AuditNoteBuilder()
						.withApprovedBy(forcedApprovedBy)
						.withModifiedBy(forcedApprovedBy)
						.withApprovedDate(d)
						.buildNoteText()))
				.buildJson();
		ensurePass(api.submitSubstance(jsn));
		assertEquals(forcedApprovedBy, api.fetchSubstanceJsonByUuid(jsn.at("/uuid").asText()).at("/approvedBy").asText());
		assertEquals(forcedApprovedBy, api.fetchSubstanceJsonByUuid(jsn.at("/uuid").asText()).at("/lastEditedBy").asText());
		Date approvedDate = new Date(api.fetchSubstanceJsonByUuid(jsn.at("/uuid").asText()).at("/approved").asLong());

		//Less than a second difference between set approved Date
		//and actual approved date
		assertTrue(Math.abs(d.getTime()-approvedDate.getTime())<1000);

	}
}
