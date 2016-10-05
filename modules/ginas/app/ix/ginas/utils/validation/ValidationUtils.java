package ix.ginas.utils.validation;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import ix.core.chem.StructureProcessor;
import ix.core.models.Keyword;
import ix.core.models.Payload;
import ix.core.models.Structure;
import ix.core.plugins.PayloadPlugin;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.EmbeddedKeywordList;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Component;
import ix.ginas.models.v1.DisulfideLink;
import ix.ginas.models.v1.GinasChemicalStructure;
import ix.ginas.models.v1.MixtureSubstance;
import ix.ginas.models.v1.Moiety;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Note;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.Property;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Site;
import ix.ginas.models.v1.StructurallyDiverseSubstance;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.Substance.SubstanceDefinitionLevel;
import ix.ginas.models.v1.Substance.SubstanceDefinitionType;
import ix.ginas.models.v1.SubstanceReference;
import ix.ginas.models.v1.Subunit;
import ix.ginas.models.v1.Unit;
import ix.ginas.utils.*;
import ix.core.GinasProcessingMessage;
import ix.core.GinasProcessingMessage.Link;
import play.Configuration;
import play.Logger;
import play.Play;
import play.mvc.Call;

public class ValidationUtils {

	public static interface ValidationRule<K>{
		public GinasProcessingMessage validate(K obj);
	}
	
	public static class SubstanceCantBeNull implements ValidationRule<Substance>{
		@Override
		public GinasProcessingMessage validate(Substance s) {
			if (s == null) {
				return GinasProcessingMessage
						.ERROR_MESSAGE("Substance cannot be parsed");
			}
			return GinasProcessingMessage.SUCCESS_MESSAGE("Substance is parsable");
		}
	}
	
	
	static PayloadPlugin _payload = null;
	public static boolean extractLocators = true;
	static Config validationConf = null;
	static boolean requireName = true;

	static {
		init();
	}

	public static void init() {
		Configuration conf = Play.application().configuration();
		URL validationUrl = Play.application().classloader()
				.getResource("validation.conf");
		if (validationUrl != null) {
			validationConf = ConfigFactory.load(Play.application()
					.classloader(), "validation.conf");
			requireName = validationConf.getBoolean("requireNames");
		}
		_payload = Play.application().plugin(PayloadPlugin.class);
		extractLocators = conf.getBoolean("ix.ginas.prepare.extractlocators",
				true);
	}

