package ix.utils;

import java.util.Optional;

/**
 * A very limited form of a "stack", typically useful
 * for ensuring that whatever you've added gets removed
 * when the process is done.
 * @author peryeata
 *
 * @param <K>
 */
public interface ExecutionStack<K> {

	void pushAndPopWith(K obj, Runnable r);

	K getFirst();

	Optional<K> getOptionalFirst();

	void setMaxDepth(Integer maxDepth);

}