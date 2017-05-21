package web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by priye on 5/14/17.
 */
public class WebPersistenceVerticle extends AbstractVerticle {


    @Override
    public void start(Future<Void> fut) {


    }


}
