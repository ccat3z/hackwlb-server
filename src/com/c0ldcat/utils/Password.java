package com.c0ldcat.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;

public class Password implements HttpHandler{
    private HttpHandler handler;
    private String pass;

    public Password(String pass, HttpHandler httpHandler) {
        this.handler = httpHandler;
        this.pass = pass;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        HashMap<String, String> pars = UrlParameterParser.parse(httpExchange.getRequestURI().getQuery());
        if (pars.containsKey("pass")){
            if (pars.get("pass").equals(pass)) {
                handler.handle(httpExchange);
            } else {
                Utils.httpResp("wrong parameter (pass)\n", httpExchange);
            }
        } else {
            Utils.httpResp("no parameter (pass)\n", httpExchange);
        }
    }
}
