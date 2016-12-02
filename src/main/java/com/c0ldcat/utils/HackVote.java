package com.c0ldcat.utils;

import fi.iki.elonen.NanoHTTPD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HackVote implements HackTask{
    private boolean run = false; //voter run controller
    private int id = 0; //target id
    private int time = 0; //remain vote time
    private boolean state = false; //vote state
    private SpeedMonitor speedMonitor; //speed monitor
    private int delay; //vote delay
    private HashMap<String, HackTask> actionMap; //server actions
    private ArrayList<Thread> voteThreads; //voters
    private Thread attackThread; //attacker
    private boolean attackRun; //attacker run controller
    private int vote; //vote sum

    final static private String ACTION_START = "start";
    final static private String ACTION_ATTACK = "attack";
    final static private String ACTION_STOP = "stop";

    final static private String ACTION_ID = "id";
    final static private String ACTION_VOTE = "vote";
    final static private String ACTION_RANK = "rank";

    final static private String ACTION_TIME = "time";
    final static private String ACTION_SPEED = "speed";
    final static private String ACTION_REAL_SPEED = "real-speed";
    final static private String ACTION_THREAD = "thread";
    final static private String ACTION_DELAY = "delay";
    final static private String ACTION_RUN_STATE = "run-state";
    final static private String ACTION_VOTE_STATE = "vote-state";

    public HackVote() {
        actionMap = new HashMap<>();

        actionMap.put(ACTION_START, new StartHandler());
        actionMap.put(ACTION_ATTACK, new AttackHandler());
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
                return NanoHTTPD.newFixedLengthResponse("" + (speedMonitor != null ? speedMonitor.getSpeed() : 0));
            }
        });
        actionMap.put(ACTION_REAL_SPEED, new HackTask() {
            @Override
            public NanoHTTPD.Response handle(TaskHelper th) {
                return NanoHTTPD.newFixedLengthResponse("" + (speedMonitor != null ? speedMonitor.getRealSpeed() : 0));
            }
        });
        actionMap.put(ACTION_THREAD, new HackTask() {
            @Override
            public NanoHTTPD.Response handle(TaskHelper th) {
                return NanoHTTPD.newFixedLengthResponse(Integer.toString(voteThreads.size()));
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

        voteThreads = new ArrayList<>();
        speedMonitor = new SpeedMonitor();
        speedMonitor.start();
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

    public void start(int id, int time, int thread, int delay){
        this.id = id;
        this.time = time;
        this.run = true;
        this.delay = delay;
        if (delay != 0) {
            thread = 1;
        }
        while (voteThreads.size() < thread){
            Thread t = new Voter();
            voteThreads.add(t);
            t.start();
        }
    }

    public void attack(int id, int attackId, int thread, int delay){
        this.id = id;
        this.delay = delay;
        this.attackRun = true;
        attackThread = new Thread(new Attacker(attackId, thread));
        attackThread.start();
    }

    public void stop(){
        this.run = false;
        this.attackRun = false;
    }

    protected boolean voteOnce(int id) {
        return false;
    }

    protected int getVote(int id) {
        return 0;
    }

    protected int getRank(int id) {
        return 0;
    }

    private class SpeedMonitor extends Thread {
        private int speed = -1;
        private int realSpeed = -1;
        private boolean pause;

        public int getSpeed() {
            return speed;
        }

        public int getRealSpeed() {
            return realSpeed;
        }

        @Override
        public void run() {
            Utils.log("Speed Monitor Thread Start");
            while (true){
                int oldvote = vote;
                int oldtime = time;
                try {
                    Thread.sleep(60000);
                } catch (Exception e) {

                }
                speed = oldtime - time;
                realSpeed = vote - oldvote;
            }
        }
    }

    private class Attacker implements Runnable {
        private int attackId;
        private int thread;

        public Attacker(int attackId, int thread) {
            this.attackId = attackId;
            this.thread = thread;
        }

        @Override
        public void run() {
            Utils.log("Attack Thread Start");
            while (attackRun){
                int attackVote = getVote(attackId);
                int vote = getVote(id);
                if ( attackVote > 0 && vote > 0) {
                    int delta = attackVote - vote;
                    if ( delta > 0 ){
                        //double check
                        if ( attackRun ) start(id, delta, thread, delay);
                    }
                }
            }
            Utils.log("Attack Thread Stop");
        }
    }

    private class Voter extends Thread {
        @Override
        public void run() {
            Utils.log("Vote Thread Start");
            while (run & id != 0 & time > 0){
                time--;
                vote++;
                state = voteOnce(id);
                try {
                    Thread.sleep(delay * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            run = false;
            voteThreads.remove(this);
            Utils.log("Vote Thread Stop");
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

    private class AttackHandler implements HackTask {
        @Override
        public NanoHTTPD.Response handle(TaskHelper th) {
            Map<String, String> pars = th.getParameters();
            if (pars.containsKey("id")
                    && pars.containsKey("attack")
                    && pars.containsKey("thread")
                    && pars.containsKey("delay")){
                try {
                    int id = Integer.parseInt(pars.get("id"));
                    int attack = Integer.parseInt(pars.get("attack"));
                    int thread = Integer.parseInt(pars.get("thread"));
                    int delay = Integer.parseInt(pars.get("delay"));
                    if (!attackRun) {
                        attack(id, attack, thread, delay);
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
                if(!pars.containsKey("attack")){
                    enterPage.add(EnterPage.TYPE_TEXT, "attack");
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
