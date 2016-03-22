package ix.core.chem;


import gov.nih.ncgc.chemical.Chemical;
import gov.nih.ncgc.chemical.ChemicalAtom;
import gov.nih.ncgc.chemical.ChemicalBond;
import gov.nih.ncgc.chemical.ChemicalGroup;
import gov.nih.ncgc.chemical.ChemicalReader;
import gov.nih.ncgc.chemical.encoder.ChemicalEncoder;
import gov.nih.ncgc.chemical.encoder.PsuedoInchi;
import ix.core.models.Structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * This will decompose a polymer into its constituents
 * 
 * @author peryeata
 * 
 * 		
 *
 */
public class PolymerDecode {
	private static final String FRAGMENT = "FRAGMENT";
	private static final String MOIETY = "MOIETY";
	private static final String SRU_RANDOM = "SRU-RANDOM";
	private static final String GARBAGE = "GARBAGE";
	private static final String END_GROUP = "END_GROUP";
	private static final String SRU_BLOCK = "SRU-BLOCK";
	private static final int RGROUP_PLACEHOLDER = 2;
	static ChemicalReader cfac = ChemicalReader.DEFAULT_CHEMICAL_FACTORY().createChemicalReader();
	static final String pseudoString = "A";
	

	public static Chemical getDiagram(Chemical c,Collection<StructuralUnit> sunitsb){
		List<StructuralUnit> sunits = new ArrayList<StructuralUnit>(sunitsb);
		Chemical c2= cfac.createChemical();
		 Map<StructuralUnit,ChemicalAtom> aliasMap = new HashMap<StructuralUnit,ChemicalAtom>();
		 for(StructuralUnit su:sunits){
			 ChemicalAtom ca=addChemicalPseudo(c2,getMapSet(c,su.amap),su.label,-('A'-su.label.charAt(0))+1);
			 aliasMap.put(su, ca);
		 }
		 for(int i=0;i<sunits.size();i++){
			 StructuralUnit s1=sunits.get(i);
			 for(int j=i+1;j<sunits.size();j++){
				 StructuralUnit s2=sunits.get(j);
				 if(s1.connectsTo(s2)){
					 c2.addBond(aliasMap.get(s1), aliasMap.get(s2), 1, 0);
				 }
			 }
		 }
		 return c2;
	}
	public static Set<ChemicalAtom> getMapSet(Chemical c, List<Integer> map){
		int i=0;
		Set<ChemicalAtom> caset = new HashSet<ChemicalAtom>();
		for(ChemicalAtom ca: c.getAtomArray()){
			if(map.contains(i)){
				caset.add(ca);
			}
			i++;
		}
		return caset;
	}
	public static ChemicalAtom addChemicalPseudo(Chemical nchem, Set<ChemicalAtom> aset,String term,int m){
		float[] avgPt = new float[3];
		//System.out.println("Size:" + aset.size());
		for(ChemicalAtom ca:aset){
			float[] coord= ca.getCoords();
			for(int i=0;i<coord.length;i++){
				avgPt[i]+=coord[i];
			}
		}
		for(int i=0;i<avgPt.length;i++){
			avgPt[i]/=aset.size();
		}
		ChemicalAtom ca=nchem.addAtom("A");
		ca.setAlias("[" + term + "]");
		ca.setAtomMap(m);
		ca.setCoords(avgPt);
		return ca;
	}
	public static void makeRgroup(ChemicalAtom ca, int id){
		Chemical c = cfac.createChemical();
		ChemicalAtom cab = c.addAtom(pseudoString);
		ca.setAtomNo(RGROUP_PLACEHOLDER);
		c.removeAtom(cab);
		ca.setRgroupIndex(id);
//		ca.setRgroupInd();
		
	}
	public static Set<StructuralUnit> DecomposePolymerSU(Chemical c){
		Set<Chemical> chems = DecomposePolymer(c);
		Set<StructuralUnit> sunits = new LinkedHashSet<StructuralUnit>();
		for(Chemical c2:chems){
			sunits.add(new StructuralUnit(c2));
		}
		return sunits;
	}
	public static Collection<StructuralUnit>  DecomposePolymerSU(Chemical c, boolean canonicalize) {
		Collection<StructuralUnit> sunits=DecomposePolymerSU(c);
		if(canonicalize){
			sunits=canonicalize(sunits,false);
		}
		return sunits;
	}
	/**
	 * This returns a set of chemicals with * atoms given a polymer with SRU 
	 * brackets.
	 * 
	 * 
	 * 
	 * By default now, this will make components which are always connected have
	 * the same R-group number
	 * 
	 * We don't want this to be true in the future. Instead, we'd like for each
	 * pseudoatom to be unique, and mapping to other atoms should be handled as
	 * a property. This will be done in the canonicalize method
	 * 
	 * 
	 * 
	 * @param c
	 * @return
	 */
	public static Set<Chemical> DecomposePolymer(Chemical c) {
		boolean debugPrint = false;
		//System.out.println(c.getSGroupCount());
		Chemical c2 = c.cloneChemical();
		//System.out.println(c2.getSGroupCount());
		c2.generateAtomMap();
		Set<Chemical> polyconst = new LinkedHashSet<Chemical>();
		int attachType = 1;
		Map<Integer,Integer> assignedRgroup = new HashMap<Integer,Integer>();
		for (Chemical c3 : c2.getComponents()) {
			// System.out.println(new
			// Jchemical(m2).export(Chemical.FORMAT_SMARTS));
			Set<ChemicalAtom> mat = new HashSet<ChemicalAtom>();
			Map<Integer, Integer> needNewPseudo = new HashMap<Integer, Integer>();
			Map<Integer, float[]> needNewPseudoCoords= new HashMap<Integer, float[]>();
			for (ChemicalGroup sg : c3.getSGroups()) {
				switch(sg.getType()){
					case ChemicalGroup.TYPE_SUPERATOM:
					case ChemicalGroup.TYPE_FORMULATION:
					case ChemicalGroup.TYPE_GENERIC:
					case ChemicalGroup.TYPE_COMPONENT:
					case ChemicalGroup.TYPE_ANY:
					case ChemicalGroup.TYPE_MONOMER:
					case ChemicalGroup.TYPE_DATA:
						break;
					default:
						List<Integer> amapSet = new ArrayList<Integer>();
						for(ChemicalAtom ca:sg.getAtomArray()){
							amapSet.add(ca.getAtomMap());
						}
						//System.out.println(amapSet);
						//System.out.println("OK");
						int satt = attachType;
						debugPrint=true;
						ChemicalAtom[] out = sg.getOutsideNeighbors();
						//System.out.println(out.length);

						
						//The outside neighbors of group
						Set<ChemicalAtom> badAtoms = new HashSet<ChemicalAtom>();
						Map<ChemicalAtom, ChemicalAtom> newOldMap = new HashMap<ChemicalAtom, ChemicalAtom>();
						if (out != null) {
							for (ChemicalAtom o : out) {
								badAtoms.add(o);
							}
						}
						//add atoms from sg to this new molecule
						Chemical sub = cfac.createChemical();
						for (ChemicalAtom ca : sg.getAtomArray()) {
							ChemicalAtom newca = sub.addAtom(ca.getSymbol());
							//These are attachments NOT formed from the brackets themselves:
							if (ca.isQueryAtom() || ca.isRgroupAtom() || ca.getAtomNo()==RGROUP_PLACEHOLDER) {
								ChemicalAtom cab = c3.addAtom(pseudoString);
								
								newca.setRgroupIndex(attachType);
								newca.setAtomNo(RGROUP_PLACEHOLDER);
								
								
								c3.removeAtom(cab);
								newca.setAtomMap(attachType++);
								//System.out.println("Q:"+newca.getSymbol());
								String rgroups = sub.getProperty("rgroups");
								if(rgroups==null || rgroups.equals("")){
									rgroups="";
								}else{
									rgroups=rgroups+",";
								}
								sub.setProperty("rgroups", rgroups + (attachType-1));
							}else{
								newca.setAtomNo(ca.getAtomNo());
							}
							newca.setParity(ca.getParity());
							newca.setIsotope(ca.getIsotope());
							newca.setCharge(ca.getCharge());
							newca.setCoords(ca.getCoords());
							newca.setRadical(ca.getRadical());
							newOldMap.put(ca, newca);
						}
						//now look for details about border atoms
						for (ChemicalAtom ca : c3.getAtomArray()) {
							ChemicalAtom nAtom1 = newOldMap.get(ca);
							//if the atom is part of the group
							if (nAtom1 != null) {
								//for each bond
								for (ChemicalBond cb : ca.getBonds()) {
									ChemicalAtom oAtom = cb.getOtherAtom(ca);
									ChemicalAtom newca = null;
									//If the neighbor is a border atom
									if (badAtoms.contains(oAtom)) {
										newca = sub.addAtom(pseudoString);
										Integer attemp = assignedRgroup.get(ca.getAtomMap());
										newca.setCoords(oAtom.getCoords());
										if(attemp!=null){
											int t=attachType;
											attachType=attemp;
											attemp=t;
										}else{
											attemp=attachType;
										}
										newca.setAtomMap(attachType);
										newca.setRgroupIndex(attachType);
										newca.setAtomNo(RGROUP_PLACEHOLDER); //helium by default
										String prev2 = sub.getProperty("madeAttach");
										if(prev2==null || prev2.equals("")){
											prev2="";
										}else{
											prev2=prev2+",";
										}
										String rgroups = sub.getProperty("rgroups");
										if(rgroups==null || rgroups.equals("")){
											rgroups="";
										}else{
											rgroups=rgroups+",";
										}
										sub.setProperty("rgroups", rgroups + attachType);
										
										sub.setProperty("madeAttach", prev2 + attachType);
										newOldMap.put(oAtom, newca);
										assignedRgroup.put(oAtom.getAtomMap(),attachType);
										attachType=attemp;										
										needNewPseudo.put(oAtom.getAtomMap(),
												attachType++);
										needNewPseudoCoords.put(oAtom.getAtomMap(),ca.getCoords());
										
									}
									newca = newOldMap.get(oAtom);
									if (newca != null) {
										if (sub.getBond(newca, nAtom1) == null) {
											sub.addBond(nAtom1, newca, cb.getType(),
													cb.getStereo());
										}
									}
								}
							}
						}
						sub.setProperty("amap", amapSet.toString());
						sub.setProperty("component", "SRU-BLOCK");
						sub.setProperty("attach", (attachType- satt)+"");
						sub.setProperty("subScript", sg.getSubScript());
						sub.setProperty("superScript", sg.getSuperScript());
						sub.setProperty("type", sg.getTypeString()+"");
						polyconst.add(sub);
						for (ChemicalAtom ma : sg.getAtomArray()) {
							mat.add(ma);
						}
				}
			}
			for (ChemicalAtom ma : mat) {
				c3.removeAtom(ma);
			}
			for (Chemical m3 : c3.getComponents()) {
				String type = PolymerDecode.MOIETY;
				m3.setProperty("amap", m3.getAtomMap().values().toString());
				int connectcount = 0;
				Map<ChemicalAtom, Integer> rgroupMap = new HashMap<ChemicalAtom, Integer>();
				for (ChemicalAtom ca : m3.getAtomArray()) {
					Integer rGroup = needNewPseudo.get(ca.getAtomMap());
					float[] coords=needNewPseudoCoords.get(ca.getAtomMap());
					if (ca.isQueryAtom()) {
						connectcount++;
						
						ChemicalAtom cab = c3.addAtom(pseudoString);
						ca.setRgroupIndex(attachType);
						//ca.setAtomNo(cab.getAtomNo());
						ca.setAtomNo(PolymerDecode.RGROUP_PLACEHOLDER); //helium by default
						//System.out.println("Q:" + ca.getAtomNo());
						String rgroups = m3.getProperty("rgroups");
						if(rgroups==null || rgroups.equals("")){
							rgroups="";
						}else{
							rgroups=rgroups+",";
						}
						m3.setProperty("rgroups", rgroups + attachType);
						c3.removeAtom(cab);
						rgroupMap.put(ca, attachType++);
					}
					if (rGroup != null) {
						connectcount++;
						ChemicalAtom ca2 = m3.addAtom(pseudoString);
						ca2.setCoords(coords);
						rgroupMap.put(ca2, rGroup);
						m3.addBond(ca, ca2, ChemicalBond.BOND_TYPE_SINGLE,
								ChemicalBond.BOND_STEREO_NONE);
					}
				}
				
				//System.out.println(m3.getProperty("amap"));
				m3.removeAtomMap();
				
				for (ChemicalAtom ca : rgroupMap.keySet()) {
					ca.setAtomMap(rgroupMap.get(ca));
					ca.setRgroupIndex(rgroupMap.get(ca));
					ca.setAtomNo(RGROUP_PLACEHOLDER); //helium by default
					String rgroups = m3.getProperty("rgroups");
					if(rgroups==null || rgroups.equals("")){
						rgroups="";
					}else{
						rgroups=rgroups+",";
					}
					m3.setProperty("rgroups", rgroups + rgroupMap.get(ca));
				}
				if (connectcount > 0) {
					debugPrint=true;
					if (connectcount == 1) {
						type = PolymerDecode.END_GROUP;
					} else {
						if (m3.getAtomCount() == connectcount) {
							type = PolymerDecode.GARBAGE;
						} else {
						
							type = PolymerDecode.SRU_RANDOM;
						}
					}
				}
				m3.setProperty("component", type);
				m3.setProperty("attach", connectcount+"");
				polyconst.add(m3);
			}
		}
		debugPrint=false;
		if(debugPrint){
			for (Chemical ch : polyconst) {
				try {
					ch.setName(c.getName());
					
					
					String out = c.getName()+"\t"+ch.getProperty("component") + 
							"\t"+ch.getProperty("type")+
							"\t"+ch.getProperty("attach")+ 
							"\t"+ch.getProperty("subScript")+
							"\t"+ch.getProperty("superScript")+
							"\t"+ch.export(Chemical.FORMAT_MOL);
					//System.out.println(out);
				} catch (Exception e) {
					//System.out.println(c.getName() + " ERROR");
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return polyconst;
	}
	
	
	static List<StructuralUnit> removeGarbage(Collection<StructuralUnit> sunitsb){
		List<StructuralUnit> sunits = new ArrayList<StructuralUnit>(sunitsb);
		List<StructuralUnit> toRemove = new ArrayList<StructuralUnit>();
		for(StructuralUnit su : sunits){
			if(su.type.equals(PolymerDecode.GARBAGE)){
				toRemove.add(su);
			}
		}
		sunits.removeAll(toRemove);
		return sunits;
	}
	static boolean standardizeFragments(Collection<StructuralUnit> sunits){
		//List<StructuralUnit> sunits = new ArrayList<StructuralUnit>(sunitsb);
		boolean changed=false;
		for(StructuralUnit su:sunits){
			if(su.type.equals(PolymerDecode.SRU_RANDOM)){
				//System.out.println("*********GOT RANDOM");
				Map<String, List<String>> m=su.attachmentMap;
				boolean outside = false;
				boolean inside = false;
				//System.out.println(m);
				for(String k:m.keySet()){
					List<String> l=m.get(k);
					
					for(String s: l){
						//System.out.println("****" + s);
						if(m.containsKey(s)){
							inside=true;
						}else{
							outside=true;
						}
					}
				}
				if(!inside && outside){
					su.type=PolymerDecode.FRAGMENT;
					changed=true;
				}
			}
		}
		return changed;
	}
	//public Map<String,String> invertMap()
	/**
	 * This canonicalized the sunits.
	 * This involves the following:
	 * 		1) 	Remove garbage links (*-*)
	 * 		2)	Order units
	 * 			i.e.) By type, size, psuedoInchi
	 * 		3) TODO: order attachment groups by some cannonical measure
	 * 		4)
	 * @param sunits
	 * @return
	 */
	static List<StructuralUnit> canonicalize(Collection<StructuralUnit> sunitsb, boolean rename){
		List<StructuralUnit> sunits = removeGarbage(new ArrayList<StructuralUnit>(sunitsb));
		//List<StructuralUnit> toRemove = new ArrayList<StructuralUnit>();
		StructuralUnitComparator suc = new StructuralUnitComparator();
		
		Collections.sort(sunits,suc);
		//Mapping from old grouping to new grouping
		Map<String,Set<String>> canonicalGroup = new LinkedHashMap<String,Set<String>>();
		//Mapping from new grouping to old grouping
		Map<String,String> canonicalGroupINV = new LinkedHashMap<String,String>();
		
		
		int groupNum = 0;
		
		for(StructuralUnit su : sunits){
			
				Map<String,String> locCanonicalGroup = new LinkedHashMap<String,String>();
				Map<Integer,Integer> locCanonicalInt = new LinkedHashMap<Integer,Integer>();
				for(String rgroup : su.attachmentMap.keySet()){
					String ngroup =getRGroupLabel(groupNum++);
					locCanonicalGroup.put(rgroup, ngroup);
					int rind=Integer.parseInt(rgroup.replace("R", ""));
					locCanonicalInt.put(rind, groupNum-1);
				}
				Map<String,List<String>> nmap =new TreeMap<String,List<String>>();
				//Now change internal links
				for(String rgroup : new HashSet<String>(su.attachmentMap.keySet())){
					 List<String> lstr= su.attachmentMap.get(rgroup);
					 List<String> nlist= new ArrayList<String>();
					 for(String s:lstr){
						 nlist.add(locCanonicalGroup.get(s));
					 }
					 //su.attachmentMap.remove(rgroup);
					 nmap.put(locCanonicalGroup.get(rgroup), nlist);
				}
				su.attachmentMap=nmap;
				//System.out.println("Converting:" + su.structure);
				Chemical frag = su.getChemical();
				for(ChemicalAtom ca:frag.getAtomArray()){
					if(ca.isRgroupAtom() || ca.getAtomNo()==RGROUP_PLACEHOLDER || ca.getAtomMap()!=0){
						
						int r = ca.getRgroupIndex();
						if(r==0){
							r=ca.getAtomMap();
						}
						//System.out.println("RGROUP:" + r);
						int rnew= locCanonicalInt.get(r)+1;
						
						ca.setAtomMap(rnew);
						ca.setRgroupIndex(rnew);
						ca.setAtomNo(RGROUP_PLACEHOLDER); //helium by default
						ca.setAtomMap(rnew);
						//ca.setIsotope(rnew);
						//
						
					}
					
				}
				try {
					su.structure=frag.export(Chemical.FORMAT_MOL);
					//su.structure=frag.export(Chemical.FORMAT_SMARTS);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for(String rgroup:locCanonicalGroup.keySet()){
					String ngroup=locCanonicalGroup.get(rgroup);
					Set<String> nset=canonicalGroup.get(rgroup);
					if(nset==null){
						nset=new HashSet<String>();
						canonicalGroup.put(rgroup, nset);
					}
					nset.add(ngroup);
					canonicalGroupINV.put(ngroup,rgroup);
				}
		}
		
		for(StructuralUnit su : sunits){
			
			for(String ngroup : su.attachmentMap.keySet()){
				String ogroup = canonicalGroupINV.get(ngroup);
				Set<String> eqNewGroups = canonicalGroup.get(ogroup);
				if(eqNewGroups.size()>1){
					 List<String> att=su.attachmentMap.get(ngroup);
					 for(String eqNewGroup:eqNewGroups){
						 if(!eqNewGroup.equals(ngroup)){
							 att.add(eqNewGroup);
						 }
					 }
				}
			}
		}
		//All sru-randoms that are not connected to themselves are considered "FRAGMENTS"
		
		if(standardizeFragments(sunits)){
			Collections.sort(sunits,suc);
		}
		Set<String> uLabels = new HashSet<String>();
		Map<String,String> hashToLabel = new TreeMap<String,String>();
		Map<String,Integer> labCount = new TreeMap<String,Integer>();
		
		
		
		if(rename){
			//rename labels
			char strt = 'A';
			for(StructuralUnit su:sunits){
				String hash = su.getHash();
				String lab = hashToLabel.get(hash);
				if(lab==null){
					lab = strt+"";
					strt++;
					hashToLabel.put(hash,lab);
					labCount.put(lab,1);
					lab=lab+"1";
					//lab=lab+"1";
				}else{
					int c = labCount.get(lab);
					labCount.put(lab,c+1);
					lab=lab + (c+1);
					
				}
				su.label=lab;
			}
		}else{
			
			for(StructuralUnit su:sunits){
				String lab =su.label;
				String hash = su.getHash();
				if(lab!=null){
					hashToLabel.put(hash,lab);
					Integer c = labCount.get(lab);
					if(c==null){
						labCount.put(lab,1);
					}else{
						labCount.put(lab,c+1);
					}				
				}
			}
			
			for(StructuralUnit su:sunits){
				String lab =su.label;
				String hash = su.getHash();
				String nlab = hashToLabel.get(hash);
				if(lab==null){
					if (nlab == null) {
						lab = getNextAvailableLetter(labCount.keySet());
						hashToLabel.put(hash, lab);
						Integer c = labCount.get(lab);
						if (c == null) {
							labCount.put(lab, 1);
						} else {
							labCount.put(lab, c + 1);
						}
					}else{
						Integer c = labCount.get(nlab);
						
						lab=nlab+(c+1);
						labCount.put(lab, c + 1);
					}
					su.label=lab;
				}
			}
		}
		
		return sunits;
	}
	public static String getRGroupLabel(int i){
		return "R" + (i+1);
	}
	public static String getNextAvailableLetter(Set<String> used){
		for(int i=1;true;i++){
			String lab = toName(i);
			if(!used.contains(lab))return lab;
		}
	}
	public static String toName(int number) {
        StringBuilder sb = new StringBuilder();
        while (number-- > 0) {
            sb.append((char)('A' + (number % 26)));
            number /= 26;
        }
        return sb.reverse().toString();
    }
	
	/**
	 * Sort sru by:
	 * 	1)Type (SRU, END_GROUP, MOIETY)
	 * 	2)Atom count of structures
	 *  3)Lexigraphic cannonical structure, with psuedo atoms set to He
	 * @author peryeata
	 *
	 */
	
	 static class StructuralUnitComparator implements Comparator<StructuralUnit>{
		static Map<String,Integer> typeOrder = new HashMap<String,Integer>();
		static{
			typeOrder.put(PolymerDecode.SRU_BLOCK,0);
			typeOrder.put(PolymerDecode.SRU_RANDOM,1);
			typeOrder.put(PolymerDecode.FRAGMENT,2);
			typeOrder.put(PolymerDecode.END_GROUP,3);
			typeOrder.put(PolymerDecode.MOIETY,4);
		}
		
		static ChemicalEncoder<String> encoder= new PsuedoInchi();
		@Override
		public int compare(StructuralUnit arg0, StructuralUnit arg1) {
			if(arg0==null && arg1 ==null) return 0;
			if(arg0==null)return 1;
			if(arg1==null)return -1;
			if(!arg0.type.equals(arg1.type)){
				//System.out.print(arg0.type + "\t" + arg1.type);
				Integer i1=typeOrder.get(arg0.type);
				Integer i2=typeOrder.get(arg1.type);
				if(i1!=null && i2!=null){
					//System.out.println(i1 - i2);
					return i1-i2;
				}else{
					if(i1==null)return -1;
					return 1;
				}
			}else{
				Chemical c1 = arg0.getChemical();
				Chemical c2 = arg1.getChemical();
				if(c1==null && c2 ==null) return 0;
				if(c1==null)return 1;
				if(c2==null)return -1;
				if(c1.getAtomCount()!=c2.getAtomCount()){					
					return -c1.getAtomCount()+c2.getAtomCount();
				}else{
					int sin= (int) Math.signum(-c1.getMass()+c2.getMass());
					
					if(sin!=0){
						return sin;
					}
					
					String enc1="";
					String enc2="";
					try {
						enc1=encoder.encode(c1);
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try{
						enc2=encoder.encode(c2);
					}catch(Exception e){
						e.printStackTrace();
					}
					
					return -enc1.compareTo(enc2);
				}
				
			}
		}
		
	}
	public static class StructuralUnit {
		static ChemicalEncoder<String> encoder= new PsuedoInchi();
		public String structure;
		public String type;
		public String label;
		public Map<String,List<String>> attachmentMap;
		public List<Integer> amap= new ArrayList<Integer>();
		public int attachmentCount=0;
		public Amount amount=new Amount();
		public Structure _structure;
		
		public StructuralUnit(Chemical ch){
			type=ch.getProperty("component");
			try {
				//System.out.println(ch.export(Chemical.FORMAT_MOL));
				structure = ch.export(Chemical.FORMAT_MOL);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			label = ch.getProperty("subScript");
			String at = ch.getProperty("attach");
			if(at!=null){
				try{
					attachmentCount=Integer.parseInt(at);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			String myamap = ch.getProperty("amap").replace("[", "").replace("]", "");
			//List<Integer> amap = new ArrayList<Integer>();
			for(String mmap:myamap.split(",")){
				amap.add(Integer.parseInt(mmap.trim())-1);
			}
			attachmentMap=new HashMap<String,List<String>>();
			String rgroups = ch.getProperty("rgroups");
			//System.out.println(rgroups);
			if(rgroups!=null && !rgroups.trim().equals("")){
				String[] att = rgroups.split(",");
				for(String b: att){
					List<String> attList=attachmentMap.get(b);
					if(attList==null)
						attList = new ArrayList<String>();
					attachmentMap.put("R" + b,attList);
				}
			}
			String madeAt = ch.getProperty("madeAttach");
			if(madeAt!=null && !madeAt.trim().equals("")){
				String[] att = madeAt.split(",");
				if(att.length!=2){
					//System.out.println(att.length + "-----WHAT?" + ch.getName());
				}
				for(String b: att){
					List<String> attList=attachmentMap.get(b);
					if(attList==null)
						attList = new ArrayList<String>();
					for(String c:att){
						if(!c.equals(b)){
							attList.add("R" + c);
						}
					}
					attachmentMap.put("R" + b,attList);
				}
			}
			
		}
		
		public boolean connectsTo(StructuralUnit su){
			Set<String> mset = this.attachmentMap.keySet();
			Collection<List<String>> mappings= su.attachmentMap.values();
			for(List<String> ls :mappings){
				for(String k:mset){
					if(ls.contains(k)){
						return true;
					}
				}
			}
			return false;
		}
		private Chemical getChemical(){
			return cfac.createChemical(structure, Chemical.FORMAT_AUTO);
		}
		private String getHash(){
			Chemical c =this.getChemical();
			try{
				return encoder.encode(c);
			}catch(Exception e){
				
			}
			return Math.random()+"";
		}
	}
	public static class Amount{
		public Double average;
		public Double lowLimit;
		public Double highLimit;
		public String units=null;
		public String nonNumericValue=null;
		public String type=null;
		
	}
	
}
