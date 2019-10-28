package ix.seqaln;

import org.jcvi.jillion.align.NucleotideSubstitutionMatrices;
import org.jcvi.jillion.align.pairwise.NucleotidePairwiseSequenceAlignment;
import org.jcvi.jillion.align.pairwise.PairwiseAlignmentBuilder;
import org.jcvi.jillion.align.pairwise.PairwiseSequenceAlignment;
import org.jcvi.jillion.core.residue.nt.Nucleotide;
import org.jcvi.jillion.core.residue.nt.NucleotideSequence;

/**
 * Created by katzelda on 11/7/18.
 */
public class NucleotideSeqAlignmentHelper implements SequenceAlignmentHelper<Nucleotide, NucleotideSequence>{
    @Override
    public NucleotideSequence toSequence(String seq) {
        return NucleotideSequence.of(Nucleotide.cleanSequence(seq, "N"));
    }

    @Override
    public PairwiseAlignmentBuilder<Nucleotide, NucleotideSequence, NucleotidePairwiseSequenceAlignment> createAlignmentBuilder(NucleotideSequence query, NucleotideSequence target) {
        return PairwiseAlignmentBuilder.createNucleotideAlignmentBuilder(query, target, NucleotideSubstitutionMatrices.getIdentityMatrix());
    }
}
