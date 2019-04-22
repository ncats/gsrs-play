package ix.ginas.modelBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import ix.core.models.Keyword;
import ix.ginas.models.v1.*;

public class ProteinSubstanceBuilder  extends AbstractSubstanceBuilder<ProteinSubstance, ProteinSubstanceBuilder>{

    @Override
    protected Substance.SubstanceClass getSubstanceClass() {
        return Substance.SubstanceClass.protein;
    }

    @Override
    protected ProteinSubstanceBuilder getThis() {
        return this;
    }

    @Override
    public Supplier<ProteinSubstance> getSupplier(){
        return ProteinSubstance::new;
    }

    public ProteinSubstanceBuilder setProtein(Protein prot){
        return andThen(s ->{ s.setProtein(prot);});
    }

    public ProteinSubstanceBuilder() {
    }

    public ProteinSubstanceBuilder(Substance copy) {
        super(copy);
        if(copy instanceof  ProteinSubstance){
            Protein prot = ((ProteinSubstance)copy).protein;
            if(prot !=null){
                setProtein(prot);
            }
        }
    }

    public ProteinSubstanceBuilder setGlycosylation(Glycosylation glycosylation) {
        return andThen(s->{
            if(s.protein ==null){
                s.protein = new Protein();
            }

            s.protein.glycosylation = glycosylation;
        });
    }

    public ProteinSubstanceBuilder setGlycosylationNSites(String shorthand){
        List<Site> sites =SiteContainer.parseShorthandRanges(shorthand);
        return andThen(s->{
            if(s.protein ==null){
                s.protein = new Protein();
            }
            if(s.protein.glycosylation ==null){
                s.protein.glycosylation = new Glycosylation();
            }
            s.protein.glycosylation.setNGlycosylationSites(sites);
        });
    }
    public ProteinSubstanceBuilder setGlycosylationOSites(String shorthand){
        List<Site> sites =SiteContainer.parseShorthandRanges(shorthand);
        return andThen(s->{
            if(s.protein ==null){
                s.protein = new Protein();
            }
            if(s.protein.glycosylation ==null){
                s.protein.glycosylation = new Glycosylation();
            }
            s.protein.glycosylation.setOGlycosylationSites(sites);
        });
    }

    public ProteinSubstanceBuilder setGlycosylationCSites(String shorthand){
        List<Site> sites =SiteContainer.parseShorthandRanges(shorthand);
        return andThen(s->{
            if(s.protein ==null){
                s.protein = new Protein();
            }
            if(s.protein.glycosylation ==null){
                s.protein.glycosylation = new Glycosylation();
            }
            s.protein.glycosylation.setCGlycosylationSites(sites);
        });
    }
    public ProteinSubstanceBuilder setDisulfideLinks(List<DisulfideLink> disulfideLinks){
        return andThen(s->{
            if(s.protein ==null){
                s.protein = new Protein();
            }
            s.protein.setDisulfideLinks(disulfideLinks);
        });
    }
    public ProteinSubstanceBuilder addDisulfideLink(String shorthand){
        List<Site> sites =SiteContainer.parseShorthandRanges(shorthand);
        return andThen(s->{
            if(s.protein ==null){
                s.protein = new Protein();
            }
            DisulfideLink disulfideLink = new DisulfideLink();
            disulfideLink.setSites(sites);
            //the getter here should make an empty list if it's null?
            List<DisulfideLink> disulfideLinks = s.protein.getDisulfideLinks();
            disulfideLinks.add(disulfideLink);
            //this is to force the json caching to update internally
            s.protein.setDisulfideLinks(disulfideLinks);
        });
    }

    public ProteinSubstanceBuilder setOtherLinks(List<OtherLinks> otherLinks){
        return andThen(s->{
            if(s.protein ==null){
                s.protein = new Protein();
            }
            s.protein.otherLinks = otherLinks;
        });
    }

    public ProteinSubstanceBuilder addOtherLink(String linkageType, String shorthand){
        OtherLinks other = new OtherLinks();
        other.linkageType = Objects.requireNonNull(linkageType);
        List<Site> sites =SiteContainer.parseShorthandRanges(shorthand);
        other.setSites(sites);

        return andThen(s->{
            if(s.protein ==null){
                s.protein = new Protein();
            }
            if(s.protein.otherLinks ==null){
                s.protein.otherLinks = new ArrayList<>();
            }
            s.protein.otherLinks.add(other);
            //make new reference for ebean
            s.protein.otherLinks = new ArrayList<>(s.protein.otherLinks);
        });
    }
    /**
     * Add a new subunit with the given sequence and
     * add/set all the needed additonal fields like subunit index
     * and adding a public reference etc to pass validation.
     * @param sequence the sequence to use as a new subunit
     *                 can not be null.
     * @return this builder
     * @throws NullPointerException if sequence is null
     */
    public ProteinSubstanceBuilder addSubUnit(String sequence){
        Objects.requireNonNull(sequence);
        return andThen( s->{
            if(s.protein ==null){
                s.protein = new Protein();
            }
            int index = s.protein.getSubunits().size() +1;
            Subunit subunit = new Subunit();
            subunit.sequence = sequence;
            subunit.subunitIndex = index;

            Reference r = AbstractSubstanceBuilder.createNewPublicDomainRef();
            s.protein.addReference(r, s);

            s.protein.getSubunits().add(subunit);


        });
    }
    public ProteinSubstanceBuilder addSubUnit(Subunit subunit){
        Objects.requireNonNull(subunit);
        return andThen( s->{
            if(s.protein ==null){
                s.protein = new Protein();
            }
            s.protein.getSubunits().add(subunit);

        });
    }
}
