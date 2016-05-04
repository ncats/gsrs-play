package ix.core.stats;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import ix.core.util.TimeUtil;

public class Statistics implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -5730152930484560692L;
		public AtomicInteger recordsExtractedSuccess= new AtomicInteger();
		public AtomicInteger recordsProcessedSuccess=new AtomicInteger();
		public AtomicInteger recordsPersistedSuccess=new AtomicInteger();
		
		public AtomicInteger recordsExtractedFailed=new AtomicInteger();
		public AtomicInteger recordsProcessedFailed=new AtomicInteger();
		public AtomicInteger recordsPersistedFailed=new AtomicInteger();
		
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
		private AtomicLong lastChangeVersion = new AtomicLong();
		
		
		public String toString(){
			String msg = "Extracted: " + recordsExtractedSuccess.get() + " (" + recordsExtractedFailed.get() + " failed)\n";
			msg += "Processed: " + recordsProcessedSuccess.get() + " (" + recordsProcessedFailed.get() + " failed)\n";
			msg += "Persisted: " + recordsPersistedSuccess.get() + " (" + recordsPersistedFailed.get() + " failed)\n";
			if(totalRecords!=null)
				msg += "Total rec: " + totalRecords.getCount() + " (" + totalRecords.getType().toString() + ")\n";
			return msg;			
		}
		 
		
		public int totalFailedAndPersisted() {
			return recordsExtractedFailed.get()+recordsProcessedFailed.get()+recordsPersistedFailed.get()+recordsPersistedSuccess.get();
		}
		
		public void markExtractionDone(){
			totalRecords= new Estimate(recordsExtractedSuccess.get()+recordsExtractedFailed.get(), Estimate.TYPE.EXACT);
		}
		
		public boolean _isDone(){
			if(totalFailedAndPersisted()>=totalRecords.getCount() && totalRecords.getType()==Estimate.TYPE.EXACT){
				return true;
			}
			return false;
		}

		public double getAverageTimeToPersist(){
			return this.getAverageTimeToPersistMS(TimeUtil.getCurrentTimeMillis());
		}
		
		public long getEstimatedTimeLeft(){
			long totalDone= totalFailedAndPersisted();
			long total=1;
			if(this.totalRecords!=null){
				total = this.totalRecords.getCount();
			}
			
			long totalRemaining=total-totalDone;
			long timeSoFar=TimeUtil.getCurrentTimeMillis()-start;
			
			double avg=timeSoFar/Math.max(totalDone,1);
			return (long) (totalRemaining*avg);
		}

		public double getAverageTimeToPersistMS(long end){
			long totalPersist=recordsPersistedSuccess.get();
			if(totalPersist==0){
				totalPersist=1;
			}
			return (end-start)/(totalPersist + 0.0);
		}
		
		public void applyChange(CHANGE c){
			if(c!=null){
				this.lastChangeAction=c;

				//DO NOT REORDER CASE STATEMENTS
				//the order of the case statements
				//is in the same order as the declarations in the enum
				//this should compile to a jump table instead of a table switch
				//which is an O(1) switch instead of a O(n)
				//so it should be faster.
                //if we ever add new values to the enum
                //make sure they get put here in the same order
                //enum values that shouldn't be processed
                //should either be the first or last values
                //so we have a continuous block
				switch(c){

					case ADD_EX_GOOD:
						recordsExtractedSuccess.getAndIncrement();
						break;
					case ADD_EX_BAD:
						recordsExtractedFailed.getAndIncrement();
						break;
					case ADD_PR_GOOD:
						recordsProcessedSuccess.getAndIncrement();
						break;
					case ADD_PR_BAD:
						recordsProcessedFailed.getAndIncrement();
						break;
					case ADD_PE_GOOD:
						recordsPersistedSuccess.getAndIncrement();
						break;
					case ADD_PE_BAD:
						recordsPersistedFailed.getAndIncrement();
						break;
					case MARK_EXTRACTION_DONE:
						this.markExtractionDone();
						break;
					default:
						break;
				}
			}
			lastChangeVersion.getAndIncrement();
		}


		public void applyChange(Statistics s) {
			this.applyChange(s.lastChangeAction);
		}


		public boolean isNewer(Statistics st) {

            return this.lastChangeVersion.get() > st.lastChangeVersion.get();
		}
		
	}