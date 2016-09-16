package ix.ginas.processors;

import java.util.Optional;

import ix.core.EntityProcessor;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;

/**
 * This processor will help clean up public-domain records (no access restriction)
 * to have a "PUBLIC_DOMAIN_RELEASE" tag on a public domain reference.
 * 
 * Specifically, this is accomplished by finding the first public domain reference,
 * and adding the release tag. This is simply here to help users avoid the
 * headache of having to select this tag on every update to a record, if
 * import of data had previously not had this release tag.
 * 
 * @author peryeata
 *
 */
public class PublicTagFlagger implements EntityProcessor<Substance>{
	
	public PublicTagFlagger(){}

	@Override
	public void prePersist(Substance obj) throws ix.core.EntityProcessor.FailProcessingException {
		if(obj.isPublic()){
			//Need to get a public domain reference here
			Optional<Reference> pr=obj.references
				.stream()
				.filter(Reference::isPublicReleaseReference)
				.findAny();
			if(!pr.isPresent()){
				obj.references
				.stream()
				.filter(Reference::isPublicDomain)
				.findFirst().ifPresent(r->{
					r.makePublicReleaseReference();
					r.addTag("AUTO_SELECTED");
				});
			}
		}
	}
	
}