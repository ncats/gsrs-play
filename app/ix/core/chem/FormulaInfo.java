package ix.core.chem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import TestFormula.FormulaInfo;

public class FormulaInfo implements Comparable<FormulaInfo>{
		private final static Pattern initCount = Pattern.compile("^([0-9]*)(.*)");
		private final static Pattern atomSplitter = Pattern.compile("([A-Z][a-z+]*)([0-9]*)");
		private final static Pattern formSplitter = Pattern.compile("[.]");
		Map<String,ElementInfo> elementInfo = new LinkedHashMap<String,ElementInfo>();
		
		int count=1;
		String initForm;
		String simpleForm;
		
		
		public FormulaInfo(String form){
			setFormula(form);
		}
		
		public void setFormula(String form){
			initForm=form;
			Matcher m=initCount.matcher(initForm);
			if(m.find()){
				String ss = m.group(1);
				String formsimple = m.group(2);
				if(ss.length()>0){
					count=Integer.parseInt(ss);
				}
				simpleForm = formsimple;
			}
			process();
		}
		
		private void process(){
			elementInfo.clear();
			Matcher m=atomSplitter.matcher(simpleForm);
			while(m.find()){
				String symbol=m.group(1);
				String cnt=m.group(2);
				
				int count=1;
				if(cnt.length()>0){
					count=Integer.parseInt(cnt);
				}
				
				ElementInfo ei =new ElementInfo(symbol,count);
				elementInfo.put(ei.symbol, ei);
			}
		}
		
		boolean isOrganic(){
			return elementInfo.containsKey("C");
		}
		

		public int getCount(String atom){
			ElementInfo ei=this.elementInfo.get(atom);
			if(ei==null)return 0;
			return ei.count;
		}
		
		public boolean hasOnly(String atom){
			if(this.elementInfo.size()!=1)return false;
			return getCount(atom)>0;
		}
		
		
		public boolean hasSameAtoms(FormulaInfo o){
			boolean c1=this.elementInfo.keySet().containsAll(o.elementInfo.keySet());
			return c1 && o.elementInfo.keySet().containsAll(this.elementInfo.keySet());
			
		}
		
		public boolean shareStart(FormulaInfo o){
			int s1=this.simpleForm.length();
			int s2=o.simpleForm.length();
			String larger = (s1>s2)?this.simpleForm:o.simpleForm;
			int min = Math.min(s1,s2);
			if(!this.simpleForm.substring(0, min).equals(o.simpleForm.substring(0,min))){
				return false;
			}
			
			if(larger.charAt(min)>='a' && larger.charAt(min)<='z'){
				return false;
			}
			return true;
		}
		
		/**
		 * This ordering of molecular formulas is loosely based on the
		 * Hill System Order, with some changes in order to make
		 * approximately the same order as MDL cartridge generation.
		 * 
		 * First, carbon-containing compounds are ranked before non-carbon
		 * containing compounds (same as in Hill System Order).
		 * 
		 * Next, if one formula string starts with the other formula string,
		 * and refer to the same atoms, then the shorter string comes first
		 * (in some Hill System Orderings).
		 * 
		 * For carbon-containing compounds, the atom order is the following
		 * (same as in Hill System Ordering)
		 * 
		 * 1. Carbon count
		 * 2. Hydrogen count
		 * 3+. All other elements in alphabetical order
		 * 
		 * For non-carbon containing compounds, the atom order is 
		 * the following (not typical Hill System ordering):
		 * 
		 * 1. 'H+' count
		 * 2. Negative Hydrogen count  
		 * 3. All other elements in alphabetical order **
		 *  ** except for oxygens which work a little differently
		 *     a. if an oxygen is present in both formulas being 
		 *        compared:
		 *        i.  and the atom sets of both formulas is the 
		 *            same, then don't sort by oxygen at all,
		 *            except at the end as a tie breaker, in 
		 *            reverse order (smaller O content first).
         *
		 *        ii. if the formulas have different atom sets,
		 *            then sort by oxygen in reverse order, in
		 *            the same alphabetical order as the other
		 *            atoms.
		 *        
		 * 
		 * 
		 * 
		 * 
		 */
		
