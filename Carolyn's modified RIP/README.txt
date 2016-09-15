This code is not for public distribution. It is under development and is not very user-friendly or cleaned-up. There is a lot of code that you will not need (because I tried lots of things and then disabled them), and some functions don’t work for all learning models. Use at your own risk.

Run the program with this syntax at the command prompt. You may want to redirect the output to a file or pipe it to less.

java STOTEM TS2000Grammar_secondary.txt TS1_Dist.txt 1 1000 .1 OT newRIP 2 0

You may want to redirect the output to a file or pipe it to less like this:
java STOTEM TS2000Grammar_secondary.txt TS1_Dist.txt 1 1000 .1 OT newRIP 2 0 > output.txt
java STOTEM TS2000Grammar_secondary.txt TS1_Dist.txt 1 1000 .1 OT newRIP 2 0 | less

In order to do this you will need java and java runtime environment installed, and your computer will have to know where to find java.

The program is set up to print out:
- on every iteration: the grammar, its error over all data with noise, its error over all data without noise
- every 100 iterations it additionally prints out the accuracy for each word in the learning data

The arguments after STOTEM (the name of the program) specify:

GRAMMAR FILE
- should be in the same format as provided TS2000Grammar_secondary.txt

DISTRIBUTION FILE
- should be in the same format as provided TS1_Dist.txt

SAMPLE SIZE
- set this to 1. This shouldn’t be a parameter.

ITERATIONS
- this is the number of times individual data forms are processed algorithms
- reasonable values are something like 100-1000 or higher for very complex data

LEARNING RATE
- How much constraint ranking/weighting values get bumped when there’s an update
- typical value is something like 0.1

GRAMMAR TYPE
- set this to OT, HG, or ME (maxent)

LEARNER
newRIP - is what I called EIP in Jarosz (2013)
oldRIP - is the original RIP as proposed for Stochastic OT by Boersma (2003)
oldRIPovert - is what I called RRIP in Jarosz (2013)
randRIP - this is a baseline model that doesn’t bother parsing; when there’s an error is simply generates another output randomly and uses that as a ‘winner’ to make an update.

NOISE
- what’s the variance around the ranking/weighting value
- typical setting is something like 2

NEGOK?
- should the learner be allowed to posit and use negative weights. Set to 0 to keep weights non-negative.

GRAPHS
I’ve modified the file to generate CSV files of data. These can be turned into graphs by running the graph.R file.

This file is an R program. It should be run as follows:

Rscript graph.R OT-randRIP

where the last variable is the name of the CSV file (minus the extension). This name is used as the title of the graph and the name of the PDF file that the graph is saved into.
