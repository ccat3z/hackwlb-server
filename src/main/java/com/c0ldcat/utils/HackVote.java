package com.c0ldcat.utils;

import fi.iki.elonen.NanoHTTPD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HackVote implements Runnable, HackTask{
    private boolean run = false;
    private int id = 0;
    private int time = 0;
    private boolean state = false;
    private int speed = 0;
    private Thread speedTestThread;
    private int thread;
    private int delay;
    private HashMap<String, HackTask> actionMap;

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

        actionMap.put(ACTION_ID, new HackTask() {
            @Override
            public NanoHTTPD.Response handle(TaskHelper th) {
                return NanoHTTPD.newFixedLengthResponse("" + id);
            }
        });
        actionMap.put(ACTION_VOTE, new VoteHandler());
        actionMap.put(ACTION_RANK, new RankHandler());

        actionMap.put(ACTION_TIME, new HackTask() {
            @Override
            public NanoHTTPD.Response handle(TaskHelper th) {
                return NanoHTTPD.newFixedLengthResponse("" + time);
            }
        });
        actionMap.put(ACTION_SPEED, new HackTask() {
            @Override
            public NanoHTTPD.Response handle(TaskHelper th) {
                return NanoHTTPD.newFixedLengthResponse("" + speed);
            }
        });
        actionMap.put(ACTION_THREAD, new HackTask() {
            @Override
            public NanoHTTPD.Response handle(TaskHelper th) {
                return NanoHTTPD.newFixedLengthResponse("" + thread);
            }
        });
        actionMap.put(ACTION_DELAY, new HackTask() {
            @Override
            public NanoHTTPD.Response handle(TaskHelper th) {
                return NanoHTTPD.newFixedLengthResponse("" + delay);
            }
        });
        actionMap.put(ACTION_RUN_STATE, new HackTask() {
            @Override
            public NanoHTTPD.Response handle(TaskHelper th) {
                return NanoHTTPD.newFixedLengthResponse("" + run);
            }
        });
        actionMap.put(ACTION_VOTE_STATE, new HackTask() {
            @Override
            public NanoHTTPD.Response handle(TaskHelper th) {
                return NanoHTTPD.newFixedLengthResponse("" + state);
            }
        });
    }


    @Override
    public NanoHTTPD.Response handle(TaskHelper th) {
        ArrayList<String> path = th.getPath();
        if (path.size() != 0) {
            String action = path.get(0);
            if (actionMap.containsKey(action)) {
                return actionMap.get(action).handle(th);
            }
        }

        return new StateHandler().handle(th);
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

    private class StartHandler implements HackTask {
        @Override
        public NanoHTTPD.Response handle(TaskHelper th) {
            Map<String, String> pars = th.getParameters();
            if (pars.containsKey("id")
                    && pars.containsKey("time")
                    && pars.containsKey("thread")
                    && pars.containsKey("delay")){
                try {
                    int id = Integer.parseInt(pars.get("id"));
                    int time = Integer.parseInt(pars.get("time"));
                    int thread = Integer.parseInt(pars.get("thread"));
                    int delay = Integer.parseInt(pars.get("delay"));
                    if (!run) {
                        start(id, time, thread, delay);
                        return NanoHTTPD.newFixedLengthResponse("true");
                    } else {
                        return NanoHTTPD.newFixedLengthResponse("false");
                    }
                } catch (NumberFormatException e) {
                    return new EnterPage(th).add(pars).getResponse();
                }
            } else {
                EnterPage enterPage = new EnterPage(th).add(pars);
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
                return enterPage.getResponse();
            }
        }
    }

    private class StopHandler implements HackTask {
        @Override
        public NanoHTTPD.Response handle(TaskHelper th) {
            stop();
            return NanoHTTPD.newFixedLengthResponse("true");
        }
    }

    private class VoteHandler implements HackTask {
        @Override
        public NanoHTTPD.Response handle(TaskHelper th) {
            Map<String, String> pars = th.getParameters();
            if (pars.containsKey("id")){
                try {
                    int id = Integer.parseInt(pars.get("id"));
                    return NanoHTTPD.newFixedLengthResponse("" + getVote(id));
                } catch (NumberFormatException e) {
                    return NanoHTTPD.newFixedLengthResponse("wrong parameter");
                }
            } else {
                return NanoHTTPD.newFixedLengthResponse("" + getVote(id));
            }
        }
    }

    private class RankHandler implements HackTask {
        @Override
        public NanoHTTPD.Response handle(TaskHelper th) {
            Map<String, String> pars = th.getParameters();
            if (pars.containsKey("id")){
                try {
                    int id = Integer.parseInt(pars.get("id"));
                    return NanoHTTPD.newFixedLengthResponse("" + getRank(id));
                } catch (NumberFormatException e) {
                    return NanoHTTPD.newFixedLengthResponse("wrong parameter");
                }
            } else {
                return NanoHTTPD.newFixedLengthResponse("" + getRank(id));
            }
        }
    }

    private class StateHandler implements HackTask {
        @Override
        public NanoHTTPD.Response handle(TaskHelper th) {
            String resp = Utils.getStringFromInputStream(getClass().getResourceAsStream("/HackVoteState.html"))
                    .replaceAll("BASE_PATH", th.getBasePath());
            return NanoHTTPD.newFixedLengthResponse(resp);
        }
    }
}
