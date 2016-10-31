package ix.core.search;

import java.util.stream.Stream;

public interface SingleRecordResultMapper<R,T> extends ResultMapper<R,T> {

	public T instrument(R result);
	
	default Stream<T> map(R result){
		T t= instrument(result);
		if(t==null){
			return Stream.empty();
		}else{
			return Stream.of(t);
		}
	}
}
