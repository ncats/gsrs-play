package ix.ginas.processors;

import ix.core.EntityProcessor;
import ix.ginas.models.v1.ControlledVocabulary;

public class CVProcessor implements EntityProcessor<ControlledVocabulary>{

	@Override
	public void prePersist(ControlledVocabulary obj) {
		/*
		System.out.println("Vocab being updated");
		
		if(obj.domain.equals("ACCESS_GROUP")){
			Set<String> existingGroups= new HashSet<String>();	
			List<Group> groups=AdminFactory.groupfinder.findList();
			for(Group g: groups){
				existingGroups.add(g.name);
			}
			for(VocabularyTerm vt:obj.terms){
				if(!existingGroups.contains(vt.value)){
					System.out.println("Saving new group:" + vt.value);
					Group g = new Group();
					g.name=vt.value;
					g.save();
				}else{
					System.out.println("Already exists:" + vt.value);
				}
			}
		}*/
	}
	@Override
	public void preUpdate(ControlledVocabulary obj) {
		prePersist(obj);
	}

	@Override
	public void postPersist(ControlledVocabulary obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preRemove(ControlledVocabulary obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postRemove(ControlledVocabulary obj) {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public void postUpdate(ControlledVocabulary obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postLoad(ControlledVocabulary obj) {
		// TODO Auto-generated method stub
		
	}

}
