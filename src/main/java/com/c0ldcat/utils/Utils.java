package com.c0ldcat.utils;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Random;

public class Utils {
    public static String getRandomIp(){
        int[][] range = {{607649792,608174079},//36.56.0.0-36.63.255.255
                {1038614528,1039007743},//61.232.0.0-61.237.255.255
                {1783627776,1784676351},//106.80.0.0-106.95.255.255
                {2035023872,2035154943},//121.76.0.0-121.77.255.255
                {2078801920,2079064063},//123.232.0.0-123.235.255.255
                {-1950089216,-1948778497},//139.196.0.0-139.215.255.255
                {-1425539072,-1425014785},//171.8.0.0-171.15.255.255
                {-1236271104,-1235419137},//182.80.0.0-182.92.255.255
                {-770113536,-768606209},//210.25.0.0-210.47.255.255
                {-569376768,-564133889}, //222.16.0.0-222.95.255.255
        };

        Random rdint = new Random();
        int index = rdint.nextInt(10);
        return num2ip(range[index][0]+new Random().nextInt(range[index][1]-range[index][0]));
    }

    public static String num2ip(int ip) {
        int [] b=new int[4] ;
        b[0] = ((ip >> 24) & 0xff);
        b[1] = ((ip >> 16) & 0xff);
        b[2] = ((ip >> 8) & 0xff);
        b[3] = (ip & 0xff);
        return Integer.toString(b[0])+"."+Integer.toString(b[1])+"."+Integer.toString(b[2])+"."+Integer.toString(b[3]);
    }

    public static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public static void log(String log){
        System.out.println("[" + new Date() + "]" + log);
    }

    public static void httpResp(String resp, HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(200, resp.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(resp.getBytes());
        os.flush();
        os.close();
    }

    public static void httpResp(boolean resp, HttpExchange httpExchange) throws IOException {
        httpResp("" + resp, httpExchange);
    }

    public static void httpResp(int resp, HttpExchange httpExchange) throws IOException {
        httpResp("" + resp, httpExchange);
    }

    public static void httpResp(long resp, HttpExchange httpExchange) throws IOException {
        httpResp("" + resp, httpExchange);
    }

    public static void httpRespHtml(String resp, HttpExchange httpExchange) throws IOException {
        httpExchange.getResponseHeaders().add("Content-Type","text/html; charset=utf-8");
        httpExchange.sendResponseHeaders(200, resp.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(resp.getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();
    }
}
