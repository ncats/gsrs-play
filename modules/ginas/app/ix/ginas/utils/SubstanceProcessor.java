package ix.ginas.utils;

import ix.ginas.modelBuilders.AbstractSubstanceBuilder;

public interface SubstanceProcessor<I, B extends AbstractSubstanceBuilder> {

    B process(I input, B builder);
}