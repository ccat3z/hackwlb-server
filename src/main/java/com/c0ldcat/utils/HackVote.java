package com.c0ldcat.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;

public class HackVote implements Runnable, HttpHandler{
    private boolean run = false;
    private int id = 0;
    private int time = 0;
    private boolean state = false;
    private int speed = 0;
    private Thread speedTestThread;
    private int thread;
    private HashMap<String, HttpHandler> actionMap;

    final static private String ACTION_START = "start";
    final static private String ACTION_STOP = "stop";
    final static private String ACTION_VOTE = "vote";
    final static private String ACTION_RANK = "rank";
    final static private String ACTION_TIME = "time";
    final static private String ACTION_RUN_STATE = "run-state";
    final static private String ACTION_VOTE_STATE = "run-state";

    public HackVote() {
        actionMap = new HashMap<>();
        actionMap.put(ACTION_START, new StartHandler());
        actionMap.put(ACTION_STOP, new StopHandler());
        actionMap.put(ACTION_VOTE, new VoteHandler());
        actionMap.put(ACTION_RUN_STATE, new RunStateHandler());
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String fullPath[] = httpExchange.getRequestURI().getPath().split("/");
        if (fullPath.length != 0) {
            String action = fullPath[fullPath.length - 1];
            if (actionMap.containsKey(action)) {
                actionMap.get(action).handle(httpExchange);
                return;
            }
        }
        Utils.httpResp(welcome(), httpExchange);
    }

    public String welcome() {
        return "Welcome";
    }

    @Override
    public void run() {
        Utils.log("Vote Thread Start");
        thread++;
        while (run & id != 0 & time != 0){
            time--;
            state = voteOnce(id);
        }
        run = false;
        thread--;
        Utils.log("Vote Thread Stop");
    }

    public void start(){
        start(id,time);
    }

    public void start(int id, int time){
        this.id = id;
        this.time = time;
        this.run = true;
        if (speedTestThread == null) {
            speedTestThread = new Thread(new SpeedTest());
            speedTestThread.start();
        }
        new Thread(this).start();
    }

    public void stop(){
        this.run = false;
    }

    public String getStateString() {
        return "run state:" + run
                + ",id:" + id
                + ",time:" + time
                + ",state:" + state
                + ",speed:" + speed + "votes/min"
                + ",thread:" + thread
                + ",vote:" + getVote(id)
                + ",rank:" + getRank(id);
    }

    protected boolean voteOnce(int id) {
        return false;
    }

    protected long getVote(int id) {
        return 0;
    }

    protected int getRank(int id) {
        return 0;
    }

    protected String getPass() {
        return "123456";
    }

    private class SpeedTest implements Runnable {
        @Override
        public void run() {
            Utils.log("Speed Test Thread Start");
            while (run){
                int oldtime = time;
                try {
                    Thread.sleep(60000);
                } catch (Exception e) {

                }
                speed = oldtime - time;
            }
            speedTestThread = null;
            Utils.log("Speed Test Thread Stop");
        }
    }


    private class StartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            HashMap<String, String> pars = UrlParameterParser.parse(httpExchange.getRequestURI().getQuery());
            if (pars.containsKey("id") && pars.containsKey("time")){
                try {
                    int id = Integer.parseInt(pars.get("id"));
                    int time = Integer.parseInt(pars.get("time"));
                    Utils.httpResp("id:" + id + ",time:" + time + "\n", httpExchange);
                    start(id,time);
                } catch (NumberFormatException e) {
                    new EnterPage(httpExchange).add(pars).push();
                }
            } else {
                EnterPage enterPage = new EnterPage(httpExchange).add(pars);
                if(!pars.containsKey("id")){
                    enterPage.add(EnterPage.TYPE_TEXT, "id");
                }
                if(!pars.containsKey("time")){
                    enterPage.add(EnterPage.TYPE_TEXT, "time");
                }
                enterPage.push();
            }
        }
    }

    private class RunStateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            Utils.httpResp(getStateString() + "\n", httpExchange);
        }
    }

    private class StopHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            stop();
            Utils.httpResp("stopded\n", httpExchange);
        }
    }

    private class VoteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String resp = "";
            HashMap<String, String> pars = UrlParameterParser.parse(httpExchange.getRequestURI().getQuery());
            if (pars.containsKey("id")){
                try {
                    int id = Integer.parseInt(pars.get("id"));
                    Utils.httpResp( "id:" + id
                            + ",vote:" + getVote(id)
                            + ",rank:" + getRank(id)
                            + "\n", httpExchange);
                } catch (NumberFormatException e) {
                    new EnterPage(httpExchange).add(pars).push();
                }
            } else {
                EnterPage enterPage = new EnterPage(httpExchange).add(pars);
                enterPage.add(EnterPage.TYPE_TEXT, "id");
                enterPage.push();
            }
        }
    }
}
