package com.c0ldcat.utils;

import fi.iki.elonen.NanoHTTPD;

import java.util.*;

public class TaskHelper {
    private NanoHTTPD.IHTTPSession session;
    private ArrayList<String> path;
    private String basePath;

    public TaskHelper(NanoHTTPD.IHTTPSession session) {
        this.session = session;
        setPath(session.getUri());
        basePath = "/";
    }

    public void setPath(String path) {
        if (path.startsWith("/"))
            path = path.replaceAll("^/", "");
        if (path.endsWith("/"))
            path = path.replaceAll("/$","");
        this.path = new ArrayList<>(Arrays.asList(path.split("/")));
    }


    public void setPath(int index) {
        for (int i = 0; i < index; i++) {
            basePath += path.remove(0) + "/";
        }
    }

    public ArrayList<String> getPath() {
        return path;
    }

    public String getRealPath() {
        return session.getUri();
    }

    public String getBasePath() {
        return basePath;
    }

    public Map<String, String> getParameters() {
        HashMap<String, String> m = new HashMap<>();
        for (Map.Entry<String, List<String>> e : session.getParameters().entrySet()) {
            m.put(e.getKey(), e.getValue().get(0));
        }
        return m;
    }
}
