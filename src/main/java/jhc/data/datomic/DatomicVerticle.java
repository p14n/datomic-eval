package jhc.data.datomic;

import datomic.Connection;
import datomic.ListenableFuture;
import datomic.Peer;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.platform.Verticle;

import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DatomicVerticle extends Verticle {

    @Override
    public void start() {
        String uri = "datomic:mem://seattle";
        Peer.createDatabase(uri);
        final Connection conn = Peer.connect(uri);

        getVertx().eventBus().registerHandler("ds-write", new Handler<Message<JsonArray>>() {
            @Override
            public void handle(Message<JsonArray> event) {

                /*JsonArray inserts = ConvertToEdn.convertVertToEdnObjects(new AtomicInteger(-1000000), "dashboard", event.body());
                inserts = ConvertToEdn.flatten(inserts, new JsonArray());
                inserts = ConvertToEdn.replaceMapsWithRefs(inserts);
                String convertedJson = ConvertToEdn.encode(inserts);
                System.out.println(convertedJson);
                List edn = (List) datomic.Util.readAll(new StringReader(convertedJson)).get(0);
                edn = ConvertToEdn.bigIntFix(edn);
                ListenableFuture f = conn.transactAsync(edn);
                try {
                    event.reply(f.get());
                } catch (Exception e) {
                    event.fail(0, e.getMessage());
                }*/
            }
        });

        getVertx().eventBus().registerHandler("ds-read", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> event) {
                try {
                    Collection<List<Object>> results = Peer.q(event.body(), conn.db());
                    //event.reply(Convert.convertFromDB(results));
                } catch (Exception e) {
                    event.fail(0, e.getMessage());
                }
            }
        });
    }
}