package com.c0ldcat.tasks;

import com.c0ldcat.utils.HackTask;
import com.c0ldcat.utils.TaskHelper;
import com.c0ldcat.utils.Utils;
import fi.iki.elonen.NanoHTTPD;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HackScript implements HackTask {
    private HashMap<String, HackTask> actionMap;

    final static private String ACTION_RUN = "run";
    final static private String ACTION_STATE = "state";

    public HackScript() {
        actionMap = new HashMap<>();
        actionMap.put(ACTION_RUN, new GetHandler());
        actionMap.put(ACTION_STATE, new StateHandler());
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

    private String runCmd(String cmd) throws IOException,InterruptedException {
        final Process p = Runtime.getRuntime().exec(cmd);

        final StringBuilder sb = new StringBuilder();

        Thread t = new Thread(new Runnable() {
            public void run() {
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = null;

                try {
                    while ((line = input.readLine()) != null) {
                        sb.append(line);
                        sb.append("<br>");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();

        p.waitFor();
        t.join();

        return sb.toString();
    }

    private class StateHandler implements HackTask {
        @Override
        public NanoHTTPD.Response handle(TaskHelper th) {
            String resp = Utils.getStringFromInputStream(getClass().getResourceAsStream("/HackScript.html"))
                    .replaceAll("BASE_PATH", th.getBasePath());
            return NanoHTTPD.newFixedLengthResponse(resp);
        }
    }

    private class GetHandler implements HackTask {
        @Override
        public NanoHTTPD.Response handle(TaskHelper th) {
            Map<String, String> pars = th.getParameters();
            if (pars.containsKey("cmd")){
                try {
                    return NanoHTTPD.newFixedLengthResponse("result<br>" + runCmd(pars.get("cmd")));
                } catch (NumberFormatException e) {
                    return NanoHTTPD.newFixedLengthResponse("wrong parameter");
                } catch (IOException|InterruptedException e) {
                    return NanoHTTPD.newFixedLengthResponse("error");
                }
            } else {
                return NanoHTTPD.newFixedLengthResponse("no parameter");
            }
        }
    }
}
