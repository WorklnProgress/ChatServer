package web;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;

/**
 * Created by priye on 5/20/17.
 */
public class Message {

    @JsonProperty(required = true)  int id;

    @JsonProperty(required = true)  String message;

    public Message(String message) {
        this.message = message;
        this.id = -1;
    }

    public Message(int id, String message) {
        this.id = id;
        this.message = message;
    }
}