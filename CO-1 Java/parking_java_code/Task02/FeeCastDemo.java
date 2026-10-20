// Task 2 - casting and integer-division risks in fee calculation
public class FeeCastDemo {
    public static void main(String[] args) {
        int minutes = 130;
        double rate = 30;                               // Rs per hour (car)

        int h1 = minutes / 60;                          // integer division: 2
        System.out.println("wrong 1  minutes/60 * rate               = " + h1 * rate);

        double h2 = (double) (minutes / 60);            // cast AFTER the division: still 2.0
        System.out.println("wrong 2  (double)(minutes/60) * rate     = " + h2 * rate);

        double h3 = Math.ceil(minutes / 60);            // ceil of an already truncated 2
        System.out.println("wrong 3  Math.ceil(minutes/60) * rate    = " + h3 * rate);

        double h4 = Math.ceil((double) minutes / 60);   // right, but floating point
        System.out.println("ok       Math.ceil((double)m/60) * rate  = " + h4 * rate);

        int hours = (minutes + 59) / 60;                // integer ceiling: no double needed
        System.out.println("chosen   (minutes+59)/60 * rate          = " + hours * rate);

        // precision: float and narrowing
        int fromDouble = (int) 29.99;                   // truncates, does not round
        float f = 0.1f * 3;
        System.out.println("(int) 29.99 = " + fromDouble + ",  0.1f*3 = " + f + ",  0.1*3 = " + 0.1 * 3);
        long bigMinutes = 3_000_000_000L;               // about 5707 years, shows int overflow
        System.out.println("(int) 3_000_000_000L = " + (int) bigMinutes);
        System.out.println("Rs 0.1 + 0.2 in double = " + (0.1 + 0.2));
    }
}
