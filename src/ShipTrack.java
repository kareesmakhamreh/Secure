import java.util.Scanner;

public class ShipTrack {

    static Scanner in = new Scanner(System.in);
    static DataStore store;
    static String role = "", userId = "";
    static int userIdx = -1;

    static String ask(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine();
            if (s.length() > 0 && !s.contains(",")) return s;
            System.out.println("invalid (empty or contains a comma)");
        }
    }

    static String askPassword() {
        while (true) {
            System.out.print("Enter password: ");
            String pw = in.nextLine();
            if (pw.contains(",")) { System.out.println("no commas allowed"); continue; }
            String r = SecurityUtil.validate(pw, store.policy[0], store.policy[1], store.policy[2], store.policy[3], store.policy[4]);
            if (r.equals("ok")) return pw;
            System.out.println(r);
        }
    }

    static int askInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(in.nextLine()); }
            catch (NumberFormatException e) { System.out.println("please enter a number"); }
        }
    }

    static String[] newUserRow(String r, String id, String name, String contact, String pw) {
        return new String[]{r, id, name, contact, SecurityUtil.hash(pw), "0", "0"};
    }

    static boolean confirm(String summary) {
        System.out.println("\n---------- audit log ----------");
        if (store.auditLog.size() == 0) System.out.println("(no actions yet)");
        else for (String a : store.auditLog) System.out.println(a);
        System.out.println("-------------------------------");
        System.out.print("Execute '" + summary + "'? (yes/no): ");
        return in.nextLine().equalsIgnoreCase("yes");
    }

    static int doLogin(String r) {
        System.out.print("Enter id: ");
        String id = in.nextLine();
        System.out.print("Enter password: ");
        String pw = in.nextLine();
        int idx = store.findUser(r, id);
        if (idx < 0) { System.out.println("no such " + r + " account"); return -1; }
        String[] u = store.users.get(idx);
        if (u[5].equals("1")) { System.out.println("account is locked, contact admin"); return -1; }
        if (SecurityUtil.check(pw, u[4])) {
            u[6] = "0"; store.saveUsers();
            System.out.println("login successful");
            return idx;
        }
        int a = Integer.parseInt(u[6]) + 1;
        u[6] = Integer.toString(a);
        if (a >= store.policy[5]) { u[5] = "1"; System.out.println("wrong password, account is now locked"); }
        else System.out.println("wrong password, " + (store.policy[5] - a) + " attempt(s) left");
        store.saveUsers();
        return -1;
    }

    static String tryLogin(String r) {
        int i = doLogin(r);
        if (i < 0) return "main";
        userIdx = i; userId = store.users.get(i)[1]; role = r;
        return r;
    }

    static void listShipments(String filter, String value) {
        boolean any = false;
        for (String[] s : store.shipments) {
            boolean ok = (filter.equals("customer") && s[1].equals(value))
                      || (filter.equals("driver") && s[6].equals(value))
                      || (filter.equals("unassigned") && s[6].equals("-"));
            if (ok) {
                System.out.println("id=" + s[0] + " from=" + s[2] + " to=" + s[3] + " status=" + s[5] + " driver=" + s[6]);
                any = true;
            }
        }
        if (!any) System.out.println("(none)");
    }

    static void changeStatus(String driverIdOrNull) {
        String sid = ask("Enter shipment id: ");
        int idx = store.findShipment(sid);
        if (idx < 0) { System.out.println("no such shipment"); return; }
        String[] s = store.shipments.get(idx);
        if (driverIdOrNull != null && !s[6].equals(driverIdOrNull)) { System.out.println("this shipment is not assigned to you"); return; }
        System.out.println("current status: " + s[5]);
        System.out.println("1. pending  2. in transit  3. delivered");
        System.out.print("Choose new status: ");
        String c = in.nextLine();
        if (c.equals("1")) s[5] = "pending";
        else if (c.equals("2")) s[5] = "in transit";
        else if (c.equals("3")) s[5] = "delivered";
        else { System.out.println("not a valid choice"); return; }
        store.saveShipments();
        System.out.println("status updated to " + s[5]);
    }

    static void personalInfo() {
        String[] u = store.users.get(userIdx);
        System.out.println("id: " + u[1] + "  name: " + u[2] + "  contact: " + u[3]);
        System.out.println("1. Update name  2. Update contact  3. Change password  4. Back");
        System.out.print("Choose: ");
        String c = in.nextLine();
        if (c.equals("1")) { u[2] = ask("new name: "); store.saveUsers(); System.out.println("name updated"); }
        else if (c.equals("2")) { u[3] = ask("new contact: "); store.saveUsers(); System.out.println("contact updated"); }
        else if (c.equals("3")) { u[4] = SecurityUtil.hash(askPassword()); store.saveUsers(); System.out.println("password updated"); }
        else if (c.equals("4")) return;
        else System.out.println("not a valid choice");
    }

    static void setLock(String value) {
        String id = ask("Enter user id: ");
        int idx = store.findUserById(id);
        if (idx < 0) { System.out.println("no such user"); return; }
        String[] u = store.users.get(idx);
        if (u[5].equals(value)) { System.out.println("already " + (value.equals("1") ? "locked" : "unlocked")); return; }
        String summary = (value.equals("1") ? "lock" : "unlock") + " account " + u[0] + " id=" + u[1];
        if (!confirm(summary)) { System.out.println("cancelled"); return; }
        u[5] = value;
        if (value.equals("0")) u[6] = "0";
        store.saveUsers();
        store.appendAudit(userId, summary);
        System.out.println("done");
    }

    static void registerUser(String r, boolean needAudit) {
        String id = ask("Enter id number: ");
        if (store.findUserById(id) >= 0) { System.out.println("that id already exists"); return; }
        String name = ask("Enter name: ");
        String contact = ask("Enter contact number: ");
        String pw = askPassword();
        if (needAudit) {
            String summary = "register " + r + " id=" + id + " name=" + name;
            if (!confirm(summary)) { System.out.println("cancelled"); return; }
            store.users.add(newUserRow(r, id, name, contact, pw));
            store.saveUsers();
            store.appendAudit(userId, summary);
        } else {
            store.users.add(newUserRow(r, id, name, contact, pw));
            store.saveUsers();
        }
        System.out.println(r + " registered");
    }

    public static void main(String[] args) {
        store = new DataStore();

        if (!store.hasAdmin()) {
            System.out.println("---------- first run admin setup ----------");
            String id = ask("Enter id number: ");
            String name = ask("Enter name: ");
            String contact = ask("Enter contact number: ");
            String pw = askPassword();
            store.users.add(newUserRow("admin", id, name, contact, pw));
            store.saveUsers();
            System.out.println("admin account created");
        }

        String mode = "main";

        while (true) {
            switch (mode) {
                case "main": System.out.println("\n========== ShipTrack ==========");
                    System.out.println("1. Customer  2. Dispatcher  3. Driver  4. Admin  5. Register as customer  6. Exit"); break;
                case "customer": System.out.println("\n========== Customer ==========");
                    System.out.println("1. Create shipment  2. Track packages  3. Personal info  4. Logout"); break;
                case "dispatcher": System.out.println("\n========== Dispatcher ==========");
                    System.out.println("1. Assign delivery  2. Update status  3. Personal info  4. Logout"); break;
                case "driver": System.out.println("\n========== Driver ==========");
                    System.out.println("1. View assigned  2. Update status  3. Logout"); break;
                case "admin": System.out.println("\n========== Admin ==========");
                    System.out.println("1. Register dispatcher  2. Register driver  3. Remove staff");
                    System.out.println("4. Password policy  5. Max login attempts  6. Lock  7. Unlock  8. Logout"); break;
            }
            System.out.print("Choose: ");
            String c = in.nextLine();
            boolean handled = true;

            if (mode.equals("main")) {
                if (c.equals("1")) mode = tryLogin("customer");
                else if (c.equals("2")) mode = tryLogin("dispatcher");
                else if (c.equals("3")) mode = tryLogin("driver");
                else if (c.equals("4")) mode = tryLogin("admin");
                else if (c.equals("5")) registerUser("customer", false);
                else if (c.equals("6")) { System.out.println("goodbye"); break; }
                else handled = false;
            }
            else if (mode.equals("customer")) {
                if (c.equals("1")) {
                    String origin = ask("Enter origin: ");
                    String dest = ask("Enter destination: ");
                    String desc = ask("Enter description: ");
                    String sid = store.nextShipmentId();
                    store.shipments.add(new String[]{sid, userId, origin, dest, desc, "pending", "-"});
                    store.saveShipments();
                    System.out.println("shipment created with id " + sid);
                }
                else if (c.equals("2")) listShipments("customer", userId);
                else if (c.equals("3")) personalInfo();
                else if (c.equals("4")) { System.out.println("logging out"); mode = "main"; }
                else handled = false;
            }
            else if (mode.equals("dispatcher")) {
                if (c.equals("1")) {
                    listShipments("unassigned", "");
                    String sid = ask("Enter shipment id: ");
                    int sIdx = store.findShipment(sid);
                    if (sIdx < 0) System.out.println("no such shipment");
                    else {
                        String[] s = store.shipments.get(sIdx);
                        if (!s[6].equals("-")) System.out.println("already assigned to " + s[6]);
                        else {
                            String did = ask("Enter driver id: ");
                            if (store.findUser("driver", did) < 0) System.out.println("no such driver");
                            else { s[6] = did; store.saveShipments(); System.out.println("assigned to " + did); }
                        }
                    }
                }
                else if (c.equals("2")) changeStatus(null);
                else if (c.equals("3")) personalInfo();
                else if (c.equals("4")) { System.out.println("logging out"); mode = "main"; }
                else handled = false;
            }
            else if (mode.equals("driver")) {
                if (c.equals("1")) listShipments("driver", userId);
                else if (c.equals("2")) changeStatus(userId);
                else if (c.equals("3")) { System.out.println("logging out"); mode = "main"; }
                else handled = false;
            }
            else if (mode.equals("admin")) {
                if (c.equals("1")) registerUser("dispatcher", true);
                else if (c.equals("2")) registerUser("driver", true);
                else if (c.equals("3")) {
                    String id = ask("Enter user id to remove: ");
                    int idx = store.findUserById(id);
                    if (idx < 0) System.out.println("no such user");
                    else {
                        String[] u = store.users.get(idx);
                        if (!u[0].equals("dispatcher") && !u[0].equals("driver")) System.out.println("can only remove dispatcher or driver");
                        else {
                            String summary = "remove " + u[0] + " id=" + u[1] + " name=" + u[2];
                            if (!confirm(summary)) System.out.println("cancelled");
                            else {
                                store.users.remove(idx); store.saveUsers();
                                store.appendAudit(userId, summary);
                                System.out.println("user removed");
                            }
                        }
                    }
                }
                else if (c.equals("4")) {
                    System.out.println("current: minLen=" + store.policy[0] + " upper=" + store.policy[1] + " lower=" + store.policy[2] + " digit=" + store.policy[3] + " special=" + store.policy[4]);
                    int minLen = askInt("minimum length: ");
                    int upper = askInt("require uppercase (1/0): ");
                    int lower = askInt("require lowercase (1/0): ");
                    int digit = askInt("require digit (1/0): ");
                    int special = askInt("require special (1/0): ");
                    String summary = "policy minLen=" + minLen + " upper=" + upper + " lower=" + lower + " digit=" + digit + " special=" + special;
                    if (!confirm(summary)) System.out.println("cancelled");
                    else {
                        store.policy[0] = minLen; store.policy[1] = upper; store.policy[2] = lower; store.policy[3] = digit; store.policy[4] = special;
                        store.savePolicy(); store.appendAudit(userId, summary);
                        System.out.println("policy updated");
                    }
                }
                else if (c.equals("5")) {
                    int v = askInt("new max attempts: ");
                    if (v < 1) System.out.println("must be at least 1");
                    else {
                        String summary = "set max login attempts to " + v;
                        if (!confirm(summary)) System.out.println("cancelled");
                        else {
                            store.policy[5] = v; store.savePolicy();
                            store.appendAudit(userId, summary);
                            System.out.println("max attempts updated");
                        }
                    }
                }
                else if (c.equals("6")) setLock("1");
                else if (c.equals("7")) setLock("0");
                else if (c.equals("8")) { System.out.println("logging out"); mode = "main"; }
                else handled = false;
            }
            else handled = false;

            if (!handled) System.out.println("not a valid choice");
        }

        in.close();
    }
}
