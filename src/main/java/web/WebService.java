package web;


import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


/**
 * Created by priye on 5/14/17.
 */
public class WebService {

    static Logger logger = LoggerFactory.getLogger(WebService.class);

    public static void main(String[] args){
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new WebVerticle());
    }
}
