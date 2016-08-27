package com.c0ldcat.tasks;

import com.c0ldcat.netease.music.NetEaseMusic;
import com.c0ldcat.netease.music.Song;
import com.c0ldcat.netease.music.utils.NoLoginException;
import com.c0ldcat.utils.HackTask;
import com.c0ldcat.utils.TaskHelper;
import com.c0ldcat.utils.Utils;
import fi.iki.elonen.NanoHTTPD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HackNeteaseMusic implements HackTask {
    private NetEaseMusic netEaseMusic;
    private HashMap<String, HackTask> actionMap;

    final static private String ACTION_GET = "get";
    final static private String ACTION_STATE = "state";

    public HackNeteaseMusic() {
        actionMap = new HashMap<>();
        actionMap.put(ACTION_GET, new GetHandler());
        actionMap.put(ACTION_STATE, new StateHandler());

        String username = System.getenv("NMG_USER");
        String password = System.getenv("NMG_PASS");
        String configFile = System.getenv("NMG_CONFIG");

        if ( username == null || password == null || configFile == null ) {
            Utils.log("nmg launch failed");
            return;
        }

        netEaseMusic = new NetEaseMusic(configFile);
        Utils.log("nmg login " + (netEaseMusic.login(username, password) ? "success" : "failed"));
    }

    public String getUrl(int id) {
        Song s = new Song(netEaseMusic, id);
        try {
            s.update();
        } catch (NoLoginException e) {
            return "bad config";
        }
        return s.getUrl();
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

    private class StateHandler implements HackTask {
        @Override
        public NanoHTTPD.Response handle(TaskHelper th) {
            String resp = Utils.getStringFromInputStream(getClass().getResourceAsStream("/HackNeteaseMusic.html"))
                .replaceAll("BASE_PATH", th.getBasePath());
            return NanoHTTPD.newFixedLengthResponse(resp);
        }
    }

    private class GetHandler implements HackTask {
        @Override
        public NanoHTTPD.Response handle(TaskHelper th) {
            Map<String, String> pars = th.getParameters();
            if (pars.containsKey("id")){
                try {
                    int id = Integer.parseInt(pars.get("id"));
                    return NanoHTTPD.newFixedLengthResponse("" + getUrl(id));
                } catch (NumberFormatException e) {
                    return NanoHTTPD.newFixedLengthResponse("wrong parameter");
                }
            } else {
                return NanoHTTPD.newFixedLengthResponse("no parameter");
            }
        }
    }
}
