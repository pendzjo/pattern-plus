package com.consultwithcase.patternplus;

import java.util.function.Predicate;

/**
 *
 * @author johnnp
 */
public class Pattern {
    
    java.util.regex.Pattern p;
    
    private Pattern(String regex){
        this.p = java.util.regex.Pattern.compile(regex);
    }
    
    private Pattern(String regex, int flags) {
        this.p = java.util.regex.Pattern.compile(regex, flags);
    }
    
    public static Pattern compile(String regex) {
        return new Pattern(regex);
    }
    
    public static Pattern compile(String regex, int flags) {
        return new Pattern(regex, flags);
    }
    
    public Predicate<String> asPredicate() {
        return this.p.asPredicate();
    }
    
    public int flags() {
        return this.p.flags();
    }
    
    public com.consultwithcase.patternplus.Matcher matcher(CharSequence input) {
        return new Matcher(p.matcher(input));
    }
    
}
