package ix.ginas.models.utils;

import ix.core.models.ProcessingRecord;
import ix.core.plugins.GinasRecordProcessorPlugin.PayloadExtractedRecord;
import ix.core.plugins.GinasRecordProcessorPlugin.RecordTransformer;
import ix.ginas.models.v1.Substance;

import java.util.Map;

public class GinasFlatMapTransformer extends RecordTransformer<Map<String,String>,Substance>{

	@Override
	public Substance transform(PayloadExtractedRecord<Map<String,String>> pr, ProcessingRecord rec) {
		System.out.println("############## transforming:" + pr.theRecord.get("name"));
		try{
			rec.name = pr.theRecord.get("name");
		}catch(Exception e){
			rec.name = "Nameless";
		}
		rec.job = pr.job;
		rec.start = System.currentTimeMillis();
		Substance struc = null;
		try {
			struc = GinasSDFExtractor.convertToStructure(pr.theRecord);
			rec.status = ProcessingRecord.Status.ADAPTED;
		} catch (Throwable t) {
			rec.stop = System.currentTimeMillis();
			rec.status = ProcessingRecord.Status.FAILED;
			rec.message = t.getMessage();
			t.printStackTrace();
		}
		return struc;
	}

	

}
