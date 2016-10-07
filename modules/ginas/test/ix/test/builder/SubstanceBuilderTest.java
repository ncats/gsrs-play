package ix.test.builder;

import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Substance;
import ix.test.server.GinasTestServer;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.Assert.*;
/**
 * Created by katzelda on 9/6/16.
 */
public class SubstanceBuilderTest {

    @Rule
    public GinasTestServer ts = new GinasTestServer();

    @Test
    public void setName(){
        Substance substance = new SubstanceBuilder()
                                    .addName("foo")
                                    .build();

        assertEquals("foo", substance.getName());
    }

    @Test
    public void ChemicalSubstanceFromJsonInputStream() throws Exception{
        try(InputStream in = new FileInputStream(new File("test/testJSON/pass/2moities.json"))){
            assertNotNull(in);
            ChemicalSubstanceBuilder builder = SubstanceBuilder.from(in);

            assert2MoietiesBuiltCorrectly(builder);
        }
    }

    @Test
    public void ChemicalSubstanceFromJsonFile() throws Exception{
        ChemicalSubstanceBuilder builder = SubstanceBuilder.from(new File("test/testJSON/pass/2moities.json"));

        assert2MoietiesBuiltCorrectly(builder);

    }

    private void assert2MoietiesBuiltCorrectly(ChemicalSubstanceBuilder builder) {
        ChemicalSubstance substance = builder.build();

        assertEquals("1db30542-0cc4-4098-9d89-8340926026e9", substance.getUuid().toString());
        assertEquals(2, substance.moieties.size());
    }

    @Test
    public void modifyChemicalSubstanceFromJson() throws Exception{
        ///home/katzelda/GIT/inxight3/modules/ginas/test/testJSON/pass/2moities.json
        String path = "test/testJSON/pass/2moities.json";

        try(InputStream in = new FileInputStream(new File("test/testJSON/pass/2moities.json"))){
            assertNotNull(in);
            ChemicalSubstanceBuilder builder = SubstanceBuilder.from(in);

            UUID uuid = UUID.randomUUID();
            builder.setUUID( uuid);
            ChemicalSubstance substance = builder.build();

            assertEquals(uuid, substance.getUuid());
            assertEquals(2, substance.moieties.size());
        }
    }
}
