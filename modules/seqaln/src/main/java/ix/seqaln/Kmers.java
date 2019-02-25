package ix.seqaln;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A basic class for kmer
 */
public class Kmers implements Comparator<String> {
    protected ConcurrentMap<String, BitSet> kmers =
        new ConcurrentHashMap<String, BitSet>();
    protected ConcurrentMap<String, Set<String>> neighbors =
        new ConcurrentHashMap<String, Set<String>>();
    protected final int K;
    
    public static final int FP_SIZE = 1024;

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



    public HoloFingerprint holoFingerPrint(){
    	return  HoloFingerprint.createFrom(this);
    }



    public int length(){
    	return positionEntrySet().stream().mapToInt(es->es.getValue().cardinality()).sum()+2;
    }


    public int getK () { return K; }

    public static class HoloFingerprint{
        private final int[] fp;
        private static final Pattern SEPARATOR = Pattern.compile("\\|");

        private int maxValue=0;

        public static HoloFingerprint createFrom(Kmers kmers){
            return new HoloFingerprint(kmers);
        }
        public String encode(){
            StringBuilder sb= new StringBuilder(fp.length*3);
            for(int i=0;i<fp.length;i++){
                int v = fp[i];
                if(v !=0){
                    sb.append(i).append(':').append(v).append('|');
                }
            }
            if(sb.length()==0){
            	return "";
            }
            return sb.substring(0, sb.length()-1);
        }
        public static HoloFingerprint decode(String fp){

            String[] sarray = SEPARATOR.split(fp);
            int[] intArray = new int[FP_SIZE];



            int maxValue=0;
            for(int i=0; i<sarray.length; i++){
                String v = sarray[i];
                if(v.isEmpty())continue;
                int index = v.indexOf(':');
                int offset = Integer.parseInt(v.substring(0, index));
                int value = Integer.parseInt(v.substring(index+1));
                intArray[offset] =value;
                if(value > maxValue){
                    maxValue = value;
                }
            }
            return new HoloFingerprint(intArray, maxValue);

        }

        private HoloFingerprint(int[] fp, int maxValue){
            this.fp = fp;
            this.maxValue = maxValue;
        }
        private HoloFingerprint(Kmers kmers){
            fp = new int[Kmers.FP_SIZE];

            for(Map.Entry<String, BitSet> es: kmers.positionEntrySet()){
                int count = es.getValue().cardinality();
                int pos = Math.abs(es.getKey().hashCode())%Kmers.FP_SIZE;
                fp[pos]+=count;
            }
            int max=0;
            for(int i=0; i<fp.length; i++){
                int v=fp[i];
                if(v > max){
                   max=v;
                }
            }
            maxValue = max;
        }


        public int hammingDistanceTo(HoloFingerprint other){
        	int[] o=hammingMoreAndLessDistanceTo(other);
            return o[0]+o[1];
        }
        public int[] hammingMoreAndLessDistanceTo(HoloFingerprint other){
            int[] ml = new int[2];
            ml[0]=0;
            ml[1]=0;


            for(int i=0;i<fp.length;i++){
                int hammingpart=fp[i]-other.fp[i];
                if(hammingpart>0){
                	ml[0]+=Math.abs(hammingpart);
                }else{
                	ml[1]+=Math.abs(hammingpart);
                }
            }
            return ml;
        }
    }


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
