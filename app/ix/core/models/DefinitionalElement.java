package ix.core.models;

import java.util.Objects;

/**
 * Created by katzelda on 2/7/19.
 *
 * Added layer
 */
public interface DefinitionalElement extends Comparable<DefinitionalElement>{

    String getKey();

    String getDefinitionalString();

	int getLayer();

		String getValue();

    static DefinitionalElement of(String key,String value){
        return new KeyValueDefinitionalElement(key,value);
    }

    static DefinitionalElement of(String key,String value, int layer){
        return new KeyValueDefinitionalElement(key,value, layer);
    }



    class KeyValueDefinitionalElement implements DefinitionalElement{
        private final String key,value;
         private int layer;

        public KeyValueDefinitionalElement(String key, String value) {
             this(key,value, 1);
         }

         public KeyValueDefinitionalElement(String key, String value, int layer) {
            this.key = Objects.requireNonNull(key);
            this.value = value;
             this.layer = layer;
        }
        @Override
        public int compareTo(DefinitionalElement o){
            return getDefinitionalString().compareTo(o.getDefinitionalString());
        }
        @Override
        public String getKey() {
            return key;
        }

         @Override
         public int getLayer() {
             return layer;
         }

         @Override
         public String getValue(){
             return value;
         }

        @Override
        public String getDefinitionalString() {
             return key + "->" + value + "@" + layer;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DefinitionalElement)) return false;

            DefinitionalElement that = (DefinitionalElement) o;

             return getDefinitionalString().equals(that.getDefinitionalString());
        }

        @Override
        public int hashCode() {
            return getDefinitionalString().hashCode();
        }

        @Override
        public String toString() {
            return "KeyValueDefinitionalElement: " +getDefinitionalString();
        }
    }
}