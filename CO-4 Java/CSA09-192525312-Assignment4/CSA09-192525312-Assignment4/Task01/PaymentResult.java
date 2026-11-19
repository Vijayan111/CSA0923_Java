/** Outcome of a payment attempt; carries only data, no GUI classes. */
public record PaymentResult(boolean ok, String message, String receiptNo) {
    public static PaymentResult fail(String why) { return new PaymentResult(false, why, null); }
}
