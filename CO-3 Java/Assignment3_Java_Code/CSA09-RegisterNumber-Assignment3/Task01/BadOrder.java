import java.io.FileNotFoundException;
public class BadOrder {
    static void read() throws FileNotFoundException { throw new FileNotFoundException("card.dat"); }
    public static void main(String[] args) {
        try { read(); }
        catch (Exception e) { System.out.println("general"); }
        catch (FileNotFoundException e) { System.out.println("specific"); }
    }
}
