import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ShipTrackTests {

    // ShipTrack reads/writes real files and uses System.in/out, so back them up and restore.
    private String[] files = {"users.txt", "shipments.txt", "policy.txt", "audit.txt"};
    private Map<String, byte[]> backup = new HashMap<String, byte[]>();
    private InputStream origIn;
    private PrintStream origOut;

    @Before
    public void setUp() throws Exception {
        for (String f : files) {
            File file = new File(f);
            if (file.exists()) {
                backup.put(f, Files.readAllBytes(file.toPath()));
                file.delete();
            }
        }
        origIn = System.in;
        origOut = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        ShipTrack.userIdx = -1;
        ShipTrack.userId = "";
        ShipTrack.role = "";
    }

    @After
    public void tearDown() throws Exception {
        System.setIn(origIn);
        System.setOut(origOut);
        ShipTrack.in = new Scanner(System.in);
        for (String f : files) new File(f).delete();
        for (Map.Entry<String, byte[]> e : backup.entrySet()) Files.write(Paths.get(e.getKey()), e.getValue());
    }

    private void feed(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ShipTrack.in = new Scanner(System.in);
    }

    private void writeFile(String path, String content) throws Exception {
        FileWriter w = new FileWriter(path);
        w.write(content);
        w.close();
    }

    @Test
    public void test1() {
        String[] row = ShipTrack.newUserRow("driver", "42", "Bob", "555", "Secret1!");
        assertEquals(7, row.length);
        assertEquals("driver", row[0]);
        assertEquals("42", row[1]);
        assertEquals("Bob", row[2]);
        assertEquals("555", row[3]);
        assertEquals(SecurityUtil.hash("Secret1!"), row[4]);
        assertEquals("0", row[5]);
        assertEquals("0", row[6]);
    }

    @Test
    public void test2() {
        feed("\nhello\n");
        assertEquals("hello", ShipTrack.ask("prompt: "));
    }

    @Test
    public void test3() {
        feed("a,b\nhello\n");
        assertEquals("hello", ShipTrack.ask("prompt: "));
    }

    @Test
    public void test4() {
        feed("abc\n42\n");
        assertEquals(42, ShipTrack.askInt("prompt: "));
    }

    @Test
    public void test5() throws Exception {
        String pwHash = SecurityUtil.hash("Pa$$w0rd1");
        writeFile("users.txt", "admin,0,admin,555," + pwHash + ",0,0\n");
        ShipTrack.store = new DataStore();
        feed("0\nPa$$w0rd1\n");
        int idx = ShipTrack.doLogin("admin");
        assertTrue(idx >= 0);
    }

    @Test
    public void test6() throws Exception {
        String pwHash = SecurityUtil.hash("Pa$$w0rd1");
        writeFile("users.txt", "admin,0,admin,555," + pwHash + ",0,0\n");
        ShipTrack.store = new DataStore();
        feed("0\nwrongpw\n");
        int idx = ShipTrack.doLogin("admin");
        assertEquals(-1, idx);
        int userIdx = ShipTrack.store.findUser("admin", "0");
        assertEquals("1", ShipTrack.store.users.get(userIdx)[6]);
    }

    @Test
    public void test7() throws Exception {
        String pwHash = SecurityUtil.hash("Pa$$w0rd1");
        writeFile("users.txt", "admin,0,admin,555," + pwHash + ",0,0\n");
        ShipTrack.store = new DataStore();
        ShipTrack.store.policy[5] = 2;
        feed("0\nwrong\n0\nwrong\n");
        ShipTrack.doLogin("admin");
        ShipTrack.doLogin("admin");
        int userIdx = ShipTrack.store.findUser("admin", "0");
        assertEquals("1", ShipTrack.store.users.get(userIdx)[5]);
    }

    @Test
    public void test8() throws Exception {
        String pwHash = SecurityUtil.hash("Pa$$w0rd1");
        writeFile("users.txt", "admin,0,admin,555," + pwHash + ",1,0\n");
        ShipTrack.store = new DataStore();
        feed("0\nPa$$w0rd1\n");
        int idx = ShipTrack.doLogin("admin");
        assertEquals(-1, idx);
    }

    @Test
    public void test9() throws Exception {
        String pwHash = SecurityUtil.hash("Pa$$w0rd1");
        writeFile("users.txt", "admin,0,admin,555," + pwHash + ",0,0\n");
        ShipTrack.store = new DataStore();
        feed("9\nPa$$w0rd1\n");
        int idx = ShipTrack.doLogin("admin");
        assertEquals(-1, idx);
    }
}
