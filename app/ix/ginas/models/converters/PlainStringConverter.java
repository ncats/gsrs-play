package ix.ginas.models.converters;

import java.util.Arrays;

/**
 * Strings manipulation class
 * for strings stored in Plain text format.
 *
 * @author epuzanov
 * @since 2.6
 */
public class PlainStringConverter extends AbstractStringConverter {

    private static String[] htmlStrings = {"&amp;", "&lt;", "&gt;", "&quot;", "&apos;"};
    private static String[] plainStrings = {"&", "<", ">", "\"", "'"};

    /**
     * Converts string from internal DB storage format to HTML.
     *
     * @param str string in internal DB storage format;
     *            will never be null.
     * @return a HTML formated string.
     */
    @Override
    public String toHtml(String str){
        return this.replaceFromLists(str, plainStrings, htmlStrings);
    }

    /**
     * Truncate string.
     *
     * @param str string in internal DB storage format;
     *            will never be null.
     * @param maxBytes maximal stings length in Bytes
     * @return a truncated string.
     */
    @Override
    public String truncate(String str, int maxBytes){
        byte[] b = (str + "   ").getBytes();
        if(maxBytes >= b.length){
            return str;
        }
        boolean lastComplete=false;
        for(int i = maxBytes; i >= 0; i--){
            if(lastComplete)
                return new String(Arrays.copyOf(b, i));
            if((b[i] & 0x80) == 0){
                return new String(Arrays.copyOf(b, i));
            }
            if(b[i] == -79){
                lastComplete = true;
            }
        }
        return "";
    }
}
