package ix.ncats.controllers;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

public class DefaultTokenizer implements Tokenizer {
    final protected String pattern;
    public DefaultTokenizer () {
        this ("[\\s;,\n\t]");
    }
    public DefaultTokenizer (String pattern) {
        this.pattern = pattern;
    }

    public Enumeration<String> tokenize (String input) {
        String[] tokens = input.split(pattern);
        return Collections.enumeration(Arrays.asList(tokens));
    }
}