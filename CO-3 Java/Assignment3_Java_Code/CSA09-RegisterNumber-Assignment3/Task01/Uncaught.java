public class Uncaught {
    static int parse(String s) { return Integer.parseInt(s); }
    static void withdraw(String s) { System.out.println("Dispensing " + parse(s)); }
    public static void main(String[] args) { withdraw("12x"); }
}
