package ix.ginas.models.v1;

import java.util.Comparator;

public enum SubunitComparator implements Comparator<Subunit> {

	INSTANCE;

	@Override
	public int compare(Subunit o1, Subunit o2) {
		if( o1.sequence == null && o2.sequence != null){
			return -1;
		} else if( o1.sequence != null && o2.sequence == null) {
			return 1;
		} else if( o1.sequence == null && o2.sequence == null) {
			return 0;
		}
		
		if( o1.sequence.length() < o2.sequence.length() ) {
			return -1;
		}
		if( o2.sequence.length() < o1.sequence.length()  ) {
			return 1;
		}
		return o1.sequence.compareTo(o2.sequence);

	}

}