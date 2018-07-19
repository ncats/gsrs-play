package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.models.Keyword;
import ix.core.util.CachedSupplier;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.EmbeddedKeywordList;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.GinasUtils;
import ix.ginas.utils.validation.ValidationUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by katzelda on 5/11/18.
 */
public class NamesValidator extends AbstractValidatorPlugin<Substance> {

    boolean extractLocators;
    public static CachedSupplier<List<Replacer>> replacers = CachedSupplier.of(()->{
        List<Replacer> repList = new ArrayList<>();
        repList.add(new Replacer("^(\\s+)","" ).message("Name \"$0\" has leading whitespace which was removed"));
        repList.add(new Replacer("(\\s+)$","" ).message("Name \"$0\" has trailing whitespace which was removed"));


        repList.add(new Replacer("[\\t\\n\\r]", " ")
                .message("Name \"$0\" has non-space whitespace characters. They will be replaced with spaces."));
        repList.add(new Replacer("\\s\\s\\s*", " ")
                .message("Name \"$0\" has consecutive whitespace characters. These will be replaced with single spaces."));

        return repList;

    });

    public static class Replacer{
        Pattern p;
        String replace;
        String message = "String \"$0\" matches forbidden pattern";

        public Replacer(String regex, String replace){
            this.p=Pattern.compile(regex);
            this.replace=replace;
        }

        public boolean matches(String test){
            return this.p.matcher(test).find();
        }
        public String fix(String test){
            return test.replaceAll(p.pattern(), replace);
        }

        public Replacer message(String msg){
            this.message=msg;
            return this;
        }

