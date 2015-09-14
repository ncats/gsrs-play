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

    public HarmonogramCDF(String uniprotId, String dataSource, String dataType, Double cdf) {
        this.uniprotId = uniprotId;
        this.dataSource = dataSource;
        this.dataType = dataType;
        this.cdf = cdf;
    }

    public HarmonogramCDF(String uniprotId, String symbol, String dataSource, String dataType, Double cdf) {
        this.uniprotId = uniprotId;
        this.symbol = symbol;
        this.dataSource = dataSource;
        this.dataType = dataType;
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
