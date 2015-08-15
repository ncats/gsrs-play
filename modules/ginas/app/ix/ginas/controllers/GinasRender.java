package ix.ginas.controllers;

import ix.ginas.models.utils.GinasV1ProblemHandler;
import ix.ginas.models.v1.Substance;
import play.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GinasRender {
	public static String nullMolfile = "\n\n\n  0  0  0     0  0            999 V2000\nM  END\n> <SUBSTANCE_TYPE>\n\n\n> <TOP_TEXT>\nNO IMAGE\n\n$$$$";

	public static String displayMolfileFromJSON(Substance ret2)
			throws Exception {
		String ret = null;
		ObjectMapper mapper = new ObjectMapper ();
		mapper.addHandler(new GinasV1ProblemHandler ());
		JsonNode tree = mapper.valueToTree(ret2);
		if(tree==null){
			Logger.info("Map is null");
		}else{
			Logger.info("Working with map");
		}
		JsonNode subclass = tree.get("substanceClass");
		if (subclass != null && !subclass.isNull()) {
			Substance.SubstanceClass type =
					Substance.SubstanceClass.valueOf(ret2.substanceClass.toString());
			//Logger.info(type);
			switch (type) {
			case chemical:
				ret = extractChemicalMfile(tree);
				break;
			case protein:
				ret = extractProteinMfile(tree);
				break;
			case nucleicAcid:
				ret = extractNucleicAcidMfile(tree);
				break;
			case structurallyDiverse:
				ret = extractStructurallyDiverseMfile(tree);
				break;
			case polymer:
				ret = extractPolymerMfile(tree);
				break;
			case mixture:
				ret = extractMixtureMfile(tree);
				break;
			default:
				return nullMolfile;
			}
			}
		
		return ret;
	}


			private static String extractMixtureMfile(JsonNode tree) {
				JsonNode seq = tree.get("mixture");

				// System.out.println(seq.toString());
				StringBuilder sb = new StringBuilder();
				sb.append("MIXTURE");
				StringBuilder sb2 = new StringBuilder();
				sb2.append(seq.size() + " COMPONENTS");
				return "\n\n\n  0  0  0     0  0            999 V2000\nM  END\n> <SUBSTANCE_TYPE>\n\n\n> <TOP_TEXT>\n"
				+ sb.toString()
				+ "\n\n> <BOTTOM_TEXT>\n"
				+ sb2.toString()
				+ "\n\n$$$$";
			}

			private static String extractPolymerMfile(JsonNode tree) {

				String ret;
				try {
					ret =  tree.get("polymer").get("displayStructure").get("molfile").asText();
				} catch (Exception e) {
					ret = nullMolfile;
				}

				ret = SimpleSDFPropertyAppend(ret, "BOTTOM_TEXT", "POLYMER");
				return ret;
			}

			private static String extractChemicalMfile(JsonNode tree) {
				String ret = tree.get("structure").get("molfile").asText();
				JsonNode moieties = tree.get("moieties");
				String btext = null;
				System.out.println(moieties.size());
				for (JsonNode moi : moieties) {
					// System.out.println("OK");
					String s = moi.get("stereochemistry").asText() + "";
					if (s.equals("") || s.equals("null")) {

					} else {
						if (!s.equals("ACHIRAL")) {
							btext = s;
							if (s.equals("UNKNOWN")) {
								btext = "UNKNOWN " + moi.get("opticalActivity");
							}
						}
					}
				}
				if (btext != null) {
					ret = SimpleSDFPropertyAppend(ret, "BOTTOM_TEXT", btext);
				}

				return ret;

			}

			private static String extractStructurallyDiverseMfile(
					JsonNode tree) {
				JsonNode seq = tree.get("structurallyDiverse");
				// System.out.println(seq.toString());
				StringBuilder sb = new StringBuilder();
				sb.append(seq.get("organismGenus"));
				sb.append(" ");
				sb.append(seq.get("organismSpecies"));
				StringBuilder sb2 = new StringBuilder();
				sb2.append(seq.get("part"));
				// sb.append(seq.get("\n"));
				// sb.append(seq.get("part"));
				return "\n\n\n  0  0  0     0  0            999 V2000\nM  END\n> <SUBSTANCE_TYPE>\n\n\n> <TOP_TEXT>\n"
				+ sb.toString()
				+ "\n\n> <BOTTOM_TEXT>\n"
				+ sb2.toString()
				+ "\n\n$$$$";
			}

			public static String extractProteinMfile(JsonNode tree) {
				JsonNode seq = tree.get("protein").get("subunits");
				// System.out.println(seq.toString());
				StringBuilder sb = new StringBuilder();
				for (JsonNode sub : seq) {
					sb.append(sub.get("sequence"));
				}
				return "\n\n\n  0  0  0     0  0            999 V2000\nM  END\n> <SUBSTANCE_TYPE>\n\n\n> <AMINO_ACID_SEQUENCE>\n"
				+ sb.toString() + "\n\n$$$$";
			}

			public static String extractNucleicAcidMfile(JsonNode tree) {
				JsonNode seq = tree.get("nucleicAcid").get("subunits");
				// System.out.println(seq.toString());
				StringBuilder sb = new StringBuilder();
				for (JsonNode sub : seq) {
					sb.append(sub.get("sequence"));
				}
				return "\n\n\n  0  0  0     0  0            999 V2000\nM  END\n> <SUBSTANCE_TYPE>\n\n\n> <AMINO_ACID_SEQUENCE>\n5'-"
				+ sb.toString() + "-3'\n\n$$$$";
			}

			private static String SimpleSDFPropertyAppend(String sdf, String key,
					String val) {
				StringBuilder sb = new StringBuilder();
//				Map<String, String> kvprops = new HashMap<String, String>();
				boolean replacing = false;
				for (String line : sdf.split("\n")) {
					if (line.startsWith(">")) {
						String k = line.split("<")[1].split(">")[0];
						if (k.equals(key)) {
							replacing = true;
						} else {
							replacing = false;
						}

						// kvprops.put(key, value)
					}
					if (!replacing) {
						if (!line.equals("$$$$")) {
							sb.append(line + "\n");
						}
					}
				}
				sb.append("> <" + key + ">\n" + val + "\n\n");
				sb.append("$$$$");
				return sb.toString();

			}
		}
