This code is not for public distribution. It is under development and is not very user-friendly. Use at your own risk.

To run POEM, use this syntax at the command prompt. You may want to redirect the output to a file or pipe it to less.

java POEM TS2000Grammar_secondary.txt TS1_Dist.txt 100 1000 0 0 1 1 1

You may want to redirect the output to a file or pipe it to less like this:
java POEM TS2000Grammar_secondary.txt TS1_Dist.txt 100 1000 0 0 1 1 1 > output.txt
java POEM TS2000Grammar_secondary.txt TS1_Dist.txt 100 1000 0 0 1 1 1 | less

In order to do this you will need java and java runtime environment installed, and your computer will have to know where to find java.

The arguments after POEM (the name of the program) specify:

GRAMMAR FILE
- should be in the same format as provided TS2000Grammar_secondary.txt

DISTRIBUTION FILE
- should be in the same format as provided TS1_Dist.txt

SAMPLE SIZE
- this means different things for different learning models, but higher numbers mean slower runtime and more consistent performance
- The batch model (learner 1) should be set to something like 20-100
- For other model (learner 2), this indicates how many data forms are sampled and processed before the grammar is updated. If it’s set to 1, it will be an online learner, but it can also be set higher to take larger samples before updating.

ITERATIONS
- this the number of passes through the data for batch algorithms and the number of times individual data forms are processed for online algorithms
- reasonable values are something like 100-1000

INITIAL BIAS
- If your grammar file encodes which constraints are markedness and which are faithfulness, you can set this to 1 to begin with an M >> F grammar. You can encode M vs. F in your grammar file - it’s the sixth field of the constraint names section. In the sample file they are all set to 1.
- set this to 0 by default

RANK BIAS
- set this to 0. This was something I tried for one of the models, but it didn’t work out.

LEARNER
1 - batch Expectation Driven Learner in Jarosz (submitted). Runs the learning function learn_batch_parameter_EM().
2 - online Expectation Driven Learner in Jarosz (submitted). Runs the learning function learn_sample_parameter().
3-4 are different learning models that I haven’t published and don’t work as well.

SPEED-UP?
- set this to 0. If you set it to 1, this will try to increase learning rate for learning model 1.

VERBOSE?
- setting this to 0 will suppress most of the output making things run faster if all you care about is the final grammar. If you want it to print progress, set this to 1.

To run STOTEM, use this syntax at the command prompt. You may want to redirect the output to a file or pipe it to less.

java STOTEM TS2000Grammar_secondary.txt TS1_Dist.txt 1 1000 .1 OT EIP 2 0

You may want to redirect the output to a file or pipe it to less like this:
java STOTEM TS2000Grammar_secondary.txt TS1_Dist.txt 1 1000 .1 OT EIP 2 0 > output.txt
java STOTEM TS2000Grammar_secondary.txt TS1_Dist.txt 1 1000 .1 OT EIP 2 0 | less

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
 EIP - Jarosz (2013)
 RIP - is the original RIP as proposed for Stochastic OT by Boersma (2003)
 RRIP - is what I called RRIP in Jarosz (2013)
 randRIP - this is a baseline model that doesn’t bother parsing; when there’s an error is simply generates another output randomly and uses that as a ‘winner’ to make an update.

 NOISE
 - what’s the variance around the ranking/weighting value
 - typical setting is something like 2

 NEGOK?
 - should the learner be allowed to posit and use negative weights. Set to 0 to keep weights non-negative.