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

    private static String[] htmlStrings = {
        "&amp;", "&lt;", "&gt;", "&quot;", "\u00b1",
        "\u03b1", "\u03b2", "\u03b3", "\u03b4",
        "\u03b5", "\u03b6", "\u03b7", "\u03b8",
        "\u03b9", "\u03ba", "\u03bb", "\u03bc",
        "\u03bd", "\u03be", "\u03bf", "\u03c0",
        "\u03c1", "\u03c3", "\u03c4", "\u03c5",
        "\u03c6", "\u03c7", "\u03c8", "\u03c9"};
    private static String[] plainStrings = {
        "&", "<", ">", "\"", "+/-",
        ".ALPHA.", ".BETA.", ".GAMMA.", ".DELTA.",
        ".EPSILON.", ".ZETA.", ".ETA.", ".THETA.",
        ".IOTA.", ".KAPPA.", ".LAMBDA.", ".MU.",
        ".NU.", ".XI.", ".OMIKRON.", ".PI.",
        ".RHO.", ".SIGMA.", ".TAU.", ".UPSILON.",
        ".PHI.", ".CHI.", ".PSI.", ".OMEGA."};

    /**
     * Converts string from Plain text to HTML.
     *
     * @param str string in Plain text format;
     *            will never be null.
     * @return a HTML formated string.
     */
    @Override
    public String toHTML(String str){
        return this.replaceFromLists(str, plainStrings, htmlStrings);
    }

    /**
     * Truncate string.
     *
     * @param str string in Plain text format;
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
