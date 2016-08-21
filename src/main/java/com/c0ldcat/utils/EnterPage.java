package com.c0ldcat.utils;

import com.sun.net.httpserver.HttpExchange;
import j2html.tags.Tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static j2html.TagCreator.*;

public class EnterPage {
    static final public String TYPE_TEXT = "text";
    static final public String TYPE_PASSWD = "password";

    private HttpExchange httpExchange;
    private ArrayList<Tag> inputTags;

    public EnterPage(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
        inputTags = new ArrayList<>();
    }

    public EnterPage add(String type, String name, String value) {
        inputTags.add(
                p().with(
                        span(name + ":").withId(name)
                                .withClass("info"),
                        input().withType(type)
                                .withName(name)
                                .withValue(value)
                )
        );
        return this;
    }

    public EnterPage add(String type, String name) {
        return add(type, name, "");
    }

    public EnterPage add(HashMap<String, String> map) {
        String type = TYPE_TEXT;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().equals(Password.PAR_PASS)) {
                type = TYPE_PASSWD;
            } else {
                type = TYPE_TEXT;
            }
            add(type, entry.getKey(), entry.getValue());
        }
        return this;
    }

    public void push() throws IOException{
        String resp = Utils.getStringFromInputStream(getClass().getResourceAsStream("/EnterPage.html"))
                .replaceAll("FORM_PART",
                        form().withAction(httpExchange.getRequestURI().getPath()).withMethod("GET")
                                .with(inputTags)
                                .with(
                                        input().withId("submit")
                                                .withClass("info")
                                                .withType("submit")
                                                .withValue("Submit")
                                ).toString());
        Utils.httpRespHtml(resp, httpExchange);
    }
}