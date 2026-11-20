public class Daemon {
    public static void main(String[] a) throws Exception {
        Thread station = new Thread(() -> {
            for (int i = 1; i <= 100; i++) {
                System.out.println("Station-D scanned script " + i);
                try { Thread.sleep(20); } catch (InterruptedException e) { return; }
            }
            System.out.println("Station-D finished all 100 scripts");
        }, "Station-D");
        station.setDaemon(a.length > 0 && a[0].equals("daemon"));
        System.out.println("Station-D daemon = " + station.isDaemon());
        station.start();
        Thread.sleep(70);
        System.out.println("main() ends now");
    }
}
