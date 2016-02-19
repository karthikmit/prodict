import com.prodict.Entry;
import com.prodict.ProDict;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

/**
 * Created by karthik on 2/18/16.
 */
public class ProDictTests {

    @Test
    public void testProDictGetAndPut() {
        ProDict proDict = new ProDict(100);

        final String value = "Hello World!";
        final Entry entry = new Entry().setExpiresIn(10).setExpiresInUnit(TimeUnit.SECONDS)
                .setKey("Test-Karthik-1".toLowerCase()).setValue(value);
        proDict.put(entry);
        Entry result = proDict.get(entry.getKey());
        Assert.assertEquals(result.getValue(), value);
    }

    @Test
    public void testProDictGetAndPutBeyondCapaityAndCheckInMemoryGet() {
        ProDict proDict = new ProDict(1);

        final String value = "Hello World!";
        Entry entry = new Entry().setExpiresIn(10).setExpiresInUnit(TimeUnit.SECONDS)
                .setKey("Test-Karthik-1".toLowerCase()).setValue(value);
        proDict.put(entry);

        entry = new Entry().setExpiresIn(10).setExpiresInUnit(TimeUnit.SECONDS)
                .setKey("Test-Karthik-2".toLowerCase()).setValue(value);
        proDict.put(entry);

        Entry result = proDict.getOnlyIfInMemory("Test-Karthik-1".toLowerCase());
        Assert.assertEquals(result, null);
    }

    @Test
    public void testProDictGetAndPutBeyondCapaity() {
        ProDict proDict = new ProDict(1);

        final String value = "Hello World 123";
        Entry entry = new Entry().setExpiresIn(10).setExpiresInUnit(TimeUnit.SECONDS)
                .setKey("Test-Karthik-1".toLowerCase()).setValue(value);
        proDict.put(entry);

        entry = new Entry().setExpiresIn(10).setExpiresInUnit(TimeUnit.SECONDS)
                .setKey("Test-Karthik-2".toLowerCase()).setValue(value);
        proDict.put(entry);

        Entry result = proDict.get("Test-Karthik-1".toLowerCase());
        Assert.assertNotEquals(result, null);
    }

    @Test
    public void testProDictGetAndPutForOrder() {
        ProDict proDict = new ProDict(2);

        final String value = "Hello World 1";
        Entry entry = new Entry().setExpiresIn(10).setExpiresInUnit(TimeUnit.SECONDS)
                .setKey("Test-Karthik-1".toLowerCase()).setValue(value);
        proDict.put(entry);

        entry = new Entry().setExpiresIn(10).setExpiresInUnit(TimeUnit.SECONDS)
                .setKey("Test-Karthik-2".toLowerCase()).setValue("Hello world 2");
        proDict.put(entry);

        Deque<Entry> all = proDict.getAll();

        Assert.assertEquals(all.getFirst().getKey(), "Test-Karthik-2".toLowerCase());

        // Fetch call should rearrange the inner entries.
        proDict.get("Test-Karthik-1".toLowerCase());
        all = proDict.getAll();
        Assert.assertEquals(all.getFirst().getKey(), "Test-Karthik-1".toLowerCase());
    }

    @Test
    public void dummyReadTests() throws IOException {
        ProDict dict = new ProDict(10);

        System.out.println(dict.get("test-mother").getValue());
    }
}