package org.apache.spark.spark_streaming_kafka_0_10_2;
import bean.Twitter;
import com.alibaba.fastjson.JSONArray;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Utils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
    private static final SimpleDateFormat DATE_FORMAT_TO = new SimpleDateFormat("yyyyMMdd:HH:mm:ss");

    public static String dateString2long(String date) {
        try {
            return DATE_FORMAT_TO.format(DATE_FORMAT.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String twitterJson2String(String json) {
        if (json == null) {
            return null;
        }
        Twitter twitter;
        try {
            twitter = JSONArray.parseObject(json, Twitter.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (twitter == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (twitter.getId_str() == null || twitter.getId_str().length() == 0) {
            return null;
        }
        sb.append(twitter.getId_str());
        sb.append(";");
        try {
            sb.append(dateString2long(twitter.getCreated_at()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        sb.append(";");
        sb.append(twitter.getFavorite_count());
        sb.append(";");
        if (twitter.getUser() == null) {
            return null;
        }
        sb.append(twitter.getUser().getId());
        sb.append(";");
        sb.append(twitter.getUser().getName());
        sb.append(";");
        sb.append(twitter.getUser().getLocation());
        sb.append(";");
        String s = twitter.getText().replaceAll("\n", " ").replaceAll(";", ".");
        sb.append(s);

        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
//        String json = "{ \"created_at\": \"Thu Apr 06 15:24:15 +0000 2017\", \"id_str\": \"850006245121695744\", \"text\": \"1\\/ Today we\\u2019re sharing our vision for the future of the Twitter API platform!\\thttps:\\/\\/t.co\\/XweGngmxlP\", \"user\": { \"id\": 2244994945, \"name\": \"Twitter Dev\", \"screen_name\": \"TwitterDev\", \"location\": \"Internet\", \"url\": \"https:\\/\\/dev.twitter.com\\/\", \"description\": \"Your official source for Twitter Platform news, updates & events. Need technical help? Visit https:\\/\\/twittercommunity.com\\/ \\u2328\\ufe0f #TapIntoTwitter\" }, \"place\": { }, \"entities\": { \"hashtags\": [ ], \"urls\": [ { \"url\": \"https:\\/\\/t.co\\/XweGngmxlP\", \"unwound\": { \"url\": \"https:\\/\\/cards.twitter.com\\/cards\\/18ce53wgo4h\\/3xo1c\", \"title\": \"Building the Future of the Twitter API Platform\" } } ], \"user_mentions\": [ ] } }";
//        System.out.println(twitterJson2String(json));

        String output = "/Users/sunshuhan/Documents/mum/course/BDT/BDT-spark/SparkSqlProject/src/main/resources/twitter100.txt";
        FileInputStream inputStream = new FileInputStream("/Users/sunshuhan/Documents/mum/course/BDT/BDT-spark/SparkSqlProject/src/main/resources/twitter100.json");
        //BufferedReader是可以按行读取文件
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        FileWriter writer = new FileWriter(output, true);
        String str = null;
        while ((str = bufferedReader.readLine()) != null) {
            String s = twitterJson2String(str);
            if (s != null && s.length() != 0) {
                writer.write(s + "\n");
            }
        }

        //close
        inputStream.close();
        bufferedReader.close();
        writer.close();

    }
}
