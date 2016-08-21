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
    private int delay;
    private HashMap<String, HttpHandler> actionMap;

    final static private String ACTION_START = "start";
    final static private String ACTION_STOP = "stop";

    final static private String ACTION_ID = "id";
    final static private String ACTION_VOTE = "vote";
    final static private String ACTION_RANK = "rank";

    final static private String ACTION_TIME = "time";
    final static private String ACTION_SPEED = "speed";
    final static private String ACTION_THREAD = "thread";
    final static private String ACTION_DELAY = "delay";
    final static private String ACTION_RUN_STATE = "run-state";
    final static private String ACTION_VOTE_STATE = "vote-state";

    public HackVote() {
        actionMap = new HashMap<>();

        actionMap.put(ACTION_START, new StartHandler());
        actionMap.put(ACTION_STOP, new StopHandler());

        actionMap.put(ACTION_ID, new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                Utils.httpResp(id, httpExchange);
            }
        });
        actionMap.put(ACTION_VOTE, new VoteHandler());
        actionMap.put(ACTION_RANK, new RankHandler());

        actionMap.put(ACTION_TIME, new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                Utils.httpResp(time, httpExchange);
            }
        });
        actionMap.put(ACTION_SPEED, new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                Utils.httpResp(speed, httpExchange);
            }
        });
        actionMap.put(ACTION_THREAD, new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                Utils.httpResp(thread, httpExchange);
            }
        });
        actionMap.put(ACTION_DELAY, new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                Utils.httpResp(delay, httpExchange);
            }
        });
        actionMap.put(ACTION_RUN_STATE, new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                Utils.httpResp(run, httpExchange);
            }
        });
        actionMap.put(ACTION_VOTE_STATE, new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                Utils.httpResp(state, httpExchange);
            }
        });
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
        String resp = Utils.getStringFromInputStream(getClass().getResourceAsStream("/HackVoteState.html"));
        return resp;
    }

    @Override
    public void run() {
        Utils.log("Vote Thread Start");
        thread++;
        while (run & id != 0 & time != 0){
            time--;
            state = voteOnce(id);
            try {
                Thread.sleep(delay * 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        run = false;
        thread--;
        Utils.log("Vote Thread Stop");
    }

    public void start(int id, int time, int thread, int delay){
        this.id = id;
        this.time = time;
        this.run = true;
        this.delay = delay;
        if (delay != 0) {
            thread = 1;
        }
        if (speedTestThread == null) {
            speedTestThread = new Thread(new SpeedTest());
            speedTestThread.start();
        }
        for(int i = 0; i < thread; i++){
            new Thread(this).start();
        }
    }

    public void stop(){
        this.run = false;
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
            if (pars.containsKey("id") && pars.containsKey("time") && pars.containsKey("thread")){
                try {
                    int id = Integer.parseInt(pars.get("id"));
                    int time = Integer.parseInt(pars.get("time"));
                    int thread = Integer.parseInt(pars.get("thread"));
                    int delay = Integer.parseInt(pars.get("delay"));
                    if (!run) {
                        Utils.httpResp(true, httpExchange);
                        start(id, time, thread, delay);
                    } else {
                        Utils.httpResp(false, httpExchange);
                    }
                } catch (NumberFormatException e) {
                    new EnterPage(httpExchange).add(pars).push();
                }
            } else {
                EnterPage enterPage = new EnterPage(httpExchange).add(pars);
                if(!pars.containsKey("id")){
                    enterPage.add(EnterPage.TYPE_TEXT, "id");
                }
                if(!pars.containsKey("time")){
                    enterPage.add(EnterPage.TYPE_TEXT, "time", "1000");
                }
                if(!pars.containsKey("thread")){
                    enterPage.add(EnterPage.TYPE_TEXT, "thread", "10");
                }
                if(!pars.containsKey("delay")){
                    enterPage.add(EnterPage.TYPE_TEXT, "delay", "0");
                }
                enterPage.push();
            }
        }
    }

    private class StopHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            stop();
            Utils.httpResp(true, httpExchange);
        }
    }

    private class VoteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            HashMap<String, String> pars = UrlParameterParser.parse(httpExchange.getRequestURI().getQuery());
            if (pars.containsKey("id")){
                try {
                    int id = Integer.parseInt(pars.get("id"));
                    Utils.httpResp(getVote(id), httpExchange);
                } catch (NumberFormatException e) {
                    new EnterPage(httpExchange).add(pars).push();
                }
            } else {
                Utils.httpResp(getVote(id), httpExchange);
            }
        }
    }

    private class RankHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            HashMap<String, String> pars = UrlParameterParser.parse(httpExchange.getRequestURI().getQuery());
            if (pars.containsKey("id")){
                try {
                    int id = Integer.parseInt(pars.get("id"));
                    Utils.httpResp(getRank(id), httpExchange);
                } catch (NumberFormatException e) {
                    new EnterPage(httpExchange).add(pars).push();
                }
            } else {
                Utils.httpResp(getRank(id), httpExchange);
            }
        }
    }
}
