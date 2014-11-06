package ix.ncats.models;

import java.util.Map;
import java.util.TreeMap;

import play.db.ebean.Model;
import javax.persistence.*;

import ix.core.models.Indexable;

@Entity
@Table(name="ix_ncats_program")
public class Program extends Model {
    static final Map<String, String> mapping = new TreeMap<String, String>();
    static {
        mapping.put("ChemTech", "Chemistry Technology Program");
        mapping.put("ADST", "Assay Development and Screening Technology");
        mapping.put("Tox21", "Toxicology in the 21st Century");
        mapping.put("RNAi", "RNA Interference Program");
        mapping.put("exRNA", "Extracellular RNA Communication Program");
        mapping.put("NCGC", "NCATS Chemical Genomics Center");
        mapping.put("NTU", 
                    "Discovering New Therapeutic Uses for Existing Molecules");
        mapping.put("TissueChip", "Tissue Chip for Drug Screening");
        mapping.put("BrIDGs", "Bridging Interventional Development Gaps");
        mapping.put("TRND", "Therapeutics for Rare and Neglected Diseases");
        mapping.put("CTSA", "Clinical and Translational Science Awards");
        mapping.put("ORDR", "Office of Rare Diseases Research");
    }

    @Id
    public Long id;

    @Column(length=64)
    @Indexable(facet=true, name="Program")
    public String name;
    public String fullname;
    
    public Program () {}
    public Program (String name) {
        this (name, mapping.get(name));
    }
    public Program (String name, String fullname) {
        this.name = name;
        this.fullname = fullname;
    }
}
