package ix.ginas.indexers;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.ginas.models.v1.Substance;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author mitch
 */
public class INNAndUSANIndexValueMaker implements IndexValueMaker<Substance> {

    private static class FacetStem {

        private Pattern pattern;

        private String type;

        private String category; //INN or USAN

        public FacetStem(String regex, String type, String category) {
            this.pattern = Pattern.compile(regex);
            this.type = type;
            this.category = category;
        }

        public boolean matches(String name) {
            return pattern.matcher(name).matches();
        }

        public String getType() {
            return this.type;
        }

        public String getCategory() {
            return this.category;
        }
    }

    public INNAndUSANIndexValueMaker() {
        play.Logger.debug("INNAndUSANIndexValueMaker ctor");
        init();
    }
    private List<FacetStem> facetStems;

    private String usanInputFile = "modules/ginas/conf/facets/usan.facet.data.txt";
    private String innInputFile = "modules/ginas/conf/facets/inn.facet.data.txt";
    private void init() {
        facetStems = new ArrayList<>();
        try {
            play.Logger.debug("innInputFile: " + innInputFile);
            List<String> innSourceLines = Files.readAllLines(Paths.get(innInputFile));
            play.Logger.debug("total inn source lines: " + innSourceLines.size());
            innSourceLines.subList(1, innSourceLines.size() - 1).forEach(l -> {
                play.Logger.debug("l: " + l);
                String[] facetParts =l.split("\t");
                play.Logger.debug("parts: " + facetParts.length);
                facetStems.add(new FacetStem(facetParts[0], facetParts[1], "INN Stem"));
            });
        } catch (IOException ex) {
            play.Logger.error("Error reading file " + ex.getMessage());
        }
        try {
            List<String> sourceLines = Files.readAllLines(new File(usanInputFile).toPath());
            play.Logger.debug("total usan source lines: " + sourceLines.size());
            sourceLines.subList(1, sourceLines.size() - 1).forEach(l -> {
                facetStems.add(new FacetStem(l.split("\t")[0], l.split("\t")[1], "USAN Stem"));
            });
        } catch (IOException ex) {
            play.Logger.error("Error reading file " + ex.getMessage());
        }
    }

    @Override
    public void createIndexableValues(Substance substance,
            Consumer<IndexableValue> consumer) {

        Set<String> innOrUsanReferences = substance.references.stream()
                .filter(r -> r.citation!= null && (r.citation.contains("[INN]") || r.citation.contains("[USAN]")))
                .map(r -> r.getUuid().toString())
                .collect(Collectors.toSet());

        if (substance.names != null) {
            List<String> namesToMatch = substance.names
                    .stream()
                    .filter(n -> n.languages.stream().map(l -> l.term).anyMatch(t -> t.equals("en")))
                    .filter(n -> (n.getName().contains("[INN]") || n.getName().contains("[USAN]"))
                    || (!n.nameOrgs.isEmpty())
                    || n.getReferences().stream()
                            .map(r -> r.term)
                            .anyMatch(u -> innOrUsanReferences.contains(u))
                    )
                    .map(n -> n.getName().toLowerCase())
                    .collect(Collectors.toList());

            facetStems
                    .stream()
                    .filter(p -> namesToMatch.stream()
                    .anyMatch(n -> p.matches(n))
                    )
                    .forEach(p -> {
                        consumer.accept(IndexableValue
                                .simpleFacetStringValue(p.category, p.getType()));
                    });
        }
    }

}
