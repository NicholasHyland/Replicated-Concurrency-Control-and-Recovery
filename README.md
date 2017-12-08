# Replicated-Concurrency-Control-and-Recovery

To run inside Docker container:

(Create the image and run the container)

docker build -t java .
docker run java:latest


Otherwise:

Compile and Run with:
- sh run.sh

(This runs all the tests currently inside the Tests folder in order. Change this script if more tests are added)

OR

- javac RepCRec.java
- java RepCRec fileName fileName ...


NOTE: all test files must be inside Tests/
