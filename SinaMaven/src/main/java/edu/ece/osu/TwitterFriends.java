package edu.ece.osu;

import m.sina.EmbeddedNeo4j;
import m.sina.HtmlUtils;
import m.sina.TwitterHtmlUtils;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Vincent on 15/4/20.
 */
public class TwitterFriends {
    public EmbeddedNeo4j db;
    public TwitterFriends(){ this.db = new EmbeddedNeo4j();}
    public HashMap<String,Integer> users = new HashMap<>();


    public static void main(String[] args) throws Exception {
        int level = 3;
        TwitterFriends tf = new TwitterFriends();
        TwitterHtmlUtils user = new TwitterHtmlUtils();
        user.login();
        tf.db.cleanDb();
        tf.db.createDb();

//        File output = new File("/Users/Vincent/Desktop/weiboAnalysis/output");
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
//            writer.write(test);
//            writer.flush();
//            writer.close();
//        } catch (FileNotFoundException e){
//            e.printStackTrace();
//        }
        //lisarsloane
        //soulflowered
        //VincentKan2
        tf.digFromOne("MayliMonti",level);
        System.out.println("total number: "+tf.users.size());
//        for(String u: tf.users.keySet()){
//            System.out.println(u);
//        }

    }

    public void digFromOne(String centerUser,Integer level) throws IOException, InterruptedException {
        if(centerUser.equals("ambertheysay")) return;
        if(level == 0) return;
        String nextPosition = "-1";
        String url = "https://twitter.com/"+centerUser+"/followers/users?include_available_features=1&include_entities=1&max_position="+nextPosition;
        List<String> followList = new ArrayList<>();
        String followPage = HtmlUtils.HTMLGet(url);
        int maxpage = 1;
        while (!nextPosition.equals("0") && maxpage-- >0 ){

//            System.out.println(nextPosition);
            String[] FollowerInOnePage = this.getFollower(followPage);
            if(FollowerInOnePage !=null){
                for(String u : FollowerInOnePage){
                    if(!users.containsKey(u)){
                        users.put(u,1);
                        System.out.println(u);
                        System.out.println("level: "+level);
                        addToGraph(centerUser,u,"0");
                        digFromOne(u, level - 1);
                    }
                }
            }
            nextPosition = this.getPosition(followPage);
            url = "https://twitter.com/"+centerUser+"/followers/users?include_available_features=1&include_entities=1&max_position="+nextPosition;
            Thread.sleep(1000);
            followPage = HtmlUtils.HTMLGet(url);
        }


    }

    private String getPosition(String html){
        Matcher m = Pattern.compile("\\\"min_position\\\":\\\"\\d{0,}").matcher(html);
        String js = "";
        if(m.find()){
            js = m.group();
            int divide = js.indexOf(":");
            String PositionNum = js.substring(divide+2,js.length());
            return PositionNum;
        }else return "";
    }

    private String[] getFollower(String html){
        List<String> nameList = new ArrayList<>();
        Matcher m = Pattern.compile("\\\"ProfileCard-bg js-nav\\\\\" href=\\\\\\\"\\\\/[A-Za-z0-9]+").matcher(html);
        String js = "";
        while(m.find()) {
            js = m.group();
            int divide = js.indexOf("/");
            String Follower = js.substring(divide + 1, js.length());
            nameList.add(Follower);
        }
//        System.out.println("haah");
//        System.out.println(nameList.size());
        if(nameList.size()>=1){
            return (String[])nameList.toArray(new String[nameList.size()]);
        }else return null;
    }

    private void addToGraph(String preName, String curName, String relType){
        Node cur, pre;
        if(db.findByScreenName(curName) == null){
            cur = db.createTwitterNode(curName,"0");
        }else {
            cur = db.findByScreenName(curName);
            //db.updateRelNum(cur, relNum);//record the max realNum of one user
        }
        if(db.findByScreenName(preName) == null){
            pre = db.createTwitterNode(preName,"0");
        }else {
            pre = db.findByScreenName(preName);
            //db.updateRelNum(cur, relNum);//record the max realNum of one user
        }

        Relationship rel = db.findRelByNodes(pre,cur);
        if(rel == null){
            db.addrel(pre, cur, relType);
        }


    }
}
