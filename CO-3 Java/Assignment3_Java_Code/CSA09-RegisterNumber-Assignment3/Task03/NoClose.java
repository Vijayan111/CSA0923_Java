import java.io.*;
import java.nio.file.*;
public class NoClose {
    public static void main(String[] a) throws Exception {
        Files.createDirectories(Path.of("many"));
        for (int i = 0; i < 400; i++) Files.writeString(Path.of("many", "store" + i + ".csv"), "pen,1,1.0\n");
        int opened = 0;
        try {
            for (int i = 0; i < 400; i++) { new FileReader("many/store" + i + ".csv"); opened++; }   // never closed
        } catch (IOException e) { System.out.println("Failed after " + opened + " files: " + e.getMessage()); }
    }
}
