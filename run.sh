#!bin/bash
javac RepCRec.java
#for i in $(ls Tests/Test*.txt); do java RepCRec $(basename $i); done
#java RepCRec Test1.txt 
for ((i=1;i<4;i++))
{
java RepCRec Test$i.txt
}

java RepCRec Test3.5.txt

for ((i=4;i<22;i++))
{
java RepCRec Test$i.txt
}