package com.zf3zzzx.tasks;

import com.zf3zzzx.utils.HackVote;
import com.zf3zzzx.utils.Utils;
import org.apache.commons.logging.Log;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class HackWLB extends HackVote {
    @Override
    protected boolean voteOnce(int id){
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
            e.printStackTrace();
        }
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
}
