import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.zip.CRC32;

/** Task A1: text layout with escaping + CRC, binary layout (DataOutputStream), fixed-length RandomAccessFile. */
public class A1Demo {
    // ---------- (a) delimited text: passId,regNo,name,route,stop,validity,amount,crc32 ----------
    static String esc(String s) { return s.replace("%", "%25").replace(",", "%2C").replace("\n", "%0A").replace("\r", "%0D"); }
    static String unesc(String s) { return s.replace("%2C", ",").replace("%0A", "\n").replace("%0D", "\r").replace("%25", "%"); }
    static String crc(String s) { CRC32 c = new CRC32(); c.update(s.getBytes(StandardCharsets.UTF_8)); return String.format("%08x", c.getValue()); }
    static String toLine(String id, String reg, String name, int route, String stop, String valid, double amt) {
        String body = String.join(",", id, reg, esc(name), "" + route, esc(stop), valid, "" + amt);
        return body + "," + crc(body);
    }
    /** returns null if the line is good, otherwise the reason it is rejected */
    static String check(String line) {
        if (line.isEmpty()) return "empty line";
        String[] f = line.split(",", -1);
        if (f.length != 8) return "field count " + f.length + " (need 8)";
        String body = line.substring(0, line.lastIndexOf(','));
        if (!crc(body).equals(f[7])) return "CRC mismatch (damaged or incomplete write)";
        try { Integer.parseInt(f[3]); Double.parseDouble(f[6]); } catch (NumberFormatException e) { return "non-numeric route/amount"; }
        return null;
    }
    // ---------- (b) binary: DataOutputStream ----------
    static void writeBin(DataOutputStream o, String id, String reg, String name, int route, String stop, int yyyymmdd, double amt) throws IOException {
        o.writeUTF(id); o.writeUTF(reg); o.writeUTF(name); o.writeShort(route); o.writeUTF(stop); o.writeInt(yyyymmdd); o.writeDouble(amt);
    }
    // ---------- RandomAccessFile fixed-length record ----------
    static final int ID = 8, REG = 12, NAME = 30, STOP = 24, DATE = 8;           // chars (1 byte each, ASCII)
    static final int REC = ID + REG + NAME + 4 + STOP + DATE + 8;                 // route int(4), amount double(8)
    static void put(RandomAccessFile r, String s, int len) throws IOException {
        byte[] b = new byte[len]; java.util.Arrays.fill(b, (byte) ' ');
        byte[] src = s.getBytes(StandardCharsets.US_ASCII); System.arraycopy(src, 0, b, 0, Math.min(len, src.length)); r.write(b);
    }
    public static void main(String[] a) throws Exception {
        System.out.println("== A1.1 text layout: delimiter handling ==");
        String name = "Arun, K\nS";
        String line = toLine("P000042", "192311042", name, 12, "Anna Nagar, Gate 2", "20271031", 4500.0);
        System.out.println("raw name        : " + name.replace("\n", "\\n"));
        System.out.println("stored line     : " + line);
        String[] f = line.split(",", -1);
        System.out.println("fields found    : " + f.length + "   name restored: " + unesc(f[2]).replace("\n", "\\n"));
        System.out.println();
        System.out.println("== A1.3 damaged-data checks ==");
        String good = line;
        String truncated = good.substring(0, good.length() - 3);              // power failure: line cut short
        String cutAtField = good.substring(0, good.length() - 9);             // cut at a delimiter
        String flipped = good.replace("4500.0", "4900.0");                    // bit-rot / manual edit
        String[][] cases = {{"good line", good}, {"truncated mid-CRC (power cut)", truncated}, {"cut after last field", cutAtField}, {"edited amount", flipped},
                {"wrong field count", "P000043,192311043,Divya"}, {"empty line", ""}};
        for (String[] c : cases) { String r = check(c[1]); System.out.printf("%-30s -> %s%n", c[0], r == null ? "ACCEPTED" : "REJECTED: " + r); }
        try { Files.readAllLines(Path.of("no_such_passes.csv")); }
        catch (NoSuchFileException e) { System.out.printf("%-30s -> handled: NoSuchFileException(%s), start with an empty store%n", "missing file", e.getMessage()); }
        System.out.println();

        int n = 8000;
        Path txt = Files.createTempFile("p", ".csv"), bin = Files.createTempFile("p", ".bin"), raf = Files.createTempFile("p", ".raf");
        try (BufferedWriter w = Files.newBufferedWriter(txt);
             DataOutputStream o = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(bin.toFile())));
             RandomAccessFile r = new RandomAccessFile(raf.toFile(), "rw")) {
            for (int i = 1; i <= n; i++) {
                String id = String.format("P%06d", i), reg = "" + (192300000 + i), nm = "Student " + i, stop = "Stop " + (i % 40);
                w.write(toLine(id, reg, nm, 1 + i % 30, stop, "20271031", 4500.0)); w.newLine();
                writeBin(o, id, reg, nm, 1 + i % 30, stop, 20271031, 4500.0);
                put(r, id, ID); put(r, reg, REG); put(r, nm, NAME); r.writeInt(1 + i % 30); put(r, stop, STOP); put(r, "20271031", DATE); r.writeDouble(4500.0);
            }
        }
        long st = Files.size(txt), sb = Files.size(bin), sr = Files.size(raf);
        System.out.println("== sizes for " + n + " passes (one year) ==");
        System.out.printf("text (CSV + CRC)        : %,9d bytes  (%.1f B/record, variable)%n", st, st / (double) n);
        System.out.printf("binary DataOutputStream : %,9d bytes  (%.1f B/record, variable)%n", sb, sb / (double) n);
        System.out.printf("RandomAccessFile fixed  : %,9d bytes  (%d B/record, fixed)%n", sr, REC);
        System.out.printf("padding waste vs binary : %.1f %%  of the fixed file is padding%n", 100.0 * (sr - sb) / sr);
        System.out.println();
        System.out.println("== find pass P007999: scan vs seek ==");
        String target = "P007999";
        double tScan = 1e9, tSeek = 1e9;
        for (int k = 0; k < 40; k++) {
            long s0 = System.nanoTime(); String found = null;
            try (BufferedReader br = Files.newBufferedReader(txt)) { String l; while ((l = br.readLine()) != null) if (l.startsWith(target + ",")) { found = l; break; } }
            tScan = Math.min(tScan, (System.nanoTime() - s0) / 1e3);
            s0 = System.nanoTime();
            try (RandomAccessFile r = new RandomAccessFile(raf.toFile(), "r")) {
                int idx = Integer.parseInt(target.substring(1)) - 1; r.seek((long) idx * REC); byte[] b = new byte[ID]; r.readFully(b);
                if (!new String(b).trim().equals(target)) throw new IllegalStateException();
            }
            tSeek = Math.min(tSeek, (System.nanoTime() - s0) / 1e3);
        }
        System.out.printf("text scan  O(n): %8.1f us (best of 40)%n", tScan);
        System.out.printf("RAF seek   O(1): %8.1f us (best of 40)   -> x%.0f faster%n", tSeek, tScan / tSeek);
        Files.delete(txt); Files.delete(bin); Files.delete(raf);
    }
}
