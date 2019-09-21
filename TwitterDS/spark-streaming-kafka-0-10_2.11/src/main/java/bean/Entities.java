package bean;

import java.io.Serializable;
import java.util.Arrays;

public class Entities implements Serializable {
    private String[] hashtags;
//    private String[] urls;
    private String[] user_mentions;

    public String[] getHashtags() {
        return hashtags;
    }

    public void setHashtags(String[] hashtags) {
        this.hashtags = hashtags;
    }

    public String[] getUser_mentions() {
        return user_mentions;
    }

    public void setUser_mentions(String[] user_mentions) {
        this.user_mentions = user_mentions;
    }

    @Override
    public String toString() {
        return "Entities{" +
                "hashtags=" + (hashtags == null ? null : Arrays.asList(hashtags)) +
                ", user_mentions=" + (user_mentions == null ? null : Arrays.asList(user_mentions)) +
                '}';
    }
}
