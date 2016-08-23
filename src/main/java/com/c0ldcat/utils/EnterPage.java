package com.c0ldcat.utils;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import j2html.tags.Tag;

import java.util.ArrayList;
import java.util.Map;

import static j2html.TagCreator.*;

public class EnterPage {
    static final public String TYPE_TEXT = "text";
    static final public String TYPE_PASSWD = "password";

    private TaskHelper th;
    private ArrayList<Tag> inputTags;

    public EnterPage(TaskHelper th) {
        this.th = th;
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

    public EnterPage add(Map<String, String> map) {
        String type = TYPE_TEXT;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            add(type, entry.getKey(), entry.getValue());
        }
        return this;
    }

    public Response getResponse() {
        String resp = Utils.getStringFromInputStream(getClass().getResourceAsStream("/EnterPage.html"))
                .replaceAll("FORM_PART",
                        form().withAction(th.getRealPath())
                                .withMethod("GET")
                                .with(inputTags)
                                .with(
                                        input().withId("submit")
                                                .withClass("info")
                                                .withType("submit")
                                                .withValue("Submit")
                                ).toString());
        return NanoHTTPD.newFixedLengthResponse(resp);
    }
}