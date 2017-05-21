package utils;

import io.vertx.core.json.Json;

/**
 * Created by priye on 5/20/17.
 */
public class JsonMapper {

    public static <T> T read(String s, Class<T> cfgType) { return Json.decodeValue(s, cfgType); }

    public static String write(Object o) { return Json.encode(o); }
}
