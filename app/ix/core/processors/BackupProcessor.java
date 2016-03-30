package ix.core.processors;


import ix.core.EntityProcessor;
import ix.core.controllers.BackupFactory;
import ix.core.models.BackupEntity;
import ix.core.models.BaseModel;

public class BackupProcessor implements EntityProcessor<BaseModel>{

	@Override
	public void prePersist(BaseModel obj) {
		
	}

	

	@Override
	public void preRemove(BaseModel obj) {
		
	}

	

	@Override
	public void preUpdate(BaseModel obj) {
		
	}
	
	@Override
	public void postPersist(BaseModel obj) {
		
		try{
			BackupEntity be = new BackupEntity();
			be.setInstantiated(obj);
			be.save();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void postUpdate(BaseModel obj) {
		try{
			BackupEntity be=BackupFactory.getByRefId(obj.fetchGlobalId());
			be.setInstantiated(obj);
			be.update();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	@Override
	public void postRemove(BaseModel obj) {
		BackupEntity be=BackupFactory.getByRefId(obj.fetchGlobalId());
		be.delete();
		
	}

	@Override
	public void postLoad(BaseModel obj) {
		
	}

	private static BackupProcessor _processor=new BackupProcessor();
	public static BackupProcessor getInstance(){
		if(_processor==null){
			_processor=new BackupProcessor();
		}
		return _processor;
	}
	

}
