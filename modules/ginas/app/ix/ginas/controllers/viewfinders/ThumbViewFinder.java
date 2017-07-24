package ix.ginas.controllers.viewfinders;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import ix.core.search.SearchResultContext;
import ix.core.util.CachedSupplier;
import ix.ginas.controllers.ResultRenderer;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.MixtureSubstance;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.StructurallyDiverseSubstance;
import ix.ginas.models.v1.Substance;
import play.mvc.Result;
import play.twirl.api.Html;

public class ThumbViewFinder {
    static CachedSupplier<Map<String, ResultRenderer<?>>> thumbRenderers = CachedSupplier.of(() -> {
        Map<String, ResultRenderer<?>> thumbs = new HashMap<>();

        thumbs.put(Substance.class.getName(), (t, ct) -> {
            return ix.ginas.views.html.thumbs.conceptthumb.render((Substance) t);
        });
        
        
        thumbs.put(ChemicalSubstance.class.getName(), (t, ct) -> {
            return ix.ginas.views.html.thumbs.chemthumb.render((ChemicalSubstance) t, Optional.ofNullable(ct).map(c->c.getId()).orElse(null));
        });
        thumbs.put(ProteinSubstance.class.getName(), (t, ct) -> {
            return ix.ginas.views.html.thumbs.proteinthumb.render((ProteinSubstance) t, Optional.ofNullable(ct).map(c->c.getId()).orElse(null));
        });
        thumbs.put(NucleicAcidSubstance.class.getName(), (t, ct) -> {
            return ix.ginas.views.html.thumbs.nucleicacidthumb.render((NucleicAcidSubstance) t, Optional.ofNullable(ct).map(c->c.getId()).orElse(null));
        });
        thumbs.put(PolymerSubstance.class.getName(), (t, ct) -> {
            return ix.ginas.views.html.thumbs.polymerthumb.render((PolymerSubstance) t, Optional.ofNullable(ct).map(c->c.getId()).orElse(null));
        });
        thumbs.put(MixtureSubstance.class.getName(), (t, ct) -> {
            return ix.ginas.views.html.thumbs.mixturethumb.render((MixtureSubstance) t);
        });
        thumbs.put(StructurallyDiverseSubstance.class.getName(), (t, ct) -> {
            return ix.ginas.views.html.thumbs.diversethumb.render((StructurallyDiverseSubstance) t);
        });
        return thumbs;
    });
    
    public static <T> ResultRenderer<T> getRendererOrDefault(Class<T> cls, ResultRenderer<T> rend){
        return (ResultRenderer<T>) thumbRenderers.get().getOrDefault(cls.getName(), rend);
    }
    
    public static <T> Html render(T t, SearchResultContext ctx, ResultRenderer<T> rend) throws Exception{
        return getRendererOrDefault((Class<T>)t.getClass(), rend).render(t,ctx);
    }
}
