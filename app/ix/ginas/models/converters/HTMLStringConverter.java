package ix.ginas.models.converters;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
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
    /**
     * JTidy is not thead safe and we need to use this a lot so make a thread-local
     * version.
     */
    private static final ThreadLocal<Tidy> TIDY_POOL = ThreadLocal.withInitial(new Supplier<Tidy>(){
        //need to use anonymous class since Play can't "enhance" lambdas
        @Override
        public Tidy get() {
            Tidy tidy = new Tidy();
            tidy.setInputEncoding("UTF-8");
            tidy.setXHTML(true);
            tidy.setWraplen(0);
            tidy.setPrintBodyOnly(true);
            tidy.setTidyMark(false);
            tidy.setErrout(new PrintWriter(new StringWriter()));
            return tidy;
        }
    });


    private static String[] allowedHtmlTags = new String[] {"I", "B", "SUB", "SUP", "SMALL"};
    private static Pattern htmlTagPattern = Pattern.compile("<\\s*/?([^>]+)\\s*>");
    private static List<Character> badLastChars = Arrays.asList('<', '/', '&');
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
        if(maxBytes >= str.length()){
            return str;
        }
        String truncatedStr = "";
        StringWriter writer = new StringWriter();
        Tidy tidy = TIDY_POOL.get();
        for(int i = maxBytes; i >= 0; i--){
            if (badLastChars.contains(str.charAt(i-1))) {
                continue;
            }
            tidy.parse(new StringReader("<html><head><title>Test</title></head><body>" + str.substring(0, i) + "</body></html>"), writer);
            truncatedStr = writer.toString().replace("\n", "");
            if (tidy.getParseErrors() == 0 && tidy.getParseWarnings() == 0 && truncatedStr.length() <= maxBytes) {
                return truncatedStr;
            }
            writer.getBuffer().setLength(0);
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

        Matcher m = htmlTagPattern.matcher(str.toUpperCase());
        while (m.find()) {
            if (!ArrayUtils.contains(allowedHtmlTags, m.group(1))) {
                errors.add("contains not allowed HTML tag " + m.group(1));
            }
        }
        if (!errors.isEmpty()) {
            return errors;
        }

        Tidy tidy = TIDY_POOL.get();
        tidy.parse(new StringReader("<html><head><title>Test</title></head><body>" + str + "</body></html>"), new StringWriter());
        if (tidy.getParseErrors() > 0 || tidy.getParseWarnings() > 0) {
            errors.add("contains bad HTML");
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
