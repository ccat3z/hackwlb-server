package com.c0ldcat.tasks;

import com.c0ldcat.utils.HackVote;
import com.c0ldcat.utils.Utils;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class HackWLB extends HackVote {
    @Override
    protected boolean voteOnce(int id){
        //request vote
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://weilaibei.smartstudy.com/ajax/toupiao.aspx");
        try {
            httpPost.setEntity(new StringEntity("pid=" + id,  ContentType.create("plain/text", Consts.UTF_8)));
        } catch (Exception e){
            e.printStackTrace();
        }
        final int TIMEOUTMS = 15 * 1000;
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(TIMEOUTMS).setConnectTimeout(TIMEOUTMS).setSocketTimeout(TIMEOUTMS).build();
        httpPost.setConfig(requestConfig);
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
        } catch (Exception e) {
            Utils.log("request error");
        }

        //analyze response
        if(resp == null){
            Utils.log("vote " + id + " request failed");
            return false;
        } else {
            int r = 0;
            try {
                r = Integer.parseInt(resp);
            } catch (NumberFormatException e) {
                Utils.log("vote " + id + " request failed");
                return false;
            }
            switch (r){
                case 0:
                case 1:
                case -1:
                    Utils.log("Vote " + id + " failed");
                    return false;
                default:
                    Utils.log("Vote " + id + " success (" + r + ")");
                    return true;
            }
        }
    }

    @Override
    protected long getVote(int id) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://weilaibei.smartstudy.com/qbxs/qbxsInfo.aspx?itemid=" + id);
        final int TIMEOUTMS = 15 * 1000;
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(TIMEOUTMS).setConnectTimeout(TIMEOUTMS).setSocketTimeout(TIMEOUTMS).build();
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("X-Forwarded-For", Utils.getRandomIp());
        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
        String resp = null;
        try {
            resp = httpClient.execute(httpPost, new ResponseHandler<String>() {
                @Override
                public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                    return Utils.getStringFromInputStream(httpResponse.getEntity().getContent());
                }
            });
        } catch (Exception e) {
            Utils.log("request error");
        }
        if(resp == null){
            Utils.log("vote " + id + " request failed");
            return -1;
        } else {
            Document doc = Jsoup.parse(resp);
            Element element = doc.getElementById("span_" + id);
            if (element == null)
            {
                return -1;
            } else {
                return Integer.parseInt(element.text());
            }
        }
    }

    @Override
    protected int getRank(int id) {
        //send http post
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://weilaibei.smartstudy.com/Ajax/GetPm.aspx");
        final int TIMEOUTMS = 30 * 1000;
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(TIMEOUTMS).setConnectTimeout(TIMEOUTMS).setSocketTimeout(TIMEOUTMS).build();
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("X-Forwarded-For", Utils.getRandomIp());
        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
        String resp = null;
        try {
            resp = httpClient.execute(httpPost, new ResponseHandler<String>() {
                @Override
                public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                    return Utils.getStringFromInputStream(httpResponse.getEntity().getContent());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Utils.log("request error");
        }

        //analyze response
        if(resp == null){ //if no response
            Utils.log("rank " + id + " request failed");
            return -1;
        } else {
            try {
                //parse json
                JSONArray jsonArray = new JSONArray(resp);

                //convert id(int) to unicode code string
                String s = "" + id;
                String re = "";
                for (int j = 0; j < s.length(); j++){
                    re += String.format("%%u%04x",Character.codePointAt(s, j));
                }

                //find target id
                for (Object o : jsonArray) {
                    if (((JSONObject) o).getString("Whir_Mem_Member_PID").equals(re)) {
                        String pm = ((JSONObject) o).getString("pm");
                        return Integer.parseInt(pm.replaceAll("%u003",""));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //if no target
            return -1;
        }
    }
}
