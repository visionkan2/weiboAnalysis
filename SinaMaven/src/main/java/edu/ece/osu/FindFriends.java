package edu.ece.osu;

import m.sina.EmbeddedNeo4j;
import m.sina.HtmlUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.cypher.internal.compiler.v2_0.functions.Str;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Vincent on 15/3/23.
 */
public class FindFriends {
    public EmbeddedNeo4j db;
    public FindFriends(){ this.db = new EmbeddedNeo4j();}
    public HashMap<String,Integer> followers = new HashMap<>();

    public String userUrl = "http://m.weibo.cn/page/json?containerid=";
    public String postUrl = "http://m.weibo.cn/single/rcList?format=cards&id=";

    public static void main(String[] args) throws IOException, InterruptedException {

        FindFriends test = new FindFriends();
        test.seekFriend();
    }

    public void seekFriend() throws IOException, InterruptedException {
        String cookies = HtmlUtils.login();
        System.out.println("find all posts...");
        db.cleanDb();
        db.createDb();

        searchFromOne("1881186552",cookies);
        searchFromOne("1686829752",cookies);
        searchFromOne("2804073892",cookies);
        searchFromOne("1968712865",cookies);
        searchFromOne("1955174322",cookies);
        searchFromOne("1354705323",cookies);



    }
    public void searchFromOne(String id, String cookies) throws JSONException, IOException, InterruptedException {
        int num_repliers = 0;

        String userid = "100505"+id;
        String url = userUrl + userid + "_-_WEIBO_SECOND_PROFILE_WEIBO&";
        String allposts = HtmlUtils.HTMLGet(url+"page=1",cookies);
        String name = getScreenName(allposts);
        int maxpage = getPostPageNum(allposts);
        if(maxpage>25) maxpage = 25;
        if(db.findById(id)==null) db.createNode(id,name,"0");
        //traverse all pages of posts
        for(int cur_page_post = 1;cur_page_post <= maxpage; cur_page_post++){
            Thread.sleep(1000*10);
            JSONObject curpage_post = new JSONObject(HtmlUtils.HTMLGet(url+"page="+ cur_page_post,cookies));
            System.out.println(url+"page="+ cur_page_post);
            JSONArray postArray = curpage_post.getJSONArray("cards").getJSONObject(0).getJSONArray("card_group");
            //traverse all posts in one page
            for(int num_post = 0; num_post < postArray.length(); num_post++){
                JSONObject curpost_jsonObject = postArray.getJSONObject(num_post);
                if(!curpost_jsonObject.has("mblog")) continue;
                String mid = curpost_jsonObject.getJSONObject("mblog").getString("mid");
                //traverse all replies in one post
                int num_page_reply = getReplyPageNum(mid,cookies);
                for(int cur_page_reply = 1; cur_page_reply<=num_page_reply; cur_page_reply++){
//
                    String post = HtmlUtils.HTMLGet(postUrl + mid + "&type=comment&hot=0&" + "page=" + cur_page_reply);
                    System.out.println(postUrl + mid + "&type=comment&hot=0&" + "page=" + cur_page_reply);
                    System.out.println(post);
                    if(post.charAt(0)!='[') post = HtmlUtils.HTMLGet(postUrl + mid + "&type=comment&hot=0&" + "page=" + cur_page_reply,cookies);
                    if(post.charAt(0)!='[') {
                        System.out.println("weibo bug!!!");
                        continue;
                    }
                    JSONArray curpage_reply = new JSONArray(post);
                    JSONObject mod = curpage_reply.getJSONObject(curpage_reply.length()-1);
                    if(!mod.has("card_group")) continue;
                    JSONArray card_group = mod.getJSONArray("card_group");
                    for(int i = 0; i<card_group.length();++i){
                        JSONObject reply = card_group.getJSONObject(i);
                        String followerId = reply.getJSONObject("user").get("id").toString();
                        String followerName = reply.getJSONObject("user").getString("screen_name");

                        if(!followerId.equals(id)){
                            num_repliers++;
                            if(!followers.containsKey(followerId)){
                                followers.put(followerId,1);
                                addToGraph(id, followerId, followerName,"KNOW","0");
                                System.out.println("No." + num_repliers + " follower: " + followerId + " " + followerName + " add to "+ id +" "+ name+" as KNOW.");
                            }
                            else followers.put(followerId,followers.get(followerId)+1);

                            if(followers.get(followerId)==5){
                                addToGraph(id, followerId, followerName,"FRIEND1","1");
                                System.out.println("No." + num_repliers + " follower: " + followerId + " " + followerName + " add to "+ id +" "+ name+" as FRIEND (LEVEL 1).");
                            }

                            if(followers.get(followerId)==10){
                                addToGraph(id, followerId, followerName,"FRIEND2","2");
                                System.out.println("No." + num_repliers + " follower: " + followerId + " " + followerName + " add to "+ id +" "+ name+" as FRIEND (LEVEL 2).");
                            }

                            if(followers.get(followerId)==18){
                                addToGraph(id, followerId, followerName,"FRIEND3","3");
                                System.out.println("No." + num_repliers + " follower: " + followerId + " " + followerName + " add to "+ id +" "+ name+" as FRIEND (LEVEL 3).");
                            }

                        }

                    }

                }

            }

        }
        System.out.println("number of repliers: "+ num_repliers);
    }

    private String getScreenName(String postUrl){
        JSONObject jsonObject = new JSONObject(postUrl);
        JSONArray card_group = jsonObject.getJSONArray("cards").getJSONObject(0).getJSONArray("card_group");
        JSONObject card = card_group.getJSONObject(card_group.length()-1);
        if(!card.has("mblog")) return "UnknownUser";
        return card.getJSONObject("mblog").getJSONObject("user").get("screen_name").toString();
    }

    private int getPostPageNum(String postJSON){
        JSONObject jsonObject = new JSONObject(postJSON);
        JSONArray jsonArray = jsonObject.getJSONArray("cards");
        JSONObject maxPage = jsonArray.getJSONObject(0);
        if(!maxPage.has("maxPage")) return 1;
        else return maxPage.getInt("maxPage");

    }
    private int getReplyPageNum(String mid,String cookies) throws IOException {
        String firstpostUrl = postUrl + mid + "&type=comment&hot=0&" + "page=1";


        String j = HtmlUtils.HTMLGet(firstpostUrl);
        if(j.charAt(0)!='[') j = HtmlUtils.HTMLGet(firstpostUrl,cookies);
        if(j.charAt(0)!='['){
            System.out.println("weibo bug!");
            return 1;
        }
        JSONArray jsonArray = new JSONArray(j);
        JSONObject jsonObject = jsonArray.getJSONObject(1);
        if(!jsonObject.has("maxPage")) return 1;
        else return jsonObject.getInt("maxPage");

    }

    private void addToGraph(String preId, String curId, String screen_name, String relType, String relNum){
        Node cur;
        if(db.findById(curId)==null){
            cur = db.createNode(curId,screen_name,relNum);
        }else {
            cur = db.findById(curId);
            db.UpdateRelNum(cur, relNum);
        }
        Node pre = db.findById(preId);
        db.UpdateRelNum(pre,relNum);
        Relationship rel = db.findRelByNodes(pre,cur);
        if(rel == null){
            db.addrel(pre,cur,"KNOW");
        }else{
            db.deleteRel(pre,cur);
            db.addrel(pre,cur,relType);
        }
    }


}
