package ix.ginas.models.converters;

import java.util.ArrayList;
import java.util.List;

/**
 * Strings manipulation class
 * for strings stored in Plain text format.
 *
 * @author epuzanov
 * @since 2.6
 */
public abstract class AbstractStringConverter implements StringConverter {
    /**
     * Replace values from searchList with values from replaceList
     *
     * @param str source string; may be null.
     * @param searchList Array with search patterns;
     *            will never be null.
     * @param replaceList Array with new values;
     *            will never be null.
     * @return a string; if the input string was null then null is returned.
     */
    protected static String replaceFromLists(String str, String[] searchList, String[] replaceList) {
        if(str ==null){
            return null;
        }
        StringBuilder sb = new StringBuilder(str);
        for (int i = 0; i < searchList.length; i++)
        {
            String key = searchList[i];
            if ("".equals(key)) {
                continue;
            }
            String value = replaceList[i];

            int start = sb.indexOf(key, 0);
            while (start > -1) {
                int end = start + key.length();
                int nextSearchStart = start + value.length();
                sb.replace(start, end, value);
                start = sb.indexOf(key, nextSearchStart);
            }
        }
        return sb.toString();
    }

    /**
     * Converts string from internal DB storage format to HTML.
     *
     * @param str string in internal DB storage format
     *            will never be null.
     * @return a HTML formated string.
     */
    @Override
    public String toHtml(String str){
        return str;
    }

    /**
     * Converts string from HTML to internal DB storage format.
     *
     * @param str HTML formated string;
     *            will never be null.
     * @return a string in internal DB storage format.
     */
    @Override
    public String fromHtml(String str){
        return str;
    }

    /**
     * return Standard Name text.
     *
     * @param str string in internal DB storage format;
     *            will never be null.
     * @return a string as Standard Name format.
     */
    @Override
    public String toStd(String str){
        return str;
    }

    /**
     * Converts string from internal DB storage format to Custom format.
     *
     * @param str string in internal DB storage format;
     *            will never be null.
     * @param format target format name;
     *            will never be null.
     * @return a Plain text string.
     */
    @Override
    public String toFormat(String format, String str){
        return str;
    }

    /**
     * Truncate string.
     *
     * @param str string in internal DB storage format
     *            will never be null.
     * @param maxBytes maximal stings length in Bytes
     * @return a truncated string.
     */
    @Override
    public String truncate(String str, int maxBytes){
        return str.substring(0, maxBytes);
    }

    /**
     * Is the given string passes the formatting and
     * validation rules.
     *
     * @param str a string to check
     *            should not be null.
     * @return {@code true} if this str is valid; {@code false} otherwise.
     */
    @Override
    public boolean isValid(String str){
        return validationErrors(str).isEmpty();
    }

    /**
     * Is the given string passes the formatting and
     * validation rules.
     *
     * @param str a string to check
     *            should not be null.
     * @return ArayList of validation errors messages. 
     */
    @Override
    public List<String> validationErrors(String str){
        return new ArrayList<String>();
    }
}
