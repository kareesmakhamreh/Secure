# Test Plan — ShipTrack (Unit Testing)

## 1. Objective
Apply quality-based testing techniques (unit testing) to the ShipTrack secure-coding
application. Each unit test verifies one behaviour of a single method in isolation,
following the same JUnit approach used in the QA lab exercises.

## 2. Test Environment
| Item | Value |
|------|-------|
| Language | Java (JDK 25) |
| Testing framework | JUnit 4.13.2 (+ Hamcrest 1.3) |
| IDE | Visual Studio Code |
| Classes under test | `SecurityUtil`, `DataStore`, `ShipTrack` (in `final/src/`) |
| Test classes | `SecurityUtilTests`, `DataStoreTests`, `ShipTrackTests` (in `final/testing/`) |
| Test runner | `AllTests` (JUnit `@Suite`) |
| Total test cases | 43 |
| Result | **43 / 43 passed** |

## 3. Test Strategy
- **Unit level:** every test targets one method and one scenario.
- **Coverage approach:** normal (valid) cases, boundary values, and error/branch cases.
- **Isolation:** `SecurityUtil` is pure (no setup needed). `DataStore` and `ShipTrack`
  read/write data files, so `@Before`/`@After` back up and restore the real data files
  and reset `System.in`/`System.out` so tests do not affect real data and can run in any order.

---

## 4. Test Cases

### 4.1 SecurityUtilTests — password validation, hashing, hex (15 cases)

| ID | Method under test | Scenario | Input | Expected result | Actual | Status |
|----|-------------------|----------|-------|-----------------|--------|--------|
| SU-01 | `validate` | Fully compliant password | `"Aa1!aaaa", 8,1,1,1,1` | `"ok"` | `"ok"` | Pass |
| SU-02 | `validate` | Too short | `"ab", 8,0,0,0,0` | `"password must be at least 8 characters"` | same | Pass |
| SU-03 | `validate` | Missing uppercase | `"aa1!aaaa", 8,1,0,0,0` | `"password must contain an uppercase letter"` | same | Pass |
| SU-04 | `validate` | Missing lowercase | `"AA1!AAAA", 8,0,1,0,0` | `"password must contain a lowercase letter"` | same | Pass |
| SU-05 | `validate` | Missing digit | `"Aa!aaaaa", 8,0,0,1,0` | `"password must contain a digit"` | same | Pass |
| SU-06 | `validate` | Missing special char | `"Aa1aaaaa", 8,0,0,0,1` | `"password must contain a special character"` | same | Pass |
| SU-07 | `validate` | All rules disabled | `"password", 8,0,0,0,0` | `"ok"` | `"ok"` | Pass |
| SU-08 | `hash` | Same input → same hash (deterministic) | `"hello"` twice | both hashes equal | equal | Pass |
| SU-09 | `hash` | Different input → different hash | `"a"` vs `"b"` | hashes not equal | not equal | Pass |
| SU-10 | `hash` | Known SHA-256 of empty string | `""` | `e3b0c44298fc...b7852b855` | same | Pass |
| SU-11 | `check` | Correct password vs its hash | `"Pa$$w0rd!"` | `true` | `true` | Pass |
| SU-12 | `check` | Wrong password vs hash | `"wrong"` vs hash of `"Pa$$w0rd!"` | `false` | `false` | Pass |
| SU-13 | `toHex` | Single byte keeps leading zero | `{0x0A}` | `"0a"` | `"0a"` | Pass |
| SU-14 | `validate` | Boundary: length exactly = minLen | `"12345678", 8,0,0,0,0` | `"ok"` | `"ok"` | Pass |
| SU-15 | `toHex` | Multiple bytes | `{0x0A, 0xBC}` | `"0abc"` | `"0abc"` | Pass |

### 4.2 DataStoreTests — file storage, lookups, IDs, policy, audit (19 cases)

