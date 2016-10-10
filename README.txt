This code is not for public distribution. It is under development and is not very user-friendly. Use at your own risk.

To run POEM, use this syntax at the command prompt. You may want to redirect the output to a file or pipe it to less.

java POEM TS2000Grammar_secondary.txt TS1_Dist.txt 100 1000 0 1 1

You may want to redirect the output to a file or pipe it to less like this:
java POEM TS2000Grammar_secondary.txt TS1_Dist.txt 100 1000 0 1 1 > output.txt
java POEM TS2000Grammar_secondary.txt TS1_Dist.txt 100 1000 0 1 1 | less

In order to do this you will need java and java runtime environment installed, and your computer will have to know where to find java.

The arguments after POEM (the name of the program) specify:

GRAMMAR FILE
- should be in the same format as provided TS2000Grammar_secondary.txt

DISTRIBUTION FILE
- should be in the same format as provided TS1_Dist.txt

GRAMMAR SAMPLE SIZE
-this is the number of times that a grammar is sampled during each round of learning
- reasonable values are 50-1000

ITERATIONS
- this the number of passes through the data for batch algorithms and the number of times individual data forms are processed for online algorithms
- reasonable values are something like 100 for the batch version and 10000 for the online version

INITIAL BIAS
- If your grammar file encodes which constraints are markedness and which are faithfulness, you can set this to 1 to begin with an M >> F grammar.
- You can encode M vs. F in your grammar file in the 6th field of the constraint names section. In the sample file they are all set to 1.
- set this to 0 by default

LEARNER
1 - batch Expectation Driven Learner in Jarosz (submitted). Runs the learning function EDL_batch().
2 - online Expectation Driven Learner in Jarosz (submitted). Runs the learning function EDL_online().

VERBOSE?
- 0 will suppress most of the output, which will make the program faster.
- 1 will print progress as the program runs. \\TODO: add to STOTEM too

To run STOTEM, use this syntax at the command prompt. You may want to redirect the output to a file or pipe it to less.

java STOTEM TS2000Grammar_secondary.txt TS1_Dist.txt 1000 .1 OT EIP 2 0 1

You may want to redirect the output to a file or pipe it to less like this:
java STOTEM TS2000Grammar_secondary.txt TS1_Dist.txt 1000 .1 OT EIP 2 0 1 > output.txt
java STOTEM TS2000Grammar_secondary.txt TS1_Dist.txt 1000 .1 OT EIP 2 0 1 | less

In order to do this you will need java and java runtime environment installed, and your computer will have to know where to find java.

The program is set up to print out:
 - on every iteration: the grammar, its error over all data with noise, its error over all data without noise
 - every 100 iterations it additionally prints out the accuracy for each word in the learning data

The arguments after STOTEM (the name of the program) specify:

GRAMMAR FILE
- should be in the same format as provided TS2000Grammar_secondary.txt

DISTRIBUTION FILE
 - should be in the same format as provided TS1_Dist.txt

ITERATIONS
 - this is the number of times individual data forms are processed algorithms
 - reasonable values are something like 100-1000 or higher for very complex data

LEARNING RATE
 - How much constraint ranking/weighting values get bumped when there’s an update
 - typical value is something like 0.1

 GRAMMAR TYPE
 - set this to OT, HG, or ME (maxent)

LEARNER
 EIP - Jarosz (2013)
 RIP - is the original RIP as proposed for Stochastic OT by Boersma (2003)
 RRIP - is what I called RRIP in Jarosz (2013)
 randRIP - baseline model without parsing; when there’s an error it generates a random output as the ‘winner’ for the update.

 NOISE
 - what’s the variance around the ranking/weighting value
 - typical setting is something like 2

 NEGOK?
 - Indicates whether the learner be allowed to use negative weights.
 - Set to 0 to keep weights non-negative.

 VERBOSE?
 - 0 will suppress most of the output, which will make the program faster.
 - 1 will print progress as the program runs.