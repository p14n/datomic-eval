package jhc.data.datomic;

import clojure.lang.IPersistentVector;
import org.junit.Test;
import org.vertx.java.core.json.JsonArray;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by p14n on 25/09/2014.
 */
public class StructureTest {
    @Test
    public void shouldConvertJsonDocIntoClojureObjects() throws Exception {
        String json = Util.streamToString(new FileInputStream("datomic-mod/src/main/resources/dashboard.json"));
        IPersistentVector edn = ConvertToEdn.convert(new JsonArray(json),"dashboard");
        edn = ConvertToEdn.bigIntFix(edn);

        assert (edn.length() == 7);
        assert (((Map) edn.nth(0)).size() == 2);
        assert (((Map) edn.nth(1)).size() == 4);
        assert (((Map) edn.nth(2)).size() == 3);
        assert (((Map) edn.nth(3)).size() == 2);
        assert (((Map) edn.nth(4)).size() == 3);
        assert (((Map) edn.nth(5)).size() == 2);
        assert (((Map) edn.nth(6)).size() == 4);
        System.out.print(edn);
    }

}
