package com.c0ldcat.utils;

import java.util.HashMap;

public class UrlParameterParser {
    static public HashMap<String, String> parse(String allPar) {
        HashMap<String, String> map = new HashMap<>();
        String pars[] = allPar.split("&");
        for (String par : pars) {
            String strs[] = allPar.split("=");
            if (strs.length == 2){
                map.put(strs[0], strs[1]);
            }
        }
        return map;
    }
}
