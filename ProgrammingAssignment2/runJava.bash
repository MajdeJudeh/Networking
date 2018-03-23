#!/bin/bash

javac *.java # compile the program
rm outfile
for((i=0;i<=8;i++))
do
echo "Output of Iteration $i" >> outfile$i &
java DVNode 137.30.201.174 44444 44445 >> outfile$i &
done
cat outfile0 outfile1 outfile2 outfile3 outfile4 outfile5 outfile6 outfile7 outfile8 >> outfile
rm  outfile0 outfile1 outfile2 outfile3 outfile4 outfile5 outfile6 outfile7 outfile8
