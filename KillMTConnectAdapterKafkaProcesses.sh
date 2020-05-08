#!/bin/bash

file=/Users/sar6/Documents/TimSprockProject/kafka_2.12-2.5.0/ProcessIDs.txt
while IFS= read -r line
do
  a=( $line )
  echo Killing process ${a[0]} with ID ${a[1]}
  kill ${a[1]} # try kill -9 if this doesn't work to force kill, not graceful...
  sleep 2s
done < "$file"


echo Currently running processes:
sleep 3s
ps

