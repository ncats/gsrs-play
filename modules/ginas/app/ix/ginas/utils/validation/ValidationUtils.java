package ix.ginas.utils.validation;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import ix.core.UserFetcher;
import ix.core.validator.*;
import ix.core.chem.Chem;
import ix.core.chem.StructureProcessor;
import ix.core.controllers.AdminFactory;
import ix.core.controllers.search.SearchRequest;
import ix.core.controllers.search.SearchRequest.Builder;
import ix.core.models.*;
import ix.core.plugins.PayloadPlugin;
import ix.core.search.SearchResult;
import ix.core.util.CachedSupplier;
import ix.ginas.controllers.v1.ReferenceFactory;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.EmbeddedKeywordList;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.v1.*;
import ix.ginas.models.v1.Substance.SubstanceClass;
import ix.ginas.models.v1.Substance.SubstanceDefinitionLevel;
import ix.ginas.models.v1.Substance.SubstanceDefinitionType;
import ix.ginas.utils.*;
import ix.core.validator.GinasProcessingMessage.Link;
import play.Application;
import play.Configuration;
import play.Logger;
import play.Play;
import play.mvc.Call;

public class ValidationUtils {

	private static final String VALIDATION_CONF = "validation.conf";

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
	
	
	private static CachedSupplier<PayloadPlugin> _payload = CachedSupplier.of(()->Play.application().plugin(PayloadPlugin.class));
	static CachedSupplier<ValidationConfig> validationConf = CachedSupplier.of(()->{
		return new ValidationConfig(Play.application());
	});

	
	public static class ValidationConfig{
		Config validationConf;
		private boolean requireName=true;
		private boolean extractLocators=true;
		public ValidationConfig(Application app){
			Configuration conf = app.configuration();
			URL validationUrl = app.classloader().getResource(VALIDATION_CONF);
			if (validationUrl != null) {
				validationConf = ConfigFactory.load(app.classloader(), VALIDATION_CONF);
			}
			extractLocators = conf.getBoolean("ix.ginas.prepare.extractlocators",true);
			if(validationConf!=null){
				requireName=validationConf.getBoolean("requireNames");
			}
		}
		public boolean requireName(){
			return requireName;
		}
	}
	
	
	
	

