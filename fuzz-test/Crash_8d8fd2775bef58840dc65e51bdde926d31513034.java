import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Crash_8d8fd2775bef58840dc65e51bdde926d31513034 {
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdXIAAltCrPMX+AYIVOACAAB4cAAAAALCCng=");

    public static void main(String[] args) throws Throwable {
        Crash_8d8fd2775bef58840dc65e51bdde926d31513034.class.getClassLoader().setDefaultAssertionStatus(true);
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