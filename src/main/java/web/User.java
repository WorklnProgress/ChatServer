package web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.vertx.core.json.JsonObject;
import utils.JsonMapper;

/**
 * Created by priye on 5/20/17.
 */
public class User {

    @JsonProperty(required = false) int id;

    @JsonProperty(required = true) String name;

    @JsonProperty(required = true) String pwd;

    public User(){
        this.id = -1;
    }
    public User(String name, String pwd) {
        this.name = name;
        this.pwd = pwd;
        this.id = -1;
    }

    public User(int id, String name, String pwd) {
        this.id = id;
        this.name = name;
        this.pwd = pwd;
    }
    public User(JsonObject json){
        this.name = json.getString("USERNAME");
        this.pwd = json.getString("PWD");
        this.id = json.getInteger("USERID");
    }
}
