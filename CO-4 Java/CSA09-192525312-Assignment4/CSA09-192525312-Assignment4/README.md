# CSA09-192525312-Assignment4

Name: Vijayan P | Register no.: 192525312 | Java: OpenJDK 21 | Course: Programming in Java (CSA09), CO4

| Task | Compile / run | Result |
|---|---|---|
| Task01 | `javac *.java` (FeeWindowBad.java fails by design) ; `java FeeWindow` ; `java FeeServiceTest` ; `java DispatchBench` | 12/12 service tests pass; if-else chain O(k) vs listener O(1) measured |
| Task02 | `javac *.java` ; `java AppointmentWindow` ; `java DoctorBench` ; `java TraceDemo` | Map lookup 16-79 ns vs 0.6 us-0.86 ms for rescan |
| Task03 | `javac *.java` ; `java LibraryScreen` | results area grows, buttons keep size; null layout does not resize |
| Task04 | `javac DrawPad.java` ; `java DrawPad` ; `javac DrawPadOverride.java` (expected error) ; `java RepaintBench` | dots survive repaint; @Override exposes the typo |
| Task05 | `javac Converter.java` ; `java Converter` ; `v1/` and `v2/` compile separately ; `java ListenerCost` | Enter key + dialog: 5/8/10/8 lines changed (A/B/C/D) |
| Task06 | `sql/schema.sql` in MySQL ; `javac *.java` ; `java -cp .:mysql-connector-j.jar -Ddb.url=jdbc:mysql://localhost:3306/campus_events -Ddb.user=root -Ddb.pwd=... MainFrame` | 14/14 service tests pass (`java ServiceTest`) |

`tools/` (Render, Shot) and the `Demo/Harness` classes only produce off-screen screenshots; they are not part of the applications.
`logs/` holds the console output of the runs; `screenshots/` the images used in the report.
Replace the sample screenshots with your own from running the programs on a machine with a display (and MySQL for Task06).
