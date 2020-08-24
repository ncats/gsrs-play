package ix.ginas.utils.validation;



import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import gov.nih.ncats.molwitch.Atom;
import gov.nih.ncats.molwitch.Bond;
import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.molwitch.ChemicalBuilder;



public class PeptideInterpreter {
	private static final int MOD_AMINO_GROUP_ISOTOPE = 55;
	public static Map<String, String> AAmap = new HashMap<String, String>();
	public static Map<String, String> AAmap3Let = new HashMap<String, String>();
	public static Map<String, Integer> AAmapCHI = new HashMap<String, Integer>();
	public static Map<String, Integer> modFreq = new HashMap<String,Integer>();
	public static Map<String, String> modName = new HashMap<String,String>();
	public static Map<String, String> modMap = new HashMap<String,String>();

	public static int modNumber=1;
	public static String aminoAcids = "P\tC1C[Ne]C1;A\tO=C(O)[C@@H](N)C;C\tSC[C@H](N)C(O)=O;D\tOC(C[C@H](N)C(O)=O)=O;E\tO=C(O)[C@@H](N)CCC(O)=O;F\tN[C@H](C(O)=O)CC1=CC=CC=C1;G\tO=C(O)CN;H\tN[C@@H](CC1=CN=CN1)C(O)=O;I\tO=C(O)[C@@H](N)[C@H](C)CC;K\tN[C@H](C(O)=O)CCCCN;L\tN[C@@H](CC(C)C)C(O)=O;M\tOC([C@@H](N)CCSC)=O;N\tNC(C[C@H](N)C(O)=O)=O;P\tO=C([C@@H]1CCCN1)O;Q\tOC([C@@H](N)CCC(N)=O)=O;R\tO=C(O)[C@@H](N)CCCNC(N)=N;R\tN[C@H](C(=O)O)CCCN=C(N)N;S\tOC([C@@H](N)CO)=O;T\tC[C@H]([C@@H](C(=O)O)N)O;V\tN[C@H](C(O)=O)C(C)C;W\tO=C(O)[C@@H](N)CC1=CNC2=C1C=CC=C2;Y\tN[C@@H](CC1=CC=C(O)C=C1)C(O)=O;W\tO=C([C@H](Cc1c2ccccc2[n]c1)N)O;H\tN[C@H](C(=O)O)Cc1c[nH]cn1;I\tO=C(O)[C@@H](N)[C@@H](C)CC;H\tN[C@H](C(=O)O)Cc1cncn1";
	public static String aminoAcidsLet = "A	Ala;R	Arg;N	Asn;D	Asp;C	Cys;E	Glu;Q	Gln;G	Gly;H	His;I	Ile;L	Leu;K	Lys;M	Met;F	Phe;P	Pro;S	Ser;T	Thr;W	Trp;Y	Tyr;V	Val";

