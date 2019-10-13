package ix.seqaln;

import org.jcvi.jillion.align.AminoAcidSubstitutionMatrix;
import org.jcvi.jillion.align.SequenceAlignment;
import org.jcvi.jillion.align.pairwise.PairwiseAlignmentBuilder;
import org.jcvi.jillion.align.pairwise.PairwiseSequenceAlignment;
import org.jcvi.jillion.align.pairwise.ProteinPairwiseSequenceAlignment;
import org.jcvi.jillion.core.residue.aa.AminoAcid;
import org.jcvi.jillion.core.residue.aa.ProteinSequence;

/**
 * Created by katzelda on 11/7/18.
 */
class ProteinSeqAlignmentHelper implements SequenceAlignmentHelper<AminoAcid, ProteinSequence>{

    @Override
    public PairwiseAlignmentBuilder<AminoAcid, ProteinSequence, ProteinPairwiseSequenceAlignment> createAlignmentBuilder(ProteinSequence query, ProteinSequence target) {
        return PairwiseAlignmentBuilder.createProteinAlignmentBuilder(query, target, MATRIX);
    }

    private static AminoAcidSubstitutionMatrix MATRIX = (a, b) -> a==b? 1: 0;

    @Override
    public ProteinSequence toSequence(String seq) {
        return ProteinSequence.of(AminoAcid.cleanSequence(seq, "X"));
    }
}
