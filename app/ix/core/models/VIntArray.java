package ix.core.models;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@DiscriminatorValue("IAR")
public class VIntArray extends VBin {
    public VIntArray () {}
    public VIntArray (String label, int[] array) {
        this.label = label;
        data = new byte[array.length*4];
        for (int i = 0; i < array.length; ++i) {
            int x = array[i];
            data[i+3] = (byte)((x >> 24) & 0xff);
            data[i+2] = (byte)((x >> 16) & 0xff);
            data[i+1] = (byte)((x >>  8) & 0xff);
            data[i] = (byte)(x & 0xff);
        }
        size = data.length;
        mimeType = getClass().getName();
    }

    public int getLength () { return data.length / 4; }

    @Indexable(indexed=false)
    public int[] getArray () {
        int[] array = new int[data.length/4];
        getArray (array);
        return array;
    }

    public void getArray (int[] array) {
        for (int i = 0; i < array.length; ++i) {
            array[i] = ((data[i+3] & 0xff) << 24) 
                | ((data[i+2] & 0xff) << 16)
                | ((data[i+1] & 0xff) << 8)
                | (data[i] & 0xff);
        }
    }
}
