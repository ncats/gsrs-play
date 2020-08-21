package ix.ginas.utils.validation.validators;

import ix.core.models.Role;
import ix.core.models.UserProfile;
import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.GinasUtils;
import ix.ginas.utils.validation.ValidationUtils;
import java.util.Arrays;
import java.util.List;
import play.Logger;

/**
 *
 * @author Mitch Miller
 */
public class SubstanceUniquenessValidator extends AbstractValidatorPlugin<Substance> {

	private final String DEFINITION_CHANGED = "Primary defintion of substance has changed!";
	private static final List<String> SubstanceClassesHandled = Arrays.asList("chemical", "mixture",
					"structurallyDiverse", "polymer", "concept", "specifiedSubstanceG1");


	@Override
	public void validate(Substance testSubstance, Substance oldSubstance, ValidatorCallback callback) {
		Logger.debug(String.format("starting in SubstanceUniquenessValidator. substance type: <%s>", testSubstance.substanceClass));
		if( !SubstanceClassesHandled.stream().anyMatch(s->s.equalsIgnoreCase(testSubstance.substanceClass.toString()))){
			Logger.debug("skipping this substance because of class");
			return;
		}
		if(testSubstance.getDefinitionalElements().getElements().isEmpty()){
			return;
		}
	
		List<Substance> fullMatches = ValidationUtils.findFullDefinitionalDuplicateCandidates(testSubstance);
		Logger.debug("total fullMatches " + fullMatches.size());
		if (fullMatches.size() > 0) {
			for (int i = 0; i < fullMatches.size(); i++) {
				Substance possibleMatch = fullMatches.get(i);

				UserProfile up=getCurrentUser();
				//MessageType messageType = MessageType.WARNING;
				String messageText = String.format("Substance %s (ID: %s) appears to be a full duplicate\n",
								possibleMatch.getName(), possibleMatch.uuid);
				GinasProcessingMessage mes;
				if( oldSubstance == null && (!up.hasRole(Role.SuperUpdate) || !up.hasRole(Role.SuperDataEntry))) {
						mes= GinasProcessingMessage.ERROR_MESSAGE(messageText);
				}else{
					mes= GinasProcessingMessage.WARNING_MESSAGE(messageText);
				}
				mes.addLink(GinasUtils.createSubstanceLink(possibleMatch));
				callback.addMessage(mes);
			}
		}
		else {
			List<Substance> matches = ValidationUtils.findDefinitionaLayer1lDuplicateCandidates(testSubstance);
			Logger.debug("substance of type " + testSubstance.substanceClass.name() + " total matches: " + matches.size());
			if (matches.size() > 0) {
				for (int i = 0; i < matches.size(); i++) {
					Substance possibleMatch = matches.get(i);
					String message = String.format("Substance %s (ID: %s) is a possible duplicate\n",
									possibleMatch.getName(), possibleMatch.uuid);
					Logger.debug("in SubstanceUniquenessValidator, creating warning with message " + message);
					GinasProcessingMessage mes = GinasProcessingMessage.WARNING_MESSAGE(message);
					Logger.debug("in SubstanceUniquenessValidator after message creation");
					mes.addLink(GinasUtils.createSubstanceLink(possibleMatch));
					callback.addMessage(mes);
				}
			}
		}
	}
}
