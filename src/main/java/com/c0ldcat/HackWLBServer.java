package com.c0ldcat;

import com.c0ldcat.tasks.HackZXB;
import com.c0ldcat.utils.HackTask;
import com.c0ldcat.utils.TaskHelper;
import com.c0ldcat.utils.Utils;
import fi.iki.elonen.NanoHTTPD;

import java.util.HashMap;

public class HackWLBServer extends NanoHTTPD{
    HashMap<String, HackTask> taskMap;

    public static void main(String[] args) throws Exception{
        HackWLBServer s;
        if (args.length != 2) {
            s = new HackWLBServer("0.0.0.0",1234);
        } else {
            s = new HackWLBServer(args[0],Integer.parseInt(args[1]));
        }
        s.start();
        while (true);
    }

    public HackWLBServer(String host, int port) {
        //create server
        super(host,port);
        Utils.log("Server will start on " + host + ":" + port + ".", false);

        //init
        taskMap = new HashMap<>();
        taskMap.put("zxb", new HackZXB());
    }

    public boolean isKey(String tag) {
        return taskMap.containsKey(tag);
    }

    @Override
    public Response serve(IHTTPSession session) {
        //get host and path
        String host = session.getHeaders().get("host");
        String path = session.getUri();

        //add '/' at the end of path
        if (path.charAt(path.length() - 1) != '/')
            path += '/';

        //create TaskHelper
        TaskHelper th = new TaskHelper(session);

        //if the first path is tag
        for (String tag : taskMap.keySet()){
            if (path.startsWith("/" + tag + "/")){
                th.setPath(1);
                return taskMap.get(tag).handle(th);
            }
        }

        //if host contain tag
        if (host.contains(".")) {
            String tag = host.split("\\.")[0];
            if (taskMap.containsKey(tag)) {
                return taskMap.get(tag).handle(th);
            }
        }

        //no tag, return index page
        return newChunkedResponse(Response.Status.OK, "text/html", getClass().getResourceAsStream("/index.html"));
    }
}