	static List<GinasProcessingMessage> validateAndPrepare(Substance s, Substance old,
			GinasProcessingStrategy strat) {
		long start = System.currentTimeMillis();

		List<GinasProcessingMessage> gpm = new ArrayList<GinasProcessingMessage>();

		try {
			
			if(s.status == null){
				GinasProcessingMessage mes = GinasProcessingMessage
						.WARNING_MESSAGE(
								"No status specified for substance, defaulting to 'pending'")
						.appliableChange(true);
				gpm.add(mes);
				strat.processMessage(mes);
				if (mes.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
					s.status=Substance.STATUS_PENDING;
				}
			}

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
			s.references.stream()
			 .filter(r->{
				 if("IND".equals(r.docType))return true;
				 Pattern indPattern = Pattern.compile(".*[ ]IND[ -]*[0-9][0-9][0-9]*.*");
				 if(indPattern.matcher(" " + r.citation).find()){
					 return true;
				 }
				 return false;
			 })
			 .filter(r->r.isPublic() || r.isPublicDomain() || r.isPublicReleaseReference())
			 .forEach(r->{
				 
				 GinasProcessingMessage mes = GinasProcessingMessage
							.WARNING_MESSAGE(
									"IND-like reference:\""
											+ r.docType + ":" + r.citation + "\" cannot be public. Setting to protected.")
							.appliableChange(true);
					gpm.add(mes);
					strat.processMessage(mes);
					if (mes.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
						r.publicDomain=false;
						Group g=AdminFactory.getGroupByName("protected");
						if(g==null){
							g=new Group("protected");
							
						}
						
						EmbeddedKeywordList klist = new EmbeddedKeywordList();
						
						r.tags.stream()
						.filter(t->!Reference.PUBLIC_DOMAIN_REF.equals(t.getValue()))
						.forEach(t->{
							klist.add(t);
						});
						
						r.tags=klist;
						
						r.setAccess(Stream.of(g).collect(Collectors.toSet()));
						
						
						
					}
			 });
			 
			 ;
			 
			
			if (s.definitionType == SubstanceDefinitionType.PRIMARY) {
				if (ValidationUtils.validationConf.get().requireName()) {
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
						strat, true));
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
				if(old instanceof ProteinSubstance){
					gpm.addAll(validateAndPrepareProtein((ProteinSubstance) s, (ProteinSubstance) old,
							strat));	
				}else{
					gpm.addAll(validateAndPrepareProtein((ProteinSubstance) s, null,
							strat));
				}
				
				break;
			case structurallyDiverse:
				gpm.addAll(validateAndPrepareStructurallyDiverse(
						(StructurallyDiverseSubstance) s, strat));
				break;
			case reference:
				break;
			case specifiedSubstanceG1:
				gpm.addAll(validateAndPrepareSSG1(
						(SpecifiedSubstanceGroup1Substance) s, strat));
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

	public enum ReferenceAction {
		FAIL, WARN, ALLOW
	}
	
	private static <D extends GinasAccessReferenceControlled> void validatePublicReferenced(Substance s,
			D data,
			List<GinasProcessingMessage> gpm, 
			GinasProcessingStrategy strat, 
			Function<D,String> namer) {
		//If public
		if(data.getAccess().isEmpty()){
			boolean hasPublicReference = data.getReferences().stream()
			                    .map(r->r.getValue())
			                    .map(r->s.getReferenceByUUID(r))
			                    .filter(r->r.isPublic())
			                    .filter(r->r.isPublicDomain())
			                    .findAny()
			                    .isPresent();
			
			if(!hasPublicReference){
				GinasProcessingMessage mes = GinasProcessingMessage
						.ERROR_MESSAGE(namer.apply(data) + " needs an unprotected reference marked \"Public Domain\" in order to be made public.");
				gpm.add(mes);
				strat.processMessage(mes);
			}
		}
		
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

	public static boolean validateReference(Substance s,
											 GinasAccessReferenceControlled data,
											 ValidatorCallback callback,
											 ReferenceAction onemptyref) {

		AtomicBoolean worked = new AtomicBoolean(true);

		Set<Keyword> references = data.getReferences();
		if ((references == null || references.size() <= 0)) {
			if (onemptyref == ReferenceAction.ALLOW) {
				return worked.get();
			}

			GinasProcessingMessage gpmerr = null;
			if (onemptyref == ReferenceAction.FAIL) {
				gpmerr = GinasProcessingMessage.ERROR_MESSAGE(
						data.toString() + " needs at least 1 reference")
						.appliableChange(true);
				worked.set(false);
			} else if (onemptyref == ReferenceAction.WARN) {
				gpmerr = GinasProcessingMessage.WARNING_MESSAGE(
						data.toString() + " needs at least 1 reference")
						.appliableChange(true);
				worked.set(false);
			} else {
				gpmerr = GinasProcessingMessage.WARNING_MESSAGE(
						data.toString() + " needs at least 1 reference")
						.appliableChange(true);
				worked.set(false);
			}

			//this extra reference is so it's effectively final
			//and we can reference it in the lambda
			GinasProcessingMessage gpmerr2 = gpmerr;
			callback.addMessage(gpmerr2, () ->{
				gpmerr2.appliedChange = true;
				Reference r = Reference.SYSTEM_ASSUMED();
				s.references.add(r);
				data.addReference(r.getOrGenerateUUID().toString());
				worked.set(true);
			});

		} else {
			for (Keyword ref : references) {
				Reference r = s.getReferenceByUUID(ref.getValue());
				if (r == null) {
					//GSRS-933 more informative error message if you can find the Reference
					Reference dbReference = ReferenceFactory.getReference(ref.getValue());
					if(dbReference !=null) {
						callback.addMessage(GinasProcessingMessage.ERROR_MESSAGE("Reference  type: \"" + dbReference.docType +
								"\" citation: \"" + dbReference.citation + "\"  uuid \""
								+ ref.getValue() + "\" not found on substance."));
					}else{
					callback.addMessage(GinasProcessingMessage.ERROR_MESSAGE("Reference \""
							+ ref.getValue() + "\" not found on substance."));
					}
					worked.set(false);
				}
			}
		}

		return worked.get();
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

       public static CachedSupplier<List<Replacer>> replacers = CachedSupplier.of(()->{
               List<Replacer> repList = new ArrayList<>();
               repList.add(new Replacer("[\\t\\n\\r]", " ")
                               .message("Name \"$0\" has non-space whitespace characters. They will be replaced with spaces."));
               repList.add(new Replacer("\\s\\s\\s*", " ")
                       .message("Name \"$0\" has consecutive whitespace characters. These will be replaced with single spaces."));
               
               return repList;
               
       });
       
       public static class Replacer{
               Pattern p;
               String replace;
               String message = "String \"$0\" matches forbidden pattern";
               
               public Replacer(String regex, String replace){
                       this.p=Pattern.compile(regex);
                       this.replace=replace;
               }
               
               public boolean matches(String test){
                       return this.p.matcher(test).find();
               }
              public String fix(String test){
                       return test.replaceAll(p.pattern(), replace);
               }
               
               public Replacer message(String msg){
                       this.message=msg;
                       return this;
               }
              
               public String getMessage(String test){
                       return message.replace("$0", test);
               }
               
       }


	private static boolean validateNames(Substance s,
			List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat) {
		boolean preferred = false;
		int display = 0;
		List<Name> remnames = new ArrayList<Name>();

		boolean anyFailed= false;

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
				if (validationConf.get().extractLocators) {
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
				if (n.type == null) {
                    GinasProcessingMessage mes = GinasProcessingMessage
                            .WARNING_MESSAGE(
                                    "Must specify a name type for each name. Defaults to \"Common Name\" (cn)")
                            .appliableChange(true);
                    gpm.add(mes);
                    strat.processMessage(mes);
                    if (mes.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
                        n.type="cn";
                    }
                }

				for(Replacer r: replacers.get()){
                   if(r.matches(n.getName())){
                                               GinasProcessingMessage mes = GinasProcessingMessage
                                   .WARNING_MESSAGE(
                                           r.getMessage(n.getName()))
                                   .appliableChange(true);
                           gpm.add(mes);
                           strat.processMessage(mes);
                           if (mes.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
                               n.setName(r.fix(n.getName()));
                           }   
                   }
                 }
				validatePublicReferenced(s,n,gpm,strat, n1->"The name :\"" + n1.getName() + "\"");

			}
			if (!validateReferenced(s, n, gpm, strat, ReferenceAction.FAIL)) {
				anyFailed = true;
			}
		}

		s.names.removeAll(remnames);
		if (s.names.size() <= 0) {
			GinasProcessingMessage mes = GinasProcessingMessage
					.ERROR_MESSAGE("Substances must have names");
			gpm.add(mes);
			strat.processMessage(mes);
		}

//		if (!preferred) {
//			GinasProcessingMessage mes = GinasProcessingMessage
//					.WARNING_MESSAGE(
//							"Substances should have at least one (1) preferred name, Default to using:"
//									+ s.getName()).appliableChange(true);
//			gpm.add(mes);
//			strat.processMessage(mes);
//			if (mes.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
//				if (s.names.size() > 0) {
//					Name.sortNames(s.names);
//					s.names.get(0).preferred = true;
//					mes.appliedChange = true;
//				}
//			}
//		}
		if (display == 0) {
			GinasProcessingMessage mes = GinasProcessingMessage
					.INFO_MESSAGE(
							"Substances should have exactly one (1) display name, Default to using:"
									+ s.getName()).appliableChange(true);
			gpm.add(mes);
			strat.processMessage(mes);
			if (mes.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
				if (s.names.size() > 0) {
					s.sortLists();
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

		Map<String, Set<String>> nameSetByLanguage = new HashMap<>();
		
		
		for (Name n : s.names) {
			Iterator<Keyword> iter = n.languages.iterator();
			String uppercasedName = n.getName().toUpperCase();
			
			while(iter.hasNext()){
				String language = iter.next().getValue();
//				System.out.println("language for " + n + "  = " + language);
				Set<String> names = nameSetByLanguage.computeIfAbsent(language, k->new HashSet<>());
				if(!names.add(uppercasedName)){
		        GinasProcessingMessage mes = GinasProcessingMessage
                        .ERROR_MESSAGE(
                                "Name '"
                                        + n.getName()
											+ "' is a duplicate name in the record.")
                        .markPossibleDuplicate();
                gpm.add(mes);
                strat.processMessage(mes);
		    }
		    
			}
		    //nameSet.add(n.getName());
			try {
				List<Substance> sr = ix.ginas.controllers.v1.SubstanceFactory
						.getSubstancesWithExactName(100, 0, n.name);
				if (sr != null && !sr.isEmpty()) {
					Substance s2 = sr.iterator().next();
					if (!s2.getUuid().toString().equals(s.getUuid().toString())) {
						GinasProcessingMessage mes = GinasProcessingMessage
								.ERROR_MESSAGE(
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
		return anyFailed;
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
				if (isEffectivelyNull(cd.code)) {
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
				
				if (isEffectivelyNull(cd.codeSystem)) {
				    GinasProcessingMessage mes = GinasProcessingMessage
				            .ERROR_MESSAGE(
				                    "'Code System' should not be null in code objects")
				            .appliableChange(true);
				    strat.processMessage(mes);
				    if (mes.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
				        cd.codeSystem="<no system>";
				        mes.appliedChange = true;
				    }
				    gpm.add(mes);
				    
				}

				if (isEffectivelyNull(cd.type)) {
				    GinasProcessingMessage mes = GinasProcessingMessage
				            .WARNING_MESSAGE(
				                    "Must specify a code type for each name. Defaults to \"PRIMARY\" (PRIMARY)")
				            .appliableChange(true);
				    gpm.add(mes);
				    strat.processMessage(mes);
				    if (mes.actionType == GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE) {
				        cd.type="PRIMARY";
				    }
				}

			}
			if (!validateReferenced(s, cd, gpm, strat, ReferenceAction.ALLOW)) {
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


	/**
	 * Check if the object is effectively null. That is,
	 * is it literally null, or is the string representation
	 * of the object an empty string.
	 *
	 * @param o
	 * @return
	 */
	public static boolean isEffectivelyNull(Object o) {
		if (o == null) {
			return true;
		}
		return o.toString().isEmpty();
	}

	private static boolean validateRelationships(Substance s,
			List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat) {
		List<Relationship> remnames = new ArrayList<Relationship>();
		for (Relationship n : s.relationships) {
			if (isEffectivelyNull(n)) {
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
			if (isEffectivelyNull(n.relatedSubstance)) {
				GinasProcessingMessage mes = GinasProcessingMessage
						.ERROR_MESSAGE(
								"Relationships must specify a related substance");
				gpm.add(mes);
				strat.processMessage(mes);
			}
			if(isEffectivelyNull(n.type)){
				GinasProcessingMessage mes = GinasProcessingMessage
						.ERROR_MESSAGE(
								"Relationships must specify a type");
				gpm.add(mes);
				strat.processMessage(mes);
			}
			if (!validateReferenced(s, n, gpm, strat, ReferenceAction.ALLOW)) {
				return false;
			}
		}
		
		long parentList=s.relationships.stream()
		               .filter(r->"SUBSTANCE->SUB_CONCEPT".equals(r.type))
		               .count();
		
		if(parentList>1){
			GinasProcessingMessage mes = GinasProcessingMessage
					.ERROR_MESSAGE(
							"Variant concepts may not specify more than one parent record");
			gpm.add(mes);
			strat.processMessage(mes);
		}else if(parentList>=1 && (s.substanceClass != SubstanceClass.concept)){
			GinasProcessingMessage mes = GinasProcessingMessage
					.ERROR_MESSAGE(
							"Non-concepts may not be specified as subconcepts.");
			gpm.add(mes);
			strat.processMessage(mes);
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
				Payload payload = _payload.get().createPayload("Sequence Search",
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
					if(dupes > 0) {
						if(dupes > 1){
							mes.message = "There are "
									+ dupes
									+ " substances with a similar sequence to subunit ["
									+ su.subunitIndex + "]:";
						}
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

	private static List<? extends GinasProcessingMessage> validateAndPrepareSSG1(
			SpecifiedSubstanceGroup1Substance cs, GinasProcessingStrategy strat) {
		List<GinasProcessingMessage> gpm = new ArrayList<GinasProcessingMessage>();
		if (cs.specifiedSubstance == null) {
			gpm.add(GinasProcessingMessage
					.ERROR_MESSAGE("Specified substance must have a specified substance component"));
		} else {
			if (cs.specifiedSubstance.constituents== null || cs.specifiedSubstance.constituents.size()==0) {
				gpm.add(GinasProcessingMessage
						.ERROR_MESSAGE("Specified substance must have at least 1 constituent"));
			} else {
				cs.specifiedSubstance.constituents.stream()
							.filter(c->c.substance==null)
							.findAny()
							.ifPresent(missingSubstance->{
								gpm.add(GinasProcessingMessage
										.ERROR_MESSAGE("Specified substance constituents must have an associated substance record"));
							});

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
			
			if(withDisplay && !withIdealized){
				
			}else{
				cs.polymer.displayStructure=null;
				withDisplay=false;
			}
			
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
					|| cs.nucleicAcid.getSubunits().isEmpty()) {
				gpm.add(GinasProcessingMessage
						.ERROR_MESSAGE("Nucleic Acid substance must have at least 1 subunit"));
			}
			if (cs.nucleicAcid.getSugars() == null
					|| cs.nucleicAcid.getSugars().isEmpty()) {
				gpm.add(GinasProcessingMessage
						.ERROR_MESSAGE("Nucleic Acid substance must have at least 1 specified sugar"));
			}
			if (cs.nucleicAcid.getLinkages() == null
					|| cs.nucleicAcid.getLinkages().isEmpty()) {
				gpm.add(GinasProcessingMessage
						.ERROR_MESSAGE("Nucleic Acid substance must have at least 1 specified linkage"));
			}


				int unspSugars = NucleicAcidUtils
						.getNumberOfUnspecifiedSugarSites(cs);
				if (unspSugars != 0) {
					gpm.add(GinasProcessingMessage
							.ERROR_MESSAGE("Nucleic Acid substance must have every base specify a sugar fragment. Missing "
									+ unspSugars + " sites."));
				}

				int unspLinkages = NucleicAcidUtils
						.getNumberOfUnspecifiedLinkageSites(cs);
				//This is meant to say you can't be MISSING a link between 2 sugars in an NA
				if (unspLinkages >0) {
					gpm.add(GinasProcessingMessage
							.ERROR_MESSAGE("Nucleic Acid substance must have every linkage specify a linkage fragment. Missing "
									+ unspLinkages + " sites."));
					//Typically you can't also have an extra link (on the 5' end), but it's allowed
				}else if(unspLinkages < 0 && unspLinkages >= -cs.nucleicAcid.subunits.size()){
					gpm.add(GinasProcessingMessage
							.INFO_MESSAGE("Nucleic Acid Substance specifies more linkages than typically expected. This is typically done to specify a 5' phosphate, but is often sometimes done by accident."));
				}else if(unspLinkages < 0){
					gpm.add(GinasProcessingMessage
							.ERROR_MESSAGE("Nucleic Acid Substance has too many linkage sites specified."));
				}

		}
		return gpm;
	}

	private static List<? extends GinasProcessingMessage> validateAndPrepareProtein(
			ProteinSubstance cs,ProteinSubstance old, GinasProcessingStrategy strat) {

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
		boolean sequenceHasChanged = sequenceHasChanged(cs, old);
//		System.out.println("SEQUENCE HAS CHANGED ?? " + cs.approvalID + "  " + old.approvalID + " ? " + sequenceHasChanged);

		if(sequenceHasChanged) {
			strat.addAndProcess(validateSequenceDuplicates(cs), gpm);
		}
		return gpm;
	}

	private static boolean sequenceHasChanged(ProteinSubstance cs, ProteinSubstance old) {
		if(old ==null){
			return true;
		}

		Protein newProtein = cs.protein;
		Protein oldProtein = old.protein;

		if(oldProtein ==null){
//			System.out.println("old protein is null");
			return newProtein !=null;
		}
		List<Subunit> newSubs = newProtein.getSubunits();
		List<Subunit> oldSubs = oldProtein.getSubunits();
		if(newSubs.size() != oldSubs.size()){
//			System.out.println("subunit size differs " + newSubs.size() + " " + oldSubs.size());
//			System.out.println(newSubs);
//			System.out.println(oldSubs);
			return true;
		}
		int size = newSubs.size();
		for(int i=0; i< size; i++){
			Subunit newSub = newSubs.get(i);
			Subunit oldSub = oldSubs.get(i);

			//handles null
			if(!Objects.equals(newSub.sequence, oldSub.sequence)){
				return true;
			}
		}
		return false;
	}

	public static List<? extends GinasProcessingMessage> validateAndPrepareChemical(
			ChemicalSubstance cs, GinasProcessingStrategy strat, boolean includeReferenceCheck) {
		List<GinasProcessingMessage> gpm = new ArrayList<GinasProcessingMessage>();
		if (cs.structure == null) {
			gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Chemical substance must have a chemical structure"));
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

			  if (cs.moieties != null
	            		&& !cs.moieties.isEmpty()
	                    && cs.moieties.size() != moietiesForSub.size()) {

				GinasProcessingMessage mes = GinasProcessingMessage
						.WARNING_MESSAGE("Incorrect number of moieties")
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
			}else if (cs.moieties == null || cs.moieties.isEmpty()) {

                GinasProcessingMessage mes = GinasProcessingMessage
                        .INFO_MESSAGE("No moieties found in submission. They will be generated automatically.")
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
			
			ChemUtils.fixChiralFlag(cs.structure, gpm);
			ChemUtils.checkChargeBalance(cs.structure, gpm);
			
			if( includeReferenceCheck) {
				Logger.debug("validateAndPrepareChemical about to call validateReferenced");
				validateReferenced((Substance) cs,
						(GinasAccessReferenceControlled) cs.structure, gpm, strat,
						ReferenceAction.FAIL);
			}
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
		oldhash = oldstr.getExactHash();
		newhash = newstr.getExactHash();
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
				oldstr.setMwt(struc2.mwt);
				oldstr.smiles = struc2.smiles;
				oldstr.ezCenters = struc2.ezCenters;
				oldstr.definedStereo = struc2.definedStereo;
				oldstr.stereoCenters = struc2.stereoCenters;
				oldstr.digest = struc2.digest;
				Chem.setFormula(oldstr);
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

		ChemUtils.checkValance(newstr, gpm);
		


		return gpm;
	}


	public static class GinasValidationResponseBuilder<T> extends ValidationResponseBuilder<T> {

		private final GinasProcessingStrategy _strategy;
		public GinasValidationResponseBuilder(T o){
			this(o, GinasProcessingStrategy.ACCEPT_APPLY_ALL());
		}
		public GinasValidationResponseBuilder(T o, GinasProcessingStrategy strategy) {
			super(o, strategy);
			this._strategy = Objects.requireNonNull(strategy);
			boolean allowPossibleDuplicates=false;

			UserProfile currentUser = getCurrentUser();

			if(		currentUser.hasRole(Role.SuperUpdate) ||
					currentUser.hasRole(Role.SuperDataEntry) ||
					currentUser.hasRole(Role.Admin)){
				allowPossibleDuplicates=true;
			}
			this.allowPossibleDuplicates(allowPossibleDuplicates);


		}

		private static UserProfile getCurrentUser(){
			UserProfile up= UserFetcher.getActingUserProfile(true);
			if(up==null){
				up=UserProfile.GUEST();
			}
			return up;
		}


			@Override
		public ValidationResponse<T> buildResponse() {
			ValidationResponse<T> resp =  super.buildResponse();


			if(resp.getNewObect() instanceof Substance) {
				Substance objnew = (Substance) resp.getNewObect();
				List<GinasProcessingMessage> messages = resp.getValidationMessages()
						.stream()
						.filter(m -> m instanceof GinasProcessingMessage)
						.map(m -> (GinasProcessingMessage) m)
						.collect(Collectors.toList());
				messages.stream().forEach(_strategy::processMessage);


				if (_strategy.handleMessages(objnew, messages)) {
					resp.setValid();
				}
				_strategy.addProblems(objnew, messages);


				if (GinasProcessingMessage.ALL_VALID(messages)) {
					resp.addValidationMessage(GinasProcessingMessage.SUCCESS_MESSAGE("Substance is valid"));
				}else{
					//check if anything even after processing is still an error,
					//if so force to invalid
					if(resp.getValidationMessages().stream().filter(ValidationMessage::isError).findAny().isPresent()){
						resp.setInvalid();
					}
				}
			}
			return resp;
		}
	}

	public static List<Substance> findDefinitionaLayer1lDuplicateCandidates(Substance substance){
		Logger.debug("starting in findDefinitionaLayer1lDuplicateCandidates. ");
		List<Substance> candidates = new ArrayList<>();
		try	{
			String layer1Search = "root_definitional_hash_layer_1:"
											+ substance.getDefinitionalElements().getDefinitionalHashLayers().get(0);
			Logger.debug("	layer1Search: " + layer1Search);
			SearchResult sres = new SearchRequest.Builder()
							.query(layer1Search)
							.kind(Substance.class)
							.fdim(0)
							.build()
							.execute();
			sres.waitForFinish();
			List<Substance> submatches = (List<Substance>) sres.getMatches();
			Logger.debug("size of intial match list in findDefinitionaLayer1lDuplicateCandidates: "
							+ submatches.size());

			//return submatches.stream().filter(s -> !s.getUuid().equals(substance.getUuid())).collect(Collectors.toList());
			for (int i = 0; i < submatches.size(); i++)	{
				Substance s = submatches.get(i);
				if (!s.getUuid().equals(substance.getUuid()))	{
					candidates.add(s);
				}
			}
		} catch (Exception ex){
			Logger.error("Error running query", ex);
		}
		return candidates;
	}

	public static List<Substance> findFullDefinitionalDuplicateCandidates(Substance substance){
		Logger.debug("starting in findFullDefinitionalDuplicateCandidates. def has: "
						+ substance.getDefinitionalElements().getDefinitionalHashLayers().get(0));
		List<Substance> candidates = new ArrayList<>();
		try	{
			Builder searchBuilder= new SearchRequest.Builder();
			List<String> hashes= substance.getDefinitionalElements().getDefinitionalHashLayers();
			for(int layer = 0; layer < hashes.size(); layer++) {
				Logger.debug("handling layer: " + (layer+1));
				String searchItem = "root_definitional_hash_layer_" + (layer+1) + ":"
								+ substance.getDefinitionalElements().getDefinitionalHashLayers().get(layer);
				searchBuilder = searchBuilder.query(searchItem);
			}
			SearchResult sres = searchBuilder
							.kind(Substance.class)
							.fdim(0)
							.build()
							.execute();
			sres.waitForFinish();
			List<Substance> submatches = (List<Substance>) sres.getMatches();
			Logger.debug("total submatches: " + submatches.size());

			for (int i = 0; i < submatches.size(); i++)	{
				Substance s = submatches.get(i);
				if (!s.getUuid().equals(substance.getUuid()))	{
					candidates.add(s);
				}
			}
		} catch (Exception ex) {
			Logger.error("Error running query", ex);
		}
		return candidates;
	}

}
