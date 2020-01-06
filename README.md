# IPD_network_sim
A simulation of the Iterated Prisoner's Dilemma on a network

This is set up as a Netbeans project. So the easiest way to build it is to open the project using Netbeans.

There is, however, a jar provided in the dist/ folder. The dist/lib/ folder also contains all
the needed libraries.

To run the project, go to the test/ folder and run the run_expt.sh script from the command line.
It takes an experiment number as its sole argument. For example:

`./run_expt.sh 1`

will create a folder named expt1/ in the test/ folder and run the code inside it. All the results
will also be placed in the expt1/ folder.

The human subject data is contained in the human_subject_data/ folder. That folder also contains
two python scripts. One computes the moody conditional cooperation probabilities from the human
subject data. The other computes the same thing from simulation results. You will need to put
in the correct filenames in the script at the top.


