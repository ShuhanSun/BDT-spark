import bean.Twitter;
import com.alibaba.fastjson.JSONArray;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");

    public static String twitterJson2String(String json) {
        Twitter twitter;
        try {
            twitter = JSONArray.parseObject(json, Twitter.class);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

        if(twitter == null){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(twitter.getId_str());
        sb.append(";");
        Date parse = null;
        try {
            parse = DATE_FORMAT.parse(twitter.getCreated_at());
            sb.append(parse.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        sb.append(";");
        if(twitter.getUser() != null){
            sb.append(twitter.getUser().getId());
            sb.append(";");
            sb.append(twitter.getUser().getName());
            sb.append(";");
            sb.append(twitter.getUser().getLocation());
            sb.append(";");
        }
        if(twitter.getText() != null){
            sb.append(twitter.getText().replace(";", "."));
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        String json = "{ \"created_at\": \"Thu Apr 06 15:24:15 +0000 2017\", \"id_str\": \"850006245121695744\", \"text\": \"1\\/ Today we\\u2019re sharing our vision for the future of the Twitter API platform!\\thttps:\\/\\/t.co\\/XweGngmxlP\", \"user\": { \"id\": 2244994945, \"name\": \"Twitter Dev\", \"screen_name\": \"TwitterDev\", \"location\": \"Internet\", \"url\": \"https:\\/\\/dev.twitter.com\\/\", \"description\": \"Your official source for Twitter Platform news, updates & events. Need technical help? Visit https:\\/\\/twittercommunity.com\\/ \\u2328\\ufe0f #TapIntoTwitter\" }, \"place\": { }, \"entities\": { \"hashtags\": [ ], \"urls\": [ { \"url\": \"https:\\/\\/t.co\\/XweGngmxlP\", \"unwound\": { \"url\": \"https:\\/\\/cards.twitter.com\\/cards\\/18ce53wgo4h\\/3xo1c\", \"title\": \"Building the Future of the Twitter API Platform\" } } ], \"user_mentions\": [ ] } }";
        System.out.println(twitterJson2String(json));
    }
}
