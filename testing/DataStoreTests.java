import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStoreTests {

    // DataStore reads/writes real files, so back them up before each test and restore after.
    private String[] files = {"users.txt", "shipments.txt", "policy.txt", "audit.txt"};
    private Map<String, byte[]> backup = new HashMap<String, byte[]>();

    @Before
    public void setUp() throws Exception {
        for (String f : files) {
            File file = new File(f);
            if (file.exists()) {
                backup.put(f, Files.readAllBytes(file.toPath()));
                file.delete();
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        for (String f : files) new File(f).delete();
        for (Map.Entry<String, byte[]> e : backup.entrySet()) Files.write(Paths.get(e.getKey()), e.getValue());
    }

    private void writeFile(String path, String content) throws Exception {
        FileWriter w = new FileWriter(path);
        w.write(content);
        w.close();
    }

    private String[] adminRow() {
        return new String[]{"admin", "0", "admin", "12345678", "deadbeef", "0", "0"};
    }

    @Test
    public void test1() throws Exception {
        writeFile("users.txt", "admin,0,admin,12345678,deadbeef,0,0\n");
        DataStore ds = new DataStore();
        assertEquals(0, ds.findUser("admin", "0"));
    }

    @Test
    public void test2() throws Exception {
        writeFile("users.txt", "admin,0,admin,12345678,deadbeef,0,0\n");
        DataStore ds = new DataStore();
        assertEquals(-1, ds.findUser("customer", "0"));
    }

    @Test
    public void test3() throws Exception {
        DataStore ds = new DataStore();
        assertEquals(-1, ds.findUser("admin", "99"));
    }

    @Test
    public void test4() throws Exception {
        writeFile("users.txt", "admin,0,admin,12345678,deadbeef,0,0\n");
        DataStore ds = new DataStore();
        assertEquals(0, ds.findUserById("0"));
    }

    @Test
    public void test5() throws Exception {
        DataStore ds = new DataStore();
        assertEquals(-1, ds.findUserById("0"));
    }

    @Test
    public void test6() throws Exception {
        writeFile("users.txt", "admin,0,admin,12345678,deadbeef,0,0\n");
        DataStore ds = new DataStore();
        assertTrue(ds.hasAdmin());
    }

    @Test
    public void test7() throws Exception {
        writeFile("users.txt", "customer,1,Alice,555,hash,0,0\n");
        DataStore ds = new DataStore();
        assertFalse(ds.hasAdmin());
    }

    @Test
    public void test8() throws Exception {
        writeFile("shipments.txt", "1,0,A,B,stuff,pending,-\n");
        DataStore ds = new DataStore();
        assertEquals(0, ds.findShipment("1"));
    }

    @Test
    public void test9() throws Exception {
        DataStore ds = new DataStore();
        assertEquals(-1, ds.findShipment("404"));
    }

    @Test
    public void test10() throws Exception {
        DataStore ds = new DataStore();
        assertEquals("1", ds.nextShipmentId());
    }

    @Test
    public void test11() throws Exception {
        writeFile("shipments.txt", "1,0,A,B,x,pending,-\n3,0,A,B,x,pending,-\n5,0,A,B,x,pending,-\n");
        DataStore ds = new DataStore();
        assertEquals("6", ds.nextShipmentId());
    }

    @Test
    public void test12() throws Exception {
        writeFile("shipments.txt", "abc,0,A,B,x,pending,-\n2,0,A,B,x,pending,-\nx,0,A,B,x,pending,-\n");
        DataStore ds = new DataStore();
        assertEquals("3", ds.nextShipmentId());
    }

    @Test
    public void test13() throws Exception {
        DataStore ds = new DataStore();
        ds.users.add(adminRow());
        ds.users.add(new String[]{"driver", "7", "Bob", "5551", "hash7", "0", "0"});
        ds.saveUsers();
        ds.users.clear();
        ds.loadUsers();
        assertEquals(2, ds.users.size());
        assertArrayEquals(adminRow(), ds.users.get(0));
        assertArrayEquals(new String[]{"driver", "7", "Bob", "5551", "hash7", "0", "0"}, ds.users.get(1));
    }

    @Test
    public void test14() throws Exception {
        DataStore ds = new DataStore();
        ds.policy[0] = 12;
        ds.policy[1] = 0;
        ds.policy[2] = 1;
        ds.policy[3] = 0;
        ds.policy[4] = 1;
        ds.policy[5] = 5;
        ds.savePolicy();
        DataStore ds2 = new DataStore();
        assertArrayEquals(new int[]{12, 0, 1, 0, 1, 5}, ds2.policy);
    }

    @Test
    public void test15() throws Exception {
        DataStore ds = new DataStore();
        int before = ds.auditLog.size();
        ds.appendAudit("0", "did something");
        assertEquals(before + 1, ds.auditLog.size());
        assertTrue(ds.auditLog.get(ds.auditLog.size() - 1).contains(",0,did something"));
        List<String> lines = Files.readAllLines(Paths.get("audit.txt"));
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains(",0,did something"));
    }

    @Test
    public void test16() throws Exception {
        writeFile("policy.txt", "a,b,c,d,e,f\n");
        DataStore ds = new DataStore();
        assertArrayEquals(new int[]{8, 1, 1, 1, 1, 3}, ds.policy);
    }

    @Test
    public void test17() throws Exception {
        writeFile("policy.txt", "8,1,1,1,1\n");
        DataStore ds = new DataStore();
        assertArrayEquals(new int[]{8, 1, 1, 1, 1, 3}, ds.policy);
    }

    @Test
    public void test18() throws Exception {
        writeFile("shipments.txt", "x,0,A,B,x,pending,-\ny,0,A,B,x,pending,-\nz,0,A,B,x,pending,-\n");
        DataStore ds = new DataStore();
        assertEquals("1", ds.nextShipmentId());
    }

    @Test
    public void test19() throws Exception {
        DataStore ds = new DataStore();
        ds.shipments.add(new String[]{"1", "0", "A", "B", "stuff", "pending", "-"});
        ds.saveShipments();
        ds.shipments.clear();
        ds.loadShipments();
        assertEquals(1, ds.shipments.size());
        assertArrayEquals(new String[]{"1", "0", "A", "B", "stuff", "pending", "-"}, ds.shipments.get(0));
    }
}
