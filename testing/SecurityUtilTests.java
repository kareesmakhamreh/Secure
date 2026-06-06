import org.junit.Test;
import static org.junit.Assert.*;

public class SecurityUtilTests {

    @Test
    public void test1() {
        assertEquals("ok", SecurityUtil.validate("Aa1!aaaa", 8, 1, 1, 1, 1));
    }

    @Test
    public void test2() {
        assertEquals("password must be at least 8 characters", SecurityUtil.validate("ab", 8, 0, 0, 0, 0));
    }

    @Test
    public void test3() {
        assertEquals("password must contain an uppercase letter", SecurityUtil.validate("aa1!aaaa", 8, 1, 0, 0, 0));
    }

    @Test
    public void test4() {
        assertEquals("password must contain a lowercase letter", SecurityUtil.validate("AA1!AAAA", 8, 0, 1, 0, 0));
    }

    @Test
    public void test5() {
        assertEquals("password must contain a digit", SecurityUtil.validate("Aa!aaaaa", 8, 0, 0, 1, 0));
    }

    @Test
    public void test6() {
        assertEquals("password must contain a special character", SecurityUtil.validate("Aa1aaaaa", 8, 0, 0, 0, 1));
    }

    @Test
    public void test7() {
        assertEquals("ok", SecurityUtil.validate("password", 8, 0, 0, 0, 0));
    }

    @Test
    public void test8() {
        assertEquals(SecurityUtil.hash("hello"), SecurityUtil.hash("hello"));
    }

    @Test
    public void test9() {
        assertNotEquals(SecurityUtil.hash("a"), SecurityUtil.hash("b"));
    }

    @Test
    public void test10() {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", SecurityUtil.hash(""));
    }

    @Test
    public void test11() {
        String pw = "Pa$$w0rd!";
        assertTrue(SecurityUtil.check(pw, SecurityUtil.hash(pw)));
    }

    @Test
    public void test12() {
        assertFalse(SecurityUtil.check("wrong", SecurityUtil.hash("Pa$$w0rd!")));
    }

    @Test
    public void test13() {
        assertEquals("0a", SecurityUtil.toHex(new byte[]{0x0A}));
    }

    @Test
    public void test14() {
        assertEquals("ok", SecurityUtil.validate("12345678", 8, 0, 0, 0, 0));
    }

    @Test
    public void test15() {
        assertEquals("0abc", SecurityUtil.toHex(new byte[]{0x0A, (byte) 0xBC}));
    }
}
