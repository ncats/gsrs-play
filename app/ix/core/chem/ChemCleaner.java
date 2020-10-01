package ix.core.chem;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ix.core.util.CachedSupplier;
import ix.core.util.StreamUtil;
import ix.utils.FortranLikeParserHelper;
import ix.utils.FortranLikeParserHelper.LineParser;
import ix.utils.FortranLikeParserHelper.LineParser.ParsedOperation;
import ix.utils.Tuple;

public class ChemCleaner {


	private static LineParser MOLFILE_COUNT_LINE_PARSER=new LineParser("aaabbblllfffcccsssxxxrrrpppiiimmmvvvvvv");

	private static char[] alpha="aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxWyYzZ0123456789".toCharArray();
	private static String[] nalpha=IntStream.range(0, alpha.length)
			.mapToObj(i->""+alpha[i])
			.map(a->""+a+a+a+a)
			.toArray(i->new String[i]);
	private static LineParser CHG_LINE_PARSER=CachedSupplier.of(()->{
		StringBuilder sb = new StringBuilder();
		sb.append("......???");

		for(int i=0;i<nalpha.length;i+=2){
			sb.append(nalpha[i]);
			sb.append(nalpha[i+1]);
		}
		return new LineParser(sb.toString());
	}).get();

	//										  new LineParser("......nnnaaaabbbbccccdddd");

	private static Pattern NEW_LINE_PATTERN = Pattern.compile("\n");

	public static String cleanMolfileWithTypicalWhiteSpaceIssues(String molfile){
		String[] lines=NEW_LINE_PATTERN.split(molfile);
		lines[3]=MOLFILE_COUNT_LINE_PARSER.parseAndOperate(lines[3])
				.remove("iii")
				.set("mmm", "999")
				.set("vvvvvv", "V2000")
				.toLine();

		return Arrays.stream(lines)
				.map(l->{
					if(l.startsWith("M  END")){
						return "M  END"; //ignore anything after the M  END line start, as it sometimes is added by accident in a few tools
					}else if(l.startsWith("M  CHG")){
						List<String> nlist = new ArrayList<>();


						ParsedOperation po=CHG_LINE_PARSER.parseAndOperate(l);
						int ccount=po.getAsInt("???");
						if(ccount>8){
							po.set("???", "8");
						}

						for(int i=16;i<Math.min(ccount*2,nalpha.length);i+=2){
							po.remove(nalpha[i]);
							po.remove(nalpha[i+1]);
						}
						nlist.add(0,po.toLine().trim());
						if(ccount>8){
							String b = ("A" +l.substring(73)).trim().substring(1);
							for(int j=0;j<b.length();j+=64){
								int endIndex=Math.min(b.length(),j+64);
								int scount=(int)Math.ceil((endIndex-j)/8.0);
								String nsection = b.substring(j, endIndex);
								ParsedOperation no=CHG_LINE_PARSER.parseAndOperate("M  CHG").set("???", ""+scount);
								nlist.add(CHG_LINE_PARSER.parseAndOperate(no.toLine().trim()+nsection).toLine().trim());
							}
						}
						return nlist.stream().collect(Collectors.joining("\n"));

					}
					return l;
				})
				.collect(Collectors.joining("\n"));

	}

	/**
	 * Returns a cleaner form of a molfile, with some common jsdraw
	 * polymer parts re-interpreted
	 * @param mfile
	 * @return
	 */
	public static String getCleanMolfile(String mfile) {

		if(mfile == null || !mfile.contains("M  END"))return mfile;
		
		// JSdraw adds this to some S-GROUPS
		// that aren't always good
		if(!mfile.contains("MUL")){
			mfile = mfile.replaceAll("M  SPA[^\n]*\n", "");
		}
		
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
		mfile=  cleanMolfileWithTypicalWhiteSpaceIssues(mfile);
		mfile = removeLegacyTagLines(mfile);
		return mfile;
	}

	private static LineParser COUNTS_LINE_PARSER=new LineParser("aaabbblllfffcccsssxxxrrrpppiiimmmvvvvvv");
	private static String removeLegacyTagLines(String mfile) {
		StringBuilder builder = new StringBuilder(mfile.length());
		try(BufferedReader reader = new BufferedReader(new StringReader(mfile))){
			//assume valid mol file
			builder.append(reader.readLine()).append('\n');
			builder.append(reader.readLine()).append('\n');
			builder.append(reader.readLine()).append('\n');
			String countsLine = reader.readLine();
			//aaabbblllfffcccsssxxxrrrpppiiimmmvvvvvv
			Map<String, FortranLikeParserHelper.ParsedSection> map =COUNTS_LINE_PARSER.parse(countsLine);
			int numAtoms = Integer.parseInt(map.get("aaa").getValueTrimmed());
			int numBonds = Integer.parseInt(map.get("bbb").getValueTrimmed());
			int numAtomLists = Integer.parseInt(map.get("lll").getValueTrimmed());
			//rewrite with atomList set to 0
//			builder.append(countsLine.substring(0, 6)+"  0" + countsLine.substring(9)).append('\n');
			builder.append(countsLine).append('\n');
			int linesInAtomAndBondBlocks = numAtoms+numBonds;
			for(int i=0; i<linesInAtomAndBondBlocks; i++){
				builder.append(reader.readLine()).append('\n');
			}
			String line;
			while( (line =reader.readLine()) !=null){
				if(line.startsWith("M  ")){
					builder.append(line).append('\n');
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//remove last new line
		builder.setLength(builder.length()-1);
		return builder.toString();
	}

	public static String removeSGroups(String mol){
		return StreamUtil.lines(mol)
  				.filter(l->!l.matches("^M  S.*$"))
  				.collect(Collectors.joining("\n"));
	}

}
