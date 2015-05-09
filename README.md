<Created by vincent>
Structure of this project:
1. edu.ece.osu:
a) FindFriends
  Dig from one user to generate a whole friendship level network of Sina Weibo.
b) PostRelation
  Dig from one user to get all fans from a number of users of Sina Weibo.
c) Regex
  Regex examples.
d) TwitterFriends
  Dig from one user to generate friendship network of Twitter.
2. m.sina:
a) EmbeddedNeo4j
  Driver of embedded Neo4j database.
b) HtmlUtils
  Basic functions related to Sina Weibo login and html content get.
c) Tools
  Time format functions.
d) TwitterHtmlUtils
  Basic functions related to Twitter login and html content get.
3. FrontForweibo:
friendNetwork.html: using javaScript to generate friendship network of sina weibo.
twitterNetwork.html: using javaScript to generate friendship network of Twitter.
Ignore others.
4. Others:
  Before running this project, Neo4j graph database should be installed. The template of JavaScript codes can be found on http://d3js.org

Good luck!
