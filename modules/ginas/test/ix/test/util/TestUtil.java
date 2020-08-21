package ix.test.util;

import static org.junit.Assert.assertTrue;

import java.io.*;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ix.core.util.CachedSupplier;
import ix.ginas.utils.GinasGlobal;
import ix.ginas.utils.UNIIGenerator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.tools.*;

/**
 * Created by katzelda on 4/12/16.
 */
public class TestUtil {

    private static CachedSupplier<int[]> checkSumArray = CachedSupplier.of(()->{

        char[] chars = UNIIGenerator.alphabet;
        int[] array = new int[256];
        for(int i=0; i< chars.length; i++){
            array[chars[i]] = i;
        }
        return array;
    });


    public static String addUniiCheckDigit(String allButCheckDigit){
        char[] chars = allButCheckDigit.toCharArray();
        int[] array = checkSumArray.get();
        int sum=0;
        for(int i=0; i< chars.length; i++){
            sum+= array[chars[i]];
        }

        int checkDigit = sum % UNIIGenerator.alphabet.length;
        return allButCheckDigit + UNIIGenerator.alphabet[checkDigit];
    }
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
        //dir.getPath().delete();
        //Files.delete(dir.toPath());
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
    
    public static void waitForParam(String p){
        CountDownLatch cdl = new CountDownLatch(1);
        GinasGlobal.runWithRequestListener(()->{
            System.out.println("WAITING FOR '" +p + "' PARAM");
            try {
                cdl.await();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }, r->{
            if(r.getQueryString(p)!=null){
                cdl.countDown();
            }
        });
    }
    
    /**
     * A Builder that takes a bunch of java code
     * either as files or raw Strings and can compile them
     * and put the compiled {@code .class} files in the specified output
     * directory.
     */
    public static class JavaCompilerBuilder{

        private File outputDir;
        private List<SimpleJavaFileObject> javaFiles = new ArrayList<>();

        private volatile boolean compiled=false;

        public JavaCompilerBuilder(){
            this(null);
        }

        /**
         * Create a new instance and put all the compiled
         * class files in the specified root directory
         * @param outputDir
         */
        public JavaCompilerBuilder(File outputDir){
            if(outputDir !=null){
                outputDir.mkdirs();
            }
            this.outputDir = outputDir;
        }

        public JavaCompilerBuilder addClass(String fullyQualifiedJavaClassname, File javaSource){
            javaFiles.add(new MyJavaFileObject(fullyQualifiedJavaClassname, javaSource));
            return this;
        }
        public JavaCompilerBuilder addClass(String fullyQualifiedJavaClassname, String javaSource){
            javaFiles.add(new JavaStringObject(fullyQualifiedJavaClassname, javaSource));
            return this;
        }

        public JavaCompilerBuilder compile() throws IOException{
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();


            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

            StandardJavaFileManager fileManager =
                    compiler.getStandardFileManager(diagnostics, null, null);


            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, outputDir ==null? null : Collections.singleton(outputDir));


            JavaCompiler.CompilationTask task = compiler.getTask(null,
                    fileManager, diagnostics, null, null, javaFiles);

            if(!task.call()){
                throw new IOException("error compiling source : " + diagnostics.getDiagnostics().toString());
            }
            compiled=true;
            return this;
        }

        public void makeJar(File outputJar) throws IOException{
            if(!compiled){
                compile();
            }
            File parentFolder = outputJar.getParentFile();
            if(parentFolder !=null){
                parentFolder.mkdirs();
            }
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

            try(JarOutputStream out = new JarOutputStream(new FileOutputStream(outputJar), manifest)){
                if(outputDir ==null){
                    walkDir(out, new File(""),nameConverter());
                }else{
                    walkDir(out, outputDir, nameConverter());
                }
            }

        }

        private Function<File, String> nameConverter(){
            //output jar needs to use relative paths for package root
            //if we don't convert ot relative the path will be the absolute path
            //which means the classloader won't find the .class files because they
            //aren't in the expected location.

            Function<File, File> convertToRelative;
            if(outputDir ==null){
                convertToRelative = Function.identity();
            }else {
                convertToRelative = f -> outputDir.toPath().relativize(f.toPath()).toFile();
            }
            if(SystemUtils.IS_OS_WINDOWS){
                return convertToRelative.andThen( f-> f.getPath().replace('\\', '/'));
            }
            return convertToRelative.andThen(File::getPath);
        }
        private void walkDir(JarOutputStream out, File dir, Function<File, String> nameConverter) throws IOException{
            if(dir.isDirectory()){
                String name = nameConverter.apply(dir);
//                if(!name.isEmpty()) {
                    if (!name.endsWith("/")) {
                        name += "/";
                    }
                    JarEntry entry = new JarEntry(name);
                    out.putNextEntry(entry);
                    out.closeEntry();
//                }
                File[] children = dir.listFiles();
                if(children !=null){
                    for(File child : children){
                        walkDir(out, child, nameConverter);
                    }
                }
            }else{
                String name = nameConverter.apply(dir);
                JarEntry entry = new JarEntry(name);
                out.putNextEntry(entry);
                //apache IOUtils buffers for us so don't need tomake BufferedInputStream
                try(InputStream in = new FileInputStream(dir)) {
                    IOUtils.copy(in, out);
                }
                out.closeEntry();
            }
        }
    }
   private  static class JavaStringObject extends SimpleJavaFileObject {
        private final String source;


        public JavaStringObject(String fullClassNameWithPackage, String source) {
            super(URI.create("string:///" + fullClassNameWithPackage.replaceAll("\\.", "/") +
                    Kind.SOURCE.extension), Kind.SOURCE);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors)
                throws IOException {
            return source;
        }
    }

    private static class MyJavaFileObject extends SimpleJavaFileObject {
        private final File source;


        public MyJavaFileObject(String fullClassNameWithPackage, File source) {
            super(URI.create("file:///" + fullClassNameWithPackage.replaceAll("\\.", "/") +
                    Kind.SOURCE.extension), Kind.SOURCE);
            this.source = source;
        }

        @Override
        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            return new BufferedReader(new FileReader(source));
        }
    }

}
