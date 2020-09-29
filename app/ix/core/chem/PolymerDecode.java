package ix.core.chem;




import gov.nih.ncats.molwitch.*;

import ix.core.models.Structure;
import ix.core.util.CachedSupplier;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;


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
	
	
	static String pseudoString = "A";
	


	public static Chemical getDiagram(Chemical c,Collection<StructuralUnit> sunitsb){
		List<StructuralUnit> sunits = new ArrayList<StructuralUnit>(sunitsb);
		ChemicalBuilder c2= new ChemicalBuilder();
		Map<StructuralUnit,Atom> aliasMap = new HashMap<>();
		for(StructuralUnit su:sunits){
			Atom ca=addChemicalPseudo(c2,getMapSet(c,su.amap),su.label,-('A'-su.label.charAt(0))+1);
			aliasMap.put(su, ca);
		}
		for(int i=0;i<sunits.size();i++){
			StructuralUnit s1=sunits.get(i);
			for(int j=i+1;j<sunits.size();j++){
				StructuralUnit s2=sunits.get(j);
				if(s1.connectsTo(s2)){
					c2.addBond(aliasMap.get(s1), aliasMap.get(s2), Bond.BondType.SINGLE);
				}
			}
		}
		return c2.build();
	}
	public static Set<Atom> getMapSet(Chemical c, List<Integer> map){
		int i=0;
		Set<Atom> caset = new HashSet<Atom>();
		for(Atom ca: c.getAtoms()){
			if(map.contains(i)){
				caset.add(ca);
			}
			i++;
		}
		return caset;
	}
	public static Atom addChemicalPseudo(ChemicalBuilder nchem, Set<Atom> aset,String term,int m){
		double[] avgPt = new double[3];
		//System.out.println("Size:" + aset.size());

		for(Atom ca:aset){
			AtomCoordinates coord= ca.getAtomCoordinates();
			avgPt[0] = coord.getX();
			avgPt[1] = coord.getY();
			avgPt[2] = coord.getZ().orElse(0D);

		}
		for(int i=0;i<avgPt.length;i++){
			avgPt[i]/=aset.size();
		}
		Atom ca=nchem.addAtom("A");
		ca.setAlias("[" + term + "]");
		ca.setAtomToAtomMap(m);
		ca.setAtomCoordinates(AtomCoordinates.valueOf(avgPt[0], avgPt[1], avgPt[2]));
		return ca;
	}
	//	public static void makeRgroup(Atom ca, int id){
