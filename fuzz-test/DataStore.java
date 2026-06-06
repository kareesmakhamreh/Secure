import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.time.LocalDateTime;

public class DataStore {

    public static final String USERS_FILE = "users.txt";
    public static final String SHIPMENTS_FILE = "shipments.txt";
    public static final String POLICY_FILE = "policy.txt";
    public static final String AUDIT_FILE = "audit.txt";

    public ArrayList<String[]> users = new ArrayList<String[]>();
    public ArrayList<String[]> shipments = new ArrayList<String[]>();
    public int[] policy = new int[]{8, 1, 1, 1, 1, 3};
    public ArrayList<String> auditLog = new ArrayList<String>();

    public DataStore() {
        loadUsers();
        loadShipments();
        loadPolicy();
        loadAudit();
    }

    private ArrayList<String> readLines(String path, String errMsg) {
        ArrayList<String> out = new ArrayList<String>();
        File f = new File(path);
        if (!f.exists()) return out;
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) if (line.length() > 0) out.add(line);
        } catch (IOException e) { System.out.println(errMsg); }
        return out;
    }

    private void writeLines(String path, ArrayList<String> lines, boolean append, String errMsg) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(path, append))) {
            for (String line : lines) { w.write(line); w.write("\n"); }
        } catch (IOException e) { System.out.println(errMsg); }
    }

    public void loadUsers() {
        for (String line : readLines(USERS_FILE, "could not read users file")) users.add(line.split(",", -1));
    }

    public void saveUsers() {
        ArrayList<String> lines = new ArrayList<String>();
        for (String[] u : users) lines.add(String.join(",", u));
        writeLines(USERS_FILE, lines, false, "could not save users file");
    }

    public void loadShipments() {
        for (String line : readLines(SHIPMENTS_FILE, "could not read shipments file")) shipments.add(line.split(",", -1));
    }

    public void saveShipments() {
        ArrayList<String> lines = new ArrayList<String>();
        for (String[] s : shipments) lines.add(String.join(",", s));
        writeLines(SHIPMENTS_FILE, lines, false, "could not save shipments file");
    }

    public void loadPolicy() {
        ArrayList<String> lines = readLines(POLICY_FILE, "could not read policy file");
        if (lines.isEmpty()) return;
        String[] parts = lines.get(0).split(",", -1);
        if (parts.length != 6) return;
        try {
            for (int i = 0; i < 6; i++) policy[i] = Integer.parseInt(parts[i]);
        } catch (NumberFormatException e) { System.out.println("policy file has bad numbers, using defaults"); }
    }

    public void savePolicy() {
        ArrayList<String> lines = new ArrayList<String>();
        lines.add(policy[0] + "," + policy[1] + "," + policy[2] + "," + policy[3] + "," + policy[4] + "," + policy[5]);
        writeLines(POLICY_FILE, lines, false, "could not save policy file");
    }

    public void loadAudit() {
        auditLog = readLines(AUDIT_FILE, "could not read audit file");
    }

    public void appendAudit(String adminId, String action) {
        String line = LocalDateTime.now().toString() + "," + adminId + "," + action;
        auditLog.add(line);
        ArrayList<String> one = new ArrayList<String>();
        one.add(line);
        writeLines(AUDIT_FILE, one, true, "could not write audit file");
    }

    public int findUser(String role, String id) {
        for (int i = 0; i < users.size(); i++) if (users.get(i)[0].equals(role) && users.get(i)[1].equals(id)) return i;
        return -1;
    }

    public int findUserById(String id) {
        for (int i = 0; i < users.size(); i++) if (users.get(i)[1].equals(id)) return i;
        return -1;
    }

    public boolean hasAdmin() {
        for (String[] u : users) if (u[0].equals("admin")) return true;
        return false;
    }

    public int findShipment(String shipmentId) {
        for (int i = 0; i < shipments.size(); i++) if (shipments.get(i)[0].equals(shipmentId)) return i;
        return -1;
    }

    public String nextShipmentId() {
        int max = 0;
        for (String[] s : shipments) {
            try { int n = Integer.parseInt(s[0]); if (n > max) max = n; }
            catch (NumberFormatException e) { }
        }
        return Integer.toString(max + 1);
    }
}
