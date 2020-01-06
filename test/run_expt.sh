#! /bin/bash

cd /Users/biocomplexity/Netbeans_projects/IPDSim/test/
mkdir expt"$1"
mkdir expt"$1"/networks
cp /Users/biocomplexity/Netbeans_projects/IPDSim/src/parameters.xml expt"$1"/
cd expt"$1"
java -jar ../../dist/IPDSim.jar parameters.xml > log.txt
