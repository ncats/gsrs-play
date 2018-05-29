package ix.seqaln;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

/**
 * A basic class for kmer
 */
public class Kmers implements Comparator<String> {
    protected ConcurrentMap<String, BitSet> kmers =
        new ConcurrentHashMap<String, BitSet>();
    protected ConcurrentMap<String, Set<String>> neighbors =
        new ConcurrentHashMap<String, Set<String>>();
    protected final int K;
    
    protected Kmers () {
        this (3);
    }
    protected Kmers (int K) {
        if (K <= 0)
            throw new IllegalArgumentException ("Bogus K value "+K);
        this.K = K;
    }
    public int compare (String s1, String s2) {
        BitSet k1 = kmers.get(s1);
        BitSet k2 = kmers.get(s2);
        return k1.nextSetBit(0) - k2.nextSetBit(0);
    }
    public int size () { return kmers.size(); }
    public Set<String> kmers () {
        Set<String> ks = new TreeSet<String>(this);
        ks.addAll(kmers.keySet());
        return ks;
    }

    public BitSet positions (String kmer) {
        return kmers.get(kmer);
    }
    
    private Kmers add (String kmer, int pos) {
        kmers.computeIfAbsent(kmer, (k)-> new BitSet()).set(pos);
        return this;
    }
    
    public Kmers add (String kmer, String neighbor) {

        neighbors.computeIfAbsent(kmer, (k)->new TreeSet<String>()).add(neighbor);

        return this;
    }

    public Set<Map.Entry<String, BitSet>> positionEntrySet(){
        return kmers.entrySet();
    }

    public int getK () { return K; }

    

    public static Kmers create (String seq, int K) {
        Kmers kmers = new Kmers (K);
        char[] chars = seq.toUpperCase().toCharArray();
        int len = seq.length() - K+1;
        for (int i = 0; i < len; ++i) {
            char[] kmer = new char[K];
            for (int j = i, k = 0; k < K; ++k) {
                kmer[k] = chars[j++];
            }
            kmers.add(new String (kmer), i);
        }
        return kmers;
    }

    public static void main (String[] argv) throws Exception {
        String text = "ABCDABCHIJKLMN";
        System.out.println("+++ \""+text+"\"");
        Kmers kmers = Kmers.create(text, 3);
        for (String k : kmers.kmers()) {
            System.out.println(k+": "+kmers.positions(k));
        }
    }
}
