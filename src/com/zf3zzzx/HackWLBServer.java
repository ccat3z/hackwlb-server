package com.zf3zzzx;

import com.sun.net.httpserver.HttpServer;
import com.zf3zzzx.tasks.HackWLB;
import com.zf3zzzx.tasks.HackZXB;

import java.io.*;
import java.net.InetSocketAddress;

public class HackWLBServer {
    private HackWLB hackWLB;

    public static void main(String[] args) {
        if (args.length != 2) {
            new HackWLBServer("0.0.0.0",1234);
        } else {
            new HackWLBServer(args[0],Integer.parseInt(args[1]));
        }
    }

    public HackWLBServer(String serv, int port) {
        hackWLB = new HackWLB();
        InetSocketAddress address = new InetSocketAddress(serv, port);
        try {
            HttpServer server = HttpServer.create(address, 5);
            new HackWLB().initServer(server);
            new HackZXB().initServer(server);
            server.setExecutor(null);
            server.start();
            System.out.println("server started at " + serv + ":" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}