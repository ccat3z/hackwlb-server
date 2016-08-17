package com.c0ldcat.utils;


import com.sun.net.httpserver.HttpServer;

public interface HackTask {
    public void initServer(String tag, HttpServer server);
}
