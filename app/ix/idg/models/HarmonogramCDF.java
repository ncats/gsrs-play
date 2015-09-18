package ix.idg.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ix_idg_harmonogram")
public class HarmonogramCDF extends play.db.ebean.Model {
    @Id
    public Long id;

    @Column(nullable = false)
    public String uniprotId;
    @Column(nullable = false)
    public String symbol;
    @Column(nullable = false)
    public String dataSource;
    @Column(nullable = true)
    public String dataType;

    @Column(nullable = false)
    public String IDGFamily;
    @Column(nullable = true)
    public String TDL;

    public Double cdf;


    public HarmonogramCDF() {
    }

    public String getUniprotId() {
        return uniprotId;
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getDataType() {
        return dataType;
    }

    public Double getCdf() {
        return cdf;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getIDGFamily() {
        return IDGFamily;
    }

    public String getTDL() {
        return TDL;
    }

    public HarmonogramCDF(String uniprotId, String symbol, String dataSource, String dataType, String IDGFamily, String TDL, Double cdf) {
        this.uniprotId = uniprotId;
        this.symbol = symbol;
        this.dataSource = dataSource;
        this.dataType = dataType;
        this.IDGFamily = IDGFamily;
        this.TDL = TDL;
        this.cdf = cdf;
    }

    @Override
    public String toString() {
        return "HarmonogramCDF{" +
                "uniprotId='" + uniprotId + '\'' +
                ", ds='" + dataSource + "\', cdf = " + cdf +
                '}';
    }
}
