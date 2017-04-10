package ix.core.chem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ix.core.util.StreamUtil;

public class ChemCleaner {
	/**
	 * Returns a cleaner form of a molfile, with some common jsdraw
	 * polymer parts re-interpreted
	 * @param mfile
	 * @return
	 */
	public static String getCleanMolfile(String mfile) {
		if(!mfile.contains("M  END"))return mfile;
		// JSdraw adds this to some S-GROUPS
		mfile = mfile.replaceAll("M  SPA[^\n]*\n", "");
		mfile = mfile.replaceAll("M  END\n", "");
		
		Matcher m = Pattern.compile("M  STY  2 (...) GEN (...) DAT").matcher(mfile);
		List<String> addList = new ArrayList<String>();
		Map<String,String> toreplace = new HashMap<String,String>();
		while (m.find()) {
			String group1= m.group(1);
			String group2=  m.group(2);
			toreplace.put(m.group(), "M  STY  2 " + group1 + " SRU "+group2+" DAT");
			Matcher m2 = Pattern.compile("M  SED " + group2 + " ([^\n]*)").matcher(
					mfile);
			m2.find();
			String lab=m2.group(1);
			String add="M  SMT " + group1 + " " + lab + "\n";
			addList.add(add);
			//M  STY  1   2 SRU
		}
		 // JSDraw sometimes repeats SGROUP indexes by accident
		 // take inventory of all SRU sgroups, to help
		 m = Pattern.compile("M  STY  1 (...) SRU").matcher(mfile);
		
		 List<String> nolabelsrus = new ArrayList<String>();
		 while (m.find()) {
				String group1= m.group(1);
				nolabelsrus.add(group1.trim());
		 }
		 
		 if(!nolabelsrus.isEmpty()){
			 // "M  SMT   1"
			 m = Pattern.compile("M  SMT (...) ([^ ]*)").matcher(mfile);
			 
			 while (m.find()) {
				 	String group1= m.group(1).trim();
					String group2= m.group(2);
				 	//if this is real
				 	if(nolabelsrus.contains(group1)){
				 		nolabelsrus.remove(group1);
				 	}else{
				 		if(!nolabelsrus.isEmpty()){
				 			String newlabel=("  " + nolabelsrus.get(0));
				 			newlabel = newlabel.substring(newlabel.length()-3, newlabel.length());
				 			toreplace.put(m.group(), "M  SMT " + newlabel + " " + group2);
				 			nolabelsrus.remove(0);
				 		}
				 	}
			 }
		 }
		 
		 
		 
		 
		for(String key:toreplace.keySet()){
			mfile=mfile.replace(key, toreplace.get(key));
		}
		for(String add:addList){
			mfile+=add;
		}
		mfile+="M  END";
		return mfile;
	}
	
	public static String removeSGroups(String mol){
		return StreamUtil.lines(mol)
  				.filter(l->!l.matches("^M  S.*$"))
  				.collect(Collectors.joining("\n"));
	}
}
