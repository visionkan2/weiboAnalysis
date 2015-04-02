package edu.ece.osu;

import m.sina.HtmlUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Vincent on 15/4/1.
 */
public class PostsRelation {

    Set<String> set = new HashSet<>();
    Integer limit = 3;


    public static void main(String[] args) throws IOException {
        HashMap<Integer, Integer> map = new HashMap<>();
        PostsRelation test = new PostsRelation();
        String cookies = HtmlUtils.login();
        test.getAllFans(map,"1821296932", 1);
        test.writeToFile(map);

    }


    public void writeToFile(HashMap<Integer, Integer> map) throws IOException {
        File output = new File("/Users/Vincent/Desktop/weiboAnalysis/output");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            for(int fanNum : map.keySet()){
                writer.write(fanNum);
                writer.write(map.get(fanNum));
                writer.newLine();
            }
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

    }

    public void storeData(String id, HashMap<Integer, Integer> map) throws IOException {
        String userUrl = "http://m.weibo.cn/u/"+id;
        String html = HtmlUtils.HTMLGet(userUrl);
        int postNum = getPostNumFromHtml(html);
        int fanNum = getFansNumFromHtml(html);
        map.put(fanNum,postNum);
    }

    public void getAllFans(HashMap<Integer, Integer> map,String id, Integer flag) throws IOException {
        if(flag == limit) return;
        flag++;
        String userid = "100505"+id;
        String fanUrl = "m.weibo.cn/page/json?containerid="+userid+"_-_FANS&page="+1;
        JSONObject allFans = new JSONObject(HtmlUtils.HTMLGet(fanUrl));
        JSONArray jsonArray = allFans.getJSONArray("cards");
        JSONArray card_group = jsonArray.getJSONObject(0).getJSONArray("card_group");
        for(int i = 0; i< card_group.length();i++){
            JSONObject user = card_group.getJSONObject(i);
            String fanid = user.getJSONObject("user").getJSONObject("id").toString();
            if(!set.contains(fanid)) set.add(fanid);
            storeData(fanid,map);
        }
        List<String> fansList = new ArrayList<>();
        for(String tmp: set){
            fansList.add(tmp);
        }
        set.clear();
        for(String fanId : fansList){
            getAllFans(map,fanId,flag);
        }

    }

    public int getPostNumFromHtml(String html){
        Matcher m = Pattern.compile("\\\"mblogNum\\\":\\\"\\d{0,}\\\"").matcher(html);
        String js = "";
        if(m.find()){
            js = m.group();
            int divide = js.indexOf(":");
            String postNum = js.substring(divide+2,js.length()-1);
            return Integer.parseInt(postNum);
        }else return 0;

    }
    public int getFansNumFromHtml(String html){
        Matcher m = Pattern.compile("\\\"fansNum\\\":\\\"\\d{0,}\\\"").matcher(html);
        String js = "";
        if(m.find()){
            js = m.group();
            int divide = js.indexOf(":");
            String fansNum = js.substring(divide+2,js.length()-1);
            return Integer.parseInt(fansNum);
        }else return 0;
    }



}
