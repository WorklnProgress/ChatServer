package web;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.core.logging.Logger;
import utils.JsonMapper;
import utils.Password;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by priye on 5/13/17.
 */
public class WebVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(WebVerticle.class);

    //config with ports
    AsyncSQLClient wcSql;
    JsonObject lconfig;

    @Override
    public void start(Future<Void> fut) {

        lconfig = config();
        // Create a JDBC client
        JsonObject sqlConfig = new JsonObject().put("username", "root").put("password", "123ublong2me")
                .put("queryTimeout", 10000);
        wcSql = MySQLClient.createShared(vertx, sqlConfig, "ChatStore");

        startBackend(
                (connection) -> startWebApp(
                                (http) -> completeStartup(http, fut)
                        ), fut
                );
    }

    private void startBackend(Handler<AsyncResult<SQLConnection>> next, Future<Void> fut) {
        wcSql.getConnection(ar -> {
            if (ar.failed()) {
                fut.fail(ar.cause());
            } else {
                next.handle(Future.succeededFuture(ar.result()));
            }
        });
    }

    private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
        // Create a router object.
        Router router = Router.router(vertx);

        // Bind "/" to our hello message.
        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response
                    .putHeader("content-type", "text/html")
                    .end("<h1>Hello</h1>");
        });

        router.route("/chat/*").handler(StaticHandler.create("chat"));
        router.get("/api/getUsers").handler(this::getAll);
        router.route("/api/users*").handler(BodyHandler.create());
        router.post("/api/users/signIn").handler(this::addUser);
        router.post("/api/users/sendMessage").handler(this::addChat);
        router.get("/api/users/fetchMessage").handler(this::fetchMessages);

        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        // Retrieve the port from the configuration,
                        // default to 8080.
                        lconfig.getInteger("http.port", 8080),
                        next::handle
                );
    }

    private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
        if (http.succeeded()) {
            fut.complete();
        } else {
            fut.fail(http.cause());
        }
    }


    @Override
    public void stop() throws Exception {
        // Close the JDBC client.
        wcSql.close();
    }

    private void addUser(RoutingContext routingContext) {
        wcSql.getConnection(ar -> {
            // Read the request's content and create an instance of User.
            final User user = JsonMapper.read(routingContext.getBodyAsString(), User.class);
            user.pwd = Password.hashPassword(user.pwd);
            SQLConnection connection = ar.result();
            insertUser(user, connection, (r) ->
                    routingContext.response()
                            .setStatusCode(201)
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(Json.encodePrettily(r.result())));
            connection.close();
        });

    }

    private void insertUser(User user, SQLConnection connection, Handler<AsyncResult<User>> next) {
        String sql = "INSERT INTO chatserver.Users (USERNAME, PWD) VALUES (?, ?)";
        connection.updateWithParams(sql,
                new JsonArray().add(user.name).add(user.pwd),
                (ar) -> {
                    if (ar.failed()) {
                        next.handle(Future.failedFuture(ar.cause()));
                        connection.close();
                        return;
                    }
                    UpdateResult result = ar.result();
                    // Build a new user instance with the generated id.
                    User u = new User(result.getKeys().getInteger(0), user.name, user.pwd);
                    next.handle(Future.succeededFuture(u));
                });
    }


    private void addChat(RoutingContext routingContext) {
        wcSql.getConnection(ar -> {

            // Read the request's content and create an instance of Chat.
            final Chat chat = JsonMapper.read(routingContext.getBodyAsString(), Chat.class);
            //hardcode the image height and length
            if (chat.message.type == Message.Type.IMAGE){
                chat.message.imgHeight = 10;
                chat.message.imgLength = 20;
            }
            //hardcode the video source and length
            else if (chat.message.type == Message.Type.VIDEO){
                chat.message.videoSource = "VIMEO";
                chat.message.videoLength = 100;
            }
            java.util.Date date = new java.util.Date();
            chat.timeStamp = new java.sql.Timestamp(date.getTime());
            SQLConnection connection = ar.result();
            insertChat(chat, connection, (r) ->
                    routingContext.response()
                            .setStatusCode(201)
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(Json.encodePrettily(r.result())));
            connection.close();
        });

    }


    private void insertChat(Chat chat, SQLConnection connection, Handler<AsyncResult<Chat>> next) {
        String sql = "INSERT INTO chatserver.CHAT (SENDERID, RECEIVERID, MESSAGE, TIMESTAMP) VALUES " +
                "((select userid from chatserver.users where username = ? limit 1)," +
                "(select userid from chatserver.users where username = ? limit 1)," +
                "?, CURRENT_TIMESTAMP())";
        connection.updateWithParams(sql,
                new JsonArray().add(chat.senderId).add(chat.receiverId).add(JsonMapper.write(chat.message).toString()),
                (ar) -> {
                    if (ar.failed()) {
                        next.handle(Future.failedFuture(ar.cause()));
                        connection.close();
                        return;
                    }
                    UpdateResult result = ar.result();
                    // Build a new chat instance
                    Chat c = new Chat(result.getKeys().getInteger(0), chat.senderId, chat.receiverId, chat.message, chat.timeStamp);
                    next.handle(Future.succeededFuture(c));
                });
    }


    private void fetchMessages(RoutingContext routingContext) {
        MultiMap params = routingContext.request().params();
        final String senderId = params.get("senderId");
        final String receiverId = params.get("receiverId");
        final int numOfMessages = params.contains("numOfMessages") ? Integer.parseInt(params.get("numOfMessages")) : 0;
        final int pageNum = params.contains("pageNum") ? Integer.parseInt(params.get("pageNum")) : 0;
        if (senderId == null || receiverId == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
            wcSql.getConnection(ar -> {
                SQLConnection connection = ar.result();
                select(senderId, receiverId, numOfMessages, pageNum, connection, result -> {
                    if (result.succeeded()) {
                        routingContext.response()
                                .setStatusCode(200)
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(Json.encodePrettily(result.result()));
                    } else {
                        routingContext.response()
                                .setStatusCode(404).end();
                    }
                    connection.close();
                });
            });
        }
    }

    private void select(String senderId, String receiverId, int numOfMessages, int pageNum, SQLConnection connection, Handler<AsyncResult<List<Chat>>> resultHandler) {
        String sqlQuery = "Select s.UserName as SENDER, r.UserName as RECEIVER, MESSAGE from " +
                "chatserver.chat c " +
                "inner join chatserver.users s " +
                "inner join chatserver.users r " +
                "on c.SENDERID = s.USERID and c.RECEIVERID = r.USERID " +
                "where s.username in (?, ?) and r.username in (?, ?) " +
                "order by timestamp asc";
        JsonArray params = new JsonArray().add(senderId).add(receiverId).add(receiverId).add(senderId);

        if (numOfMessages != 0 && pageNum != 0) {
            int startRowNum = numOfMessages * ( pageNum - 1 ) + 1;
            int endRowNum = numOfMessages * pageNum;
            sqlQuery = "Select SENDER, RECEIVER, MESSAGE from " +
                    "(Select (@row_number:=@row_number + 1) AS num, s.UserName as Sender, r.UserName as Receiver, message from " +
                    "(SELECT @row_number:=0) AS cnt " +
                    "inner join chatserver.chat c " +
                    "inner join chatserver.users s " +
                    "inner join chatserver.users r " +
                    "on c.SENDERID = s.USERID and c.RECEIVERID = r.USERID " +
                    "where s.username in (?, ?) and r.username in (?, ?) " +
                    "order by timestamp asc) AS Subset " +
                    "where SUBSET.num between ? and ?";
            params.add(startRowNum).add(endRowNum);
        }

        connection.queryWithParams(sqlQuery, params, ar -> {
            if (ar.succeeded()) {
                if (ar.result().getNumRows() >= 1) {
                    List<Chat> chats = ar.result().getRows().stream().map(Chat::new).collect(Collectors.toList());
                    resultHandler.handle(Future.succeededFuture(chats));
                } else {
                    logger.info("failed");
                    resultHandler.handle(Future.failedFuture("Chat not found"));
                }
            }
        });
    }

    private void getAll(RoutingContext routingContext) {
        wcSql.getConnection(ar -> {
            // Read the request's content and create instances of Users.
            SQLConnection connection = ar.result();
            connection.query("SELECT * FROM chatserver.Users", result -> {
                if (result.succeeded()) {
                    if (result.result().getNumRows() >= 1) {
                        List<User> users = result.result().getRows().stream().map(User::new).collect(Collectors.toList());
                        routingContext.response()
                                .setStatusCode(200)
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(Json.encodePrettily(users));
                    } else {
                        routingContext.response()
                                .setStatusCode(404).end();
                    }
                    connection.close();
                }
            });
        });
    }

}

