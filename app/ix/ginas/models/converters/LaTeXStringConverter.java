package ix.ginas.models.converters;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Strings manipulation class
 * for strings stored in LaTeX format.
 *
 * @author epuzanov
 * @since 2.6
 */
public class LaTeXStringConverter extends AbstractStringConverter {

    private static Pattern htmlTagPattern = Pattern.compile("<\\s*(i|/i|sup|sub)\\s*>");
    private static char[] brackets = "()[]{}<>".toCharArray();
    private static String[] LaTeXStrings = {"\\& ", "\\leftarrow ", "\\rightarrow ", "<", ">", "\\pm ",
        "\\Alpha ", "\\alpha ", "\\Beta ", "\\beta ", "\\Gamma ", "\\gamma ",
        "\\Delta ", "\\delta ", "\\Epsilon ", "\\epsilon ", "\\Zeta ", "\\zeta ", "\\Eta ", "\\eta ",
        "\\Theta ", "\\theta ", "\\Iota ", "\\iota ", "\\Kappa ", "\\kappa ", "\\Lambda ", "\\lambda ",
        "\\Mu ", "\\mu ", "\\Nu ", "\\nu ", "\\Xi ", "\\xi ", "\\Omicron ", "\\omicron ", "\\Pi ", "\\pi ",
        "\\Rho ", "\\rho ", "\\Sigma ", "\\sigma ", "\\Tau ", "\\tau ", "\\Upsilon ", "\\upsilon ",
        "\\Phi ", "\\phi ", "\\Chi ", "\\chi ", "\\Psi ", "\\psi ", "\\Omega ", "\\omega ", "\\ss ",
        "\\\"A ", "\\\"a ", "\\\"O ", "\\\"o ", "\\\"U ", "\\\"u ", "\\S ", "\bar{}",
        "\\text{XIII}", "\\text{XIV}", "\\text{XV}", "\\text{XVI}", "\\text{XVII}",
        "\\text{XVIII}", "\\text{XIX}", "\\text{XX}", "\\text{I}", "\\text{II}",
        "\\text{III}", "\\text{IV}", "\\text{V}", "\\text{VI}", "\\text{VII}", "\\text{VIII}", "\\text{IX}", "\\text{X}",
        "\\text{XI}", "\\text{XII}", "\\textsc{d}", "\\textsc{l}",
        "\\textsc{n}", "\\tilde{}", "\\textit{", "}", "",
        "", "^{", "}", "_{", "}", "", "\""};
    private static String[] htmlStrings = {"&amp;", "&larr;", "&rarr;", "&lt;", "&gt;", "&plusmn;",
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
    private static String[] plainStrings = {"&", "<--", "-->", "<", ">", "+/-",
        "ALPHA", "alpha", "BETA", "beta", "GAMMA", "gamma",
        "DELTA", "delta", "EPSILON", "epsilon", "ZETA", "zeta", "ETA", "eta",
        "THETA", "theta", "IOTA", "iota", "KAPPA", "kappa", "LAMBDA", "lambda",
        "MY", "my", "Ny", "ny", "XI", "xi", "OMIKRON", "omikron", "PI", "pi",
        "RHO", "rho", "SIGMA", "sigma", "TAU", "tau", "UPSILON", "upsilon",
        "PHI", "phi", "CHI", "chi", "PSI", "psi", "OMEGA", "omega", "ss",
        "Ae", "ae", "Oe", "oe", "Ue", "ue", "Par.", "XFF", "XIII", "XIV",
        "XV", "XVI", "XVII", "XVIII", "XIX", "XX", "I", "II",
        "III", "IV", "V", "VI", "VII", "VIII", "IX", "X",
        "XI", "XII", "D", "L", "N", "-", "", "", "", "", "", "", "", "", "", "\""};

    private static String fixCloseTags(String str){
        String tag;
        Stack<Character> tagStack = new Stack<>();
        StringBuilder out = new StringBuilder(str);
        Matcher matcher = htmlTagPattern.matcher(str);
        while (matcher.find()) {
            tag = matcher.group();
            if ("</i>".equals(tag)){
                out.setCharAt(matcher.end() - 2, tagStack.pop());
            }else{
                tagStack.push(tag.charAt(tag.length() - 2));
            }
        }
        return out.toString().replace("</p>", "</sup>").replace("</b>", "</sub>");
    }

    /**
     * Converts string from LaTeX text to HTML.
     *
     * @param str string in LaTeX text format;
     *            will never be null.
     * @return a HTML formated string.
     */
    @Override
    public String toHTML(String str){
        return fixCloseTags(this.replaceFromLists(str, LaTeXStrings, htmlStrings));
    }

    /**
     * Converts string from HTML to LaTeX text.
     *
     * @param str HTML formated string;
     *            will never be null.
     * @return a string as LaTeX text.
     */
    @Override
    public String fromHTML(String str){
        return this.replaceFromLists(str, htmlStrings, LaTeXStrings);
    }

    /**
     * return Plain text.
     *
     * @param str string in LaTeX text format;
     *            will never be null.
     * @return a string as Plan text.
     */
    @Override
    public String toPlain(String str){
        return this.replaceFromLists(toHTML(str), htmlStrings, plainStrings);
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
        str = toHTML(str);
        boolean lastComplete = false;
        int sTag = 0;
        for(int i = maxBytes; i >= 0; i--){
            if(lastComplete || (b[i] & 0x80) == 0) {
                str = new String(Arrays.copyOf(b, i));
                sTag = StringUtils.countMatches(str, "<");
                if(sTag == StringUtils.countMatches(str, ">")
                    && sTag / 2 == StringUtils.countMatches(str, "/")
                    && StringUtils.countMatches(str, "&") == StringUtils.countMatches(str, ";")) {
                    return fromHTML(str);
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