        public String getMessage(String test){
            return message.replace("$0", test);
        }

    }
    @Override
    public void validate(Substance s, Substance objold, ValidatorCallback callback) {
        boolean preferred = false;
        int display = 0;
        Iterator<Name> nameIterator = s.names.iterator();
        while(nameIterator.hasNext()){
            Name n = nameIterator.next();
            if (n == null) {
                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE("Null name objects are not allowed")
                        .appliableChange(true);

                callback.addMessage(mes, () -> {
                    mes.appliedChange = true;
                    nameIterator.remove();
                });


            } else {
                if (n.preferred) {
                    preferred = true;
                }
                if (n.isDisplayName()) {
                    display++;
                }
                if (extractLocators) {
                    Set<String> locators = extractLocators(n);
                    if(!locators.isEmpty()){
                        GinasProcessingMessage mes = GinasProcessingMessage
                                .WARNING_MESSAGE(
                                        "Names of form \"<NAME> [<TEXT>]\" are transformed to locators. The following locators will be added:"
                                                + locators.toString())
                                .appliableChange(true);
                        callback.addMessage(mes, ()->{
                            for (String loc : locators) {
                                n.name = n.name.replace("[" + loc + "]", "").trim();
                            }
                            for (String loc : locators) {
                                n.addLocator(s, loc);
                            }
                        });
                    }
                }
                if (n.languages == null || n.languages.isEmpty()) {
                    GinasProcessingMessage mes = GinasProcessingMessage
                            .WARNING_MESSAGE(
                                    "Must specify a language for each name. Defaults to \"English\"")
                            .appliableChange(true);
                    callback.addMessage(mes, () -> {
                        if (n.languages == null) {
                            n.languages = new EmbeddedKeywordList();
                        }
                        n.languages.add(new Keyword("en"));
                    });
                }
                if (n.type == null) {
                    GinasProcessingMessage mes = GinasProcessingMessage
                            .WARNING_MESSAGE(
                                    "Must specify a name type for each name. Defaults to \"Common Name\" (cn)")
                            .appliableChange(true);
                    callback.addMessage(mes, () -> n.type = "cn");

                }

                for (Replacer r : replacers.get()) {
                    if (r.matches(n.getName())) {
                        GinasProcessingMessage mes = GinasProcessingMessage
                                .WARNING_MESSAGE(
                                        r.getMessage(n.getName()))
                                .appliableChange(true);
                        callback.addMessage(mes, () -> n.setName(r.fix(n.getName())));

                    }
                }
                if(n.getAccess().isEmpty()){
                    boolean hasPublicReference = n.getReferences().stream()
                            .map(r->r.getValue())
                            .map(r->s.getReferenceByUUID(r))
                            .filter(r->r.isPublic())
                            .filter(r->r.isPublicDomain())
                            .findAny()
                            .isPresent();

                    if(!hasPublicReference){
                        GinasProcessingMessage mes = GinasProcessingMessage
                                .ERROR_MESSAGE("The name :\"" + n.getName() + "\" needs an unprotected reference marked \"Public Domain\" in order to be made public.");
                        callback.addMessage(mes);
                    }
                }

            }
            ValidationUtils.validateReference(s, n, callback, ValidationUtils.ReferenceAction.FAIL);
        }

        if (s.names.isEmpty()) {
            GinasProcessingMessage mes = GinasProcessingMessage
                    .ERROR_MESSAGE("Substances must have names");
            callback.addMessage(mes);
        }
        if (display == 0) {
            GinasProcessingMessage mes = GinasProcessingMessage
                    .INFO_MESSAGE(
                            "Substances should have exactly one (1) display name, Default to using:"
                                    + s.getName()).appliableChange(true);
            callback.addMessage(mes, () -> {
                if (!s.names.isEmpty()) {
                    Name.sortNames(s.names);
                    s.names.get(0).displayName = true;
                    mes.appliedChange = true;
                }
            });
        }
        if (display > 1) {
            GinasProcessingMessage mes = GinasProcessingMessage
                    .ERROR_MESSAGE("Substance should not have more than one (1) display name. Found "
                            + display);
            callback.addMessage(mes);
        }

        Map<String, Set<String>> nameSetByLanguage = new HashMap<>();


        for (Name n : s.names) {
            Iterator<Keyword> iter = n.languages.iterator();
            String uppercasedName = n.getName().toUpperCase();

            while(iter.hasNext()){
                String language = iter.next().getValue();
//				System.out.println("language for " + n + "  = " + language);
                Set<String> names = nameSetByLanguage.computeIfAbsent(language, k->new HashSet<>());
                if(!names.add(uppercasedName)){
                    GinasProcessingMessage mes = GinasProcessingMessage
                            .ERROR_MESSAGE(
                                    "Name '"
                                            + n.getName()
                                            + "' is a duplicate name in the record.")
                            .markPossibleDuplicate();
                    callback.addMessage(mes);
                }

            }
            //nameSet.add(n.getName());
            try {
                List<Substance> sr = ix.ginas.controllers.v1.SubstanceFactory
                        .getSubstancesWithExactName(100, 0, n.name);
                if (sr != null && !sr.isEmpty()) {
                    Substance s2 = sr.iterator().next();
                    if (!s2.getUuid().toString().equals(s.getUuid().toString())) {
                        GinasProcessingMessage mes = GinasProcessingMessage
                                .ERROR_MESSAGE(
                                        "Name '"
                                                + n.name
                                                + "' collides (possible duplicate) with existing name for substance:")
                                .addLink(GinasUtils.createSubstanceLink(s2));
                        callback.addMessage(mes);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    static private Set<String> extractLocators(Name n) {
        Pattern p = Pattern.compile("(?:[ \\]])\\[([A-Z0-9]*)\\]");
        Matcher m = p.matcher(n.name);
        Set<String> locators = new LinkedHashSet<String>();
        //TODO isn't while(m.find() ) sufficient?
        if (m.find()) {
            do {
                String loc = m.group(1);

                // System.out.println("LOCATOR:" + loc);
                locators.add(loc);
            } while (m.find(m.start(1)));
        }
        return locators;

    }
}
