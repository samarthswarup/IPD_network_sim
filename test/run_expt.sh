#! /bin/bash

mkdir expt"$1"
mkdir expt"$1"/networks
cp ../src/parameters.xml expt"$1"/
cd expt"$1"
java -jar ../../dist/IPD_network_sim.jar parameters.xml > log.txt
