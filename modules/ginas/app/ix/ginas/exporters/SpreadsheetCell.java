package ix.ginas.exporters;

import java.util.Date;

/**
 * Created by katzelda on 8/19/16.
 */
public interface SpreadsheetCell {

    void writeInteger(int i);

    void writeDate(Date date);

    void writeString(String s);
    
    default void write(Object o){
        writeString(o ==null? "null" : o.toString());
    }
}
