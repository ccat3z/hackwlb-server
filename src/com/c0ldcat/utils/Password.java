package com.c0ldcat.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;

public class Password implements HttpHandler{
    private HttpHandler handler;
    private String pass;
    static final public String PAR_PASS = "pass";

    public Password(String pass, HttpHandler httpHandler) {
        this.handler = httpHandler;
        this.pass = pass;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        HashMap<String, String> pars = UrlParameterParser.parse(httpExchange.getRequestURI().getQuery());
        if (pars.containsKey(PAR_PASS)){
            if (pars.get(PAR_PASS).equals(pass)) {
                handler.handle(httpExchange);
            } else {
                new EnterPage(httpExchange).add(pars).push();
            }
        } else {
            new EnterPage(httpExchange).add(pars).add(EnterPage.TYPE_PASSWD, "pass").push();
        }
    }
}
