package ix.ginas.exporters;

/**
 * Created by katzelda on 8/19/16.
 */
public interface NumberColumnRecipe<T>  extends ColumnValueRecipe<T, Number>{

    @Override
    default boolean isNumeric(){
        return true;
    }
}
