package ix.test.processor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import ix.ginas.exporters.Exporter;
import ix.ginas.exporters.JsonExporterFactory;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;
import ix.ginas.processors.PublicTagFlagger;
import ix.test.builder.SubstanceBuilder;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.GinasTestServer.User;
import ix.test.ix.test.server.RestSession;
import ix.test.ix.test.server.SubstanceAPI;
import ix.test.ix.test.server.SubstanceLoader;
import play.Configuration;

public class PublicTagAddTest {

	 @Rule
	 public GinasTestServer ts = new GinasTestServer(()->{
		 	String addconf="include \"ginas.conf\"\n" + 
				 "\n" + 
				 "ix.core.entityprocessors +={\n" + 
				 "               \"class\":\"ix.ginas.models.v1.Substance\",\n" + 
				 "               \"processor\":\"ix.ginas.processors.PublicTagFlagger\",\n" + 
				 "        }";
		 	Config additionalConfig = ConfigFactory.parseString(addconf)
		 				.resolve()
		 				.withOnlyPath("ix.core.entityprocessors");
		 	return new Configuration(additionalConfig).asMap();
	 });
	 
	 

	RestSession session;
	SubstanceAPI api;
	User u;

	@Before
	public void allowForcedAudit() {
		u=ts.createAdmin("madeUp", "SomePassword");
		session = ts.newRestSession(u);
		api = new SubstanceAPI(session);
	}

	@After
	public void disableForcedAudit() {
		session.close();
	}
		
	@Test
	public void ensureMissingPublicTagGetsAddedWithProcessor() throws IOException {
		try {
			Substance sub = new SubstanceBuilder().addName("Test Guy").andThenMutate(s -> {
				s.references.stream().forEach(r -> {
					r.tags.clear();
				});
			}).generateNewUUID().build();

			JsonExporterFactory jef = new JsonExporterFactory();

			File f = File.createTempFile("test", "gsrs");
			try {
				Exporter<Substance> export = jef.createNewExporter(new FileOutputStream(f), null);
				export.export(sub);
				export.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SubstanceLoader sl = new SubstanceLoader(ts.newBrowserSession(u));
			sl.loadJson(f);
			f.deleteOnExit();

			JsonNode jsn = api.fetchSubstanceJsonByUuid(sub.getUuid().toString());
			long publicRefs= StreamSupport.stream(jsn.at("/references").spliterator(),false)
				.filter(js->{
					JsonNode jsl=js.at("/tags");
					if(!jsl.isMissingNode()){
						boolean hasPub=false;
						boolean hasAuto=false;
						for(JsonNode tag: jsl){
							if(tag.asText().equals(Reference.PUBLIC_DOMAIN_REF)){
								hasPub=true;
							}
							if(tag.asText().equals(PublicTagFlagger.AUTO_SELECTED)){
								hasAuto=true;
							}
						}
						return hasPub&&hasAuto;
					}
					return false;
				}).count();
			assertEquals(1,publicRefs);
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}

	}
	 
}