package ix.seqaln;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

/**
 * A basic class for kmer
 */
public class Kmers {
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

    public int size () { return kmers.size(); }
    public Set<String> kmers () {
        return kmers.keySet();
    }

    public BitSet positions (String kmer) {
        return kmers.get(kmer);
    }
    
    public Kmers add (String kmer, int pos) {
        BitSet bs = new BitSet ();
        BitSet old = kmers.putIfAbsent(kmer, bs);
        if (old != null)
            bs = old;
        bs.set(pos);
        return this;
    }
    
    public Kmers add (String kmer, String neighbor) {
        Set<String> nb = new TreeSet<String>();
        Set<String> old = neighbors.putIfAbsent(kmer, nb);
        if (old != null)
            nb = old;
        nb.add(neighbor);
        return this;
    }

    public int getK () { return K; }

    public static Kmers create (CharSequence seq) {
        return create (seq, 3);
    }
    
    public static Kmers create (CharSequence seq, int K) {
        Kmers kmers = new Kmers (K);
        int len = seq.length() - K+1;
        for (int i = 0; i < len; ++i) {
            char[] kmer = new char[K];
            for (int j = i, k = 0; k < K; ++k) {
                kmer[k] = seq.charAt(j++);
            }
            kmers.add(new String (kmer).toUpperCase(), i);
        }
        return kmers;
    }

    public static void main (String[] argv) throws Exception {
        String text = "ABCDABCHIJKLMN";
        System.out.println("+++ \""+text+"\"");
        Kmers kmers = Kmers.create(text);
        for (String k : kmers.kmers()) {
            System.out.println(k+": "+kmers.positions(k));
        }
    }
}
