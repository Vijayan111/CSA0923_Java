import java.util.concurrent.atomic.AtomicInteger;

/** All validation and payment logic. No AWT/Swing class appears here, so it can be unit-tested without a window. */
public class FeeService {
    public static final double MAX_AMOUNT = 500_000;
    private final FeeRepository repo;
    private final AtomicInteger receiptCounter = new AtomicInteger(1000);
    public FeeService(FeeRepository repo) { this.repo = repo; }

    /** @return null when the input is acceptable, otherwise a message for the user. */
    public String validate(String regNo, String amountText) {
        if (regNo == null || regNo.isBlank()) return "Enter the register number.";
        if (!regNo.trim().matches("\\d{9}")) return "Register number must be exactly 9 digits.";
        if (amountText == null || amountText.isBlank()) return "Enter the amount.";
        double a;
        try { a = Double.parseDouble(amountText.trim()); }
        catch (NumberFormatException ex) { return "Amount must be a number, e.g. 45000.00"; }
        if (Double.isNaN(a) || a <= 0) return "Amount must be greater than zero.";
        if (a > MAX_AMOUNT) return "Amount exceeds the limit of " + (long) MAX_AMOUNT + ".";
        return null;
    }

    public PaymentResult pay(String regNo, String amountText) {
        String err = validate(regNo, amountText);
        if (err != null) return PaymentResult.fail(err);
        double a = Double.parseDouble(amountText.trim());
        try {
            repo.save(regNo.trim(), a);
            String no = "RCPT-" + receiptCounter.incrementAndGet();
            return new PaymentResult(true, String.format("Paid %.2f for %s", a, regNo.trim()), no);
        } catch (Exception ex) {
            return PaymentResult.fail("Payment could not be saved: " + ex.getMessage());
        }
    }

    public String receiptText(PaymentResult last) {
        if (last == null || !last.ok()) return "No successful payment yet.";
        return last.receiptNo() + " - " + last.message();
    }
}
