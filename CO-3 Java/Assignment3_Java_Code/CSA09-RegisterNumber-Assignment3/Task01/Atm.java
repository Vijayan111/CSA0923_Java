import java.io.FileNotFoundException;

public class Atm {
    static int balance = 5000;

    static void readCard(String acc) throws FileNotFoundException {   // checked
        if (acc.equals("9999")) throw new FileNotFoundException("card_9999.dat (card-reader file) not found");
    }

    static void withdraw(String acc, String amtText)
            throws FileNotFoundException, InsufficientBalanceException {
        readCard(acc);
        int amt = Integer.parseInt(amtText.trim());                   // NumberFormatException / NPE
        if (amt < 0) throw new IllegalArgumentException("Amount cannot be negative: " + amt);
        if (amt > balance) throw new InsufficientBalanceException("Requested " + amt + " but balance is " + balance);
        balance -= amt;
        System.out.println("  Dispensed " + amt + ", new balance " + balance);
    }

    static void attempt(String id, String acc, String amt) {
        System.out.println(id + ": account=" + acc + ", amount=" + (amt == null ? "null" : "\"" + amt + "\""));
        try {
            withdraw(acc, amt);
        } catch (NumberFormatException | NullPointerException e) {   // multi-catch, narrow first
            System.out.println("  catch#1 (bad input): " + e.getClass().getSimpleName());
        } catch (IllegalArgumentException e) {                        // broader than NumberFormatException
            System.out.println("  catch#2 (illegal value): " + e.getMessage());
        } catch (FileNotFoundException e) {
            System.out.println("  catch#3 (card reader): " + e.getMessage());
        } catch (InsufficientBalanceException e) {
            System.out.println("  catch#4 (balance): " + e.getMessage());
        } finally {
            System.out.println("  finally: card returned, session closed");
        }
    }

    public static void main(String[] args) {
        attempt("T1", "1234", "2000");   // valid
        attempt("T2", "1234", "abc");    // letters
        attempt("T3", "1234", "-500");   // negative
        attempt("T4", "1234", "9000");   // more than balance
        attempt("T5", "9999", "100");    // card file missing
        attempt("T6", "1234", null);     // no input at all
    }
}

class InsufficientBalanceException extends Exception {          // checked
    InsufficientBalanceException(String m) { super(m); }
}
