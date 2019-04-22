package ix.core.models;

import java.util.Objects;

/**
 * Created by katzelda on 2/7/19.
 */
public interface DefinitionalElement {

    String getKey();

    String getDefinitionalString();

    public static DefinitionalElement of(String key,String value){
        return new KeyValueDefinitionalElement(key,value);
    }

    static class KeyValueDefinitionalElement implements DefinitionalElement{
        private final String key,value;

        public KeyValueDefinitionalElement(String key, String value) {
            this.key = Objects.requireNonNull(key);
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefinitionalString() {
            return key + "->" + value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DefinitionalElement)) return false;

            DefinitionalElement that = (DefinitionalElement) o;

            return value.equals(that.getDefinitionalString());
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
