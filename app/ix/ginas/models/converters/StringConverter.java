package ix.ginas.models.converters;

import java.util.List;

/**
 * Strings manipulation class
 * the internal DB storage format can be a HTML, LaTeX or some
 * Proprietary format.
 *
 * @author epuzanov
 * @since 2.6
 */
public interface StringConverter {
    /**
     * Converts string from internal DB storage format to HTML.
     *
     * @param str string in internal DB storage format;
     *            will never be null.
     * @return a HTML formated string.
     */
    String toHtml(String str);

    /**
     * Converts string from HTML to internal DB storage format.
     *
     * @param str HTML formated string;
     *            will never be null.
     * @return a string in internal DB storage format.
     */
    String fromHtml(String str);

    /**
     * Converts string from internal DB storage format to Standard Name format.
     *
     * @param str string in internal DB storage format;
     *            will never be null.
     * @return a Standard Name string.
     */
    String toStd(String str);

    /**
     * Converts string from internal DB storage format to Custom format.
     *
     * @param str string in internal DB storage format;
     *            will never be null.
     * @param format target format name;
     *            will never be null.
     * @return a Plain text string.
     */
    String toFormat(String format, String str);

    /**
     * Truncate string.
     *
     * @param str string in internal DB storage format;
     *            will never be null.
     * @param maxBytes maximal stings length in Bytes
     * @return a truncated string.
     */
    String truncate(String str, int maxBytes);

    /**
     * Is the given string passes the formatting and
     * validation rules.
     *
     * @param str a string to check
     *            should not be null.
     * @return {@code true} if this passed in id is valid; {@code false} otherwise.
     */
    boolean isValid(String str);

    /**
     * Is the given string passes the formatting and
     * validation rules.
     *
     * @param str a string to check
     *            should not be null.
     * @return ArayList of validation errors messages. 
     */
    List<String> validationErrors(String str);
}
