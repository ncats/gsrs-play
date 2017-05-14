package ix.ginas.utils.validation;

import java.util.List;

public interface DuplicateFinder<T> {
    public List<T> findPossibleDuplicatesFor(T t);
}
