package ix.ginas.processors;

import java.util.ArrayList;
import java.util.List;

import ix.core.EntityProcessor;
import ix.ginas.models.v1.Name;

public class FDANameNormalizer implements EntityProcessor<Name>{
	
	public FDANameNormalizer(){
		
	}
	
	static final String transforms=".ALPHA.	\u03b1\n" + 
		".BETA.	\u03b2\n" + 
		".GAMMA.	\u03b3\n" + 
		".DELTA.	\u03b4\n" + 
		".EPSILON.	\u03b5\n" + 
		".ZETA.	\u03b6\n" + 
		".ETA.	\u03b7\n" + 
		".THETA.	\u03b8\n" + 
		".IOTA.	\u03b9\n" + 
		".KAPPA.	\u03ba\n" + 
		".LAMBDA.	\u03bb\n" + 
		".MU.	\u03bc\n" + 
		".NU.	\u03bd\n" + 
		".XI.	\u03be\n" + 
		".OMICRON.	\u03bf\n" + 
		".PI.	\u03c0\n" + 
		".RHO.	\u03c1\n" + 
		".SIGMA.	\u03c3\n" + 
		".TAU.	\u03c4\n" + 
		".UPSILON.	\u03c5\n" + 
		".PHI.	\u03c6\n" + 
		".CHI.	\u03c7\n" + 
		".PSI.	\u03c8\n" + 
		".OMEGA.	\u03c9\n" +
		"+/-	\u00b1\n";
	
	
	static List<String> transformList = new ArrayList<String>();
	static{
		for(String line:transforms.split("\n")){
			transformList.add(line);
		}
	}
	
	public static String toFDA(String name){
		for(String trans:transformList){
			name=name.replace(trans.split("\t")[1], trans.split("\t")[0]);
		}
		name=name.toUpperCase().trim();
		return name;
	}
	
	public static String fromFDA(String name){
		for(String trans:transformList){
			name=name.replace(trans.split("\t")[0], trans.split("\t")[1]);
		}
		return name;
	}
	
	public static String difference(String str1, String str2) {
	    if (str1 == null) {
	        return str2;
	    }
	    if (str2 == null) {
	        return str1;
	    }
	    int at = indexOfDifference(str1, str2);
	    if (at == -1) {
	        return "EMPTY";
	    }
	 return str2.substring(at);
	}
	public static int indexOfDifference(String str1, String str2) {
	    if (str1 == str2) {
	        return -1;
	    }
	    if (str1 == null || str2 == null) {
	        return 0;
	    }
	    int i;
	    for (i = 0; i < str1.length() && i < str2.length(); ++i) {
	        if (str1.charAt(i) != str2.charAt(i)) {
	            break;
	        }
	    }
	    if (i < str2.length() || i < str1.length()) {
	        return i;
	    }
	    return -1;
	}

	@Override
	public void prePersist(Name obj) {
		String name = obj.getName();
		obj.stdName=fromFDA(name);
	}
	
	@Override
	public void preUpdate(Name obj) {
		prePersist(obj);
	}
	
}