package com.zf3zzzx.tasks;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.zf3zzzx.utils.HackTask;
import com.zf3zzzx.utils.Utils;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class HackZXB implements Runnable, HackTask{
    private boolean run = false;
    private int id = 0;
    private int time = 0;
    private int state = 0;

    public static void main(String args[]){
        HackZXB hackZXB = new HackZXB();
        hackZXB.voteOnce(1457);
    }

    @Override
    public void initServer(HttpServer server) {
        server.createContext("/zxb-start", new startHandler());
        server.createContext("/zxb-state", new stateHandler());
        server.createContext("/zxb-stop", new stopHandler());
    }

    @Override
    public void run() {
        Utils.log("Vote Thread Start");
        while (run & id != 0 & time != 0){
            state = voteOnce(id);
            time--;
        }
        run = false;
        Utils.log("Vote Thread Stop");
    }

    public void start(int id, int time){
        this.id = id;
        this.time = time;
        this.run = true;
        new Thread(this).start();
    }

    public void stop(){
        this.run = false;
    }

    public String getStateString() {
        return "run state:" + run + ",id:" + id + ",time:" + time + ",vote:" + state;
    }

    private int voteOnce(int id){
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://a.ajmide.com/api.php?plugin=vote&action=postVoteWx&pp_id=478");
        try {
            httpPost.setEntity(new StringEntity("v=" +id + "&c=0&o=0",  ContentType.create("plain/text", Consts.UTF_8)));
        } catch (Exception e){
            e.printStackTrace();
        }
        httpPost.setHeader("X-Forwarded-For",Utils.getRandomIp());
        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
        String resp = null;
        try {
            resp = httpClient.execute(httpPost, new ResponseHandler<String>() {
                @Override
                public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                    return Utils.getStringFromInputStream(httpResponse.getEntity().getContent());
                }
            });
            resp = "" + new JSONObject(resp).getJSONObject("data").getInt("data");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(resp == null){
            Utils.log("vote " + id + " request failed");
            return -1;
        } else {
            int r = 0;
            try {
                r = Integer.parseInt(resp);
            } catch (NumberFormatException e) {
                Utils.log("vote " + id + " request failed");
                return -1;
            }
            switch (r){
                case 0:
                case 1:
                case -1:
                    Utils.log("Vote " + id + " failed");
                    return -1;
                default:
                    Utils.log("Vote " + id + " success (" + r + ")");
                    return r;
            }
        }
    }


    private class startHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String resp = "";
            String par = httpExchange.getRequestURI().getQuery();
            if(par != null) {
                String pars[] = par.split("&");
                if (pars.length != 2){
                    resp += "wrong parameter\n";
                } else {
                    try {
                        int id = Integer.parseInt(pars[0]);
                        int time = Integer.parseInt(pars[1]);
                        resp += "id:" + id + ",time:" + time + "\n";
                        start(id,time);
                    } catch (NumberFormatException e) {
                        resp += "wrong parameter\n";
                    }
                }
            } else  {
                resp += "no parameter\n";
            }
            httpExchange.sendResponseHeaders(200, resp.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(resp.getBytes());
            os.flush();
            os.close();
        }
    }

    private class stateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String resp = "";
            resp += getStateString() + "\n";
            httpExchange.sendResponseHeaders(200, resp.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(resp.getBytes());
            os.flush();
            os.close();
        }
    }

    private class stopHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String resp = "";
            stop();
            resp += "stoped" + "\n";
            httpExchange.sendResponseHeaders(200, resp.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(resp.getBytes());
            os.flush();
            os.close();
        }
    }
}
