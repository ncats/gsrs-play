package ix.ginas.exporters;

import java.util.*;

/**
 * Created by katzelda on 10/30/17.
 */
public class GroupColumnValueRecipe<T> implements ColumnValueRecipe<T>{


    public static <T> GroupColumnValueRecipe<T> of(ColumnValueRecipe<T>... recipe){
        return new GroupColumnValueRecipe<>(Arrays.asList(recipe));
    }





    private List<ColumnValueRecipe<T>> map;

    private GroupColumnValueRecipe(List<ColumnValueRecipe<T>> map) {
        this.map = new ArrayList<>(map);
    }

    public int numberOfColumns(){
        return map.size();
    }

    @Override
    public boolean containsColumnName(String name) {
        for(ColumnValueRecipe<?> r : map){
            if(r.containsColumnName(name)){
                return true;
            }
        }
        return false;
    }

    @Override
    public ColumnValueRecipe<T> replaceColumnName(String oldName, String newName) {
        ListIterator<ColumnValueRecipe<T>> iter = map.listIterator();
        while(iter.hasNext()){
            ColumnValueRecipe<T> o = iter.next();
            ColumnValueRecipe<T> n = o.replaceColumnName(oldName, newName);
            if( o !=n){
                iter.set(n);
            }
        }
        return this;
    }



    public int writeHeaderValues(Spreadsheet.SpreadsheetRow row, int currentOffset){
        int i=0;
        for(ColumnValueRecipe<T> v : map){
            i+= v.writeHeaderValues(row, i);
        }
        return i;
    }

    public int writeValuesFor(Spreadsheet.SpreadsheetRow row, int currentOffset, T obj){
        int i=0;
        for(ColumnValueRecipe<T> v : map){
            i+= v.writeValuesFor(row, i, obj);
        }
        return i;
    }
}
