#!/bin/bash

if [ $# -eq 0 ]; then
    echo >&2 "Usage: profiler <pid> [<output directory> [ <count> [ <delay> ] ] ]"
    echo >&2 "    Defaults: count = 5, delay = 1 (seconds)"
    exit 1
fi

pid=$1        # required
count=${3:-5} # defaults to 10 times
delay=${4:-1} # defaults to 1 second
out=${2:-.}   # default to current directory

while [ $count -gt 0 ]
do
    jstack $pid > $out/jstack.$pid.$(date +%H%M%S.%N)
    sleep $delay
    let count--
    echo -n "."
done
