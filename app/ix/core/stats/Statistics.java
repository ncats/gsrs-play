package ix.core.stats;

import java.io.Serializable;

public class Statistics implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -5730152930484560692L;
		public int recordsExtractedSuccess=0;
		public int recordsProcessedSuccess=0;
		public int recordsPersistedSuccess=0;
		
		public int recordsExtractedFailed=0;
		public int recordsProcessedFailed=0;
		public int recordsPersistedFailed=0;
		
		private long start=System.currentTimeMillis();
		
		public enum CHANGE{	ADD_EX_GOOD,
							ADD_EX_BAD,
							ADD_PR_GOOD,
							ADD_PR_BAD,
							ADD_PE_GOOD,
							ADD_PE_BAD, MARK_EXTRACTION_DONE, EXPLICIT_CHANGE
			};
		
		public Estimate totalRecords=null;
		
		private CHANGE lastChangeAction=null;
		private long lastChangeVersion=0;
		
		
		public String toString(){
			String msg = "Extracted: " + recordsExtractedSuccess + " (" + recordsExtractedFailed + " failed)\n";
			msg += "Processed: " + recordsProcessedSuccess + " (" + recordsProcessedFailed + " failed)\n";
			msg += "Persisted: " + recordsPersistedSuccess + " (" + recordsPersistedFailed + " failed)\n";
			if(totalRecords!=null)
				msg += "Total rec: " + totalRecords.getCount() + " (" + totalRecords.getType().toString() + ")\n";
			return msg;			
		}
		 
		
		public int totalFailedAndPersisted() {
			return recordsExtractedFailed+recordsProcessedFailed+recordsPersistedFailed+recordsPersistedSuccess;
		}
		
		public void markExtractionDone(){
			totalRecords= new Estimate(recordsExtractedSuccess+recordsExtractedFailed, Estimate.TYPE.EXACT);
		}
		
		public boolean _isDone(){
			if(totalFailedAndPersisted()>=totalRecords.getCount() && totalRecords.getType()==Estimate.TYPE.EXACT){
				return true;
			}
			return false;
		}


		public long getAverageTimeToPersistMS(long end){
			return (end-start)/recordsPersistedSuccess;
		}
		
		public void applyChange(CHANGE c){
			if(c!=null){
				this.lastChangeAction=c;
				switch(c){
					case ADD_EX_BAD:
						recordsExtractedFailed++;
						break;
					case ADD_EX_GOOD:
						recordsExtractedSuccess++;
						break;
					case ADD_PE_BAD:
						recordsPersistedFailed++;
						break;
					case ADD_PE_GOOD:
						recordsPersistedSuccess++;
						break;
					case ADD_PR_BAD:
						recordsProcessedFailed++;
						break;
					case ADD_PR_GOOD:
						recordsProcessedSuccess++;
						break;
					case MARK_EXTRACTION_DONE:
						this.markExtractionDone();
						break;
					default:
						break;
				}
			}
			lastChangeVersion++;			
		}


		public void applyChange(Statistics s) {
			this.applyChange(s.lastChangeAction);
		}


		public boolean isNewer(Statistics st) {
			return this.lastChangeVersion > st.lastChangeVersion;
		}
		
	}