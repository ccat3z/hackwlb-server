package com.c0ldcat.utils;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.lang.Character.UnicodeBlock;
import java.util.Date;
import java.util.Random;

public class Utils {
    static public void main(String args[]) throws Exception{
        int i = 299;
        String s = "" + i;
        log("" + s.length());
        String re = "";
        for (int j = 0; j < s.length(); j++){
            re += String.format("%%u%04x",Character.codePointAt(s, j));
        }
        log(re);
    }

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
            br = new BufferedReader(new InputStreamReader(is));
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
        httpExchange.getResponseHeaders().add("Content-Type","text/html");
        httpExchange.sendResponseHeaders(200, resp.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(resp.getBytes());
        os.flush();
        os.close();
    }

    public static String utf8ToUnicode(String inStr) {
        char[] myBuffer = inStr.toCharArray();

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < inStr.length(); i++) {
            UnicodeBlock ub = UnicodeBlock.of(myBuffer[i]);
            if(ub == UnicodeBlock.BASIC_LATIN){
                //英文及数字等
                sb.append(myBuffer[i]);
            }else if(ub == UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS){
                //全角半角字符
                int j = (int) myBuffer[i] - 65248;
                sb.append((char)j);
            }else{
                //汉字
                short s = (short) myBuffer[i];
                String hexS = Integer.toHexString(s);
                String unicode = "\\u"+hexS;
                sb.append(unicode.toLowerCase());
            }
        }
        return sb.toString();
    }
}
