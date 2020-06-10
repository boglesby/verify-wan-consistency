#!/usr/bin/env bash
arguments=--operation=$1
if [ $# -gt 1 ]; then
  arguments=${arguments},--parameter1=$2
  if [ $# -gt 2 ]; then
    arguments=${arguments},--parameter2=$3
    if [ $# -gt 3 ]; then
      arguments=${arguments},--parameter3=$4
    fi
  fi
fi
echo
./gradlew bootRun -Pargs=$arguments
