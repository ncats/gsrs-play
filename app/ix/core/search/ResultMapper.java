package ix.core.search;

import java.util.stream.Stream;

public interface ResultMapper<R,T> {
	public Stream<T> map(R result);
}
