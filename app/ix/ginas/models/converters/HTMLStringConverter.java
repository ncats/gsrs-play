package ix.ginas.models.converters;

import java.io.StringReader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.tidy.Tidy;

/**
 * Strings manipulation class
 * for strings stored in HTML format.
 *
 * @author epuzanov
 * @since 2.6
 */
public class HTMLStringConverter extends AbstractStringConverter {

    private static String[] allowedHtmlTags = new String[] {"I", "SUB", "SUP", "SMALL"};
    private static Pattern htmlTagPattern = Pattern.compile("<\\s*/?([^>]+)\\s*>");
    private static char[] brackets = "()[]{}".toCharArray();
    private static String[] htmlStrings = {"&amp;", "&larr;", "&rarr;", "&lt;", "&gt;", "&plusmn;", "-",
        "&Alpha;", "&alpha;", "&Beta;", "&beta;", "&Gamma;", "&gamma;",
        "&Delta;", "&delta;", "&Epsilon;", "&epsilon;", "&Zeta;", "&zeta;", "&Eta;", "&eta;",
        "&Theta;", "&theta;", "&Iota;", "&iota;", "&Kappa;", "&kappa;", "&Lambda;", "&lambda;",
        "&Mu;", "&mu;", "&Nu;", "&nu;", "&Xi;", "&xi;", "&Omicron;", "&omicron;", "&Pi;", "&pi;",
        "&Rho;", "&rho;", "&Sigma;", "&sigma;", "&Tau;", "&tau;", "&Upsilon;", "&upsilon;",
        "&Phi;", "&phi;", "&Chi;", "&chi;", "&Psi;", "&psi;", "&Omega;", "&omega;", "&szlig;",
        "&Auml;", "&auml;", "&Ouml;", "&ouml;", "&Uuml;", "&uuml;", "&sect;", "&macr;",
        "&#8553;&#8546;", "&#8553;&#8547;", "&#8553;&#8548;", "&#8553;&#8549;", "&#8553;&#8550;",
        "&#8553;&#8551;", "&#8553;&#8552;", "&#8553;&#8553;", "&#8544;", "&#8545;",
        "&#8546;", "&#8547;", "&#8548;", "&#8549;", "&#8550;", "&#8551;", "&#8552;", "&#8553;",
        "&#8554;", "&#8555;", "<small>D</small>", "<small>L</small>",
        "<small>N</small>", "~", "<i>", "</i>", "</sup><sup>",
        "</sub><sub>", "<sup>", "</sup>", "<sub>", "</sub>", "&zwnj;", "&quot;"};
    private static String[] stdStrings = {"&", "<--", "-->", "<", ">", "+/-", "-",
        "ALPHA", "alpha", "BETA", "beta", "GAMMA", "gamma",
        "DELTA", "delta", "EPSILON", "epsilon", "ZETA", "zeta", "ETA", "eta",
        "THETA", "theta", "IOTA", "iota", "KAPPA", "kappa", "LAMBDA", "lambda",
        "MU", "mu", "NU", "nu", "XI", "xi", "OMIKRON", "omikron", "PI", "pi",
        "RHO", "rho", "SIGMA", "sigma", "TAU", "tau", "UPSILON", "upsilon",
        "PHI", "phi", "CHI", "chi", "PSI", "psi", "OMEGA", "omega", "ss",
        "Ae", "ae", "Oe", "oe", "Ue", "ue", "Par.", "XFF", "XIII", "XIV",
        "XV", "XVI", "XVII", "XVIII", "XIX", "XX", "I", "II",
        "III", "IV", "V", "VI", "VII", "VIII", "IX", "X",
        "XI", "XII", "D", "L", "N", "-", "", "", "", "", "(", ")", "<", ">", "", "\""};


    /**
     * return Standard Name text.
     *
     * @param str string in Plain text format;
     *            will never be null.
     * @return a string as Plan text.
     */
    @Override
    public String toStd(String str){
        return this.replaceFromLists(str, htmlStrings, stdStrings);
    }

    /**
     * Truncate string.
     *
     * @param str string in HTML format;
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
        boolean lastComplete = false;
        int sTag = 0;
        for(int i = maxBytes; i >= 0; i--){
            if(lastComplete || (b[i] & 0x80) == 0) {
                str = new String(Arrays.copyOf(b, i));
                sTag = StringUtils.countMatches(str, "<");
                if(sTag == StringUtils.countMatches(str, ">")
                    && sTag / 2 == StringUtils.countMatches(str, "/")
                    && StringUtils.countMatches(str, "&") == StringUtils.countMatches(str, ";")) {
                    return str;
                }else{
                    lastComplete = false;
                }
            }
            if(b[i] == -79){
                lastComplete = true;
            }
        }
        return "";
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
        List<String> errors = new ArrayList<String>();
        Tidy tidy = new Tidy();
        tidy.setInputEncoding("UTF-8");
        tidy.setXHTML(true);
        tidy.parseDOM(new StringReader("<html><head><title>Test</title></head><body>" + str + "</body></html>"), null);
        if (tidy.getParseErrors() > 0 || tidy.getParseWarnings() > 1) {
            errors.add("contains bad HTML");
        }

        Matcher m = htmlTagPattern.matcher(str.toUpperCase());
        while (m.find()) {
            if (!ArrayUtils.contains(allowedHtmlTags, m.group(1))) {
                errors.add("contains not allowed HTML tag " + m.group(1));
            }
        }

        long brackets_count = 0;
        for (int i = 0; i < brackets.length; i++) {
            if (i%2 != 0) {
                for (int j = 0; j < str.length(); j++) {
                    if (str.charAt(j) == brackets[i]) {
                        brackets_count--;
                    }
                }
                if (brackets_count != 0) {
                    errors.add("contains unclosed bracket '" + String.valueOf(brackets[i]) + "'");
                }
                brackets_count = 0;
            } else {
                for (int j = 0; j < str.length(); j++) {
                    if (str.charAt(j) == brackets[i]) {
                        brackets_count++;
                    }
                }
            }
        }
        return errors;
    }
}
