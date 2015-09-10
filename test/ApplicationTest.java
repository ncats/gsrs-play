import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;


/**
 * Simple (JUnit) tests that can call all parts of a play app.
 * If you are interested in mocking a whole application, see the wiki for more details.
 */
public class ApplicationTest {

    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertThat(a).isEqualTo(2);
    }

    @Test
    public void renderTemplate() {
//        Content html = views.html.index.render("Your new application is ready.");
//        assertThat(contentType(html)).isEqualTo("text/html");
//        assertThat(contentAsString(html)).contains("Your new application is ready.");
    }

    @Test
    public void dtoTest() throws IOException {
//        File f = new File("/Users/guhar/dto.json");
//        ObjectMapper mapper = new ObjectMapper();
//        DrugTargetOntology dto = new DrugTargetOntology();
//        dto.setRoot(mapper.readTree(f));
//        System.out.println("dto.rootTerm = " + dto.rootTerm);
//        for (DrugTargetOntology.DtoTerm child : dto.rootTerm.children) System.out.println("child = " + child);
//
//        DrugTargetOntology.DtoTerm term = dto.findTerm("holocarboxylase synthetase deficiency");
//        System.out.println("term = " + term);
    }

}
