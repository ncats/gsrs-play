package ix.core.processing;

import ix.core.models.ProcessingRecord;
import ix.core.plugins.GinasRecordProcessorPlugin.PayloadExtractedRecord;

public abstract class RecordTransformer<K,T>{
		public abstract T transform(PayloadExtractedRecord<K> pr,ProcessingRecord rec);		
	}
	
	