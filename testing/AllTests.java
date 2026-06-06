import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    SecurityUtilTests.class,
    DataStoreTests.class,
    ShipTrackTests.class
})
public class AllTests {
}
