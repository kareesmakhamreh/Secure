import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Crash_e7064f0b80f61dbc65915311032d27baa569ae2a {
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdXIAAltCrPMX+AYIVOACAAB4cAAAAAEpeA==");

    public static void main(String[] args) throws Throwable {
        Crash_e7064f0b80f61dbc65915311032d27baa569ae2a.class.getClassLoader().setDefaultAssertionStatus(true);
        try {
            Method fuzzerInitialize = UsersFileFuzzer.class.getMethod("fuzzerInitialize");
            fuzzerInitialize.invoke(null);
        } catch (NoSuchMethodException ignored) {
            try {
                Method fuzzerInitialize = UsersFileFuzzer.class.getMethod("fuzzerInitialize", String[].class);
                fuzzerInitialize.invoke(null, (Object) args);
            } catch (NoSuchMethodException ignored1) {
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.exit(1);
        }
        com.code_intelligence.jazzer.api.CannedFuzzedDataProvider input = new com.code_intelligence.jazzer.api.CannedFuzzedDataProvider(base64Bytes);
        UsersFileFuzzer.fuzzerTestOneInput(input);
    }
}