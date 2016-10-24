package ix.ginas.controllers.viewfinders;

import java.util.HashMap;
import java.util.Map;

import ix.core.search.SearchResultContext;
import ix.core.util.CachedSupplier;
import ix.ginas.controllers.ResultRenderer;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.MixtureSubstance;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.SpecifiedSubstanceGroup1Substance;
import ix.ginas.models.v1.StructurallyDiverseSubstance;
import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;

public class ListViewFinder {
    static CachedSupplier<Map<String, ResultRenderer<?>>> listRenderers = CachedSupplier.of(() -> {
        Map<String, ResultRenderer<?>> list = new HashMap<>();
        list.put(Substance.class.getName(), (t, ct) -> {
            return ix.ginas.views.html.list.conceptlist.render((Substance) t);
        });
        list.put(ChemicalSubstance.class.getName(), (t, ct) -> {
            return ix.ginas.views.html.list.chemlist.render((ChemicalSubstance) t, ct.getId());
        });
        list.put(ProteinSubstance.class.getName(), (t, ct) -> {
            return ix.ginas.views.html.list.proteinlist.render((ProteinSubstance) t, ct.getId());
        });
        list.put(NucleicAcidSubstance.class.getName(), (t, ct) -> {
            return ix.ginas.views.html.list.nucleicacidlist.render((NucleicAcidSubstance) t, ct.getId());
        });
        list.put(PolymerSubstance.class.getName(), (t, ct) -> {
            return ix.ginas.views.html.list.polymerlist.render((PolymerSubstance) t, ct.getId());
        });
        list.put(MixtureSubstance.class.getName(), (t, ct) -> {
            return ix.ginas.views.html.list.mixlist.render((MixtureSubstance) t);
        });
        list.put(StructurallyDiverseSubstance.class.getName(), (t, ct) -> {
            return ix.ginas.views.html.list.diverselist.render((StructurallyDiverseSubstance) t);
        });
        list.put(SpecifiedSubstanceGroup1Substance.class.getName(), (t, ct) -> {
            return ix.ginas.views.html.list.g1sslist.render((SpecifiedSubstanceGroup1Substance) t);
        });
        return list;
    });
    
    public static <T> ResultRenderer<T> getRendererOrDefault(Class<T> cls, ResultRenderer<T> rend){
        return (ResultRenderer<T>) listRenderers.get().getOrDefault(cls.getName(), rend);
    }
    
    public static <T> Html render(T t, SearchResultContext ctx, ResultRenderer<T> rend) throws Exception{
        return getRendererOrDefault((Class<T>)t.getClass(), rend).render(t,ctx);
    }
}
