package com.c0ldcat.utils;

import fi.iki.elonen.NanoHTTPD;

import java.util.Map;


public class HackTask {
    public NanoHTTPD.Response handle(String uri, String path, NanoHTTPD.Method method, Map headers){};
}
