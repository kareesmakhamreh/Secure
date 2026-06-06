import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UsersFileFuzzer {
    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        byte[] content = data.consumeRemainingAsBytes();
        try {
            Files.write(Paths.get("users.txt"), content);
        } catch (IOException e) {
            return;
        }
        DataStore store = new DataStore();
        for (String[] u : store.users) {
            String role = u[0], id = u[1], hash = u[4], locked = u[5], attempts = u[6];
        }
    }
}
