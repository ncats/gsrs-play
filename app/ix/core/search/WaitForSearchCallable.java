package ix.core.search;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

class WaitForSearchCallable implements Callable<List>, SearchResultDoneListener{

	private final SearchResult result;
	
	private final CountDownLatch latch;
	final private boolean waitForAll;

	public WaitForSearchCallable(final SearchResult result){
		Objects.requireNonNull(result);
		this.result = result;
        waitForAll = true;
        latch = new CountDownLatch(result.finished()? 0 :1);

		this.result.addListener(this);
	}
	
    public WaitForSearchCallable(final SearchResult result, int numberOfRecords){
        Objects.requireNonNull(result);
        this.result = result;
        //can't have negative counts

        int count = Math.max(0, numberOfRecords - result.size());
        latch = new CountDownLatch(result.finished()? 0 : count);
        waitForAll = false;

        this.result.addListener(this);
    }
	@Override
	public List call() throws Exception {
		if(latch.getCount()>0 && !result.finished()){
//            System.out.println("awaiting....");
			latch.await();
		}
//        System.out.println("done await!!!");
        result.removeListener(this);
		return result.getMatches();
	}
	@Override
	public void searchIsDone() {
		while(latch.getCount()>0){
            latch.countDown();
        }
	}

    @Override
    public void added(Object o) {
        if(!waitForAll){
            latch.countDown();
        }
    }
}