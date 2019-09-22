package bean;

import java.io.Serializable;

public class Twitter implements Serializable {
    private String id_str;
    private String created_at;
//    private String place;
private int favorite_count;
    private User user;
    private String text;
    private Entities entities;

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getId_str() {
        return id_str;
    }

    public void setId_str(String id_str) {
        this.id_str = id_str;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Entities getEntities() {
        return entities;
    }

    public void setEntities(Entities entities) {
        this.entities = entities;
    }

    public int getFavorite_count() {
        return favorite_count;
    }

    public void setFavorite_count(int favorite_count) {
        this.favorite_count = favorite_count;
    }

    @Override
    public String toString() {
        return "Twitter{" +
                "created_at='" + created_at + '\'' +
                ", id_str='" + id_str + '\'' +
                ", text='" + text + '\'' +
                ", user=" + user +
                ", entities=" + entities +
                ", favorite_count=" + favorite_count +
                '}';
    }
}
