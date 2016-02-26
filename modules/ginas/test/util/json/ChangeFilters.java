package util.json;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by katzelda on 2/26/16.
 */
public class ChangeFilters {

    public static ChangeFilter keyMatches(final String regex){

        return new ChangeFilter() {
            final Pattern pattern = Pattern.compile(regex);

            @Override
            public boolean filterOut(Change change) {
                Matcher matcher = pattern.matcher(change.getKey());
                return matcher.find();
            }
        };
    }

    public static ChangeFilter nullOrBlankValues() {
        return new ChangeFilter() {
            @Override
            public boolean filterOut(Change change) {
                String value = change.getValue().textValue();
                return value==null || value.trim().isEmpty();
            }
        };
    }
}
