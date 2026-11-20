# CSA09 – Programming in Java – Assignment 3 (CO3)
Name / Register number: [fill in]   |   Java version: OpenJDK 21

Rename this folder to `CSA09-<RegisterNumber>-Assignment3`, then run every program yourself and replace the
screenshots with your own. Make at least one commit per task.

## Run (inside each task folder)
| Task | Compile / run |
|---|---|
| Task01 ATM | `java Atm.java`, `java Uncaught.java`, `javac BadOrder.java` (expected compile error), `java ErrorCodeVsException.java` |
| Task02 Exam portal | `javac *.java && java StorageDemo`; `java ExceptionCost`; in `portal/`: `javac Portal.java && java Portal`; `nocause/` = cause passed as null |
| Task03 Resources | `javac *.java && java ReportGen`; `java SuppressedDemo`; `ulimit -n 256; java NoClose`; `java ScaleBench` (Linux: uses /proc/self/fd) |
| Task04 Threads | `javac *.java && java Scanners`; `java Daemon daemon`; `java Daemon` |
| Task05 Seat booking | `javac *.java && java SeatBooking`; `java SeatBookingRace`; `java BookingBench` |
| Task06 Deadlock / wait-notify | `javac *.java && java Wallet`; `java TokenCounter`; `java TokenCounterIf`; `java TokenTrace`; `java Alt` |
| Task07 Processor | `javac *.java && java Processor gen`; `java Processor small`; `java Processor pools`; `java Alternatives` (run `gen` first) |

`out_*.txt` files are sample outputs (1 vCPU Linux); results such as race counts and timings will differ on your machine.
