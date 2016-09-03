package ix.core.search.text;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;

import ix.utils.ExecutionStack;


/**
 * Obviously not thread-safe. Don't try to use the same PathStack in two
 * different threads!
 * @author peryeata
 *
 */
public class PathStack implements ExecutionStack<String>{
	LinkedList<String> realStack = new LinkedList<String>();
	
	/* (non-Javadoc)
	 * @see ix.core.search.text.ExecutionStack#pushAndPopWith(K, java.lang.Runnable)
	 */
	@Override
	public void pushAndPopWith(String obj, Runnable r){
		realStack.push(obj);
		try{
			r.run();
		}finally{
			realStack.pop();
		}
	}
	
	/* (non-Javadoc)
	 * @see ix.core.search.text.ExecutionStack#getFirst()
	 */
	@Override
	public String getFirst() {
		return realStack.getFirst();
	}
	
	//Pretty lazy, really ... but don't optimize it
	public String toPath() {
		StringBuilder sb = new StringBuilder(256);
		// TP: Maybe do this?
		sb.append(TextIndexer.ROOT + "_"); //TODO: abstract this away somehow?

		for (Iterator<String> it = realStack.descendingIterator(); it.hasNext();) {
			String p =  it.next();
			if (!StringUtils.isNumeric( p)) {
				sb.append(p);
				if (it.hasNext())
					sb.append('_');
			}
		}
		return sb.toString();
	}
}