#!/usr/bin/env bash
BIN=../ratis-examples/src/main/bin
PEERS=$3

${BIN}/client.sh arithmetic assign --name $1 --value $2 --peers ${PEERS} 