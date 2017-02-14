package ix.test.processor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import ix.test.server.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import ix.AbstractGinasServerTest;
import ix.ginas.exporters.Exporter;
import ix.ginas.exporters.JsonExporterFactory;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;
import ix.ginas.processors.PublicTagFlagger;
import ix.test.builder.SubstanceBuilder;
import ix.test.server.GinasTestServer.User;
import org.junit.rules.TemporaryFolder;
import play.Configuration;

public class PublicTagAddTest extends AbstractGinasServerTest{

	@Rule
	public TemporaryFolder tmpDir = new TemporaryFolder();

	@Override
	public GinasTestServer createGinasTestServer(){
		return new GinasTestServer(()->{
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
	}

	RestSession session;
	SubstanceAPI api;
	User u;

	@Before
	public void setup() {
		u=ts.createAdmin("madeUp", "SomePassword");
		session = ts.newRestSession(u);
		api = new SubstanceAPI(session);
	}

	@After
	public void breakdown() {
		session.close();
	}


	/**
	 * Gets the substance stream as a temporary GSRS json dump file
	 * @param substances
	 * @return
	 * @throws IOException
	 */
	public File asLoadFile(Stream<Substance> substances) throws IOException{
		File f =tmpDir.newFile();
		try (JsonSubstanceWriter writer = new JsonSubstanceWriter(f)) {
			writer.writeAll(substances);
		};

		return f;

	}

	@Test
	public void ensureMissingPublicTagGetsAddedWithProcessor() throws IOException {
		try {
			Substance sub = new SubstanceBuilder().addName("Test Guy").andThenMutate(s -> {
				s.references.stream().forEach(r -> {
					r.tags.clear();
				});
			}).generateNewUUID().build();

			SubstanceLoader sl = new SubstanceLoader(ts.newBrowserSession(u));
			sl.loadJson(asLoadFile(Stream.of(sub)));

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
