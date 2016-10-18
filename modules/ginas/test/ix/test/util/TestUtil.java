package ix.test.util;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Created by katzelda on 4/12/16.
 */
public class TestUtil {

    public static void tryToDeleteRecursively(File dir) throws IOException {
        if(!dir.exists()){
            return;
        }
        Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {


            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                //we've have NFS problems where there are lock
                //objects that we can't delete
                //should be safe to keep them and delete every other file.
                if(		!file.toFile().getName().startsWith(".nfs")
                    //&& !file.toFile().getName().endsWith(".cfs")
                        ){
                    //use new delete method which throws IOException
                    //if it can't delete instead of returning flag
                    //so we will know the reason why it failed.
                    try{
                        //System.out.println("Deleting:" + file);
                        Files.delete(file);
                    }catch(IOException e){
                        System.out.println(e.getMessage());
                    }
                }
                else{
                    //System.out.println("found nfs file " + file.toString());
                }


                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir,
                                                      IOException exc)
                    throws IOException{

                FileVisitResult fvr= super.postVisitDirectory(dir, exc);
                try{
                    Files.delete(dir);
                }catch(IOException e){
                    System.err.println("unable to delete:" + e.getMessage());
                }
                return fvr;
            }

        });
    }
    public static void assertContains(String within,String find){
    	String rep=within;
    	if(rep.length()>30){
    		rep=rep.substring(0, 30) + " ... {" + (within.length()-20) +" more characters}" ;
    	}
    	assertTrue("Should have found:'" + find + "' in '" + rep + "'" ,within.contains(find));
    }
    
    
    public static List<BitSet> allPermutations(int count){
    	List<BitSet> blist = new ArrayList<>();
    	
    	for(int i=0;i<Math.pow(2, count);i++){
    		BitSet bs = BitSet.valueOf(new long[]{i});
    		
    		char[] chars=Integer.toBinaryString(i).toCharArray();
    		for(int j=0;j<chars.length;j++){
    			if(chars[j]=='1'){
    				bs.set(j);
    			}
    		}
    		blist.add(bs);
    	}
    	return blist;
    	
    }
    
    
}
