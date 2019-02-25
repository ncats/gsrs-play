package ix.test;

import ix.core.util.RunOnly;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.ginas.models.v1.Substance;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import ix.test.util.TestUtil;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TestPluginSupport {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();



    @Test
    public void addPluginRawClasses() throws Throwable{

        GinasTestServer ts = new GinasTestServer("ix.core.entityprocessors +={\n" +
                "               \"class\":\"ix.ginas.models.v1.Substance\",\n" +
                "               \"processor\":\"my.fakepackage.ExampleEntityProcessor\"}\n" +
                "gsrs.plugins.dir = [\"" + tmpDir.getRoot().getAbsolutePath() + "/\"]");


        new TestUtil.JavaCompilerBuilder(tmpDir.getRoot())
                .addClass("my.fakepackage.ExampleEntityProcessor", source)
                .compile();

        submitSubstanceAndMakeSureProcessorCalled(ts);
    }

    @Test
    public void addPluginJar() throws Throwable{

        File pluginDir = tmpDir.newFolder("myPlugins");
        File classDir = tmpDir.newFolder();

        GinasTestServer ts = new GinasTestServer("ix.core.entityprocessors +={\n" +
                "               \"class\":\"ix.ginas.models.v1.Substance\",\n" +
                "               \"processor\":\"my.fakepackage.ExampleEntityProcessor\"}\n" +
                "gsrs.plugins.dir = [\"" + pluginDir.getAbsolutePath() + "/\"]");


        new TestUtil.JavaCompilerBuilder(classDir)
                .addClass("my.fakepackage.ExampleEntityProcessor", source)
                .compile()
                .makeJar(new File(pluginDir, "myjar.jar"));


//        Thread.sleep(100_000);
        submitSubstanceAndMakeSureProcessorCalled(ts);
    }
    @Test
    public void addPluginExplicitJar() throws Throwable{

        File pluginDir = tmpDir.newFolder("myPlugins");
        File classDir = tmpDir.newFolder();

        GinasTestServer ts = new GinasTestServer("ix.core.entityprocessors +={\n" +
                "               \"class\":\"ix.ginas.models.v1.Substance\",\n" +
                "               \"processor\":\"my.fakepackage.ExampleEntityProcessor\"}\n" +
                "gsrs.plugins.dir = [\"" + pluginDir.getAbsolutePath() + "/myjar.jar\"]");


        new TestUtil.JavaCompilerBuilder(classDir)
                .addClass("my.fakepackage.ExampleEntityProcessor", source)
                .compile()
                .makeJar(new File(pluginDir, "myjar.jar"));


//        Thread.sleep(100_000);
        submitSubstanceAndMakeSureProcessorCalled(ts);
    }

    public void submitSubstanceAndMakeSureProcessorCalled(GinasTestServer ts) throws Throwable {
        ts.before();
        try {
            Substance s = new SubstanceBuilder()
                    .generateNewUUID()
                    .addName("myName")
                    .build();


            UUID uuid = s.getUuid();

            try (RestSession rs = ts.newRestSession(ts.getAdmin())) {
                SubstanceAPI api = rs.newSubstanceAPI();

                api.submitSubstance(s);

                Substance fetched = SubstanceBuilder.from(api.fetchSubstanceJsonByUuid(uuid)).build();

                assertEquals(Arrays.asList("it worked!"),
                        fetched.notes.stream().map(n-> n.note).filter(n -> Objects.equals("it worked!", n)).collect(Collectors.toList()));
            }
        }finally{
            ts.after();
        }
    }

    public String source = "package my.fakepackage;\n" +

            "import ix.core.EntityProcessor;\n" +
            "import ix.ginas.models.v1.Substance;\n" +
            "public class ExampleEntityProcessor implements EntityProcessor<Substance>{\n" +

            "@Override\n" +
            "public void prePersist(Substance obj) throws FailProcessingException {\n" +
            "obj.addNote(\"it worked!\");\n" +
            "}\n" +

            "@Override\n" +
            "public void preUpdate(Substance obj) throws FailProcessingException {\n" +
            "obj.addNote(\"it worked2!\");\n" +
            "}\n" +
            "}";
}
