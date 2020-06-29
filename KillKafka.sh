#!/bin/bash

file=$KAFKA/logs/ProcessIDs.txt
while read -r line
do
  a=( $line )
  echo Killing process ${a[0]} with ID ${a[1]}
  kill ${a[1]} # try kill -9 if this doesn't work to force kill, not graceful...
  sleep 2s
done < "$file"


echo Currently running processes:
sleep 3s
ps

