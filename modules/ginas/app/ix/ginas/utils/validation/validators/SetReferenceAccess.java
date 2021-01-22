package ix.ginas.utils.validation.validators;

import ix.core.controllers.AdminFactory;
import ix.core.models.Group;
import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.EmbeddedKeywordList;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;
import play.Logger;

/**
 * Set access on references according to configured rules
 *
 * @author mitch
 */
public class SetReferenceAccess extends AbstractValidatorPlugin<Substance>
{

    //temporarily instantiate from hard-coded strings
    private List<String> alwaysPublic;
    private List<String> alwaysPrivate;

    List<Pattern> referenceCitationPatterns = null;

    public SetReferenceAccess() {
        Logger.debug("in SetReferenceAccess ctor" );
    }

    @Override
    public void validate(Substance substance, Substance oldSubstance, ValidatorCallback callback) {
        Logger.debug("Starting in SetReferenceAccess.validate");
        Logger.debug("alwaysPublic: " + alwaysPublic);
        Logger.debug("alwaysPrivate: " + alwaysPrivate);

        substance.references.forEach(r -> {
            String msg = String.format("doc type: %s; isPublic: %b; isPublicDomain: %b; isPublicReleaseReference: %b",
                    r.docType, r.isPublic(), r.isPublicDomain(), r.isPublicReleaseReference());
            Logger.debug(msg);

            if ((alwaysPrivate.contains(r.docType))
                    && (r.isPublic() || r.isPublicDomain() || r.isPublicReleaseReference())) {
                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE(
                                "protected reference:\""
                                        + r.docType + ":" + r.citation + "\" cannot be public. Setting to protected.")
                        .appliableChange(true);
                callback.addMessage(mes, () -> makeReferenceProtected(r));
            }
            else if (referenceCitationPatterns.stream().anyMatch(p -> p.matcher((" " + r.citation).toUpperCase()).find()) ) {
							if (r.isPublic() || r.isPublicDomain() || r.isPublicReleaseReference()) {
                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE(
                                "reference:\""
                                        + r.docType + ":" + r.citation + "\" appears to be non-public. Setting to protected.")
                        .appliableChange(true);
                callback.addMessage(mes, () -> makeReferenceProtected(r));
							}
            }
            else if (alwaysPublic.contains(r.docType)
                    && (!r.isPublic() || !r.isPublicDomain())) {
                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE(
                                "public reference:\""
                                        + r.docType + ":" + r.citation + "\" cannot be private. Setting to public.")
                        .appliableChange(true);
                callback.addMessage(mes, () -> makeReferencePublic(r));
            }
        });
    }


    protected void makeReferenceProtected(Reference r) {
        r.publicDomain = false;
        Group g = AdminFactory.getGroupByName("protected");
        if (g == null) {
            g = new Group("protected");
        }

        EmbeddedKeywordList klist = new EmbeddedKeywordList();

        r.tags.stream()
                .filter(t -> !Reference.PUBLIC_DOMAIN_REF.equals(t.getValue()))
                .forEach(klist::add);
        r.tags = klist;

        LinkedHashSet<Group> gs = new LinkedHashSet<>();
        gs.add(g);
        r.setAccess(gs);
    }

    protected void makeReferencePublic(Reference r) {
        r.publicDomain = true;
        LinkedHashSet<Group> emptyGroup = new LinkedHashSet<>();
        r.setAccess(emptyGroup);
    }

    public List<String> getAlwaysPublic() {
        return alwaysPublic;
    }

    public void setAlwaysPublic(List<String> alwaysPublic) {
        this.alwaysPublic = alwaysPublic;
    }

    public List<String> getAlwaysPrivate() {
        return alwaysPrivate;
    }

    public void setAlwaysPrivate(List<String> alwaysPrivate) {
        this.alwaysPrivate = alwaysPrivate;
    }

    public List<Pattern> getReferenceCitationPatterns() {
        return referenceCitationPatterns;
    }

    public void setReferenceCitationPatterns(List<Pattern> referenceCitationPatterns) {
        this.referenceCitationPatterns = referenceCitationPatterns;
    }
}
