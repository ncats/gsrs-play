package ix.core.search.text;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;

import ix.utils.ExecutionStack;

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
	
	public String toPath() {
		StringBuilder sb = new StringBuilder(256);
		// TP: Maybe do this?
		sb.append(TextIndexer.ROOT + "_");

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