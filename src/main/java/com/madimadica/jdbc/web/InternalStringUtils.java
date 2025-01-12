package com.madimadica.jdbc.web;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal string manipulation utils
 */
class InternalStringUtils {

    /**
     * Split a string into a list of strings by a single character
     * <p>
     *     This is like calling <code>s.split(c, -1)</code>,
     *     so consecutive matches and starting/ending matches all get split
     * </p>
     * @param s string to split
     * @param c char to split by
     * @return list of split parts
     */
    static List<String> splitChar(String s, char c) {
        List<String> parts = new ArrayList<>();
        int lastStart = 0;
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            if (s.charAt(i) == c) {
                parts.add(s.substring(lastStart, i));
                lastStart = i + 1;
            }
        }
        parts.add(s.substring(lastStart, len));
        return parts;
    }

}