	static {
		//katzelda Aug 2020 - add amidated component to be ignored
		//we are purposely making the amino acid an empty string
		aminoAcids+=";\t6NH3";

		Pattern TAB_SPLIT = Pattern.compile("\t");
		for (String s : aminoAcids.split(";")) {
			String[] sArray = TAB_SPLIT.split(s);

			Chemical c=null;
			String AA=sArray[0];
//			System.out.println(sArray[1]);
			try {
				c = Chemical.createFromSmiles(sArray[1]);
				c.makeHydrogensImplicit();
				c.aromatize();
				c.setAtomMapToPosition();
				int[] pos = longestPeptideBackbone(c,1);
				int chi=c.getAtom(pos[0]).getChirality().getParity();
				AAmapCHI.put(s.split("\t")[0], chi);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			contractPeptide(c);
			for (Atom ma : c.getAtoms()) {
				if (ma.getAtomicNumber() == 10) {
					List<Bond> toRemove = new ArrayList<>();
					for(Bond b : ma.getBonds()){

						Atom mn=b.getOtherAtom(ma);
						if (mn.getAtomicNumber() == 10  ||mn.getMassNumber()==7||mn.getMassNumber()==6 ) {
							toRemove.add(b);
						}

					}
					for (Bond mb : toRemove) {
						c.removeBond(mb);
					}
				}
			}

			try {

				String smi=LOOKUP_HASH(c);
				AAmap.put(smi, AA);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(AA.equals("P")){
				c.atoms()
						.filter(a-> a.getAtomicNumber() ==10)
						.forEach(a-> a.setMassNumber(55));

				try {
					String key = LOOKUP_HASH(c);
//					System.out.println("P LOOKUP HAS = " + key);
					AAmap.put(key, AA);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			if(AA.equals("T")){
				try {
					c.generateCoordinates();

					for(Bond cb:c.getBonds()){
						cb.setStereo(Bond.Stereo.NONE);
					}

					AAmap.put(LOOKUP_HASH(c), "T");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		for(String s:aminoAcidsLet.split(";")){
			AAmap3Let.put(s.split("\t")[0], s.split("\t")[1]);
		}

	}
	public static String LOOKUP_HASH(Chemical f2) throws Exception{
		String smiles=null;
		try{
			smiles = f2.toInchi().getKey();
			return smiles.substring(0,14);
		}catch(Exception e){
			e.printStackTrace();
//			try{
//				smiles = f2.export(Chemical.FORMAT_SMARTS).split(" ")[0];
//			}catch(Exception e2){
//				try{
//					smiles = f2.export(Chemical.FORMAT_STDINCHI).split(" ")[0];
//				}catch(Exception e3){
//					smiles = f2.export(Chemical.FORMAT_MOL).replace("\n","\\n");
//				}
//			}
			smiles = f2.toMol().replace("\n", "\\n");
		}
		return smiles;
	}

	public static void contractPeptide(Chemical c) {
		contractPeptide(c,null);

	}
	public static void contractPeptide(Chemical c, int[] backboneAtoms) {
		contractChemical(c,backboneAtoms);
	}
	/**
	 * Takes in a molecule, replaces alpha peptide bonds with [Ne],
	 * keeping atom map.
	 *
	 * Also replaces carbonyl with [He].
	 *
	 * And sets modified amines at alpha groups to [55Ne].
	 *
	 *
	 * @param m2
	 * @param backboneAtoms
	 */
	public static void contractChemical(Chemical m2, int[] backboneAtoms) {
		Set<Atom> incAtoms;
//		ChemicalAtom[] catoms = m2.getAtomArray();
		if(backboneAtoms!=null){
			incAtoms = new HashSet<>();
			for(int i=0;i<backboneAtoms.length;i++){
				incAtoms.add(m2.getAtom(backboneAtoms[i]));
			}
		}else{
			incAtoms = m2.atoms().collect(Collectors.toSet());
//			for(int i=0;i<m2.getAtomCount();i++){
//				ChemicalAtom ma = catoms[i];
//				incAtoms.add(ma);
//			}
		}
		//Finding (C=O), replace --> [He]
		for (Atom ma : m2.getAtoms()) {
			if (ma.getAtomicNumber() == 6) {
				for(Bond b : ma.getBonds()){
					Atom otherAtom = b.getOtherAtom(ma);
					if (otherAtom.getAtomicNumber() == 8 && b.getBondType() == Bond.BondType.DOUBLE) {
//						if (ma.getBond(i).getType() == ChemicalBond.BOND_TYPE_DOUBLE) {
//							ma.setAtomNo(2); //Helium
//							m2.removeAtom(ma.getBond(i).getOtherAtom(ma)); //Remove =O
//						}
						ma.setAtomicNumber(2);
						m2.removeAtom(otherAtom);
					}
				}
			}
		}

		//
		for (Atom ma : m2.getAtoms()) {
			if(incAtoms.contains(ma)){
				//C
				if (ma.getAtomicNumber() == 6) {
					Atom carbonyl = null; //[He] neighbor
					Atom amine = null; //-N neighbor
					boolean mod = false;
					for(Bond b : ma.getBonds()){
//					for (int i = 0; i < ma.getBondCount(); i++) {
						if (b.getOtherAtom(ma).getAtomicNumber() == 2) {
							carbonyl = b.getOtherAtom(ma);
						} else if (b.getBondType()== Bond.BondType.SINGLE && b.getOtherAtom(ma).getAtomicNumber() == 7) {
							amine = b.getOtherAtom(ma);
						}
					}

					//Looks like alpha carbon
					if (amine != null && carbonyl != null) {
						if(amine.getBondCount()>2){
							mod=true;
						}
						for(Bond b : amine.getBonds()){
//						for (int i = 0; i < amine.getBondCount(); i++) {
							Atom oatom = b.getOtherAtom(amine);

							if (!(oatom.getAtomToAtomMap().getAsInt() ==ma.getAtomToAtomMap().getAsInt())) {
								m2.addBond(ma, oatom, b.getBondType());
								if(oatom.getMassNumber()!=PeptideInterpreter.MOD_AMINO_GROUP_ISOTOPE){
									oatom.setMassNumber(7);
								}
							}

						}
						m2.removeAtom(amine);
						for(Bond b : carbonyl.getBonds()){
//						for (int i = 0; i < carbonyl.getBondCount(); i++) {
							Atom oatom = b.getOtherAtom(carbonyl);
							if (oatom.getAtomicNumber() != 8) {
								if (!(oatom.getAtomToAtomMap().getAsInt() ==ma.getAtomToAtomMap().getAsInt())) {
									m2.addBond(ma, oatom, b.getBondType());
									if(oatom.getMassNumber()!=PeptideInterpreter.MOD_AMINO_GROUP_ISOTOPE){
										oatom.setMassNumber(6);
									}
								}
							} else {
								m2.removeAtom(oatom);
							}
						}
						m2.removeAtom(carbonyl);
						ma.setAtomicNumber(10);
						if(mod){
							ma.setMassNumber(PeptideInterpreter.MOD_AMINO_GROUP_ISOTOPE);
						}
					}
				}
			}
		}

		m2.atoms()
				.filter(a-> a.getAtomicNumber() ==10 && ( a.getMassNumber() ==7 || a.getMassNumber() == 6))
				.forEach(a-> a.setMassNumber(0));


	}
	/*
	public static int[] removeDisulfide(Molecule m2) {
		List<Integer> blist = new ArrayList<Integer>();
		for (MolBond mb : m2.getBondArray()) {
			if (mb.getAtom1().getAtno() == 16 && mb.getAtom2().getAtno() == 16) {
				m2.removeEdge(mb);
				blist.add(mb.getAtom1().getAtomMap()-1);
				blist.add(mb.getAtom2().getAtomMap()-1);
			}
		}
		int[] barr = new int[blist.size()];
		for(int i=0;i<barr.length;i++){
			barr[i]=blist.get(i);

		}
		return barr;
	}*/
	public static int[] removeDisulfide(Chemical c2) {
		List<Integer> blist = new ArrayList<Integer>();
		for (Bond mb : c2.getBonds()) {
			if (mb.getAtom1().getAtomicNumber() == 16 && mb.getAtom2().getAtomicNumber() == 16) {
				c2.removeBond(mb);
				blist.add(mb.getAtom1().getAtomToAtomMap().orElse(0)-1);
				blist.add(mb.getAtom2().getAtomToAtomMap().orElse(0)-1);
			}
		}
		int[] barr = new int[blist.size()];
		for(int i=0;i<barr.length;i++){
			barr[i]=blist.get(i);

		}
		return barr;
	}
	public static int[] longestPeptideBackbone(Chemical m, int minSize)
			throws Exception {
		//int minSize = 3;

		List<int[]> listFrags = new ArrayList<int[]>();
		Chemical m3 = m.copy();
		m3.setAtomMapToPosition();
		Chemical m4 = m3.copy();

//		System.out.println("removing disultifes");
		removeDisulfide(m4);
//		System.out.println("contracting peptides...");
		contractPeptide(m4);
//		System.out.println("now smiles is =" + m4.toSmiles());
//		System.out.println(m4.atoms().map(a-> "[ " + a.getSymbol() + " " + a.getAtomToAtomMap().getAsInt() + "]").collect(Collectors.joining(",")));
		Set<Integer> backboneAtoms  = new HashSet<>();
		Set<Integer> modind  = new HashSet<>();
//		System.out.println("after removing not He, Ns and Ne...");

		for (Atom ma : m4.atoms().collect(Collectors.toList())) {
			if(ma.getAtomicNumber() == 2 && ma.getMassNumber()==7) {
				modind.add(ma.getAtomToAtomMap().orElse(0)-1);
			}else if(ma.getAtomicNumber() != 10){
				m4.removeAtom(ma);
			}
		}
//		System.out.println("now smiles is =" + m4.toSmiles());
//		List<Chemical> tFrags =m4.getco();
//		if(tFrags.size()>1){
//			//Non-alpha AA, or linked Protein
//		}
		for(Chemical m2:m4.getConnectedComponents()){
			int[] ret = null;
			//find ends, get list
			for (Atom ma : m2.getAtoms()) {
				if (ma.getBondCount() <=1) {
					ret = new int[m2.getAtomCount()];
					int numat = 0;
					Atom pma = null;
					while (numat < m2.getAtomCount()) {
						ret[numat] = ma.getAtomToAtomMap().orElse(0) - 1;

//						if (ma.equals(pma)) {
//							System.out.println("going in loop? " + ma + " and pma = " + pma);
//						}
						for (int i = 0; i < ma.getBondCount(); i++) {
							Atom ma2 = ma.getBonds().get(i).getOtherAtom(ma);
							if (pma !=null && ma2.getAtomIndexInParent()==pma.getAtomIndexInParent()) {
								continue;
							} else {
								pma = ma;
								ma = ma2;
								break;
							}
						}
						numat++;
					}
					break;
				}
			}
			if(ret==null && m2.getAtomCount()<1)continue;
			if(ret==null){
				for (Atom ma : m2.getAtoms()) {
					ret = new int[m2.getAtomCount()];
					int numat = 0;
					Atom pma = null;
					while (numat < m2.getAtomCount()) {
						ret[numat] = ma.getAtomToAtomMap().orElse(0) - 1;
						for (int i = 0; i < ma.getBondCount(); i++) {
							Atom ma2 = ma.getBonds().get(i).getOtherAtom(ma);
							if (ma2.equals(pma)) {
								continue;
							} else {
								pma = ma;
								ma = ma2;
								break;
							}
						}
						numat++;
					}
					break;
				}
			}
			/*
			 * Now we've got a subsequence, but we need to put them in the right order (N->C)
			 * We do this by looking at the first two alpha atoms in the sequence.
			 * If the N in the first is linked to the second,
			 *
			 */
			if(ret.length>1){
				boolean reverse = false;
				Atom at1 = m3.getAtom(ret[0]);
				Atom at2 = m3.getAtom(ret[1]);
				Set<Atom> amideAtoms = new HashSet<>();
				//System.out.println("ATOM NUM:" + at1.getAtno());
				if(!modind.contains(ret[0])){
					if(!modind.contains(ret[ret.length-1])){
						for (int i = 0; i < at1.getBondCount(); i++) {
							Atom n1 = at1.getBonds().get(i).getOtherAtom(at1);
							if (n1.getAtomicNumber() == 7) {
								for (int j = 0; j < n1.getBondCount(); j++) {
									Atom m1 = n1.getBonds().get(j).getOtherAtom(n1);
									if (m1 != at1) {
										amideAtoms.add(m1);
									}
								}
							}
						}
						for (int i = 0; i < at2.getBondCount(); i++) {
							Atom n1 = at2.getBonds().get(i).getOtherAtom(at2);
							if (amideAtoms.contains(n1)) {
								reverse = true;
								break;

							}
						}
					}else{
						reverse=true;
					}
				}else{
					reverse=false;
				}
				if (reverse) {
					for (int i = 0; i < ret.length / 2; i++) {
						int t = ret[ret.length - 1 - i];
						ret[ret.length - i - 1] = ret[i];
						ret[i] = t;
					}
				}

			}
			if(ret!=null){
				for(int r:ret){
					backboneAtoms.add(r);
				}
				//System.out.println(Arrays.toString(ret));
				listFrags.add(ret);
			}
		}
		/*
		 * Now we've got all the alpha-amino acid chains, each in the right
		 * order, internally But they're not connected correctly. To do this, we
		 * go to each modified atom (carbonyl group), and search for the first connection to
		 * another backbone atom that it's not already connected to
		 */
		int[] ret = null;
		if(listFrags.size()>1){
			int tsize=0;
			Map<Integer,Integer> startFrag = new HashMap<>();
			List<Integer> startingIndexes = new ArrayList<>();
			int startingIndex = -1;
//			System.out.println("listFrags size = " + listFrags.size() + "\n" + listFrags.stream().map(Arrays::toString).collect(Collectors.joining("\n")));
			for(int[] r : listFrags){

				if(r!=null){
					if(modind.contains(r[0])){
						Set<Integer> tryAtoms = new HashSet<>(backboneAtoms);
						Set<Integer> stopAtoms = new HashSet<>();
						if(r.length>1) {
							stopAtoms.add(r[1]);
						}
						stopAtoms.add(r[0]);
						tryAtoms.removeAll(stopAtoms);
						int con = getClosestAtomInSet(m3,r[0],tryAtoms,stopAtoms);
						if(con==-1){
							if(startingIndex==-1){
								startingIndex = r[0];
							}else{
								//System.out.println("NON-CONNECTABLE");
							}
							startingIndexes.add(r[0]);
						}else{
							startFrag.put(con, r[0]);
						}
					}else{
						startingIndex=r[0];
						startingIndexes.add(r[0]);
					}
					for(int i=0;i<r.length-1;i++){
						startFrag.put(r[i], r[i+1]);
					}
					tsize+=r.length;
				}
			}
//			System.out.println("starting indexes = " + startingIndexes);

//			System.out.println("startFrag map = " + startFrag);
			List<Integer> longest = null;
			int i=0;
			for(int st:startingIndexes){
				List<Integer> order = new ArrayList<>();
				while(st!=-1){
					order.add(st);
//					System.out.println(order);
//					if(!order.add(st)){
//						break;
//					}
					Integer si = startFrag.get(st);
					if(si==null || si.intValue() ==st){
						break;
					}
					st =si;
				}
				if(longest == null || order.size()>longest.size()){
					longest=new ArrayList<>(order);
				}
				//System.out.println("ORDER " + i++ + ":" +order.toString());
			}
			if(longest==null)return null;
			ret = new int[longest.size()];
			for(int j=0;j<longest.size();j++){
				ret[j]=longest.get(j);
			}
			//System.out.println("FINAL ORDER:"+Arrays.toString(ret));
			//return ret;
		}
		//System.out.println(Arrays.toString(ret));

		if(listFrags.size()>0){
			Collections.sort(listFrags,new Comparator<int[]>(){
				@Override
				public int compare(int[] o1, int[] o2) {
					return -(o1.length-o2.length);
				}

			});
			ret = listFrags.get(0);
		}
		if(ret!=null){
			if(ret.length<minSize)
				ret=null;
		}
		return ret;

	}
	/*
	public static int getClosestAtomInSet(Molecule m, int a1, Set<Integer> aset,Set<Integer> bounds){
		//ChemicalAtom ma = m.getAtom(a1);

		Set<ChemicalAtom> left = new HashSet<ChemicalAtom>();
		Set<ChemicalAtom> wavefront = new HashSet<ChemicalAtom>();
		Map<ChemicalAtom, Integer> find = new HashMap<ChemicalAtom,Integer>();


		for(int i=0;i<m.getAtomCount();i++){
			if(!bounds.contains(i))
				left.add(m.getAtom(i));
			if(aset.contains(i)){
				find.put(m.getAtom(i),i);
			}
		}
		//left.removeAll(bounds);
		wavefront.add(m.getAtom(a1));

		while(wavefront.size()>0){
			Set<ChemicalAtom> nwavefront = new HashSet<ChemicalAtom>();
			for(ChemicalAtom ma: wavefront){
				for(int i=0;i<ma.getBondCount();i++){
					ChemicalAtom mb = ma.getBond(i).getOtherAtom(ma);

					if(!bounds.contains(mb)){
						if(left.contains(mb)){
							nwavefront.add(mb);
							left.remove(mb);
							Integer ind = find.get(mb);
							if(ind!=null){
								return ind;
							}
						}
					}

				}
			}
			wavefront = nwavefront;
		}
		return -1;
	}*/
	public static int getClosestAtomInSet(Chemical m, int a1, Set<Integer> aset,Set<Integer> bounds){
		//ChemicalAtom ma = m.getAtom(a1);

		Set<Atom> left = new HashSet<>();
		Set<Atom> wavefront = new HashSet<>();
		Map<Atom, Integer> find = new HashMap<>();


		for(int i=0;i<m.getAtomCount();i++){
			if(!bounds.contains(i))
				left.add(m.getAtom(i));
			if(aset.contains(i)){
				find.put(m.getAtom(i),i);
			}
		}
		//left.removeAll(bounds);
		wavefront.add(m.getAtom(a1));

		while(!wavefront.isEmpty()){
			Set<Atom> nwavefront = new HashSet<>();
			for(Atom ma: wavefront){
				for(Bond b : ma.getBonds()){
//				for(int i=0;i<ma.getBondCount();i++){
					Atom mb = b.getOtherAtom(ma);
					Integer index = mb.getAtomToAtomMap().orElse(0) -1;
					if(!bounds.contains(index)){
						if(left.contains(mb)){
							nwavefront.add(mb);
							left.remove(mb);
							Integer ind = find.get(mb);
							if(ind!=null){
								return ind;
							}
						}
					}

				}
			}
			wavefront = nwavefront;
		}
		return -1;
	}

	/**
	 *
	 * @param smi Structure as string
	 * @return Returns the amino acid sequence in the form <SEQ>;<SEQ>|<DISULFIDES>
	 * @throws Exception
	 *
	 * For example, a structure with two subunits, linked by a disulfide bond might give:
	 *
	 * QASCSG;NACSG|[1_4-2_3]
	 */
	public static Protein getAminoAcidSequence(String smi) throws Exception {
		try {
			smi=smi.replaceAll("M  S..*", "");
//			System.out.println("trying to import " + smi);
			Chemical c = Chemical.parse(smi);
			String smi2 = c.toMol();
//			System.out.println("parsed mol = " + smi2);
			smi2=smi2.replaceAll("M  S..*", "");
			c = Chemical.parseMol(smi2);
			return getAminoAcidSequence(c);

		} catch (Exception e) {
			e.printStackTrace();
			try {
				Chemical c = Chemical.parse(smi);
				c.expandSGroups();
				Chemical m2=buildCleanMolecule(c);
				return getAminoAcidSequence(m2);
			} catch (Exception e1) {
				return null;
			}
		}
	}
	public static Chemical buildCleanMolecule(Chemical c){
		ChemicalBuilder c2 = new ChemicalBuilder();
		Map<Atom, Integer> atMap  = new HashMap<>();
		int i=0;
		for(Atom ma : c.getAtoms()){
			Atom ma2 = c2.addAtom(ma.getSymbol());
			ma2.setMassNumber(ma.getMassNumber());
			ma2.setCharge(ma.getCharge());
			ma2.setRadical(ma.getRadical());
			ma2.setAtomCoordinates(ma2.getAtomCoordinates());
			//m2.add(ma2);
			atMap.put(ma,i++);
		}
		for(Bond mb: c.getBonds()){
			int a1 = atMap.get(mb.getAtom1());
			int a2 = atMap.get(mb.getAtom2());
			Atom at1=c2.getAtom(a1);
			Atom at2=c2.getAtom(a2);

			c2.addBond(at1,at2,mb.getBondType())
					.setStereo(mb.getStereo());

		}

		return c2.build();
	}

	/*	TODO: If it's a cyclic peptide, watch out and do something ...
	 *
	 * 	I guess, while getting the peptide backbone? Perhaps ...
	 *
	 *
	 *
	 *
	 *
	 */
	public static Protein getAminoAcidSequence(Chemical impMol) {
		if(impMol==null){
			return null;
		}
		if(impMol.getAtomCount()>=1024){
			throw new IllegalArgumentException("Too many atoms, does not support >= 1024");
		}
		//System.out.println("Number of atoms:" + impMol.getAtomCount());
		List<String> sequences1Let = new ArrayList<>();
		Map<Integer,Integer> canonicalSequenceMap = new HashMap<>();


		Chemical stdMol = impMol.copy();
		stdMol.makeHydrogensImplicit();
		stdMol.aromatize();
		stdMol.setAtomMapToPosition();
		//setAtomMap(stdMol);
		int[] bridges = removeDisulfide(stdMol);
		HashMap<Integer,String> atom_to_residue = new HashMap<Integer,String> ();

		int subunit=-1;
		for (Chemical c : stdMol.getConnectedComponents()) {
			//Molecule m = cf.getMol();
			//Jchemical c = new Jchemical(m);
			//System.out.
//			int[] stereo = c.getStereo();
			String ret = "";		//Full Sequence, single letter, semi-colon between subunits
			subunit++; //start to 0
//			try {
//				System.out.println("connected components " + subunit + " = " + c.toSmiles());
//			}catch (IOException e){
//				throw new UncheckedIOException(e);
//			}
//			System.out.println(c.atoms().map(a-> "[ " + a.getSymbol() + " " + a.getAtomToAtomMap().getAsInt() + "]").collect(Collectors.joining(",")));

			try {
				int[] seq = longestPeptideBackbone(c,4);
				//System.out.println(seq);
				if(seq==null){
					subunit--;
					continue;
				}
				Map<Integer,Integer> chi = new HashMap<>();
				Map<Integer,Integer> alphaAtomIndexToSeqIndex = new LinkedHashMap<>();

				Chemical c2 = c.copy();

				//Molecule m2 = Jchemical.makeJchemical(c2).getMol();
				int k=0;

				for (int s : seq) {
					Atom atom = c2.getAtom(s);
					int amap= atom.getAtomToAtomMap().orElse(0)-1;
					alphaAtomIndexToSeqIndex.put(amap,k++);

					chi.put(amap, atom.getChirality().getParity());
				}
				contractPeptide(c2,seq);
				List<Bond> toRemove = new ArrayList<>();
				List<String> remBond = new ArrayList<String>();
				for (Atom ma : c2.getAtoms()) {
					if (ma.getAtomicNumber() == 10) {
						for(Bond b : ma.getBonds()){
							Atom mn= b.getOtherAtom(ma);
							if (mn.getAtomicNumber() == 10 || mn.getMassNumber() ==7 || mn.getMassNumber() ==6) {
								toRemove.add(b);
							}

						}

					}
				}
				for (Bond mb : toRemove) {
					c2.removeBond(mb);
					remBond.add(mb.getAtom1().getAtomToAtomMap().orElse(0) + "_" + mb.getAtom2().getAtomToAtomMap().orElse(0));
				}
				String ctermmod = "";
				String ntermmod = "";
				Map<Integer, Integer> atomIndexToAlphaAtomIndex = new HashMap<Integer, Integer>();
				Map<Integer, String> alphaAtomIndexToAA = new HashMap<Integer, String>();
				for (Chemical cf : c2.getConnectedComponents()) {
					int mod = 0;
					int patt = -1;

					Map<Integer,Atom> myatoms = new HashMap<>();


					for (Atom ma : cf.getAtoms()) {

						if(mod==0 || mod==55){
							mod=ma.getMassNumber();
						}
						int pat = ma.getAtomToAtomMap().orElse(0);
						if (alphaAtomIndexToSeqIndex.containsKey(pat-1)) {
							patt = pat - 1;
						}else{
							//myatoms.put(pat-1,ma);
						}
						myatoms.put(pat-1,ma);
						ma.clearAtomToAtomMap();
					}
					boolean weirdCycle = false;
					//Molecule tf;
					Chemical tf=null;
					List<Bond> mbadd = new ArrayList<>();
					for (String mb: remBond) {
						int amap1= Integer.parseInt(mb.split("_")[0]);
						int amap2= Integer.parseInt(mb.split("_")[1]);
						Atom ma1,ma2;
						//System.out.println("LOOK:" +amap1 + "," + amap2);

						if((ma1 = myatoms.get(amap1-1))!=null){
							if((ma2 = myatoms.get(amap2-1))!=null){
								//System.out.println("GET HERE?");
								Bond cb = cf.addBond(ma1, ma2, Bond.BondType.SINGLE);
								mbadd.add(cb);
								cf.removeBond(cb);
								if(ma1.getSymbol().equals("Ne") || ma2.getSymbol().equals("Ne")){
									weirdCycle=true;
								}
							}
						}

					}
					if(weirdCycle){
						tf=cf.copy();

					}
					for(Bond mb:mbadd){
						cf.addBond(mb);
						mb.getAtom1().clearAtomToAtomMap();
						mb.getAtom2().clearAtomToAtomMap();

					}


					Chemical f2 = cf.copy();
					int aamap = -1;
					//Molecule f2 = ((Jchemical)cf).getMol().cloneMolecule();
					for(Atom ma:f2.getAtoms()){
						if(ma.isQueryAtom()){
							//Change all previously psuedo atoms to argon.
							//Why? It's a placeholder for a better solution.
							ma.setAtomicNumber(18);
						}
						if(ma.getAtomToAtomMap().isPresent()){
							aamap =ma.getAtomToAtomMap().getAsInt()-1;
						}
					}
//					System.out.println("looking up " + f2.toSmiles());
					String hashKey = LOOKUP_HASH(f2);
//					System.out.println(hashKey);
					String aalet = AAmap.get(hashKey);
//					System.out.println(aalet);
					if(aalet==null){
//						System.out.println("was null!! current map is " + AAmap);
						String smiles=f2.toSmiles();
						if(!weirdCycle){
							smiles=decodePeptide(smiles,false);
						}else{
							smiles=decodePeptide(tf.toSmiles(),true);
						}
//						System.out.println("now smiles = " + smiles);
						if(modFreq.get(smiles)==null){
							modFreq.put(smiles, 1);
						}else{
							modFreq.put(smiles, modFreq.get(smiles)+1);
						}
						if(mod==6){
							//TODO: make more robust cterm handling
							String let = getModName(smiles);
							aalet=let;
							if(patt==-1){
								patt=aamap;
								alphaAtomIndexToSeqIndex.put(patt,k++);
							}
						}else{
							/*
							else if(mod==7){
								if(smiles.equals("[7He]C")){
									ntermmod="Ac-";
								}else{
									ntermmod="("+smiles+")-";
								}
							}*/
							//if()
							String let = getModName(smiles);
							aalet=let;
						}
					}
					//System.out.println(patt + "\t" +smi1+"\t" + smiles+ "\t" + aalet);
					//modMap.put(, arg1)
					//System.out.println(mod+ "\t" + patt+":"+AAmap.get(smiles) + "\t" +smiles);
					for(int p:myatoms.keySet()){
						atomIndexToAlphaAtomIndex.put(p, patt);
					}
					alphaAtomIndexToAA.put(patt, aalet);
				}
				ret+=ntermmod;


				for (int s : alphaAtomIndexToSeqIndex.keySet()) {
					String let = alphaAtomIndexToAA.get(s);
					if(let==null){
						//let = "[X:"+ ++letNumber + "]";
						//TODO: should flag for bogus ring
						//modMap.put(let,"???ring");
					}else{
						boolean mod = (modName.get(let)!=null);
						if (!mod){
							int chi1 =(chi.get(s));
							int chi2 =(let!=null)?(AAmapCHI.get(let)):0;

							if(chi1!=chi2 &&
									(chi1==gov.nih.ncats.molwitch.Chirality.R.getParity() ||
											chi1==gov.nih.ncats.molwitch.Chirality.S.getParity())
							){
								let=let.toLowerCase();
							}
						}

						ret += let;

					}
				}

				for(int key:atomIndexToAlphaAtomIndex.keySet()){
					int rmap = atomIndexToAlphaAtomIndex.get(key);
					try{
						int seqp = alphaAtomIndexToSeqIndex.get(rmap);
						atom_to_residue.put(key, (subunit+1) + "_" +(seqp+1));
					}catch(Exception e){

					}
				}
				sequences1Let.add((subunit+1) + "_" + ret);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Collections.sort(sequences1Let,new SequenceComparator());
		Protein prot = new Protein();

		int sNum =1;
		String ret = "";
		for(String seq: sequences1Let){

			String[] split = seq.split("_");
			int psub = Integer.parseInt(split[0]);
			String seq2 = split[1];
			String seq3 = seq2.replaceAll("[0-9]", "");
			String[] nmod=seq2.replaceAll("[^X|0-9]", "").replaceAll("(X[0-9][0-9]*)","$1,").split(",");
			int pind=0;
			for(String n:nmod){
				String smi = PeptideInterpreter.modName.get(n);
				if(smi!=null){
					int res = seq3.indexOf("X", pind);
					pind=res+1;
					prot.addMod(sNum, res+1, smi);
				}
			}
			canonicalSequenceMap.put(psub, sNum);
			sequences1Let.set(sNum-1, seq2);
			sNum++;
			if(ret.length()>0)
				ret+=";";
			prot.addSubunit(seq3);
			ret += seq2;
		}
		ret+="|";
		if(sequences1Let.size()>0){
			List<String> dBridges = new ArrayList<String>();
			for(int i=0;i<bridges.length;i+=2){

				List<String> dBridgeT = new ArrayList<String>(2);
				dBridgeT.add("");
				dBridgeT.add("");

				String at1=canSite(atom_to_residue.get(bridges[i]),canonicalSequenceMap);
				String at2=canSite(atom_to_residue.get(bridges[i+1]),canonicalSequenceMap);
				dBridgeT.set(0,at1);
				dBridgeT.set(1,at2);
				Collections.sort(dBridgeT,new DisulfideComparator());
				dBridges.add((dBridgeT.get(0) + "-" +dBridgeT.get(1)));

			}
			Collections.sort(dBridges,new DisulfideComparator());
			for(String disulf:dBridges){
				String[] ind = disulf.replace("_", "-").split("-");
				if(ind.length>=4){
					prot.addDSBond(Integer.parseInt(ind[0]), Integer.parseInt(ind[1]), Integer.parseInt(ind[2]), Integer.parseInt(ind[3]));
				}
			}
			ret+=dBridges.toString();//+ "|" +modMap.toString();

		}
		return prot;
	}
	public static String getModName(String smiles){

		String let = modMap.get(smiles);
		if(let==null){
			let="X"+modNumber++;
			modMap.put(smiles,let);
			modName.put(let, smiles);

		}
//		System.out.println("getModName of " + smiles + " is = " + let);
		return let;

	}
	public static String canSite(String oSite,Map<Integer,Integer> newSeq){
		if(oSite==null)return null;
		String[] split = oSite.split("_");
		int oindex = Integer.parseInt(split[0]);
		int nindex = newSeq.get(oindex);
		return nindex + "_" + split[1];
	}

	public static String decodePeptide(String str, boolean weird){
		//if(true)return str;
		String str2=str;
		boolean omod =false;

//		System.out.println("in decode peptide weird = " + weird);
		if(weird){
			try {
//				System.out.println("before replace " + str2);
				str2=str2.replace("[55Ne]","C(C([*:2])=O)([NH:9]([*:1]))");
//				System.out.println("after replace " + str2);
				Chemical c = Chemical.parse(str2);
				Atom mat=null;
				Atom mat2=null;
				c.setAtomMapToPosition();
				for(Atom ma:c.getAtoms()){
					if(ma.getAtomToAtomMap().orElse(0)==9){
						mat=ma;
						ma.clearAtomToAtomMap();
					}
					//links to nitro
					if(ma.getMassNumber()==7){
						mat2=ma;
						mat2.setMassNumber(0);
					}
				}
				if(mat!=null && mat2!=null)
					c.addBond(mat,mat2, Bond.BondType.SINGLE);

				//addBond(d(new MolBond(mat,mat2));

				try{
					str2= c.toSmiles();
				}catch(Exception e){
//					e.printStackTrace();
					str2= c.toSmarts();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		str2=str2.replaceAll("\\[7([A-Z|a-z|0-9]*)\\]", "[$1]([*:1])");
		str2=str2.replaceAll("\\[6([A-Z|a-z|0-9]*)\\]", "[$1]([*:2])");
		//if(str2.indexOf("[*:1]")>-1 || str2.indexOf("[*:2]")>-1)omod=true;
		str2=str2.replace("[He]","C(=O)");
		str2=str2.replace("[he]","C(=O)");
		str2=str2.replaceAll("\\[NH[0-9]\\]","N");
		//str2=str2.replace("[Ar]","[*:3]");
		if(!omod){
			str2=str2.replace("[Ne]","C(C([*:2])=O)(N([*:1]))");
			//if(str2.contains("[55Ne]")){
			//	System.out.println(str2);
			//}
			str2=str2.replace("[55Ne]","C(C([*:2])=O)(N(C)([*:1]))");
		}
//		System.out.println("before try..." + str2);
		try {
			//System.out.println(str);
			String stchange =str2.replace("[Ne]","C(C([*:2])=O)(N([*:1]))");
			stchange =stchange.replace("[55Ne]","C(C([*:2])=O)(N([*:1]))");
//			System.out.println("now stchange = " + stchange);
			if(!str2.equals(stchange)){
				str2=stchange;
				Chemical c = Chemical.parse(str2);
				//MolHandler mh = new MolHandler();
				//mh.setMolecule(str2);
				//Molecule m = mh.getMolecule();
				List<Atom> merge = new ArrayList<>();
				List<Atom> rem = new ArrayList<>();
				c.setAtomMapToPosition();

				for(Atom ma:c.getAtoms()){
					if(ma.getAtomToAtomMap().orElse(0)==1){
						merge.add(ma.getBonds().get(0).getOtherAtom(ma));
						rem.add(ma);
					}
				}
				for(Atom ma: rem){
					c.removeAtom(ma);
				}
				for(int i=0;i<merge.size();i++){
					for(int j=i+1;j<merge.size();j++){
						c.addBond(merge.get(i),merge.get(j),Bond.BondType.SINGLE);
					}
				}
				try{
					return c.toSmiles().replace(":2",":1");
				}catch(Exception e){
					return c.toSmarts().replace(":2",":1");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		//override for pyroglu
		if(str2.equals("C(=O)1CCC(C([*:2])=O)(N([*:1]))1")){
			return "[*:2]C(=O)C1CCC(=O)N1";
		}
		return str2;
	}
	public static class DisulfideComparator implements Comparator<String>{

		@Override
		public int compare(String at1, String at2) {
			if(at1==null && at2==null)return 0;
			if(at2==null) return -1;
			if(at1==null) return 1;
			at1=at1.split("-")[0];
			at2=at2.split("-")[0];
			try{
				int at1S=Integer.parseInt(at1.split("_")[0]);
				int at2S=Integer.parseInt(at2.split("_")[0]);
				int at1R=Integer.parseInt(at1.split("_")[1]);
				int at2R=Integer.parseInt(at2.split("_")[1]);

				if(at2S<at1S){
					return 1;
				}else{
					if(at2S==at1S){
						if(at2R<at1R){
							return 1;
						}
						if(at2R==at1R){
							return 0;
						}
					}
				}
			}catch(Exception e ){
				return 0;
			}
			return -1;
		}

	}

	public static class SequenceComparator implements Comparator<String>{

		@Override
		public int compare(String seq1, String seq2) {
			if(seq1==null && seq2==null)return 0;
			if(seq1==null) return -1;
			if(seq2==null) return 1;
			int len = seq1.length()-seq2.length();
			if(len==0){
				return seq1.compareTo(seq2);
			}
			return -len;
		}

	}






	public static class Protein{
		private static final String KEY_STRUCTURAL_MODIFICATIONS = "structuralModifications";
		List<Subunit> subunits = new ArrayList<Subunit>();
		List<DisulfideBond> disulfideLinks = new ArrayList<DisulfideBond>();
		Map<String,List<Map>> modifications= new HashMap<String,List<Map>>();



		public List<Subunit> getSubunits() {
			return subunits;
		}

		public List<DisulfideBond> getDisulfideLinks() {
			return disulfideLinks;
		}

		public Map<String, List<Map>> getModifications() {
			return modifications;
		}

		public class Subunit{
			int subunitIndex;
			String sequence;
			public Subunit(int subIndex,String seq){
				this.subunitIndex=subIndex;
				this.sequence=seq;
			}

			public int getSubunitIndex() {
				return subunitIndex;
			}

			public String getSequence() {
				return sequence;
			}
		}
		public class DisulfideBond{
			Site[] sites = new Site[2];
			public DisulfideBond(int sub1, int res1, int sub2, int res2){
				sites[0]=new Site(sub1,res1);
				sites[1]=new Site(sub2,res2);
			}
			public String toString(){
				return sites[0] + "-"  + sites[1];
			}
		}
		public class Site{
			int subunitIndex;
			int residueIndex;
			public Site(int s, int r){
				this.subunitIndex=s;
				this.residueIndex=r;

			}
			public String toString(){
				return subunitIndex + "_" + residueIndex;
			}
		}
		public Protein(){
			modifications.put(Protein.KEY_STRUCTURAL_MODIFICATIONS, new ArrayList());
		}
		public void addSubunit(String seq){
			subunits.add(new Subunit(subunits.size()+1,seq));
		}
		public void addDSBond(int sub1,int res1, int sub2,int res2){
			disulfideLinks.add(new DisulfideBond(sub1,res1,sub2,res2));
		}
		public void addMod(int sub, int res, String smi){
			modifications.get(Protein.KEY_STRUCTURAL_MODIFICATIONS).add(makeStrModification(sub,res,smi));
		}
		public Map makeStrModification(int sub,int res, String smiles){

			return makeStrModification(new Site[]{new Site(sub,res)},smiles);
		}
		public Map makeStrModification(Site[] sites, String smiles){
			Map mmodMap = new HashMap();
			mmodMap.put("locationType", "SITE-SPECIFIC");
			mmodMap.put("sites", sites);
			mmodMap.put("structuralModificationType", "AMINO_ACID_REPLACEMENT");
			mmodMap.put("_molecularFragmentSmiles", smiles);
			return mmodMap;
		}
		public String toString(){
			StringBuilder sb = new StringBuilder();
			boolean first =true;
			for(Subunit sub:this.subunits){
				if(!first)
					sb.append(";");
				sb.append(sub.sequence);
				first=false;
			}
			if(this.disulfideLinks.size()>0){
				sb.append("|");
				first=true;
				for(DisulfideBond disulf:this.disulfideLinks){
					if(!first)
						sb.append(";");
					sb.append(disulf);
					first=false;
				}
			}
			return sb.toString();
		}

		/*
		 *
		   "modifications": {
            "structuralModifications": [
                {
                    "residueModified": "GLUTAMINE",
                    "locationType": "SITE-SPECIFIC",
                    "structuralModificationType": "AMINO ACID SUBSTITUTION",
                    "sites": [
                        {
                            "subunitIndex": 1,
                            "residueIndex": 1
                        }
                    ],
                    "molecularFragmentID": "SZB83O1W42",
                    "molecularFragmentName": "PYROGLU",
                    "amountType": "PERCENT",
                    "amount": {
                        "average": 100,
                        "lowLimit": null,
                        "highLimit": null,
                        "unit": null,
                        "nonNumericValue": null
                    }
                }
            ],
            "agentModifications": [],
            "physicalModifications": []
        },
		 */
	}
	public static void readFromFile(String fname, LineProcessor eachLine){
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(fname);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {

				if(!eachLine.process(strLine))break;
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public interface LineProcessor{
		public boolean process(String line);
	}

}