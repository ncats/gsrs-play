package ix.ginas.indexers;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.NameUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
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

        private String originalPattern;

        private String originalType;

        public FacetStem(String originalForm, String type, String category) {
            String transformedRegex = originalForm.replaceAll("\\-", ".*");
            Matcher parenMatcher = parenPattern.matcher(transformedRegex);
            if( parenMatcher.find()) {
                transformedRegex =parenMatcher.replaceFirst("[" + parenMatcher.group(1) +"]?");
                play.Logger.debug("parenMatcher transformed transformedRegex: " + transformedRegex);
            }
            this.pattern = Pattern.compile(transformedRegex);
            this.type = type;//may change
            this.category = category;
            this.originalPattern=originalForm;
            this.originalType=type;
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

        public String getOriginalPattern() {
            return originalPattern;
        }

        public void setOriginalPattern(String originalPattern) {
            this.originalPattern = originalPattern;
        }

        public String getOriginalType() {
            return originalType;
        }

        public void setOriginalType(String originalType) {
            this.originalType = originalType;
        }
    }

    static String parenPatternSource ="\\(([a-z]{1,2})\\)";
    static Pattern parenPattern = Pattern.compile(parenPatternSource);
    private final String ARROW= " -> ";

    public INNAndUSANIndexValueMaker() {
        play.Logger.debug("INNAndUSANIndexValueMaker ctor");
    }
    private List<FacetStem> facetStems;

    public void setUsanInputFile(String usanInputFile) {
        this.usanInputFile = usanInputFile;
    }

    public void setInnInputFile(String innInputFile) {
        this.innInputFile = innInputFile;
    }
    //this path will work on a linux deployment
    // for a window development run, add 'modules/ginas/'
    private String usanInputFile ="modules/ginas/conf/facets/finalUSANStemList_2022.txt";
    private String innInputFile ="modules/ginas/conf/facets/finalINNStemList_2022.txt";

    Pattern crossReferencePattern = Pattern.compile("\\(see ?(also)? (.*)\\)");

    private void initialize() {
        facetStems = new ArrayList<>();
        try {
            play.Logger.debug("innInputFile: " + innInputFile);
            File inputFile = new File(innInputFile);
            play.Logger.debug("exists? " + inputFile.exists());
            if(!inputFile.exists() ){
                play.Logger.debug("curr: " + System.getProperty("user.dir"));
            }
            List<String> innSourceLines = Files.readAllLines(Paths.get(innInputFile), StandardCharsets.UTF_8);
            play.Logger.debug("total inn source lines: " + innSourceLines.size());
            innSourceLines.subList(1, innSourceLines.size() - 1).forEach(l -> {
                play.Logger.debug("l: " + l);
                String[] facetParts =l.split("\t");
                play.Logger.debug("parts: " + facetParts.length);
                if( facetParts.length>=2 && facetParts[1] !=null && facetParts[1].length()>0) {
                    //remove quotes
                    String facetValue = cleanString(facetParts[1]);
                    String facetKey = cleanString(facetParts[0]);
                    play.Logger.trace(String.format("final def facetValue: %s indexed by %s", facetValue, facetKey));
                    facetStems.add(new FacetStem(facetKey, facetValue, "INN Stem"));
                }
            });

        } catch (IOException ex) {
            play.Logger.error("IOException reading file " + ex.getMessage());
            ex.printStackTrace();
        }
        try {
            play.Logger.debug("usanInputFile: " + usanInputFile);
            File inputFile = new File(usanInputFile);
            play.Logger.debug("exists? " + inputFile.exists());
            List<String> sourceLines = Files.readAllLines(new File(usanInputFile).toPath(), StandardCharsets.UTF_8);
            play.Logger.debug("total usan source lines: " + sourceLines.size());
            sourceLines.subList(1, sourceLines.size() - 1).forEach(l -> {
                String[] facetParts =l.split("\t");
                if( facetParts.length>=2 && facetParts[1] !=null && facetParts[1].length()>0) {
                    //remove quotes
                    String facetValue = cleanString(facetParts[1]);
                    String facetKey = cleanString(facetParts[0]);
                    play.Logger.trace(String.format("final USAN def facetValue: %s indexed by %s", facetValue, facetKey));
                    facetStems.add(new FacetStem(facetKey, facetValue, "USAN Stem"));
                }
            });
        } catch (IOException ex) {
            play.Logger.error("IOException reading file " + ex.getMessage());
        }
        resolveReferencedFacets();
    }
    private void resolveReferencedFacets() {
        List<FacetStem> stemsToAdd = new ArrayList<>();
        List<FacetStem> stemsToRemove = new ArrayList<>();
        facetStems.forEach(fs -> {
            List<String> thingsTolookUp = new ArrayList<>();
            Matcher seeMatcher = crossReferencePattern.matcher(fs.getOriginalType());
            if (seeMatcher.matches()) {
                String rawRef = seeMatcher.group(2);
                String separator = null;
                if (rawRef.contains("/")) {
                    separator="/";
                } else if (rawRef.contains(",")) {
                    separator=",";
                } else if( rawRef.contains(" and ")) {
                    separator=" and ";
                }

                if( separator != null ) {
                    String[] parts = rawRef.split(separator);
                    for( String part : parts) {
                        thingsTolookUp.add(part.trim());
                    }
                } else {
                    thingsTolookUp.add(rawRef);
                }
                stemsToRemove.add(fs);
            }
            thingsTolookUp.forEach(s->{
                Optional<FacetStem> stem = facetStems.stream().filter(fs2->fs2.getCategory().equals(fs.getCategory())
                        && fs2.getOriginalPattern().equals(s)).findFirst();
                if(!stem.isPresent() && !s.startsWith("-")) {
                    //give the look-up another go, prepending a hyphen
                    stem = facetStems.stream().filter(fs2->fs2.getCategory().equals(fs.getCategory())
                            && fs2.getOriginalPattern().equals("-"+s)).findFirst();
                }
                if( stem.isPresent()) {
                    stemsToAdd.add(new FacetStem(fs.getOriginalPattern(), stem.get().getType(), stem.get().getCategory())
                    );
                } else {
                    play.Logger.error("Error looking up item " +s);
                }
            });

        });
        facetStems.removeAll(stemsToRemove);
        facetStems.addAll(stemsToAdd);
    }

    @Override
    public void createIndexableValues(Substance substance,
            Consumer<IndexableValue> consumer) {

        play.Logger.trace("starting in createIndexableValues");
        if( facetStems ==null || facetStems.isEmpty()) {
            initialize();
        }
        //todo: separate INN and USAN processing
        Set<String> innReferences = substance.references.stream()
                .filter(r -> r.citation!= null && (r.citation.contains("[INN]")))
                .map(r -> r.getUuid().toString())
                .collect(Collectors.toSet());

        Set<String> usanReferences = substance.references.stream()
                .filter(r -> r.citation!= null && ( r.citation.contains("[USAN]")))
                .map(r -> r.getUuid().toString())
                .collect(Collectors.toSet());

        play.Logger.trace(String.format("total innRefs: %d; usan refs: %d", innReferences.size(), usanReferences.size()));
        if (substance.names != null) {
            List<String> usanNamesToMatch = substance.names
                    .stream()
                    .filter(n -> n.languages.stream().map(l -> l.term).anyMatch(t -> t.equals("en")))
                    .filter(n ->  n.getName().contains("[USAN]")
                    /*|| (!n.nameOrgs.isEmpty()) */
                    || n.getReferences().stream()
                            .map(r -> r.term)
                            .anyMatch(u -> usanReferences.contains(u))
                    )
                    .map(n -> n.getName().toLowerCase())
                    .collect(Collectors.toList());

            List<String> innNamesToMatch = substance.names
                    .stream()
                    .filter(n -> n.languages.stream().map(l -> l.term).anyMatch(t -> t.equals("en")))
                    .filter(n -> n.getName().contains("[INN]")
                            /*|| (!n.nameOrgs.isEmpty())*/
                            || n.getReferences().stream()
                            .map(r -> r.term)
                            .anyMatch(u -> innReferences.contains(u))
                    )
                    .map(n -> n.getName().toLowerCase())
                    .collect(Collectors.toList());
            play.Logger.trace(String.format("total innNamesToMatch: %d; usanNamesToMatch: %d", innNamesToMatch.size(), usanNamesToMatch.size()));
            facetStems
                    .stream()
                    .filter(f->f.getCategory().startsWith("INN"))
                    .filter(p -> innNamesToMatch.stream()
                    .anyMatch(n -> p.matches(n))
                    )
                    .forEach(p -> {
                        consumer.accept(IndexableValue
                                .simpleFacetStringValue(p.category, p.originalPattern + ARROW + p.getType()));
                        String debugMessage = String.format("setting INN facet on substance %s - category: %s - type: %s",
                                substance.uuid.toString(), p.category, p.getType());
                        play.Logger.trace(debugMessage);
                    });
            facetStems
                    .stream()
                    .filter(f->f.getCategory().startsWith("USAN"))
                    .filter(p -> usanNamesToMatch.stream()
                            .anyMatch(n -> p.matches(n))
                    )
                    .forEach(p -> {
                        consumer.accept(IndexableValue
                                .simpleFacetStringValue(p.category, p.originalPattern + ARROW + p.getType()));
                        String debugMessage = String.format("setting USAN facet on substance %s - category: %s - type: %s",
                                substance.uuid.toString(), p.category, p.getType());
                        play.Logger.trace(debugMessage);
                    });
        }
    }

    private String cleanString(String initial) {
        if( initial == null || initial.length()==0) return "";
        return NameUtilities.symbolsToASCII(removeQuotes(initial)).trim();
    }

    private String removeQuotes(String initial) {
        if( initial == null || initial.length() ==0) return "";

        String value= initial;
        if( value.startsWith("\"")) value=value.substring(1);
        if( value.endsWith("\"")) value=value.substring(0, value.length()-1);
        return value;
    }

}
