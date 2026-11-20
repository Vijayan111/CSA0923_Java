# CSA09 – Programming in Java – Assignment 5 (CO5): Data Persistence Engineering

**Name:** Vijayan P  **Register number:** 192525312  **Java:** OpenJDK 21

Report: `report/Assignment5_Report.docx` (14 pages).

## Run
```
./run_all.sh            # compiles everything and stores console output in outputs/
```
or one task at a time (JDBC tasks need `lib/sqlite-jdbc-3.42.0.0.jar` on the classpath):
```
./jc.sh -cp lib/sqlite-jdbc-3.42.0.0.jar -d out B1_FileStore/*.java      # jc.sh = javac
java -cp out PassStoreTest
```
(`jc.sh` / `run.sh` only wrap `javac` / `java` and hide the JAVA_TOOL_OPTIONS notice; plain `javac` / `java` work too.)

| Folder | Content | Main classes |
|---|---|---|
| A1_FileFormat | text / binary / fixed-length layouts, escaping, CRC | `A1Demo` |
| A2_BufferedIO | read-call counting, readAllLines memory | `ReadCalls` |
| A3_DataAccess | `Dao<T,K>`, `StudentDao` | `DaoDemo` |
| A4_Serialization | transient, object graph, serialVersionUID, CSV/JSON | `run_a4.sh` |
| A5_Transactions | wallet transfer with commit / rollback | `WalletTransfer` |
| B1_FileStore | `PassStore` (TODO 1), tests, buffering and growth benchmarks | `PassStoreTest`, `LineCounter`, `GrowthBench` |
| B2_Adversarial | SQL injection, damaged file, escaping vs PreparedStatement | `LoginDemo`, `DamagedFileDemo`, `EscapeBench` |
| B3_JDBC | `PassLoader` (TODO 1-3), Statement / Prepared / batch timing, SQL scripts | `B3Bench` |
| B4_Migration | migration + failure report, DatabaseMetaData, concurrency, 12 campuses | `Migrate`, `ConcurrencyDemo`, `CampusBench` |

Classes that use several folders (B2, B3, B4) are compiled together: see `run_all.sh`.

## Database
The runs in `outputs/` used **SQLite** through JDBC (no MySQL server was available). To use MySQL 8:
1. `CREATE DATABASE transport_test;` then run `B3_JDBC/schema_mysql.sql`.
2. Add `mysql-connector-j` to the classpath and change the URL in the programs to
   `jdbc:mysql://localhost:3306/transport_test?rewriteBatchedStatements=true` (plus user and password).
3. Re-run and compare with `outputs/`; auto-commit timings in particular will differ.

## Results in one line each
- A2/B1: 52,428,817 read calls fall to 6,402 with an 8 KB buffer; 50 MB counted in 12.4 s unbuffered vs 21 ms buffered.
- B1: all 6 tests pass; find scales linearly (O(n)); a HashMap is about 49 times faster for 1,000 searches.
- B2: `x' OR '1'='1` logs in through string concatenation and is rejected by PreparedStatement; a 5-field line with amount `abc` slips past the field-count check.
- B3: 100,000 rows: 68 s (Statement), 59 s (PreparedStatement, auto-commit), 0.26 s (batch in one transaction); a failure half-way leaves 5,000 rows with auto-commit and 0 with a transaction.
- B4: 19,994 of 20,000 lines migrated, 6 reported; 4 text-file counters created 8 duplicate IDs, the database 0; decision: shared database.
