package ix.core.stats;

import java.io.Serializable;

public class Estimate implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -2496288711650397338L;
		public enum TYPE{EXACT, APPROXIMATE, UNKNOWN, UPPER_BOUND, LOWER_BOUND};
		private long count;
		private TYPE type;
		
		public Estimate(){}
		
		public Estimate(long count, TYPE t){
			this.setCount(count);
			this.setType(t);				
		}
		public TYPE getType() {
			return type;
		}
		public void setType(TYPE type) {
			this.type = type;
		}
		public long getCount() {
			return count;
		}
		public void setCount(long count) {
			this.count = count;
		}
	}