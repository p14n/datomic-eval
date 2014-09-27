package jhc.data.datomic;

import datomic.Connection;
import datomic.Entity;
import datomic.Peer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vertx.java.core.json.JsonArray;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertEquals;

/**
 * Created by p14n on 28/08/2014.
 */
public class ConvertTest {

    static Connection c;

    @BeforeClass
    public static void setupDB() throws Exception {
        String uri = "datomic:mem://eval";
        Peer.createDatabase(uri);
        c = Peer.connect(uri);
        List v = readList("json-schema.edn");
        c.transact(v).get();
        v = readList("json-data.edn");
        c.transact(v).get();
    }

    private static List readList(String file) throws IOException {
        return (List) datomic.Util.readAll(new StringReader(Util.streamToString(Util.class.getClassLoader().getResourceAsStream(file)))).get(0);
    }

    @Test
    public void shouldGetWidgetValues() {
        List<Object> ids = Peer.q("[:find ?w :where [?w :widget/user \"Brendan\"]]", c.db()).iterator().next();
        JsonArray a = new JsonArray();
        for (Object id : ids) {
            Entity e = c.db().entity(id);
            e = e.touch();
            a.add(ConvertToVert.convertToVert(e));
        }
        System.out.println(a.encodePrettily());
    }


    /*public static void main(String[] args) throws IOException {
        JsonArray j = Convert.ednToJson(Convert.class.getClassLoader().getResourceAsStream("json-schema.edn"));
        System.out.print(j.encodePrettily());
        Files.write(j.encodePrettily().getBytes("utf-8"),new File("datomic-mod/src/main/resources/json-schema.json"));

    }*/


    @Test
    public void shouldConvertFromJsonToEDNMap() throws IOException {
        String json = Util.streamToString(Util.class.getClassLoader().getResourceAsStream("json-schema.json"));
        //JsonArray l = ConvertToEdn.convertVertToEdnObjects(new AtomicInteger(-1000000), "", new JsonArray(json));
        //assertEquals(6, l.size());
        /*assertEquals(6, l.get(0).size());
        assertEquals(6, l.get(1).size());
        assertEquals(7, l.get(2).size());
        assertEquals(6, l.get(3).size());
        assertEquals(6, l.get(4).size());
        assertEquals(6, l.get(5).size());

        assertEquals("#db/id[:db.part/db -1000000]", l.get(0).get(":db/id"));*/

    }
}
