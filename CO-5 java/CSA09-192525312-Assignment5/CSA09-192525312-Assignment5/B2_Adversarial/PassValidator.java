/** Returns null when a pass line is valid, otherwise the reason it is rejected. */
public class PassValidator {
    static String check(String line) {
        if (line.isBlank()) return "empty line";
        String[] f = line.split(",", -1);
        if (f.length != 5) return "wrong field count (" + f.length + ")";
        if (!f[0].matches("P\\d{4,6}")) return "bad pass ID '" + f[0] + "'";
        if (!f[1].matches("\\d{9}")) return "bad register number '" + f[1] + "'";
        if (f[2].isBlank()) return "empty name";
        try { int r = Integer.parseInt(f[3]); if (r < 1 || r > 99) return "route out of range " + r; }
        catch (NumberFormatException e) { return "non-numeric route '" + f[3] + "'"; }
        try { double amt = Double.parseDouble(f[4]); if (amt <= 0) return "amount must be positive"; }
        catch (NumberFormatException e) { return "non-numeric amount '" + f[4] + "'"; }
        return null;
    }
}
