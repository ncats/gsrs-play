package ix.core.adapters;

import com.avaje.ebean.Transaction;
import com.avaje.ebean.event.TransactionEventListener;

public class TransactionListener implements TransactionEventListener {

	@Override
	public void postTransactionCommit(Transaction arg0) {
		InxightTransaction it=InxightTransaction.getTransaction(arg0);
		if(!it.isEnhanced()){
			it.runCommits();
			it.destroy();
		}
		it.runFinally();
		
		
	}

	@Override
	public void postTransactionRollback(Transaction arg0, Throwable arg1) {
		InxightTransaction it=InxightTransaction.getTransaction(arg0);
		it.runFinally();
	}

}
