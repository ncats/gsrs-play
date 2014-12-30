package ix.ginas.models;

import javax.persistence.*;
import ix.core.models.VIntArray;

@Entity
@DiscriminatorValue("PFP")
public class PathFingerprint extends VIntArray {
    public int bits; // number of bits turned on for each pattern
    public int depth; // recursion depth

    public PathFingerprint () {}
    /**
     * size: length of fingerprint in Ints (i.e., 32-bit)
     * bits: number of bits used for each path
     * depth: recursion depth
     */
    public PathFingerprint (int[] fp, int bits, int depth) {
	this.bits = bits;
	this.depth = depth;
	setArray (fp);
    }
}
