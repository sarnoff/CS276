#!/bin/sh

# We will call your code using this script.
# It is mostly useless, but just in case someone 
# really doesn't want to write their code in Java
# you can modify this script so that your code gets 
# called correctly.  This will get called as
# runNaiveBayes.sh <mode>
# where mode specifies which part of the assignment you should run.

# When we test your code, we will first call 
# ./setup.sh <ourNewTrainDir>
# which should build your code and spit out train.gz
# if your code needs them.
#
# Then, to test the different parts of the assignment, we will call
# ./runNaiveBayes.sh binomial
# ./runNaiveBayes.sh binomial-chi2
# ./runNaiveBayes.sh multinomial
# ./runNaiveBayes.sh twcnb
#
# (you should test your code the same way)

java -Xmx1000m NaiveBayesClassifier $1 train.gz
