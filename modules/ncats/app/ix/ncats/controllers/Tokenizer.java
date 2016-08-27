package ix.ncats.controllers;

import java.util.Enumeration;

public interface Tokenizer {
    public Enumeration<String> tokenize (String input);
}