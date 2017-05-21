package web;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.joda.time.DateTime;
import utils.JsonMapper;

import java.sql.Time;
import java.sql.Timestamp;

/**
 * Created by priye on 5/19/17.
 */
public class Chat {

    @JsonProperty(required = true) int chatId;
    @JsonProperty(required = true)  String senderId;
    @JsonProperty(required = true)  String receiverId;
    @JsonProperty(required = true)  Message message;
    @JsonProperty(required = false)
    Timestamp timeStamp;

    public Chat(){
        this.chatId = -1;
    }

    public Chat(String receiverId, String senderId, Message message) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.message = message;
        this.chatId = -1;
    }

    public Chat(String receiverId, String senderId, Message message, Timestamp timeStamp) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.message = message;
        this.timeStamp = timeStamp;
        this.chatId = -1;
    }

    public Chat(int chatId, String receiverId, String senderId, Message message, Timestamp timeStamp) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.message = message;
        this.timeStamp = timeStamp;
        this.chatId = chatId;
    }

    public Chat(JsonObject json){
        this.chatId = -1;
        this.senderId = json.getString("SENDER");
        this.receiverId = json.getString("RECEIVER");
        String tempMessage = json.getString("MESSAGE");
        Message message = new Message(tempMessage);
        this.message = message;
    }



}
