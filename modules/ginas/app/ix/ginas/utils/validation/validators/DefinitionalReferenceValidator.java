package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.GinasAccessControlled;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasSubstanceDefinitionAccess;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/*
Make sure that a substance that is marked as public has at least one definitional reference 
(a reference on the part of the object that make it what it is, for example, structure for a chemical)
that is also marked public.
 */
public class DefinitionalReferenceValidator extends AbstractValidatorPlugin<Substance>
{

	@Override
	public void validate(Substance objnew, Substance objold, ValidatorCallback callback) {
		if (objnew.isPublic() && !objnew.substanceClass.equals(Substance.SubstanceClass.concept)) {
			play.Logger.trace("in DefinitionalReferenceValidator with public substance.  class: " + objnew.substanceClass);
			Stream<Reference> defRefs = getDefinitionalReferences(objnew);
			boolean allowed = defRefs
							.filter(Reference::isPublic)
							.filter(Reference::isPublicDomain)
							.findAny()
							.isPresent();
			play.Logger.trace("		allowed: " + allowed);
			if (!allowed) {
				callback.addMessage(GinasProcessingMessage
								.ERROR_MESSAGE("Public substances require a public defintional reference.  Please add one."));

			}
		}
	}

	private Stream<Reference> getDefinitionalReferences(Substance sub) {
		if (sub == null) {
			play.Logger.debug("substance is null");
			return Stream.empty();
		}
		Set<UUID> referenceIds = null;
		GinasAccessControlled definitionalPart = (sub instanceof GinasSubstanceDefinitionAccess)
						? ((GinasSubstanceDefinitionAccess) sub).getDefinitionElement() : null;

		if (definitionalPart != null && definitionalPart instanceof GinasAccessReferenceControlled) {
			play.Logger.trace("	definitionalPart not null");
			referenceIds = ((GinasAccessReferenceControlled) definitionalPart).getReferencesAsUUIDs();
		}
		else {
			play.Logger.warn("	definitionalPart not usable for references. ");
		}

		if (referenceIds != null && !referenceIds.isEmpty() && sub.references != null && !sub.references.isEmpty()) {
			play.Logger.trace("in DefinitionalReferenceValidator.getDefinitionalReferences found some definitional references");
			final Set<UUID> finalReferenceIds = referenceIds;
			return sub.references.stream().filter(r -> finalReferenceIds.contains(r.uuid));
		}
		return Stream.empty();
	}
}
