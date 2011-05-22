#/bin/sh

# This code will get called once when we first begin to grade your assignment
# You probably don't need to touch this file, but if you make significant 
# changes to the way the MessageParser works, ie if you don't want it to 
# store intermediate parsed files locally, or if you choose not to program
# in Java you will need to update this file so that it correctly builds
# your code and correctly initializes anything that needs initializing

# this code gets called with 1 command line parameters, <traindir>
# which is directory that contains several directories (one for each newsgroup).
# While testing your code, use <traingdir> = /afs/ir/data/linguistic-data/TextCat/20Newsgroups/20news-18828

# build the code
/usr/pubsw/bin/javac -g MessageParser.java NaiveBayesClassifier.java

# preprocess the data
/usr/pubsw/bin/java -Xmx800m MessageParser $1 train.gz



