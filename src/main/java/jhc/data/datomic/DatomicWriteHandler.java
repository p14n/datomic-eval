package jhc.data.datomic;

import clojure.lang.IPersistentVector;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by p14n on 26/09/2014.
 */
public class DatomicWriteHandler implements Handler<Message<JsonObject>> {
    @Override
    public void handle(Message<JsonObject> event) {
        JsonObject jo = event.body();
        String namespace = jo.getString("namespace");
        IPersistentVector edn = ConvertToEdn.convert(jo,namespace);


    }
}
