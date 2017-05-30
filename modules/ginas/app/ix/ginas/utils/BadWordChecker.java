package ix.ginas.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by katzelda on 5/30/17.
 */
public class BadWordChecker implements Predicate<String>{

    private static Set<String> BAD_WORDS;

    private static String BASE64_ENCODED_WORDS = "NHI1ZQo1aDF0CjVoaXQKYTJtCmE1NQphcjVlCmIwMGJzCmIxN2NoCmIxdGNoCmMwY2sKY2wxdApk" +
                                                "MWNrCmY0bm55CmZ1eDByCm0wZjAKbTBmbwptb2YwCm4xZ2dhCnAwcm4Kc2gxdAp0MXR0MWU1CnR3" +
                                                "NHQKdjE0Z3JhCnYxZ3JhCncwMHNlCg==";
    static{
        try {
            BAD_WORDS = decodeBadWords();

//            for(String s : BAD_WORDS){
//                System.out.println(s);
//            }
        } catch (IOException e) {
            throw new IllegalStateException("could not base 64 decode bad word list", e);
        }
    }

    private static Set<String> decodeBadWords() throws IOException{
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(Base64.getDecoder().decode(BASE64_ENCODED_WORDS))))){
            return reader.lines()
                    .flatMap(s -> Stream.of(s, s.toUpperCase()))
                    .collect(Collectors.toSet());
        }
    }

    public boolean containsBadWords(String s){
        for(int i=4; i< 6; i++){
            if(containsBadWord(s, i)){
                return true;
            }
        }
        return false;
    }

    public boolean containsBadWord(String s, int ngramLength) {
        for(String ngram : createNGram(ngramLength, s)){
//            System.out.println("\tchecking " + ngram);
            if(BAD_WORDS.contains(ngram)){
//                System.out.println("\t\tFOUND!!!!!");
                return true;
            }
        }
        return false;
    }

    public boolean isClean(String s){
        return !containsBadWords(s);
    }
    @Override
    public boolean test(String s) {
        return isClean(s);
    }


    private List<String> createNGram(int n, String s){
        List<String> list = new ArrayList<>();
        for(int i=0; i< s.length() -n+1; i++){
            list.add(s.substring(i, i+n));
        }
        return list;
    }


    public static void main(String[] args){
        BadWordChecker checker = new BadWordChecker();

        for(String s : args){
            System.out.printf("%s  = %s%n", s, checker.containsBadWords(s));
        }
    }
}
