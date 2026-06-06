import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Crash_c93d958878f48e29765c96917f63d5489072cab1 {
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdXIAAltCrPMX+AYIVOACAAB4cAAAAAIhCng=");

    public static void main(String[] args) throws Throwable {
        Crash_c93d958878f48e29765c96917f63d5489072cab1.class.getClassLoader().setDefaultAssertionStatus(true);
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