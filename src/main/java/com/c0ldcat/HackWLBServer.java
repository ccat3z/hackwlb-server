package com.c0ldcat;

import com.c0ldcat.tasks.HackWLB;
import com.c0ldcat.tasks.HackZXB;
import com.c0ldcat.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

public class HackWLBServer {

    public static void main(String[] args) {
        if (args.length != 2) {
            new HackWLBServer("0.0.0.0",1234);
        } else {
            new HackWLBServer(args[0],Integer.parseInt(args[1]));
        }
    }

    public HackWLBServer(String serv, int port) {
        InetSocketAddress address = new InetSocketAddress(serv, port);
        IndexHandler indexHandler = new IndexHandler();
        indexHandler.addHackTask("wlb", new HackWLB());
        indexHandler.addHackTask("zxb", new HackZXB());
        try {
            HttpServer server = HttpServer.create(address, 5);
            server.createContext("/", indexHandler);
            server.setExecutor(null);
            server.start();
            System.out.println("server started at " + serv + ":" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class IndexHandler implements HttpHandler {
        HashMap<String, HttpHandler> httpHandlers;

        public IndexHandler() {
            httpHandlers = new HashMap<>();
        }

        public void addHackTask(String tag, HttpHandler httpHandler) {
            httpHandlers.put(tag, httpHandler);
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String host = httpExchange.getRequestHeaders().get("Host").get(0);
            String fullPath[] = httpExchange.getRequestURI().getPath().split("/");
            if (fullPath.length != 0) {
                String tag = fullPath[1];
                if (httpHandlers.containsKey(tag)){
                    httpHandlers.get(tag).handle(httpExchange);
                    return;
                }
            }
            if (host.contains(".")) {
                String tag = host.split("\\.")[0];
                if (httpHandlers.containsKey(tag)) {
                    httpHandlers.get(tag).handle(httpExchange);
                    return;
                }
            }
            Utils.httpResp("Welcome c0ldcat's website", httpExchange);
        }
    }
}