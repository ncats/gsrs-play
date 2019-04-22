package ix.seqaln;
import org.jcvi.jillion.align.pairwise.PairwiseAlignmentBuilder;
import org.jcvi.jillion.align.pairwise.PairwiseSequenceAlignment;
import org.jcvi.jillion.core.Sequence;
import org.jcvi.jillion.core.residue.Residue;
import org.jcvi.jillion.core.residue.ResidueSequence;

/**
 * Created by katzelda on 11/7/18.
 */
public interface SequenceAlignmentHelper<R extends Residue, T extends ResidueSequence<R, T, ?>> {

    T toSequence(String seq);


    <A extends PairwiseSequenceAlignment<R,T>> PairwiseAlignmentBuilder<R, T, A> createAlignmentBuilder(T query, T target);


    default PairwiseSequenceAlignment<R, T> align(T query, T target, int gapPenalty, SequenceIndexer.CutoffType rt){
        return createAlignmentBuilder(query, target)
                .gapPenalty(-gapPenalty)
                .useGlobalAlignment(rt== SequenceIndexer.CutoffType.GLOBAL)
                .build();

    }

    static SequenceAlignmentHelper createFor(String seqType){

        if("Protein".equalsIgnoreCase(seqType)) {
            return new ProteinSeqAlignmentHelper();

        }
        return new NucleotideSeqAlignmentHelper();

    }

}
