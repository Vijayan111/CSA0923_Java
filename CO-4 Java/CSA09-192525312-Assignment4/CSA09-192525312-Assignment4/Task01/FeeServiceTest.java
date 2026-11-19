/** Plain-Java test of the service: no window needed. */
public class FeeServiceTest {
    static int pass = 0, total = 0;
    static void check(String name, Object expected, Object actual) {
        total++; boolean ok = String.valueOf(expected).equals(String.valueOf(actual)); if (ok) pass++;
        System.out.printf("%-4s  %-32s -> %s%n", ok ? "PASS" : "FAIL", name, actual);
    }
    public static void main(String[] a) {
        InMemoryFeeRepository repo = new InMemoryFeeRepository(0);
        FeeService s = new FeeService(repo);
        check("empty register number", "Enter the register number.", s.validate("", "100"));
        check("short register number", "Register number must be exactly 9 digits.", s.validate("1925", "100"));
        check("blank amount", "Enter the amount.", s.validate("192525312", " "));
        check("non-numeric amount", "Amount must be a number, e.g. 45000.00", s.validate("192525312", "abc"));
        check("negative amount", "Amount must be greater than zero.", s.validate("192525312", "-5"));
        check("amount over limit", "Amount exceeds the limit of 500000.", s.validate("192525312", "900000"));
        check("SQL text in register number", "Register number must be exactly 9 digits.", s.validate("1'; DROP TABLE fees;--", "10"));
        PaymentResult r = s.pay("192525312", "45000");
        check("valid payment ok", true, r.ok());
        check("row stored", 1, repo.rows.size());
        check("invalid payment not stored", false, s.pay("192525312", "x").ok());
        check("rows still", 1, repo.rows.size());
        FeeService broken = new FeeService((reg, amt) -> { throw new java.sql.SQLException("connection refused"); });
        check("DB failure reported, no crash", "Payment could not be saved: connection refused", broken.pay("192525312", "10").message());
        System.out.println("\n" + pass + " / " + total + " tests passed");
    }
}