	static List<GinasProcessingMessage> validateAndPrepare(Substance s,
			GinasProcessingStrategy strat) {
		long start = System.currentTimeMillis();

		List<GinasProcessingMessage> gpm = new ArrayList<GinasProcessingMessage>();

		try {

			if (s == null) {
				gpm.add(GinasProcessingMessage
						.ERROR_MESSAGE("Substance cannot be parsed"));
				return gpm;
			}
			if (s.getUuid() == null) {
				UUID uuid = s.getOrGenerateUUID();
				gpm.add(GinasProcessingMessage
						.INFO_MESSAGE("Substance had no UUID, generated one:"
								+ uuid));
			}
			if (s.definitionType == SubstanceDefinitionType.PRIMARY) {
				if (requireName) {
					validateNames(s, gpm, strat);
				}
				validateCodes(s, gpm, strat);
				validateRelationships(s, gpm, strat);
				validateNotes(s, gpm, strat);
				SubstanceReference sr = s.getPrimaryDefinitionReference();
				if (sr != null) {
					gpm.add(GinasProcessingMessage
							.ERROR_MESSAGE("Primary definitions cannot be alternative definitions for other Primary definitions"));
				}
				for (SubstanceReference relsub : s
						.getAlternativeDefinitionReferences()) {
					Substance subAlternative = SubstanceFactory
							.getFullSubstance(relsub);
					if (subAlternative.isPrimaryDefinition()) {
						gpm.add(GinasProcessingMessage
								.ERROR_MESSAGE("Primary definitions cannot be alternative definitions for other Primary definitions"));
					}
				}

			} else if (s.definitionType == SubstanceDefinitionType.ALTERNATIVE) {
				if (s.substanceClass == Substance.SubstanceClass.concept) {
					gpm.add(GinasProcessingMessage
							.ERROR_MESSAGE("Alternative definitions cannot be \"concepts\""));
				}
				if (s.names != null && s.names.size() > 0) {
					gpm.add(GinasProcessingMessage
							.ERROR_MESSAGE("Alternative definitions cannot have names"));
				}
				if (s.codes != null && s.codes.size() > 0) {
					gpm.add(GinasProcessingMessage
							.ERROR_MESSAGE("Alternative definitions cannot have codes"));
				}
				if (s.relationships == null || s.relationships.size() == 0) {
					gpm.add(GinasProcessingMessage
							.ERROR_MESSAGE("Alternative definitions must specify a primary substance"));
				} else {
					if (s.relationships.size() > 1) {
						gpm.add(GinasProcessingMessage
								.ERROR_MESSAGE("Alternative definitions may only have 1 relationship (to the parent definition), found:"
										+ s.relationships.size()));
					} else {
						SubstanceReference sr = s
								.getPrimaryDefinitionReference();
						if (sr == null) {
							gpm.add(GinasProcessingMessage
									.ERROR_MESSAGE("Alternative definitions must specify a primary substance"));
						} else {
							Substance subPrimary = SubstanceFactory
									.getFullSubstance(sr);
							if (subPrimary == null) {
								gpm.add(GinasProcessingMessage
										.ERROR_MESSAGE("Primary definition for '"
												+ sr.refPname
												+ "' ("
												+ sr.refuuid + ") not found"));
							} else {
								if (subPrimary.definitionType != SubstanceDefinitionType.PRIMARY) {
									gpm.add(GinasProcessingMessage
											.ERROR_MESSAGE("Cannot add alternative definition for '"
													+ sr.refPname
													+ "' ("
													+ sr.refuuid
													+ "). That definition is not primary."));
								} else {
									if (subPrimary.substanceClass == Substance.SubstanceClass.concept) {
										gpm.add(GinasProcessingMessage
												.ERROR_MESSAGE("Cannot add alternative definition for '"
														+ sr.refPname
														+ "' ("
														+ sr.refuuid
														+ "). That definition is not definitional substance record."));
									} else {
										subPrimary
												.addAlternativeSubstanceDefinitionRelationship(s);

									}

								}
							}
						}
					}
				}
			}

			Logger.info("substance Class " + s.substanceClass);

			switch (s.substanceClass) {
			case chemical:
				gpm.addAll(validateAndPrepareChemical((ChemicalSubstance) s,
						strat));
				break;
			case concept:
				break;
			case mixture:
				gpm.addAll(validateAndPrepareMixture((MixtureSubstance) s,
						strat));
				break;
			case nucleicAcid:
				gpm.addAll(validateAndPrepareNa((NucleicAcidSubstance) s, strat));
				break;
			case polymer:
				gpm.addAll(validateAndPreparePolymer((PolymerSubstance) s,
						strat));
				break;
			case protein:
				gpm.addAll(validateAndPrepareProtein((ProteinSubstance) s,
						strat));
				break;
			case structurallyDiverse:
				gpm.addAll(validateAndPrepareStructurallyDiverse(
						(StructurallyDiverseSubstance) s, strat));
				break;
			case reference:
				break;
			case specifiedSubstanceG1:
				break;
			case specifiedSubstanceG2:
				break;
			case specifiedSubstanceG3:
				break;
			case specifiedSubstanceG4:
				break;

			case unspecifiedSubstance:
				break;
			default:
				gpm.add(GinasProcessingMessage
						.ERROR_MESSAGE("Substance class \"" + s.substanceClass
								+ "\" is not valid"));
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
			gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Internal error:"
					+ e.getMessage()));
		}
		return gpm;
	}

	enum ReferenceAction {
		FAIL, WARN, ALLOW
	}

	private static boolean validateReferenced(Substance s,
			GinasAccessReferenceControlled data,
			List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat,
			ReferenceAction onemptyref) {

		boolean worked = true;

		Set<Keyword> references = data.getReferences();
		if ((references == null || references.size() <= 0)) {
			if (onemptyref == ReferenceAction.ALLOW) {
				return worked;
			}

			GinasProcessingMessage gpmerr = null;
			if (onemptyref == ReferenceAction.FAIL) {
				gpmerr = GinasProcessingMessage.ERROR_MESSAGE(
						data.toString() + " needs at least 1 reference")
						.appliableChange(true);
			} else if (onemptyref == ReferenceAction.WARN) {
				gpmerr = GinasProcessingMessage.WARNING_MESSAGE(
						data.toString() + " needs at least 1 reference")
						.appliableChange(true);
			} else {
				gpmerr = GinasProcessingMessage.WARNING_MESSAGE(
						data.toString() + " needs at least 1 reference")
						.appliableChange(true);
			}

			strat.processMessage(gpmerr);
			if (gpmerr.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
				gpmerr.appliedChange = true;
				Reference r = Reference.SYSTEM_ASSUMED();
				s.references.add(r);
				data.addReference(r.getOrGenerateUUID().toString());
			} else {
				worked = false;
			}
			gpm.add(gpmerr);
		} else {
			for (Keyword ref : references) {
				Reference r = s.getReferenceByUUID(ref.getValue());
				if (r == null) {
					gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Reference \""
							+ ref.getValue() + "\" not found on substance."));
					worked = false;
				}
			}
		}

		return worked;
	}

	static private void extractLocators(Substance s, Name n,
			List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat) {
		Pattern p = Pattern.compile("(?:[ \\]])\\[([A-Z0-9]*)\\]");
		Matcher m = p.matcher(n.name);
		Set<String> locators = new LinkedHashSet<String>();
		if (m.find()) {
			do {
				String loc = m.group(1);

				// System.out.println("LOCATOR:" + loc);
				locators.add(loc);
			} while (m.find(m.start(1)));
		}
		if (locators.size() > 0) {
			GinasProcessingMessage mes = GinasProcessingMessage
					.WARNING_MESSAGE(
							"Names of form \"<NAME> [<TEXT>]\" are transformed to locators. The following locators will be added:"
									+ locators.toString())
					.appliableChange(true);
			gpm.add(mes);
			strat.processMessage(mes);
			if (mes.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
				for (String loc : locators) {
					n.name = n.name.replace("[" + loc + "]", "").trim();
				}
				for (String loc : locators) {
					n.addLocator(s, loc);
				}
			}
		}
	}

	private static boolean validateNames(Substance s,
			List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat) {
		boolean preferred = false;
		int display = 0;
		List<Name> remnames = new ArrayList<Name>();
		for (Name n : s.names) {
			if (n == null) {
				GinasProcessingMessage mes = GinasProcessingMessage
						.WARNING_MESSAGE("Null name objects are not allowed")
						.appliableChange(true);
				gpm.add(mes);
				strat.processMessage(mes);
				if (mes.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
					remnames.add(n);
					mes.appliedChange = true;
				}

			} else {
				if (n.preferred) {
					preferred = true;
				}
				if (n.isDisplayName()) {
					display++;
				}
				if (extractLocators) {
					extractLocators(s, n, gpm, strat);
				}
				if (n.languages == null || n.languages.size() == 0) {
					GinasProcessingMessage mes = GinasProcessingMessage
							.WARNING_MESSAGE(
									"Must specify a language for each name. Defaults to \"English\"")
							.appliableChange(true);
					gpm.add(mes);
					strat.processMessage(mes);
					if (mes.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
						if (n.languages == null) {
							n.languages = new EmbeddedKeywordList();
						}
						n.languages.add(new Keyword("en"));
					}
				}
			}
			if (!validateReferenced(s, n, gpm, strat, ReferenceAction.FAIL)) {
				return false;
			}
		}
		s.names.removeAll(remnames);
		if (s.names.size() <= 0) {
			GinasProcessingMessage mes = GinasProcessingMessage
					.ERROR_MESSAGE("Substances must have names");
			gpm.add(mes);
			strat.processMessage(mes);
		}

		if (!preferred) {
			GinasProcessingMessage mes = GinasProcessingMessage
					.WARNING_MESSAGE(
							"Substances should have at least one (1) preferred name, Default to using:"
									+ s.getName()).appliableChange(true);
			gpm.add(mes);
			strat.processMessage(mes);
			if (mes.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
				if (s.names.size() > 0) {
					Name.sortNames(s.names);
					s.names.get(0).preferred = true;
					mes.appliedChange = true;
				}
			}
		}
		if (display == 0) {
			GinasProcessingMessage mes = GinasProcessingMessage
					.INFO_MESSAGE(
							"Substances should have exactly one (1) display name, Default to using:"
									+ s.getName()).appliableChange(true);
			gpm.add(mes);
			strat.processMessage(mes);
			if (mes.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
				if (s.names.size() > 0) {
					Name.sortNames(s.names);
					s.names.get(0).displayName = true;
					mes.appliedChange = true;
				}
			}
		}
		if (display > 1) {
			GinasProcessingMessage mes = GinasProcessingMessage
					.ERROR_MESSAGE("Substance should not have more than one (1) display name. Found "
							+ display);
			gpm.add(mes);
			strat.processMessage(mes);
		}

		for (Name n : s.names) {
			try {
				List<Substance> sr = ix.ginas.controllers.v1.SubstanceFactory
						.getSubstancesWithExactName(100, 0, n.name);
				if (sr != null && !sr.isEmpty()) {
					Substance s2 = sr.iterator().next();
					if (!s2.getUuid().toString().equals(s.getUuid().toString())) {
						GinasProcessingMessage mes = GinasProcessingMessage
								.WARNING_MESSAGE(
										"Name '"
												+ n.name
												+ "' collides (possible duplicate) with existing name for substance:")
								.addLink(GinasUtils.createSubstanceLink(s2));
						gpm.add(mes);
						strat.processMessage(mes);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	private static boolean validateCodes(Substance s,
			List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat) {
		List<Code> remnames = new ArrayList<Code>();
		for (Code cd : s.codes) {
			if (cd == null) {
				GinasProcessingMessage mes = GinasProcessingMessage
						.WARNING_MESSAGE("Null code objects are not allowed")
						.appliableChange(true);
				gpm.add(mes);
				strat.processMessage(mes);
				if (mes.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
					remnames.add(cd);
					mes.appliedChange = true;
				}
			} else {
				if (cd.code == null || cd.code.equals("")) {
					GinasProcessingMessage mes = GinasProcessingMessage
							.ERROR_MESSAGE(
									"'Code' should not be null in code objects")
							.appliableChange(true);
					strat.processMessage(mes);
					if (mes.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
						cd.code="<no code>";
						mes.appliedChange = true;
					}
					gpm.add(mes);
					
				}
			}
			if (!validateReferenced(s, cd, gpm, strat, ReferenceAction.FAIL)) {
				return false;
			}

		}
		s.codes.removeAll(remnames);
		for (Code cd : s.codes) {
			try {
				List<Substance> sr = ix.ginas.controllers.v1.SubstanceFactory
						.getSubstancesWithExactCode(100, 0, cd.code, cd.codeSystem);
				if (sr != null && !sr.isEmpty()) {
					Substance s2 = sr.iterator().next();
					if (!s2.getUuid().toString().equals(s.getUuid().toString())) {
						GinasProcessingMessage mes = GinasProcessingMessage
								.WARNING_MESSAGE(
										"Code '"
												+ cd.code
												+ "'[" +cd.codeSystem
												+ "] collides (possible duplicate) with existing code & codeSystem for substance:")
								. addLink(GinasUtils.createSubstanceLink(s2));
						gpm.add(mes);
						strat.processMessage(mes);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	private static boolean validateRelationships(Substance s,
			List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat) {
		List<Relationship> remnames = new ArrayList<Relationship>();
		for (Relationship n : s.relationships) {
			if (n == null) {
				GinasProcessingMessage mes = GinasProcessingMessage
						.WARNING_MESSAGE(
								"Null relationship objects are not allowed")
						.appliableChange(true);
				gpm.add(mes);
				strat.processMessage(mes);
				if (mes.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
					remnames.add(n);
					mes.appliedChange = true;
				}
			}
			if (!validateReferenced(s, n, gpm, strat, ReferenceAction.ALLOW)) {
				return false;
			}
		}
		s.relationships.removeAll(remnames);
		return true;
	}

	private static boolean validateNotes(Substance s,
			List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat) {
		List<Note> remnames = new ArrayList<Note>();
		for (Note n : s.notes) {
			if (n == null) {
				GinasProcessingMessage mes = GinasProcessingMessage
						.WARNING_MESSAGE("Null note objects are not allowed")
						.appliableChange(true);
				gpm.add(mes);
				strat.processMessage(mes);
				if (mes.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
					remnames.add(n);
					mes.appliedChange = true;
				}
			}
			if (!validateReferenced(s, n, gpm, strat, ReferenceAction.ALLOW)) {
				return false;
			}
		}
		s.notes.removeAll(remnames);
		return true;
	}

	private static List<GinasProcessingMessage> validateStructureDuplicates(
			ChemicalSubstance cs) {
		List<GinasProcessingMessage> gpm = new ArrayList<GinasProcessingMessage>();

		try {

			List<Substance> sr = ix.ginas.controllers.v1.SubstanceFactory
					.getCollsionChemicalSubstances(100, 0, cs);

			if (sr != null && !sr.isEmpty()) {
				int dupes = 0;
				GinasProcessingMessage mes = null;
				for (Substance s : sr) {

					if (cs.getUuid() == null
							|| !s.getUuid().toString()
									.equals(cs.getUuid().toString())) {
						if (dupes <= 0)
							mes = GinasProcessingMessage.WARNING_MESSAGE("Structure has 1 possible duplicate:");
						dupes++;
						mes.addLink(GinasUtils.createSubstanceLink(s));
					}
				}
				if (dupes > 0) {
					if (dupes > 1)
						mes.message = "Structure has " + dupes
								+ " possible duplicates:";
					gpm.add(mes);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return gpm;
	}

	public static List<GinasProcessingMessage> validateSequenceDuplicates(
			ProteinSubstance proteinsubstance) {
		List<GinasProcessingMessage> gpm = new ArrayList<GinasProcessingMessage>();
		try {
			for (Subunit su : proteinsubstance.protein.subunits) {
				Payload payload = _payload.createPayload("Sequence Search",
						"text/plain", su.sequence);
				List<Substance> sr = ix.ginas.controllers.v1.SubstanceFactory
						.getNearCollsionProteinSubstancesToSubunit(10, 0, su);
				if (sr != null && !sr.isEmpty()) {
					int dupes = 0;
					GinasProcessingMessage mes = null;
					for (Substance s : sr) {
						if (proteinsubstance.getUuid() == null
								|| !s.getUuid()
										.toString()
										.equals(proteinsubstance.getUuid()
												.toString())) {

							if (dupes <= 0) {
								mes = GinasProcessingMessage
										.WARNING_MESSAGE("There is 1 substance with a similar sequence to subunit ["
												+ su.subunitIndex + "]:");
								Link l = new Link();
								Call call = ix.ginas.controllers.routes.GinasApp
										.substances(payload.id.toString(), 16,1);
								l.href = call.url() + "&type=sequence";
								l.text = "Perform similarity search on subunit ["
										+ su.subunitIndex + "]";

								mes.addLink(l);
							}
							dupes++;
							mes.addLink(GinasUtils.createSubstanceLink(s));
						}
					}
					if (dupes > 0) {

						if (dupes > 1)
							mes.message = "There are "
									+ dupes
									+ " substances with a similar sequence to subunit ["
									+ su.subunitIndex + "]:";
						gpm.add(mes);
					}
				}
			}
		} catch (Exception e) {
			gpm.add(GinasProcessingMessage
					.ERROR_MESSAGE("Error performing seqeunce search on protein:"
							+ e.getMessage()));
		}
		return gpm;
	}

	private static List<? extends GinasProcessingMessage> validateAndPrepareMixture(
			MixtureSubstance cs, GinasProcessingStrategy strat) {
		List<GinasProcessingMessage> gpm = new ArrayList<GinasProcessingMessage>();
		if (cs.mixture == null) {
			gpm.add(GinasProcessingMessage
					.ERROR_MESSAGE("Mixture substance must have a mixture element"));
		} else {
			if (cs.mixture.components == null
					|| cs.mixture.components.size() < 2) {
				gpm.add(GinasProcessingMessage
						.ERROR_MESSAGE("Mixture substance must have at least 2 mixture components"));
			} else {
				Set<String> mixtureIDs = new HashSet<String>();
				for (Component c : cs.mixture.components) {
					if (c.substance == null) {
						gpm.add(GinasProcessingMessage
								.ERROR_MESSAGE("Mixture components must reference a substance record, found:\"null\""));
					}else if(c.type == null || c.type.length()<=0){
						gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Mixture components must specify a type"));
					}else {
						Substance comp = SubstanceFactory.getFullSubstance(c.substance);
						if (comp == null) {
							gpm.add(GinasProcessingMessage
									.WARNING_MESSAGE("Mixture substance references \""
											+ c.substance.getName()
											+ "\" which is not yet registered"));
						}
						if (!mixtureIDs.contains(c.substance.refuuid)) {
							mixtureIDs.add(c.substance.refuuid);
						} else {
							gpm.add(GinasProcessingMessage
									.ERROR_MESSAGE("Cannot reference the same mixture substance twice in a mixture:\""
											+ c.substance.refPname + "\""));
						}
					}
				}
			}
		}
		return gpm;
	}

	private static List<? extends GinasProcessingMessage> validateAndPrepareStructurallyDiverse(
			StructurallyDiverseSubstance cs, GinasProcessingStrategy strat) {
		List<GinasProcessingMessage> gpm = new ArrayList<GinasProcessingMessage>();
		if (cs.structurallyDiverse == null) {
			gpm.add(GinasProcessingMessage
					.ERROR_MESSAGE("Structurally diverse substance must have a structurally diverse element"));
		} else {
			if (cs.structurallyDiverse.sourceMaterialClass == null
					|| cs.structurallyDiverse.sourceMaterialClass.equals("")) {
				gpm.add(GinasProcessingMessage
						.ERROR_MESSAGE("Structurally diverse substance must specify a sourceMaterialClass"));
			} else {
				if (cs.structurallyDiverse.sourceMaterialClass
						.equals("ORGANISM")) {
					boolean hasParent = false;
					boolean hasTaxon = false;
					if (cs.structurallyDiverse.parentSubstance != null) {
						hasParent = true;
					}
					if (cs.structurallyDiverse.organismFamily != null
							&& !cs.structurallyDiverse.organismFamily
									.equals("")) {
						hasTaxon = true;
					}
					if (cs.structurallyDiverse.part == null
							|| cs.structurallyDiverse.part.isEmpty()) {
						gpm.add(GinasProcessingMessage
								.ERROR_MESSAGE("Structurally diverse organism substance must specify at least one (1) part"));
					}
					if (!hasParent && !hasTaxon) {
						gpm.add(GinasProcessingMessage
								.ERROR_MESSAGE("Structurally diverse organism substance must specify a parent substance, or a family"));
					}
					if (hasParent && hasTaxon) {
						gpm.add(GinasProcessingMessage
								.WARNING_MESSAGE("Structurally diverse organism substance typically should not specify both a parent and taxonomic information"));
					}
				}

			}
			if (cs.structurallyDiverse.sourceMaterialType == null
					|| cs.structurallyDiverse.sourceMaterialType.equals("")) {
				gpm.add(GinasProcessingMessage
						.ERROR_MESSAGE("Structurally diverse substance must specify a sourceMaterialType"));
			}

		}
		return gpm;
	}

	private static List<? extends GinasProcessingMessage> validateAndPreparePolymer(
			PolymerSubstance cs, GinasProcessingStrategy strat) {
		List<GinasProcessingMessage> gpm = new ArrayList<GinasProcessingMessage>();
		if (cs.polymer == null) {
			gpm.add(GinasProcessingMessage
					.ERROR_MESSAGE("Polymer substance must have a polymer element"));
		} else {
			boolean withDisplay = !isNull(cs.polymer.displayStructure);
			boolean withIdealized = !isNull(cs.polymer.idealizedStructure);
			if (!withDisplay && !withIdealized) {
				GinasProcessingMessage gpmwarn = GinasProcessingMessage
						.ERROR_MESSAGE("No Display Structure or Idealized Structure found");
				gpm.add(gpmwarn);
			} else if (!withDisplay && withIdealized) {
				GinasProcessingMessage gpmwarn = GinasProcessingMessage
						.WARNING_MESSAGE(
								"No Display Structure found, default to using Idealized Structure")
						.appliableChange(true);
				gpm.add(gpmwarn);
				strat.processMessage(gpmwarn);

				switch (gpmwarn.actionType) {
				case APPLY_CHANGE:
					try {
						cs.polymer.displayStructure = cs.polymer.idealizedStructure
								.copy();
					} catch (Exception e) {
						gpm.add(GinasProcessingMessage.ERROR_MESSAGE(e
								.getMessage()));
					}
					break;
				case DO_NOTHING:
				case FAIL:
				case IGNORE:
				default:
					break;
				}
			} else if (withDisplay && !withIdealized) {
				GinasProcessingMessage gpmwarn = GinasProcessingMessage
						.INFO_MESSAGE(
								"No Idealized Structure found, default to using Display Structure")
						.appliableChange(true);
				gpm.add(gpmwarn);
				strat.processMessage(gpmwarn);
				switch (gpmwarn.actionType) {
				case APPLY_CHANGE:
					try {
						cs.polymer.idealizedStructure = cs.polymer.displayStructure
								.copy();
					} catch (Exception e) {
						gpm.add(GinasProcessingMessage.ERROR_MESSAGE(e
								.getMessage()));
					}
					break;
				case DO_NOTHING:
				case FAIL:
				case IGNORE:
				default:
					break;
				}
			}

			if (cs.polymer.structuralUnits == null
					|| cs.polymer.structuralUnits.size() <= 0) {
				gpm.add(GinasProcessingMessage
						.WARNING_MESSAGE("Polymer substance should have structural units"));
			} else {
				List<Unit> srus = cs.polymer.structuralUnits;
				// ensure that all mappings make sense
				// first of all, any mapping should be found as a key somewhere
				Set<String> rgroupsWithMappings = new HashSet<String>();
				Set<String> rgroupsActual = new HashSet<String>();
				Set<String> rgroupMentions = new HashSet<String>();
				Set<String> connections = new HashSet<String>();

				for (Unit u : srus) {
					List<String> contained = u.getContainedConnections();
					List<String> mentioned = u.getMentionedConnections();
					if (mentioned != null) {
						if (!contained.containsAll(mentioned)) {
							gpm.add(GinasProcessingMessage
									.ERROR_MESSAGE("Mentioned attachment points '"
											+ mentioned.toString()
											+ "' in unit '"
											+ u.label
											+ "' are not all found in actual connecitons '"
											+ contained.toString() + "'. "));
						}
					}
					Map<String, LinkedHashSet<String>> mymap = u
							.getAttachmentMap();
					if (mymap != null) {
						for (String k : mymap.keySet()) {
							rgroupsWithMappings.add(k);
							for (String m : mymap.get(k)) {
								rgroupMentions.add(m);
								connections.add(k + "-" + m);
							}
						}
					}
				}
				if (!rgroupsWithMappings.containsAll(rgroupMentions)) {
					Set<String> leftovers = new HashSet<String>(rgroupMentions);
					leftovers.removeAll(rgroupsWithMappings);
					gpm.add(GinasProcessingMessage
							.ERROR_MESSAGE("Mentioned attachment point(s) '"
									+ leftovers.toString()
									+ "' cannot be found "));
				}

				Map<String, String> newConnections = new HashMap<String, String>();
				// symmetry detection
				for (String con : connections) {
					String[] c = con.split("-");
					if (!connections.contains(c[1] + "-" + c[0])) {
						GinasProcessingMessage gp = GinasProcessingMessage
								.WARNING_MESSAGE(
										"Connection '"
												+ con
												+ "' does not have inverse connection. This can be created.")
								.appliableChange(true);
						strat.processMessage(gp);
						gpm.add(gp);
						switch (gp.actionType) {
						case APPLY_CHANGE:
							String old = newConnections.get(c[1]);
							if (old == null)
								old = "";
							newConnections.put(c[1], old + c[0] + ";");
							break;
						case DO_NOTHING:
							break;
						case FAIL:
							break;
						case IGNORE:
							break;
						default:
							break;

						}

					}
				}
				for (Unit u : srus) {
					for (String c : u.getContainedConnections()) {
						String additions = newConnections.get(c);
						if (additions != null) {
							for (String add : additions.split(";")) {
								if (!add.equals("")) {
									u.addConnection(c, add);
								}
							}
						}
					}
				}

			}
			if (cs.polymer.monomers == null || cs.polymer.monomers.size() <= 0) {
				gpm.add(GinasProcessingMessage
						.WARNING_MESSAGE("Polymer substance should have monomers"));
			}
			if (cs.properties == null || cs.properties.size() <= 0) {
				gpm.add(GinasProcessingMessage
						.WARNING_MESSAGE("Polymer substance has no properties, typically expected at least a molecular weight"));
			}
		}
		return gpm;
	}

	public static boolean isNull(GinasChemicalStructure gcs) {
		if (gcs == null || gcs.molfile == null)
			return true;
		return false;
	}

	private static List<? extends GinasProcessingMessage> validateAndPrepareNa(
			NucleicAcidSubstance cs, GinasProcessingStrategy strat) {
		List<GinasProcessingMessage> gpm = new ArrayList<GinasProcessingMessage>();
		if (cs.nucleicAcid == null) {
			gpm.add(GinasProcessingMessage
					.ERROR_MESSAGE("Nucleic Acid substance must have a nucleicAcid element"));
		} else {
			if (cs.nucleicAcid.getSubunits() == null
					|| cs.nucleicAcid.getSubunits().size() < 1) {
				gpm.add(GinasProcessingMessage
						.ERROR_MESSAGE("Nucleic Acid substance must have at least 1 subunit"));
			} else {

			}
			if (cs.nucleicAcid.getSugars() == null
					|| cs.nucleicAcid.getSugars().size() < 1) {
				gpm.add(GinasProcessingMessage
						.ERROR_MESSAGE("Nucleic Acid substance must have at least 1 specified sugar"));
			} else {

			}
			if (cs.nucleicAcid.getLinkages() == null
					|| cs.nucleicAcid.getLinkages().size() < 1) {
				gpm.add(GinasProcessingMessage
						.ERROR_MESSAGE("Nucleic Acid substance must have at least 1 specified linkage"));
			} else {

			}

			{
				int unspSugars = NucleicAcidUtils
						.getNumberOfUnspecifiedSugarSites(cs);
				if (unspSugars != 0) {
					gpm.add(GinasProcessingMessage
							.ERROR_MESSAGE("Nucleic Acid substance must have every base specify a sugar fragment. Missing "
									+ unspSugars + " sites."));
				}
			}
			{
				int unspLinkages = NucleicAcidUtils
						.getNumberOfUnspecifiedLinkageSites(cs);
				if (unspLinkages != 0) {
					gpm.add(GinasProcessingMessage
							.ERROR_MESSAGE("Nucleic Acid substance must have every linkage specify a linkage fragment. Missing "
									+ unspLinkages + " sites."));
				}
			}
		}
		return gpm;
	}

	private static List<? extends GinasProcessingMessage> validateAndPrepareProtein(
			ProteinSubstance cs, GinasProcessingStrategy strat) {

		List<GinasProcessingMessage> gpm = new ArrayList<GinasProcessingMessage>();
		if (cs.protein == null) {
			gpm.add(GinasProcessingMessage
					.ERROR_MESSAGE("Protein substance must have a protein element"));
		} else {
			if(cs.protein.subunits.isEmpty() ){
				if(SubstanceDefinitionLevel.INCOMPLETE.equals(cs.definitionLevel)){
					gpm.add(GinasProcessingMessage.WARNING_MESSAGE("Having no subunits is allowed but discouraged for incomplete protein records."));
				}else{
					gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Complete protein substance must have at least one Subunit element. Please add a subunit, or mark as incomplete."));
				}
			}
			for (int i = 0; i < cs.protein.subunits.size(); i++) {
				Subunit su = cs.protein.subunits.get(i);
				if (su.subunitIndex == null) {
					GinasProcessingMessage mes = GinasProcessingMessage
							.WARNING_MESSAGE(
									"Protein subunit (at "
											+ (i + 1)
											+ " position) has no subunit index, defaulting to:"
											+ (i + 1)).appliableChange(true);
					gpm.add(mes);
					strat.processMessage(mes);

					switch (mes.actionType) {
					case APPLY_CHANGE:
						su.subunitIndex = i + 1;
						break;
					case DO_NOTHING:
						break;
					case FAIL:
						break;
					case IGNORE:
						break;
					default:
						break;
					}
				}
			}

			for (DisulfideLink l : cs.protein.getDisulfideLinks()) {

				List<Site> sites = l.getSites();
				if (sites.size() != 2) {
					GinasProcessingMessage mes = GinasProcessingMessage
							.ERROR_MESSAGE("Disulfide Link \""
									+ sites.toString() + "\" has "
									+ sites.size() + " sites, should have 2");
					gpm.add(mes);
				} else {
					for (Site s : sites) {
						String res = cs.protein.getResidueAt(s);
						if (res == null) {
							GinasProcessingMessage mes = GinasProcessingMessage
									.ERROR_MESSAGE("Site \"" + s.toString()
											+ "\" does not exist");
							gpm.add(mes);
						} else {
							if (!res.equalsIgnoreCase("C")) {
								GinasProcessingMessage mes = GinasProcessingMessage
										.ERROR_MESSAGE("Site \""
												+ s.toString()
												+ "\" in disulfide link is not a Cysteine, found: \""
												+ res + "\"");
								gpm.add(mes);
							}
						}
					}
				}

			}

			Set<String> unknownRes = new HashSet<String>();
			double tot = ProteinUtils.generateProteinWeight(cs, unknownRes);
			if (unknownRes.size() > 0) {
				GinasProcessingMessage mes = GinasProcessingMessage
						.WARNING_MESSAGE("Protein has unknown amino acid residues: "
								+ unknownRes.toString());
				gpm.add(mes);
			}

			List<Property> molprops = ProteinUtils.getMolWeightProperties(cs);
			if (molprops.size() <= 0) {

				GinasProcessingMessage mes = GinasProcessingMessage
						.WARNING_MESSAGE(
								"Protein has no molecular weight, defaulting to calculated value of: "
										+ String.format("%.2f", tot)).appliableChange(true);
				gpm.add(mes);
				strat.processMessage(mes);

				switch (mes.actionType) {
				case APPLY_CHANGE:
					cs.properties.add(ProteinUtils.makeMolWeightProperty(tot));
					if (unknownRes.size() > 0) {
						GinasProcessingMessage mes2 = GinasProcessingMessage
								.WARNING_MESSAGE("Calculated protein weight questionable, due to unknown amino acid residues: "
										+ unknownRes.toString());
						gpm.add(mes2);
					}
					break;
				case DO_NOTHING:
					break;
				case FAIL:
					break;
				case IGNORE:
					break;
				default:
					break;
				}
			} else {
				for(Property p :molprops){
					if (p.getValue() != null) {
						Double avg=p.getValue().average;
						if(avg==null)continue;
						double delta = tot - avg;
						double pdiff = delta / (avg);
	
						int len = 0;
						for (Subunit su : cs.protein.subunits) {
							len += su.sequence.length();
						}
						double avgoff = delta / len;
						// System.out.println("Diff:" + pdiff + "\t" + avgoff);
						if (Math.abs(pdiff) > .05) {
							gpm.add(GinasProcessingMessage
									.WARNING_MESSAGE(
											"Calculated weight ["
													+ String.format("%.2f", tot)
													+ "] is greater than 5% off of given weight ["
													+ String.format("%.2f", p.getValue().average) + "]")
									.appliableChange(true));
						}
						//if it gets this far, break out of the properties
						break;
					}
				}
			}
			// System.out.println("calc:" + tot);
		}
		strat.addAndProcess(validateSequenceDuplicates(cs), gpm);
		return gpm;
	}

	public static List<? extends GinasProcessingMessage> validateAndPrepareChemical(
			ChemicalSubstance cs, GinasProcessingStrategy strat) {
		List<GinasProcessingMessage> gpm = new ArrayList<GinasProcessingMessage>();
		if (cs.structure == null) {
			gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Chemical substance must have a chemical structure"));
			System.out.println("This shold not be possible");
			return gpm;
		}

		try {
			ix.ginas.utils.validation.PeptideInterpreter.Protein p = PeptideInterpreter
					.getAminoAcidSequence(cs.structure.molfile);
			if (p != null && p.subunits.size() >= 1
					&& p.subunits.get(0).sequence.length() > 2) {
				GinasProcessingMessage mes = GinasProcessingMessage
						.WARNING_MESSAGE("Substance may be represented as protein as well. Sequence:["
								+ p.toString() + "]");
				gpm.add(mes);
				strat.processMessage(mes);
			}
		} catch (Exception e) {

		}

		String payload = cs.structure.molfile;
		if (payload != null) {
			Structure struc = null;

			List<Moiety> moietiesForSub = new ArrayList<Moiety>();

			{
				List<Structure> moieties = new ArrayList<Structure>();
				struc = StructureProcessor.instrument(payload, moieties, true); // don't
																				// standardize
				for (Structure m : moieties) {
					Moiety m2 = new Moiety();
					m2.structure = new GinasChemicalStructure(m);
					m2.setCount(m.count);
					moietiesForSub.add(m2);
				}
			}
			if (cs.moieties == null
					|| cs.moieties.size() != moietiesForSub.size()) {

				GinasProcessingMessage mes = GinasProcessingMessage
						.WARNING_MESSAGE("Incorrect number of moeities")
						.appliableChange(true);
				gpm.add(mes);
				strat.processMessage(mes);
				switch (mes.actionType) {
				case APPLY_CHANGE:
					cs.moieties = moietiesForSub;
					mes.appliedChange = true;
					break;
				case FAIL:
					break;
				case DO_NOTHING:
				case IGNORE:
				default:
					break;
				}
			} else {
				for (Moiety m : cs.moieties) {
					Structure struc2 = StructureProcessor.instrument(
							m.structure.molfile, null, true); // don't
																// standardize
					strat.addAndProcess(
							validateChemicalStructure(m.structure, struc2,
									strat), gpm);
				}
			}
			strat.addAndProcess(
					validateChemicalStructure(cs.structure, struc, strat), gpm);
			validateReferenced((Substance) cs,
					(GinasAccessReferenceControlled) cs.structure, gpm, strat,
					ReferenceAction.FAIL);
			strat.addAndProcess(validateStructureDuplicates(cs), gpm);
		} else {
			gpm.add(GinasProcessingMessage
					.ERROR_MESSAGE("Chemical substance must have a valid chemical structure"));

		}
		return gpm;
	}

	private static List<GinasProcessingMessage> validateChemicalStructure(
			GinasChemicalStructure oldstr, Structure newstr,
			GinasProcessingStrategy strat) {
		List<GinasProcessingMessage> gpm = new ArrayList<GinasProcessingMessage>();

		String oldhash = null;
		String newhash = null;
		oldhash = oldstr.getLychiv4Hash();
		newhash = newstr.getLychiv4Hash();
		// Should always use the calculated pieces
		// TODO: Come back to this and allow for SOME things to be overloaded
		if (true || !newhash.equals(oldhash)) {
			GinasProcessingMessage mes = GinasProcessingMessage.INFO_MESSAGE(
					"Given structure hash disagrees with computed")
					.appliableChange(true);

			gpm.add(mes);
			strat.processMessage(mes);
			switch (mes.actionType) {
			case APPLY_CHANGE:
				Structure struc2 = new GinasChemicalStructure(newstr);
				oldstr.properties = struc2.properties;
				oldstr.charge = struc2.charge;
				oldstr.formula = struc2.formula;
				oldstr.mwt = struc2.mwt;
				oldstr.smiles = struc2.smiles;
				oldstr.ezCenters = struc2.ezCenters;
				oldstr.definedStereo = struc2.definedStereo;
				oldstr.stereoCenters = struc2.stereoCenters;
				oldstr.digest = struc2.digest;
				mes.appliedChange = true;
				break;
			case FAIL:
				break;
			case DO_NOTHING:
			case IGNORE:
			default:
				break;
			}
		}
		if (oldstr.digest == null) {
			oldstr.digest = newstr.digest;
		}
		if (oldstr.smiles == null) {
			oldstr.smiles = newstr.smiles;
		}
		if (oldstr.ezCenters == null) {
			oldstr.ezCenters = newstr.ezCenters;
		}
		if (oldstr.definedStereo == null) {
			oldstr.definedStereo = newstr.definedStereo;
		}
		if (oldstr.stereoCenters == null) {
			oldstr.stereoCenters = newstr.stereoCenters;
		}
		if (oldstr.mwt == null) {
			oldstr.mwt = newstr.mwt;
		}
		if (oldstr.formula == null) {
			oldstr.formula = newstr.formula;
		}
		if (oldstr.charge == null) {
			oldstr.charge = newstr.charge;
		}

		ChemUtils.CheckValanace(newstr, gpm);

		return gpm;
	}
}