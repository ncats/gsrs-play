package ix.ginas.utils.validation.validators;

import ix.core.controllers.AdminFactory;
import ix.core.models.Group;
import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.EmbeddedKeywordList;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;

import java.util.ArrayList;
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
    private List<String> alwaysPublic = new ArrayList<>();
    private List<String> alwaysPrivate  = new ArrayList<>();
    private List<String> suggestedPublic = new ArrayList<>();

    private List<Pattern> referenceCitationPatterns  = new ArrayList<>();

    public SetReferenceAccess() {
        Logger.debug("in SetReferenceAccess ctor" );
    }

    @Override
    public void validate(Substance substance, Substance oldSubstance, ValidatorCallback callback) {
        Logger.trace("Starting in SetReferenceAccess.validate");
        Logger.trace("alwaysPublic: " + alwaysPublic);
        Logger.trace("alwaysPrivate: " + alwaysPrivate);
        Logger.trace("suggestedPublic: " + suggestedPublic);

        substance.references.forEach(r -> {
            String msg = String.format("doc type: %s; isPublic: %b; isPublicDomain: %b; isPublicReleaseReference: %b",
                    r.docType, r.isPublic(), r.isPublicDomain(), r.isPublicReleaseReference());
            Logger.debug(msg);

            if ((alwaysPrivate.contains(r.docType))
                    && (r.isPublic() || r.isPublicDomain() || r.isPublicReleaseReference())) {
                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE(
                                "Protected reference:\""
                                        + r.docType + ":" + r.citation + "\" cannot be public. Setting to protected.")
                        .appliableChange(true);
                callback.addMessage(mes, () -> makeReferenceProtected(r));
            }
            else if (referenceCitationPatterns.stream().anyMatch(p -> p.matcher((" " + r.citation).toUpperCase()).find()) ) {
							if (r.isPublic() || r.isPublicDomain() || r.isPublicReleaseReference()) {
                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE(
                                "Reference:\""
                                        + r.docType + ":" + r.citation + "\" appears to be non-public. Setting to protected.")
                        .appliableChange(true);
                callback.addMessage(mes, () -> makeReferenceProtected(r));
							}
            }
            else if (alwaysPublic.contains(r.docType)
                    && (!r.isPublic() || !r.isPublicDomain())) {
                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE(
                                "Public reference:\""
                                        + r.docType + ":" + r.citation + "\" cannot be private. Setting to public.")
                        .appliableChange(true);
                callback.addMessage(mes, () -> makeReferencePublic(r));
            }else if(suggestedPublic.contains(r.docType) && (!r.isPublic() || !r.isPublicDomain())) {
                String messageText =String.format("References of type %s, such as \"%s:%s,\" are typically public. Consider modifying the access and public domain flag, unless there is an explicit reason to keep it restricted.",
                        r.docType, r.docType, r.citation);
                if(!substanceNotesContainWarning(substance, messageText)){
                    GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE(messageText);
                    callback.addMessage(mes);
                }else {
                    Logger.debug("warning already noted: " + messageText);
                }
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

    public List<String> getSuggestedPublic() {
        return suggestedPublic;
    }

    public void setSuggestedPublic(List<String> suggestedPublic) {
        this.suggestedPublic = suggestedPublic;
    }

    private boolean substanceNotesContainWarning(Substance s, String warningText) {

        Logger.trace("substanceNotesContainWarning looking for warning " + warningText);
        String textToSearch = "[Validation]WARNING:" + warningText;
        if( s.notes.stream().anyMatch(n->n.note.equals(textToSearch))) {
            Logger.trace("  going to return true");
            return true;
        }
        Logger.trace("  going to return false");
        return false;
    }
}
