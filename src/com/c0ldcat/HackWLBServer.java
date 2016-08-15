package com.c0ldcat;

import com.c0ldcat.tasks.HackWLB;
import com.c0ldcat.tasks.HackZXB;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

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
        try {
            HttpServer server = HttpServer.create(address, 5);
            server.createContext("/",new IndexHandler());
            new HackWLB().initServer("wlb",server);
            new HackZXB().initServer("zxb",server);
            server.setExecutor(null);
            server.start();
            System.out.println("server started at " + serv + ":" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String resp = "";
            resp += "HackWLBServer" + "\n"
                    + "by c0ldcat" + "\n";
            httpExchange.sendResponseHeaders(200, resp.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(resp.getBytes());
            os.flush();
            os.close();
        }
    }
}