public class SuppressedDemo {
    static class Res implements AutoCloseable {
        final String n; Res(String n) { this.n = n; System.out.println("open  " + n); }
        public void close() { System.out.println("close " + n); throw new IllegalStateException("close failed: " + n); }
    }
    public static void main(String[] a) {
        try (Res r1 = new Res("input-file"); Res r2 = new Res("summary-writer")) {
            System.out.println("body runs");
            throw new RuntimeException("read failed (primary)");
        } catch (Exception e) {
            System.out.println("caught   : " + e.getMessage());
            for (Throwable s : e.getSuppressed()) System.out.println("suppressed: " + s.getMessage());
        }
    }
}
