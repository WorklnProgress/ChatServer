package web;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import utils.JsonMapper;


/**
 * Created by priye on 5/20/17.
 */
public class Message {

    public enum Type{
        TEXT,
        IMAGE,
        VIDEO
    }

    @JsonProperty(required = false)  int id;
    @JsonProperty(required = true)  String message; //contains text, image link or video link
    @JsonProperty(required = false)  Type type; //image, text, video
    @JsonProperty(required = false) int imgHeight;
    @JsonProperty(required = false) int imgLength;
    @JsonProperty(required = false) String videoSource;
    @JsonProperty(required = false) int videoLength;

    public Message(){
        this.message ="";
        this.type = Type.TEXT;
    }
    public Message(String message) {
        this.id = -1;
        this.message = message;
        this.type = Type.TEXT;
    }

    public Message(int id, String message) {
        this.id = id;
        this.message = message;
        this.type = Type.TEXT;
    }

    public Message(String message, String type) {
        this.id = -1;
        this.message = message;
        this.type = Type.valueOf(type);
    }

    public Message(int id, String message, String type) {
        this.id = id;
        this.message = message;
        this.type = Type.valueOf(type);
    }

    public Message(int id, String message, String type, int imgHeight, int imgLength, String videoSource, int videoLength) {
        this.id = id;
        this.message = message;
        this.type = Type.valueOf(type);
        this.imgHeight = imgHeight;
        this.imgLength = imgLength;
        this.videoLength = videoLength;
        this.videoSource = videoSource;
    }

    public Message(JsonObject message) {
        JsonMapper.read(message.toString(), Message.class);
    }
}