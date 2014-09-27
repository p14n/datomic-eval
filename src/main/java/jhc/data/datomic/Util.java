package jhc.data.datomic;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;

public class Util {

    public static String streamToString(InputStream stream) throws IOException {
        return new String(ByteStreams.toByteArray(stream), "utf-8");
    }

}