#!/bin/bash
# builds Resident with / without serialVersionUID, v1 (no phone) and v2 (phone added), then saves with v1 and loads with v2
L=../lib/sqlite-jdbc-3.42.0.0.jar; B=/tmp/a4build; rm -rf $B; mkdir -p $B
gen() { # dir uid(1/0) phone(1/0)
  mkdir -p $B/$1; cp Room.java Hostel.java Main.java $B/$1/
  U=""; [ "$2" = 1 ] && U="private static final long serialVersionUID = 1L;"
  P=""; PS='""'; [ "$3" = 1 ] && { P="String phone;"; PS='", phone=" + phone'; }
  sed -e "s|/\*UID\*/|$U|" -e "s|/\*PHONE\*/|$P|" -e "s|/\*PHONESTR\*/|$PS|" Resident.java.tpl > $B/$1/Resident.java
  ../jc.sh -cp $L -d $B/$1 $B/$1/*.java; }
gen uid_v1 1 0; gen uid_v2 1 1; gen nouid_v1 0 0; gen nouid_v2 0 1
J=../run.sh
echo "== 1. Why transient: Connection field without transient =="; ../jc.sh -cp $L -d $B/bad BadResidentDemo.java; $J -cp $B/bad:$L BadResidentDemo
echo; echo "== 2. Serialize the Hostel (v1 class, no phone field) =="
$J -cp $B/uid_v1:$L Main save $B/hostel_uid.ser; $J -cp $B/nouid_v1:$L Main save $B/hostel_nouid.ser
echo; echo "== 3. Reload with v1 class: transient fields come back as null, shared Arun is still ONE object =="
$J -cp $B/uid_v1:$L Main load $B/hostel_uid.ser
echo; echo "== 4. Phone added (v2). Old file + class WITH serialVersionUID = 1L =="
$J -cp $B/uid_v2:$L Main load $B/hostel_uid.ser
echo; echo "== 5. Phone added (v2). Old file + class WITHOUT serialVersionUID =="
$J -cp $B/nouid_v2:$L Main load $B/hostel_nouid.ser
echo; echo "== 6. Alternative: serialization vs CSV vs JSON =="
../jc.sh -cp $B/uid_v2:$L -d $B/uid_v2 FormatBench.java; $J -cp $B/uid_v2:$L FormatBench
