public class FeeCastDemo {

    public static void main(String[] args) {

        int minutes = 121;
        double rate = 30.0;

        // Wrong 1: Integer division happens before multiplication.
        double wrong1 = (minutes / 60) * rate;

        // Wrong 2: Casting after integer division does not fix it.
        double wrong2 = (double) (minutes / 60) * rate;

        // Wrong 3: Math.ceil receives an already truncated result.
        double wrong3 = Math.ceil(minutes / 60) * rate;

        // Correct 1: Convert to double before division.
        double correct1 = Math.ceil((double) minutes / 60) * rate;

        // Correct 2: Integer arithmetic for positive parking durations.
        int billableHours = (minutes + 59) / 60;
        double correct2 = billableHours * rate;

        System.out.println("wrong 1  minutes/60 * rate                 = " + wrong1);
        System.out.println("wrong 2  (double)(minutes/60) * rate       = " + wrong2);
        System.out.println("wrong 3  Math.ceil(minutes/60) * rate      = " + wrong3);

        System.out.println(
                "ok       Math.ceil((double)minutes/60) * rate = "
                        + correct1
        );

        System.out.println(
                "chosen   (minutes+59)/60 * rate           = "
                        + correct2
        );

        // Demonstrate casting and floating-point precision.
        System.out.println("(int) 29.99 = " + (int) 29.99);

        float floatResult = 0.1f * 3;
        double doubleResult = 0.1 * 3;

        System.out.println("0.1f * 3 = " + floatResult);
        System.out.println("0.1 * 3 = " + doubleResult);

        // Demonstrate integer overflow when narrowing a long to int.
        long largeValue = 3_000_000_000L;

        System.out.println(
                "(int) 3_000_000_000L = " + (int) largeValue
        );

        // Demonstrate floating-point precision.
        double sum = 0.1 + 0.2;

        System.out.println("Rs 0.1 + 0.2 in double = " + sum);

        // Validate the calculation using additional test cases.
        System.out.println("\nAdditional parking fee tests:");

        int[] testMinutes = {0, 1, 60, 61, 120, 121};

        for (int m : testMinutes) {
            int hours = (m + 59) / 60;
            double fee = hours * rate;

            System.out.println(
                    m + " minutes -> " + hours
                            + " billable hours -> Rs " + fee
            );
        }
    }
}