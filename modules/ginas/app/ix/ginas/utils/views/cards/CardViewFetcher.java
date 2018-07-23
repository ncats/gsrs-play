package ix.ginas.utils.views.cards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ix.core.util.EntityUtils.EntityWrapper;
import ix.ginas.controllers.GinasApp;
import ix.ginas.controllers.ViewType;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.MixtureSubstance;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.StructurallyDiverseSubstance;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.Substance.SubstanceClass;
import ix.ginas.utils.views.cards.srs.FDADetailsCard;
import ix.utils.Tuple;
import play.twirl.api.Html;

public class CardViewFetcher {
	
	
	
	public static class CardView{
		List<Tuple<DetailCard, Set<ViewType>>> cards = new ArrayList<>();
		
		public CardView add(DetailCard card, ViewType ...types){
			cards.add(Tuple.of(card, Arrays.stream(types).collect(Collectors.toSet())));
			return this;
		}
		
		public List<DetailCard> getCards(ViewType vt){
			return cards.stream()
					     .filter(t->t.v().contains(vt))
					     .map(t->t.k())
					     .filter(c->c.isVisble())
					     .collect(Collectors.toList());
		}
	}
	
	/*
	 * 
History
References
Notes
Relationships
Impurities
Active Moieties
Moieties
Alternative Definitions
Chemical Details
	 */
	
	public static void addAllAdditionalCards(CardView cardView, Substance s){
		//TODO: add additional stitcher cards
		
		/*
		GinasApp.additionalDataCardTest(s).stream()
		.forEach(c->{
			if(!c.getTitle().equals("Other Information")){
				cardView.add(c,  ViewType.Substance, ViewType.Drug);	
			}
		});
		*/
	}
	
	
	
	//TODO: make abstract
	public static List<DetailCard> getCards(Substance s){
		CardView cardView = new CardView();
		
		
		if(s instanceof ChemicalSubstance){
			ChemicalSubstance cs = (ChemicalSubstance)s;
			cardView.add(new StructureCard(cs), ViewType.Substance, ViewType.Drug);
			cardView.add(new MoietiesCard(cs), ViewType.Substance);
		}
		
		addAllAdditionalCards(cardView,s);

		//FDADetails
		cardView.add(new FDADetailsCard(s), ViewType.Substance);

		cardView.add(new SubstanceOverviewCard(s), ViewType.Substance);
		cardView.add(new PrimaryDefinitionCard(s), ViewType.Substance);
		//cardView.add(new AlternativeDefinitionsCard(s), ViewType.Substance);
		cardView.add(new VariantConceptsCard(s), ViewType.Substance);
		cardView.add(new NamesCard(s), ViewType.Substance, ViewType.Drug);
		cardView.add(new ClassificationsCard(s), ViewType.Substance, ViewType.Drug);
		cardView.add(new IdentifiersCard(s), ViewType.Substance, ViewType.Drug);
		
		if(s instanceof ProteinSubstance){
			ProteinSubstance ps = (ProteinSubstance)s;
			
			//Should not be necessary, but forces loading from database.
			//This really needs to be looked into
			String cjson=EntityWrapper.of(ps).toCompactJson();
			
			cardView.add(new SubunitsCard(ps), ViewType.Substance, ViewType.Drug);
			
			cardView.add(new DisulfidesCard(ps), ViewType.Substance);
			cardView.add(new OtherLinksCard(ps), ViewType.Substance);
			cardView.add(new GlycosylationCard(ps), ViewType.Substance);
		}else if(s.substanceClass.equals(SubstanceClass.concept)){
			cardView.add(new ConceptDefinitionCard(s), ViewType.Substance);
		}else if(s instanceof StructurallyDiverseSubstance){
			//TODO
		}else if(s instanceof PolymerSubstance){
			PolymerSubstance ps = (PolymerSubstance)s;
			cardView.add(new PolymerStructureCard(ps), ViewType.Substance, ViewType.Drug);
			cardView.add(new MonomersCard(ps), ViewType.Substance);
			cardView.add(new SRUsCard(ps), ViewType.Substance);
		}else if(s instanceof NucleicAcidSubstance){
			NucleicAcidSubstance ns = (NucleicAcidSubstance)s;
			cardView.add(new SubunitsCard(ns), ViewType.Substance, ViewType.Drug);
			cardView.add(new NaSugarsCard(ns), ViewType.Substance, ViewType.Drug);
			cardView.add(new NaLinkagesCard(ns), ViewType.Substance, ViewType.Drug);
		}else if(s instanceof MixtureSubstance){
			MixtureSubstance ms = (MixtureSubstance)s;
			cardView.add(new MixtureSourceCard(ms), ViewType.Substance);
			cardView.add(new MixtureComponentsCard(ms), ViewType.Substance, ViewType.Drug);
		}
		
		
		
		cardView.add(new RelationshipsCard(s), ViewType.Substance);
		cardView.add(new RelationshipsCard(s, "Metabolites", "metabolites", s.getMetabolites()), ViewType.Substance);
		cardView.add(new RelationshipsCard(s, "Impurities", "impurities", s.getImpurities()), ViewType.Substance);
		cardView.add(new RelationshipsCard(s, "Active Moiety", "activemoieties", s.getActiveMoieties()), ViewType.Substance);
		cardView.add(new ModificationsCard(s), ViewType.Substance);
		cardView.add(new PropertiesCard(s), ViewType.Substance);
		/*
		// TODO: Show if logged in
		cardView.add(new NotesCard(s), ViewType.Substance);
		cardView.add(new AuditInfoCard(s), ViewType.Substance);
		cardView.add(new ReferencesCard(s), ViewType.Substance);
		cardView.add(new HistoryCard(s), ViewType.Substance);
		*/
		//cardView.add(new SubstanceCarCard(s), ViewType.Drug);
		
		
		
		return cardView.getCards(ViewType.Substance);
	}
}
