package ix.ginas.models;

/**
 * http://en.wikipedia.org/wiki/Amino_acid
 */
public class AminoAcid {
    public final String name;
    public final String symbol;
    public final char alpha;

    private static AminoAcid[] _lookup = new AminoAcid[50];
    private AminoAcid (String name, String symbol, char alpha) {
	this.name = name;
	this.symbol = symbol;
	this.alpha = alpha;
	_lookup[alpha-'A'] = this;
    }

    public static AminoAcid get (char alpha) {
	int p = Character.toUpperCase(alpha) - 'A';
	return p < _lookup.length ? _lookup[p] : null;
    }

    public final static AminoAcid A = new AminoAcid ("Alanine", "Ala", 'A');
    public final static AminoAcid Alanine = A;
    public final static AminoAcid Ala = A;
    
    public final static AminoAcid R = new AminoAcid ("Arginine", "Arg", 'R');
    public final static AminoAcid Arginine = R;
    public final static AminoAcid Arg = R;
    
    public final static AminoAcid N = new AminoAcid ("Asparagine", "Asn", 'N');
    public final static AminoAcid Asparagine = N;
    public final static AminoAcid Asn = N;
    
    public final static AminoAcid D = new AminoAcid ("Aspartic", "Asp", 'D');
    public final static AminoAcid Aspartic = D;
    public final static AminoAcid Asp = D;
    
    public final static AminoAcid C = new AminoAcid ("Cysteine", "Cys", 'C');
    public final static AminoAcid Cysteine = C;
    public final static AminoAcid Cys = C;
    
    public final static AminoAcid E = new AminoAcid ("Glutamic", "Glu", 'E');
    public final static AminoAcid Glutamic = E;
    public final static AminoAcid Glu = E;
    
    public final static AminoAcid Q = new AminoAcid ("Glutamine", "Gln", 'Q');
    public final static AminoAcid Glutamine = Q;
    public final static AminoAcid Gln = Q;
    
    public final static AminoAcid G = new AminoAcid ("Glycine", "Gly", 'G');
    public final static AminoAcid Glycine = G;
    public final static AminoAcid Gly = G;
    
    public final static AminoAcid H = new AminoAcid ("Histidine", "His", 'H');
    public final static AminoAcid Histidine = H;
    public final static AminoAcid His = H;
    
    public final static AminoAcid I = new AminoAcid ("Isoleucine", "Ile", 'I');
    public final static AminoAcid Isoleucine = I;
    public final static AminoAcid Ile = I;
    
    public final static AminoAcid L = new AminoAcid ("Leucine", "Leu", 'L');
    public final static AminoAcid Leucine = L;
    public final static AminoAcid Leu = L;
    
    public final static AminoAcid K = new AminoAcid ("Lysine", "Lys", 'K');
    public final static AminoAcid Lysine = K;
    public final static AminoAcid Lys = K;
    
    public final static AminoAcid M = new AminoAcid ("Methionine", "Met", 'M');
    public final static AminoAcid Methionine = M;
    public final static AminoAcid Met = M;
    
    public final static AminoAcid F =
	new AminoAcid ("Phenylalanine", "Phe", 'F');
    public final static AminoAcid Phenylalanine = F;
    public final static AminoAcid Phe = F;

    public final static AminoAcid P = new AminoAcid ("Proline", "Pro", 'P');
    public final static AminoAcid Proline = P;
    public final static AminoAcid Pro = P;
    
    public final static AminoAcid S = new AminoAcid ("Serine", "Ser", 'S');
    public final static AminoAcid Serine = S;
    public final static AminoAcid Ser = S;
    
    public final static AminoAcid T = new AminoAcid ("Threonine", "Thr", 'T');
    public final static AminoAcid Threonine = T;
    public final static AminoAcid Thr = T;
    
    public final static AminoAcid W = new AminoAcid ("Tryptophan", "Trp", 'W');
    public final static AminoAcid Tryptophan = W;
    public final static AminoAcid Trp = W;
    
    public final static AminoAcid Y = new AminoAcid ("Tyrosine", "Tyr", 'Y');
    public final static AminoAcid Tyrosine = Y;
    public final static AminoAcid Tyr = Y;
    
    public final static AminoAcid V = new AminoAcid ("Valine", "Val", 'V');
    public final static AminoAcid Valine = V;
    public final static AminoAcid Val = V;
    

    /**
     * 21st & 22nd amino acids
     */
    public final static AminoAcid U =
	new AminoAcid ("Selenocysteine", "Sec", 'U');
    public final static AminoAcid Selenocysteine = U;
    public final static AminoAcid Sec = U;    

    public final static AminoAcid O = new AminoAcid ("Pyrrolysine", "Pyl", 'O');
    public final static AminoAcid Pyrrolysine = O;
    public final static AminoAcid Pyl = O;
    
    /**
     * ambiguous amino acids
     */
    public final static AminoAcid B =
	new AminoAcid ("Asparagine/Aspartic", "Asx", 'B');
    public final static AminoAcid Z =
	new AminoAcid ("Glutamine/Glutamic", "Glx", 'Z');
    public final static AminoAcid J =
	new AminoAcid ("Leucine/Isoleucine", "Xle", 'J');
    public final static AminoAcid X = new AminoAcid ("Unknown", "Xaa", 'X');
}
