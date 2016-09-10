package ix.ginas.processors;

import ix.core.EntityProcessor;
import ix.core.models.Group;
import ix.ginas.controllers.v1.ControlledVocabularyFactory;
import ix.ginas.models.v1.ControlledVocabulary;
import ix.ginas.models.v1.VocabularyTerm;
import play.Logger;

public class GroupProcessor implements EntityProcessor<Group>{

	@Override
	public void prePersist(Group obj) {
		Logger.debug("Adding a group to CV");
		ControlledVocabulary cvv = ControlledVocabularyFactory.getControlledVocabulary("ACCESS_GROUP");
		VocabularyTerm vt=cvv.getTermWithValue(obj.name);
		Logger.debug("The domain is:" + cvv.domain + " with " + cvv.terms.size() + " terms");
		if(vt==null){
			Logger.debug("Group didn't exist before");
			vt = new VocabularyTerm();
			vt.display=obj.name;
			vt.value=obj.name;
			vt.save();
			cvv.addTerms(vt);
			cvv.save();
		}
	}
	@Override
	public void preUpdate(Group obj) {
		prePersist(obj);
	}
	
	@Override
	public void postPersist(Group obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preRemove(Group obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postRemove(Group obj) {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public void postUpdate(Group obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postLoad(Group obj) {
		// TODO Auto-generated method stub
		
	}
	

}