| ID | Method under test | Scenario | Input / Setup | Expected result | Actual | Status |
|----|-------------------|----------|---------------|-----------------|--------|--------|
| DS-01 | `findUser` | Matching role + id | users file with `admin,0,...` | index `0` | `0` | Pass |
| DS-02 | `findUser` | Right id, wrong role | `admin,0,...` ; search `customer,0` | `-1` | `-1` | Pass |
| DS-03 | `findUser` | Id not present | empty users | `-1` | `-1` | Pass |
| DS-04 | `findUserById` | Id present | `admin,0,...` | index `0` | `0` | Pass |
| DS-05 | `findUserById` | Id absent | empty users | `-1` | `-1` | Pass |
| DS-06 | `hasAdmin` | Admin exists | `admin,0,...` | `true` | `true` | Pass |
| DS-07 | `hasAdmin` | No admin (only customer) | `customer,1,...` | `false` | `false` | Pass |
| DS-08 | `findShipment` | Shipment id present | shipment `1,...` | index `0` | `0` | Pass |
| DS-09 | `findShipment` | Shipment id absent | no shipments | `-1` | `-1` | Pass |
| DS-10 | `nextShipmentId` | Empty list | no shipments | `"1"` | `"1"` | Pass |
| DS-11 | `nextShipmentId` | Returns max + 1 | ids `1,3,5` | `"6"` | `"6"` | Pass |
| DS-12 | `nextShipmentId` | Skips non-numeric ids | ids `abc,2,x` | `"3"` | `"3"` | Pass |
| DS-13 | `saveUsers`/`loadUsers` | Round-trip persistence | add 2 users, save, reload | 2 users, rows match | match | Pass |
| DS-14 | `savePolicy`/`loadPolicy` | Round-trip persistence | set policy, save, reload | `{12,0,1,0,1,5}` | same | Pass |
| DS-15 | `appendAudit` | Appends to memory + file | append one entry | size +1, file has line | yes | Pass |
| DS-16 | `loadPolicy` | Malformed (non-numeric) values | `a,b,c,d,e,f` | keep defaults `{8,1,1,1,1,3}` | defaults | Pass |
| DS-17 | `loadPolicy` | Wrong field count (5 not 6) | `8,1,1,1,1` | keep defaults | defaults | Pass |
| DS-18 | `nextShipmentId` | All ids non-numeric | ids `x,y,z` | `"1"` | `"1"` | Pass |
| DS-19 | `saveShipments`/`loadShipments` | Round-trip persistence | add shipment, save, reload | 1 shipment, row matches | match | Pass |

### 4.3 ShipTrackTests — input handling and login logic (9 cases)

| ID | Method under test | Scenario | Input / Setup | Expected result | Actual | Status |
|----|-------------------|----------|---------------|-----------------|--------|--------|
| ST-01 | `newUserRow` | Builds 7-field row, password hashed | `driver,42,Bob,555,Secret1!` | 7 fields, `[4]` = hash, `[5]=[6]="0"` | yes | Pass |
| ST-02 | `ask` | Rejects empty input, then accepts | `"\nhello\n"` | `"hello"` | `"hello"` | Pass |
| ST-03 | `ask` | Rejects value with comma, then accepts | `"a,b\nhello\n"` | `"hello"` | `"hello"` | Pass |
| ST-04 | `askInt` | Rejects non-numeric, then accepts number | `"abc\n42\n"` | `42` | `42` | Pass |
| ST-05 | `doLogin` | Correct credentials | valid admin, correct password | index ≥ 0 (success) | success | Pass |
| ST-06 | `doLogin` | Wrong password | valid admin, wrong password | `-1`, failed-attempts = `"1"` | yes | Pass |
| ST-07 | `doLogin` | Reaches max attempts → account locked | maxAttempts = 2, two wrong tries | lock flag = `"1"` | locked | Pass |
| ST-08 | `doLogin` | Already-locked account | locked admin, correct password | `-1` | `-1` | Pass |
| ST-09 | `doLogin` | Non-existent user id | admin exists, login id `9` | `-1` | `-1` | Pass |

---

## 5. How the Tests Were Run
- **In VS Code:** open the Testing panel (the flask/beaker icon in the left sidebar),
  or click **Run Test** above any `@Test` method / the `AllTests` class.
- **From a terminal** (project root = `final/`):
  ```
  javac -d build -cp "src:testing:testing/lib/junit-4.13.2.jar:testing/lib/hamcrest-core-1.3.jar" src/*.java testing/*.java
  java  -cp "build:testing/lib/junit-4.13.2.jar:testing/lib/hamcrest-core-1.3.jar" org.junit.runner.JUnitCore AllTests
  ```

## 6. Result Summary
| Test class | Cases | Passed | Failed |
|------------|-------|--------|--------|
| SecurityUtilTests | 15 | 15 | 0 |
| DataStoreTests | 19 | 19 | 0 |
| ShipTrackTests | 9 | 9 | 0 |
| **Total** | **43** | **43** | **0** |

All 43 unit tests passed (`OK (43 tests)`).
