import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PolicyFileFuzzer {
    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        byte[] content = data.consumeRemainingAsBytes();
        try {
            Files.write(Paths.get("policy.txt"), content);
        } catch (IOException e) {
            return;
        }
        DataStore store = new DataStore();
    }
}