		@Override
		public int compareTo(FormulaInfo o) {
			
			boolean o1=this.isOrganic();
			boolean o2=o.isOrganic();
			
			if(o1 && !o2){
				return -1;
			}else if(!o1 && o2){
				return 1;
			}
			
			
			//Not sure about this
			if(!o.simpleForm.equals("C") && !this.simpleForm.equals("C")){
				if(this.shareStart(o)){
					return -o.simpleForm.length()+this.simpleForm.length();
				}
			}
			
			Set<String> symbols = new HashSet<String>();
			symbols.addAll(this.elementInfo.keySet());
			symbols.addAll(o.elementInfo.keySet());
			
			List<String> symbolSort = new LinkedList<String>(symbols);
			
			Collections.sort(symbolSort);
			
			if(o1){
				symbolSort.add(0, "H");
				symbolSort.add(0, "C");
			}else{
				symbolSort.add(0, "H+");
				symbolSort.add(0, "-H");	
				
				// both have O
				if (this.getCount("O") > 0 && o.getCount("O") > 0) {
					int oi = symbolSort.indexOf("O");
					symbolSort.remove("O");
					
					//same atom set
					if (this.hasSameAtoms(o)) {
						symbolSort.add("-O");
					} else {
					//different atom set
						symbolSort.add(oi, "-O");
					}
					
				}
	
			}
			
			
			for(String s : symbolSort){
				int dir=1;
				if(s.startsWith("-")){
					dir=-1;
					s=s.substring(1);
				}
				int d=o.getCount(s)-this.getCount(s);
				if(d!=0){
					return dir*d;
				}
			}
			return 0;
		}
		
		
		public static List<FormulaInfo> parseMultiple(String mult){
			List<FormulaInfo> fis = new ArrayList<FormulaInfo>();
			String[] fset=formSplitter.split(mult);
			for(String f:fset){
				fis.add(new FormulaInfo(f));
			}
			return fis;
		}
		
		public static String toCanonicalString(List<FormulaInfo> fis){
			Collections.sort(fis);
			List<String> sforms = new ArrayList<String>();
			for(FormulaInfo fi:fis){
				if(fi.count>1){
					sforms.add(fi.count + fi.simpleForm);
				}else{
					sforms.add(fi.simpleForm);
				}
			}
			return String.join(".", sforms);
		}
		public static String toCanonicalString(String s){
			return toCanonicalString(FormulaInfo.parseMultiple(s));
		}
		
		public static void main(String[] args) {
			String[] forms = ("C16H19ClN2.ClH\n" + 
					"C17H20ClNO.ClH\n" + 
					"C16H20N2.C4H4O4\n" + 
					"C16H20N2.C7H7NO3\n" + 
					"C16H20N2.ClH\n" + 
					"C20H21FN2O.C2H2O4\n" + 
					"C20H21FN2O.C2H2O4\n" + 
					"C20H21N.ClH\n" + 
					"C20H23N.ClH\n" + 
					"C23H16O6.2C20H23N\n" + 
					"C17H19NS2.ClH\n" + 
					"C21H21NO2.C4H4O4\n" + 
					"C19H21N.ClH\n" + 
					"C15H23N3O.2ClH\n" + 
					"C15H19N5.C7H6O2\n" + 
					"2C15H19N5.H2O.H2O4S\n" + 
					"2C15H19N5.H2O4S\n" + 
					"C17H25N3O2S.C4H6O5\n" + 
					"C12H16N2O.C2H2O4\n" + 
					"C12H16N2.C4H4O4\n" + 
					"C23H27NO8.ClH").split("\n");
			int tot=0;
			int agree=0;
			
			for(String f:forms){
				tot++;
				
				String cstr=FormulaInfo.toCanonicalString(f);
				
				if(!f.equals(cstr)){
					System.out.println(f + "\t" + cstr);
				}else{
					agree++;
				}
			}
			System.out.println("Agreement:"  + (agree) / (tot+0.0));
		}
		
	}