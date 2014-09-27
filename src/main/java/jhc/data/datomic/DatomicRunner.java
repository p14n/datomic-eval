package jhc.data.datomic;

import clojure.lang.*;
import datomic.Connection;
import datomic.ListenableFuture;
import datomic.Peer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by p14n on 03/09/2014.
 */
public class DatomicRunner {


    public static void main(String args[]) throws Exception {
        String uri = "datomic:mem://eval";//"datomic:sql://?jdbc:oracle:thin:datomic/datomic@192.168.56.2:1521:XE";
        Peer.createDatabase(uri);
        final Connection conn = Peer.connect(uri);
        List l = (List) datomic.Util.readAll(new FileReader("datomic-mod/src/main/resources/dashboard.edn")).get(0);
        conn.transact(l).get();

        String json = Util.streamToString(new FileInputStream("datomic-mod/src/main/resources/dashboard.json"));
        IPersistentVector inserts = ConvertToEdn.convert(new JsonArray(json), "dashboard");
        ListenableFuture f = conn.transactAsync((List)inserts);
        f.get();

        //PersistentVector[PersistenArrayMap{MapEntry}]

        Collection<List<Object>> res = Peer.q("[:find ?w :where [?w :dashboard/title \"Investment Manager\"]]", conn.db());

        for(Long id : ids(res)){
            JsonObject ja = ConvertToVert.convertToVert(conn.db().entity(id));
            System.out.print(ja.encodePrettily());
        }
        conn.release();
        Peer.shutdown(true);
    }

    private static Iterable<? extends Long> ids(Collection<List<Object>> res) {
        List<Long> ids = new ArrayList<>();
        Iterator<List<Object>> a = res.iterator();
        if(a.hasNext()){
            List<Object> b = a.next();
            for(Object c:b){
                if(c instanceof Long)
                    ids.add((Long)c);
            }
        }
        return ids;
    }


}
