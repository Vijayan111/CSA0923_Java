#!/bin/bash
# Re-runs every task and stores the console output in outputs/ (needs JDK 17+; works from the project root)
cd "$(dirname "$0")"; L=lib/sqlite-jdbc-3.42.0.0.jar; O=/tmp/ra; rm -rf $O; mkdir -p $O outputs
J="./run.sh"; S=B3_JDBC/schema_sqlite.sql
./jc.sh -cp $L -d $O B1_FileStore/*.java B2_Adversarial/*.java A1_FileFormat/*.java A2_BufferedIO/*.java B3_JDBC/*.java B4_Migration/*.java || exit 1
./jc.sh -cp $L -d $O/a3 A3_DataAccess/*.java; ./jc.sh -cp $L -d $O/a5 A5_Transactions/*.java
$J -cp $O PassStore                          > outputs/B1_passstore_expected.txt
$J -cp $O PassStoreTest                      > outputs/B1_tests.txt
$J -cp $O LineCounter                        > outputs/B1_buffering.txt
$J -cp $O GrowthBench                        > outputs/B1_growth.txt
$J -cp $O A1Demo                             > outputs/A1_demo.txt
$J -cp $O ReadCalls                          > outputs/A2_readcalls.txt
$J -cp $O/a3:$L DaoDemo                      > outputs/A3_dao.txt
(cd A4_Serialization && ./run_a4.sh)         > outputs/A4_serialization.txt
$J -cp $O/a5:$L WalletTransfer               > outputs/A5_wallet.txt
$J -cp $O:$L LoginDemo                       > outputs/B2_login.txt
$J -cp $O:$L DamagedFileDemo                 > outputs/B2_damaged.txt
$J -cp $O:$L EscapeBench                     > outputs/B2_escape.txt
$J -Dschema=$S -cp $O:$L Migrate             > outputs/B4_migrate.txt
$J -cp $O:$L ConcurrencyDemo                 > outputs/B4_concurrency.txt
$J -Xmx1500m -Dschema=$S -cp $O:$L CampusBench > outputs/B4_campus.txt
$J -Dschema=$S -cp $O:$L B3Bench             > outputs/B3_bench.txt
echo done > outputs/DONE
