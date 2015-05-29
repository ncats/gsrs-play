package ix.qhts.models;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;
import play.db.ebean.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.core.models.Value;

/**
 * Concentration response curve
 */
@Entity
@Table(name="ix_qhts_curve")
public class Curve extends Model {
    public enum Unit {
        ppt,
        ppm,
        ppb,
        mm,
        um,
        nm,
        pm,
        fm,
        mgml,
        ugml,
        ngml,
        pgml,
        fgml,
        m,
        percent,
        ratio,
        sec,
        rsec,
        min,
        rmin,
        day,
        rday,
        other
    }
    
    @Id public Long id;
    public Unit concUnit = Unit.m; // concentration unit: molar
    public Unit respUnit = Unit.percent; // response unit: %
    public Integer length;
    
    @Indexable(indexed=false)
    @JsonIgnore
    @Lob
    @Basic(fetch=FetchType.EAGER)    
    protected byte[] conc;
    
    @Indexable(indexed=false)
    @JsonIgnore
    @Lob
    @Basic(fetch=FetchType.EAGER)
    protected byte[] response;

    public Curve () {}

    @Indexable(indexed=false)
    @JsonProperty("concentration")
    public double[] getConc () {
        return getConc (new double[conc.length/8]);
    }

    public double[] getConc (double[] conc) {
        if (conc.length < this.conc.length/8) 
            throw new IllegalArgumentException
                ("Input array is not of sufficient size!");
        return decode (conc, this.conc);
    }

    @Indexable(indexed=false)
    @JsonProperty("response")
    public double[] getResponse () {
        return getResponse (new double[response.length/8]);
    }

    public double[] getResponse (double[] response) {
        if (response.length < this.response.length/8)
            throw new IllegalArgumentException
                ("Input array is not of sufficient size!");
        return decode (response, this.response);
    }

    public void setData (double[] conc, double[] response) {
        if (conc.length != response.length)
            throw new IllegalArgumentException
                ("Concentration and response arrays are not of the same size!");
        length = conc.length;
        this.conc = new byte[length*8];
        this.response = new byte[length*8];
        encode (this.conc, conc);
        encode (this.response, response);
    }
    
    protected static double[] decode (double[] d, byte[] data) {
        for (int i = 0; i < data.length; i+= 8) {
            long l = (data[i] & 0xffl) << 56
                | (data[i+1] & 0xffl) << 48
                | (data[i+2] & 0xffl) << 40
                | (data[i+3] & 0xffl) << 32
                | (data[i+4] & 0xffl) << 24
                | (data[i+5] & 0xffl) << 16
                | (data[i+6] & 0xffl) << 8
                | (data[i+7] & 0xffl);
            d[i/8] = Double.longBitsToDouble(l);
        }
        return d;
    }
    
    protected static void encode (byte[] data, double[] d) {
        for (int i = 0, j = 0; i < d.length; ++i) {
            long l = Double.doubleToLongBits(d[i]);
            data[j++] = (byte)((l & 0xff00000000000000l) >> 56);
            data[j++] = (byte)((l & 0x00ff000000000000l) >> 48);
            data[j++] = (byte)((l & 0x0000ff0000000000l) >> 40);
            data[j++] = (byte)((l & 0x000000ff00000000l) >> 32);
            data[j++] = (byte)((l & 0x00000000ff000000l) >> 24);
            data[j++] = (byte)((l & 0x0000000000ff0000l) >> 16);
            data[j++] = (byte)((l & 0x000000000000ff00l) >> 8);
            data[j++] = (byte) (l & 0x00000000000000ffl);
        }
    }
}
