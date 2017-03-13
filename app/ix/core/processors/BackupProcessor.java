package ix.core.processors;


import ix.core.EntityProcessor;
import ix.core.controllers.BackupFactory;
import ix.core.models.BackupEntity;
import ix.core.models.BaseModel;

import java.util.Optional;

public class BackupProcessor implements EntityProcessor<BaseModel>{
	
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
			Optional<BackupEntity> be=BackupFactory.getByRefId(obj.fetchGlobalId());
			if(be.isPresent()) {
				BackupEntity entity = be.get();
				entity.setInstantiated(obj);
				entity.save();
			}else{
				//for some reason the previous version
				//didn't save...so create a new record.
				postPersist(obj);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	@Override
	public void postRemove(BaseModel obj) {
		Optional<BackupEntity> be=BackupFactory.getByRefId(obj.fetchGlobalId());
		if(be.isPresent()){
			be.get().delete();
		}
		
	}


	private static BackupProcessor _processor=new BackupProcessor();
	public synchronized static BackupProcessor getInstance(){
		if(_processor==null){
			_processor=new BackupProcessor();
		}
		return _processor;
	}
	

}
