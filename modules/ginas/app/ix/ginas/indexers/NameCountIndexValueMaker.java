package ix.ginas.indexers;

import java.util.function.Consumer;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.core.search.text.IndexableValueFromRaw;
import ix.ginas.models.v1.Substance;

public class NameCountIndexValueMaker implements IndexValueMaker<Substance>{

	@Override
	public void createIndexableValues(Substance t, Consumer<IndexableValue> consumer) {
		int nc=t.getNameCount();
		consumer.accept(new IndexableValueFromRaw("Name Count", nc).dynamic());
	}

}
