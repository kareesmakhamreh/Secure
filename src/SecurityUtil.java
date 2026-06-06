import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtil {

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    public static String hash(String password) {
        try {
            return toHex(MessageDigest.getInstance("SHA-256").digest(password.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("hashing not available");
            return "";
        }
    }

    public static boolean check(String password, String expectedHashHex) {
        return hash(password).equals(expectedHashHex);
    }

    public static String validate(String password, int minLen, int requireUpper, int requireLower, int requireDigit, int requireSpecial) {
        if (password.length() < minLen) return "password must be at least " + minLen + " characters";
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        if (requireUpper == 1 && !hasUpper) return "password must contain an uppercase letter";
        if (requireLower == 1 && !hasLower) return "password must contain a lowercase letter";
        if (requireDigit == 1 && !hasDigit) return "password must contain a digit";
        if (requireSpecial == 1 && !hasSpecial) return "password must contain a special character";
        return "ok";
    }
}