package ix.core.adapters;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import ix.utils.Util;

public class InxightTransaction {
	private static ConcurrentHashMap<Transaction, InxightTransaction> _instances=
			new ConcurrentHashMap<Transaction, InxightTransaction>();

	
	public static InxightTransaction getTransaction(Transaction t){
		
		InxightTransaction it=_instances.get(t);
		if(it!=null){
			return it;
		}else{
			it= new InxightTransaction(t);
			it.setEnhanced(false);
			return it;
		}
	}
	
	public static void cleanupTransactions(){
		//System.out.println("Looking at:" + _instances.size() + " stored transactions");
		List<InxightTransaction> toRemove = new ArrayList<InxightTransaction>();
		Iterator<Transaction> trans= _instances.keySet().iterator();
		while(trans.hasNext()){
			Transaction t1=trans.next();
			if(!t1.isActive()){
				toRemove.add(_instances.get(t1));
			}
		}
		for(InxightTransaction it: toRemove){
			it.destroy();
		}
	}
	
	public Transaction t;
	
	private boolean enhanced=true;
	
	private List<Callable> afterCommit=new ArrayList<Callable>();
	
	public void addPostCommitCall(Callable c){
			afterCommit.add(c);
	}
	public void addPostCommitRun(Runnable r){
		afterCommit.add(new Callable(){

			@Override
			public Object call() throws Exception {
				r.run();
				return null;
			}
			
		});
	}

	public static InxightTransaction beginTransaction(){
		return new InxightTransaction(Ebean.beginTransaction());
	}
	
	public InxightTransaction(Transaction t){
		this.t=t;
		_instances.put(t, this);
//		System.out.println("instances map = " + _instances);
//		if(_instances.size() >1){
//			Util.printExecutionStackTrace();
//		}
	}
	
	
	public void addModification(String arg0, boolean arg1, boolean arg2, boolean arg3) {
		t.addModification(arg0, arg1, arg2, arg3);
	}

	public void batchFlush() throws PersistenceException, OptimisticLockException {
		t.batchFlush();
	}

	public void close() throws IOException {
		t.close();
		
	}

	public void commit() throws RollbackException {
		t.commit();
		runCommits();
	}

	public void end() throws PersistenceException {
		t.end();
		//System.out.println("Destroy explicit");
		destroy();
	}
	
	public void runCommits(){
		afterCommit.forEach(new Consumer<Callable>(){
			@Override
			public void accept(Callable t) {
				try {
					t.call();	
				}catch(Exception e){
					e.printStackTrace(); //keep going
				}
			}
		});
			
		afterCommit.clear();
	}
	
	public void destroy(){
		_instances.remove(t);
		//System.out.println("destroying transaction:" + t.isActive());
	}

	public void flushBatch() throws PersistenceException, OptimisticLockException {
		t.flushBatch();
	}

	public Connection getConnection() {
		return t.getConnection();
	}

	public Object getUserObject(String arg0) {
		return t.getUserObject(arg0);
	}

	public boolean isActive() {
		return t.isActive();
	}

	public boolean isBatchFlushOnQuery() {
		return t.isBatchFlushOnQuery();
	}

	public boolean isReadOnly() {
		return t.isReadOnly();
	}

	public void putUserObject(String arg0, Object arg1) {
		t.putUserObject(arg0, arg1);
	}

	public void rollback() throws PersistenceException {
		t.rollback();
	}

	public void rollback(Throwable arg0) throws PersistenceException {
		t.rollback(arg0);
	}

	public void setBatchFlushOnMixed(boolean arg0) {
		t.setBatchFlushOnMixed(arg0);
	}

	public void setBatchFlushOnQuery(boolean arg0) {
		t.setBatchFlushOnQuery(arg0);
	}

	public void setBatchGetGeneratedKeys(boolean arg0) {
		t.setBatchGetGeneratedKeys(arg0);
	}

	public void setBatchMode(boolean arg0) {
		t.setBatchMode(arg0);
	}

	public void setBatchSize(int arg0) {
		t.setBatchSize(arg0);
	}

	public void setPersistCascade(boolean arg0) {
		t.setPersistCascade(arg0);
	}

	public void setReadOnly(boolean arg0) {
		t.setReadOnly(arg0);
	}
	public boolean isEnhanced() {
		return enhanced;
	}
	public void setEnhanced(boolean enhanced) {
		this.enhanced = enhanced;
	}
	

}
