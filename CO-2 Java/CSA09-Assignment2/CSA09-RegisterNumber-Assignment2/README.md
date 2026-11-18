# CSA09 Assignment 2 - Strings, Collections and Generics (CO2)

Name: <your name>    Register number: <your register number>    Java version: OpenJDK 21

## Compile and run
Each TaskNN folder is independent:

    cd Task01
    javac TicketBench.java
    java TicketBench

Task05 and Task06 have two files that share classes: use `javac *.java`.

## Results
- Q1: String + is O(n^2) (244 ms vs 0.14 ms for 20,000 fields); StringBuilder per ticket, StringBuffer for a shared log.
- Q2: ArrayList for the playlist, ArrayDeque for "Up Next".
- Q3: hashCode added: Unique students 3 -> 2.
- Q4: LinkedHashMap chosen (31 ns per lookup at 1,000,000 medicines).
- Q5: Comparator chain: [Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)].
- Q6: PriorityQueue with arrival counter; aging key fixes starvation.
- Q7: GapPlaylist is 76x to 1,241x faster for local edits.
