#!bin/bash
javac RepCRec.java
for i in $(ls Tests/Test*.txt); do java RepCRec $(basename $i); done
#java RepCRec Test1.txt 