//		Chemical c = cfac.createChemical();
//		ChemicalAtom cab = c.addAtom(pseudoString);
//		ca.setAtomNo(RGROUP_PLACEHOLDER);
//		c.removeAtom(cab);
//		ca.setRgroupIndex(id);
////		ca.setRgroupInd();
//
//	}
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
	public static Collection<StructuralUnit>  DecomposePolymerSU(String mol, boolean canonicalize) throws IOException {
		return DecomposePolymerSU(Chemical.parse(mol), canonicalize);
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
		Chemical c2 = c.copy();
		//System.out.println(c2.getSGroupCount());
		c2.setAtomMapToPosition();
		Set<Chemical> polyconst = new LinkedHashSet<>();
		int attachType = 1;
		Map<Integer,Integer> assignedRgroup = new HashMap<>();
		Iterator<Chemical> componentIter = c2.connectedComponents();
		
		
		if(c2.getSGroups().size()>0){
			boolean hasit=false;
			
			for(Chemical cfrag : c2.getConnectedComponents()){
				hasit=cfrag.getSGroups().size()>0;
				if(hasit)break;
			}
			
			
			if(!hasit){
				//TODO: REMOVE THIS LINE
				componentIter = Arrays.asList(c2.copy()).iterator();
			}
		}
		
		
		
		while(componentIter.hasNext()){
			Chemical c3 = componentIter.next();
			Set<Atom> mat = new HashSet<>();
			Map<Integer, Integer> needNewPseudo = new HashMap<>();
			Map<Integer, AtomCoordinates> needNewPseudoCoords= new HashMap<>();

			Set<SGroup.SGroupType> includeTypes = EnumSet.of(
					SGroup.SGroupType.COPOLOYMER,
					SGroup.SGroupType.CROSSLINK,
					SGroup.SGroupType.MULTIPLE,
					SGroup.SGroupType.SRU
			);
			;
			for (SGroup sg : c3.getSGroups()
					.stream()
					.filter(sg -> includeTypes.contains(sg.getType()))
					.collect(Collectors.toList())) {

					/*


	public final static int TYPE_COPOLYMER=5;
	public final static int TYPE_CROSSLINK=6;

	public final static int TYPE_MULTIPLE=1;

	public final static int TYPE_SRU=2;

					 */
//					case ChemicalGroup.TYPE_SUPERATOM:
//					case ChemicalGroup.TYPE_FORMULATION:
//					case ChemicalGroup.TYPE_GENERIC:
//					case ChemicalGroup.TYPE_COMPONENT:
//					case ChemicalGroup.TYPE_ANY:
//					case ChemicalGroup.TYPE_MONOMER:
//					case ChemicalGroup.TYPE_DATA:
//						break;
//					default:
				Set<Integer> amapSet = sg.getAtoms()
						.map(Atom::getAtomToAtomMap)
						.filter(OptionalInt::isPresent)
						.mapToInt(OptionalInt::getAsInt)
						.boxed()
						.collect(Collectors.toCollection(LinkedHashSet::new));

//						for(Atom ca:sg.getAtoms().collect(Collectors.toList())){
//							amapSet.add(ca.getAtomMap());
//						}
				//System.out.println(amapSet);
				//System.out.println("OK");
				int satt = attachType;
				debugPrint=true;

				//The outside neighbors of group
				Map<Atom, Atom> newOldMap = new HashMap<>();

				Set<Atom> badAtoms = sg.getOutsideNeighbors().collect(Collectors.toSet());
				//System.out.println(out.length);


				//The outside neighbors of group
//						Set<Atom> badAtoms = new HashSet<>();
//						Map<Atom, Atom> newOldMap = new HashMap<>();
//						if (out != null) {
//							for (ChemicalAtom o : out) {
//								badAtoms.add(o);
//							}
//						}
				//add atoms from sg to this new molecule
				ChemicalBuilder sub = new ChemicalBuilder();
				for (Atom ca : sg.getAtoms().collect(Collectors.toList())) {
					Atom newca = sub.addAtom(ca.getSymbol());
					//These are attachments NOT formed from the brackets themselves:
					if (ca.isQueryAtom() || ca.isRGroupAtom() || ca.getSymbol().equals("R")) {
						
						newca.setAtomToAtomMap(attachType);
						newca.setRGroup(attachType);
						newca.setAlias("_R" + newca.getRGroupIndex().getAsInt());
						newca.setAtomicNumber(RGROUP_PLACEHOLDER);
						
						attachType++;
//								System.out.println("R group is:" + newca.getRGroupIndex());

						String rgroups = sub.getProperty("rgroups");
						if(rgroups==null || rgroups.equals("")){
							rgroups="";
						}else{
							rgroups=rgroups+",";
						}
						sub.setProperty("rgroups", rgroups + (attachType-1));
					}else{
						newca.setAtomicNumber(ca.getAtomicNumber());
					}
					newca.setChirality(ca.getChirality());
					newca.setMassNumber(ca.getMassNumber());
					newca.setCharge(ca.getCharge());
					newca.setAtomCoordinates(ca.getAtomCoordinates());
					//newca.setRadical(ca.getRadical());
					newOldMap.put(ca, newca);
				}
				for (Atom ca : sg.getAtoms().collect(Collectors.toList())) {
					Atom nAtom1 = newOldMap.get(ca);
					//now look for details about border atoms
//						for (ChemicalAtom ca : c3.getAtomArray()) {
//							ChemicalAtom nAtom1 = newOldMap.get(ca);
					//if the atom is part of the group
					if (nAtom1 != null) {
						//for each bond
						for (Bond cb : ca.getBonds()) {
							Atom oAtom = cb.getOtherAtom(ca);
							Atom newca = null;
							//If the neighbor is a border atom
							if (badAtoms.contains(oAtom)) {
								newca = sub.addAtom(pseudoString);
								Integer attemp = assignedRgroup.get(ca.getAtomToAtomMap().orElse(0));
								newca.setAtomCoordinates(oAtom.getAtomCoordinates());
								if(attemp!=null){
									int t=attachType;
									attachType=attemp;
									attemp=t;
								}else{
									attemp=attachType;
								}
								newca.setAtomToAtomMap(attachType);
								newca.setRGroup(attachType);
								newca.setAlias("_R" + newca.getRGroupIndex().getAsInt());
								newca.setAtomicNumber(RGROUP_PLACEHOLDER); //helium by default
								String prev2 = sub.getProperty("madeAttach");
								if(prev2==null || prev2.equals("")){
									prev2="";
								}else{
									prev2=prev2+",";
								}
								sub.setProperty("madeAttach", prev2 + attachType);
								String rgroups = sub.getProperty("rgroups");
								if(rgroups==null || rgroups.equals("")){
									rgroups="";
								}else{
									rgroups=rgroups+",";
								}
								sub.setProperty("rgroups", rgroups + attachType);

								newOldMap.put(oAtom, newca);
								assignedRgroup.put(oAtom.getAtomToAtomMap().orElse(0),attachType);
								attachType=attemp;
								needNewPseudo.put(oAtom.getAtomToAtomMap().orElse(0),
										attachType++);
								needNewPseudoCoords.put(oAtom.getAtomToAtomMap().orElse(0),ca.getAtomCoordinates());

							}
							newca = newOldMap.get(oAtom);
							if (newca != null) {
								boolean hasBond=false;
								try{
									hasBond=sub.getBond(newca, nAtom1).isPresent();
								}catch(Exception e){}
								
								if (!hasBond) {
									//GSRS-1132 : make sure the order of atom1 and atom2 in the bond
									//match the stereo since the direction of the stereo matters on order of atom1 and 2.
									Bond newBond;
									if(ca.equals(cb.getAtom1())) {
										newBond = sub.addBond(nAtom1, newca, cb.getBondType());
									}else{
										newBond = sub.addBond(newca, nAtom1, cb.getBondType());
									}
									newBond.setStereo(cb.getStereo());
								}
							}
						}
					}
				}
				sub.setProperty("amap", amapSet.toString());
				sub.setProperty("component", "SRU-BLOCK");
				sub.setProperty("attach", (attachType- satt)+"");
				if(sg.getType() == SGroup.SGroupType.SRU){
					
					if(sg.getSruLabel().isPresent()){
						sg.getSruLabel().ifPresent(s -> sub.setProperty("subScript", s));
					}else{
						sg.getSubscript().ifPresent(s->sub.setProperty("subScript", s));
					}
					
				}else {
					sg.getSubscript().ifPresent(s->sub.setProperty("subScript", s));
				}

				sg.getSuperscript().ifPresent(s->sub.setProperty("superScript", s));
				SGroup.PolymerSubType subType = sg.getPolymerSubType();
				if(subType !=null) {
					sub.setProperty("type", subType.getCode());
				}

				Chemical s=sub.build();
				polyconst.add(s);
				
				sg.getAtoms().forEach(mat::add);
			}
			
			for (SGroup sg : c3.getSGroups()
					.stream()
					.collect(Collectors.toList())) {
				c3.removeSGroup(sg);
			}

			for (Atom ma : mat) {
				c3.removeAtom(ma);
			}
			

			for (Chemical m3 : c3.getConnectedComponents()) {

//				try {
////					System.out.println("Smarts is:" + m3.toSmarts());
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				String type = PolymerDecode.MOIETY;

				String s = m3.atoms().map(Atom::getAtomToAtomMap)
						.filter(OptionalInt::isPresent)
						.map(o-> Integer.toString(o.getAsInt()))
						.collect(Collectors.joining(","));
				m3.setProperty("amap", s);
				int connectcount = 0;
				Map<Atom, Integer> rgroupMap = new HashMap<>();
				for (Atom ca : m3.atoms().collect(Collectors.toList())) {
					Integer rGroup = needNewPseudo.get(ca.getAtomToAtomMap().orElse(0));
					AtomCoordinates coords=needNewPseudoCoords.get(ca.getAtomToAtomMap().orElse(0));


//					System.out.println("Atom is:" + ca.getAtomicNumber() + " or " + ca.getSymbol());

					boolean isR=false;
					//rgroup because it's a star atom or query
					if((ca.getSymbol().equals("*") || ca.getSymbol().equals("A")  || ca.getSymbol().equals("R"))){
						isR=true;
						connectcount++;
					}

					//rgroup for above reason, but NOT because it had been connected to something else
					if (rGroup == null && isR) {
						ca.setRGroup(attachType);

						ca.setAlias("_R" + ca.getRGroupIndex().getAsInt());
						ca.setAtomicNumber(PolymerDecode.RGROUP_PLACEHOLDER); //helium by default
//						System.out.println("Q:" + ca.getAtomicNumber() + " for count:" + connectcount);
						String rgroups = m3.getProperty("rgroups");
						if(rgroups==null || rgroups.equals("")){
							rgroups="";
						}else{
							rgroups=rgroups+",";
						}
						m3.setProperty("rgroups", rgroups + attachType);
						rgroupMap.put(ca, attachType++);
					}else if (rGroup != null) { //rgroup because it HAD been connected to something else

						connectcount++;
//						System.out.println("It's an rgroup:" + connectcount);
						Atom ca2 = m3.addAtom(pseudoString);
						ca2.setAtomCoordinates(coords);
						rgroupMap.put(ca2, rGroup);
						m3.addBond(ca, ca2, Bond.BondType.SINGLE);
					}
				}

				//System.out.println(m3.getProperty("amap"));
				m3.clearAtomMaps();

				for (Atom ca : rgroupMap.keySet()) {
					Integer gg=rgroupMap.get(ca);
					ca.setAtomToAtomMap(gg);
					ca.setRGroup(gg);
					ca.setAlias("_R" + ca.getRGroupIndex().getAsInt());
					ca.setAtomicNumber(RGROUP_PLACEHOLDER); //helium by default
					String rgroups = m3.getProperty("rgroups");
					if(rgroups==null || rgroups.equals("")){
						rgroups="";
					}else{
						rgroups=rgroups+",";
					}
					m3.setProperty("rgroups", rgroups + gg);
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
							"\t"+ch.toMol();
					System.out.println(out);
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
	 * @param sunitsb
	 * @param rename
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
			Map<String,List<String>> nmap =new TreeMap<>();
			//Now change internal links
			for(String rgroup : new HashSet<>(su.attachmentMap.keySet())){
				List<String> lstr= su.attachmentMap.get(rgroup);
				List<String> nlist= new ArrayList<>();
				for(String s:lstr){
					nlist.add(locCanonicalGroup.get(s));
				}
				//su.attachmentMap.remove(rgroup);
				nmap.put(locCanonicalGroup.get(rgroup), nlist);
			}
			su.attachmentMap=nmap;
			
			Chemical frag = su.getChemical();
			for(Atom ca:frag.getAtoms()){
				if(ca.isRGroupAtom() || ca.getAtomicNumber()==RGROUP_PLACEHOLDER || ca.hasAtomToAtomMap() || ca.getAlias().orElse("").startsWith("_R")){
					OptionalInt rGroupIndexOpt = ca.getRGroupIndex();
					int r;
					if(rGroupIndexOpt.isPresent()){
						r = rGroupIndexOpt.getAsInt();
					}else{
						r = ca.getAtomToAtomMap()
							  .orElse(0);
						if(r==0){
							String n=ca.getAlias().orElse("").replace("_R","");
							if(n.length()>0){
								r=Integer.parseInt(n);
							}
						}
					}

					//System.out.println("RGROUP:" + r);
					Integer rnew = locCanonicalInt.get(r);
					if(rnew != null){
						rnew = rnew + 1;
					}else{
						rnew = 0;
					}

					ca.setRGroup(rnew);
					ca.setAlias("_R" + ca.getRGroupIndex().getAsInt());
					ca.setAtomicNumber(RGROUP_PLACEHOLDER); //helium by default
					//katzelda 7/2019 : commented out setting it 2x
//						ca.setAtomMap(rnew);
					ca.setAtomToAtomMap(rnew);
				}

			}
			try {
				su.updateStrutureMol(frag.toMol());
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
						enc1= arg0.getHash();

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try{
						enc2=arg1.getHash();
					}catch(Exception e){
						e.printStackTrace();
					}

					return -enc1.compareTo(enc2);
				}

			}
		}

	}

	//from old chemical interface PseudoInchi
	/*
	@Override
	public String encode(Chemical c1) throws Exception {
		Chemical c=c1.cloneChemical();
		//change all to helium
		boolean pseudo=false;
		for(ChemicalAtom ca : c.getAtomArray()){
			if(ca.isQueryAtom()){
				//System.out.println("OH");
				ca.setAtomNo(2);
				ca.setIsotope(6);
				pseudo=true;
			}
		}
		String exp =c.export(Chemical.FORMAT_STDINCHIKEY);
		//if(pseudo)
		//	System.out.println(c.export(Chemical.FORMAT_SMILES));
		return exp;
	}
	 */

	private static String encodePseudoInchiKey(Chemical c) throws IOException{
		Chemical chemicalToUse = Chem.RemoveQueryAtomsForPseudoInChI(c);
		return chemicalToUse.toInchi().getKey();
	}



	public static class StructuralUnit {
		public String structure;
		public String type;
		public String label;
		public Map<String,List<String>> attachmentMap;
		public List<Integer> amap= new ArrayList<Integer>();
		public int attachmentCount=0;
		//public Amount amount=new Amount();
		public Amount amount=null;
		public Structure _structure;


		private CachedSupplier<String> pseduoInchiSupplier = CachedSupplier.of(()->{

			try{
				return encodePseudoInchiKey(this.getChemical());
			}catch(Exception e){

			}
			//katzelda 7/2019: TODO is this really the best thing to do?
			return Double.toString(Math.random());
		});

		public void updateStrutureMol(String mol){
			this.structure = mol;
			pseduoInchiSupplier.resetCache();
		}
		public StructuralUnit(Chemical ch){
			type=ch.getProperty("component");
			try {
				//System.out.println(ch.export(Chemical.FORMAT_MOL));
				structure = ch.toMol();
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
			try {
				return Chemical.parseMol(structure);
			} catch (IOException e) {
				throw new UncheckedIOException("error parsing structure", e);
			}
		}

		private String getHash(){
			return pseduoInchiSupplier.get();
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
