This code is not for public distribution. It is under development and is not very user-friendly. Use at your own risk.

Both EDL and GLA are run using the learn.java program. The first four arguments for each are the same:

LEARNER NAME
-This is either EDL or GLA

GRAMMAR FILE
- should be in the same format as provided TS2000Grammar_secondary.txt

DISTRIBUTION FILE
- should be in the same format as provided TS1_Dist.txt

ITERATIONS
 - this is the number of passes through the data
 - For GLA, reasonable values are around 100-1000 (higher for complex data)
 - The batch EDL learner sees every data point at each iteration, so a reasonable number of iterations is around 100
 - The online EDL learner sees only 1 data point at each iteration, so a comparable number of iterations is 100*the number of data points

\\TODO: add printing options for every iteration versus final eval
\\TODO: 3 parameters: how often to do mini-eval; what is printed at each mini-eval; and what is printed at final eval
\\TODO: Add a parameter controlling how many samples are taken at each intermediate round
\\TODO: Add a parameter controlling how accurately you want to measure if you can quit early
\\TODO: Add a paramter controlling how often you check if you can quit early
\\TODO: Move the print options to end of arglist and make optional; move description to bottom of file; have default kick in if nothing specified
 VERBOSE?\\TODO: make an int that controls how X many iterations something prints; also it's totally crazy in EDL, fix that
 - 0 will suppress most of the output, which will make the program faster. \\TODO: something is wrong in the output of GLA; eval isn't printing anything
 - 1 will print progress as the program runs.

If running EDL, the rest of the arguments are as follows:

GRAMMAR SAMPLE SIZE
-this is the number of times that a grammar is sampled during each round of learning
- reasonable values are 50-1000

INITIAL BIAS
- If your grammar file encodes which constraints are markedness and which are faithfulness, you can set this to 1 to begin with an M >> F grammar.
- You can encode M vs. F in your grammar file in the 6th field of the constraint names section. In the sample file they are all set to 1.
- set this to 0 by default

LEARNER\\TODO: move to first parameter
1 - batch Expectation Driven Learner in Jarosz (submitted). Runs the learning function EDL_batch().
2 - online Expectation Driven Learner in Jarosz (submitted). Runs the learning function EDL_online().

To run EDL, use this syntax at the command prompt. You may want to redirect the output to a file or pipe it to less.

java learn EDL TS2000Grammar_secondary.txt TS1_Dist.txt 100 1 1000 0 1

You may want to redirect the output to a file or pipe it to less like this:
java learn EDL TS2000Grammar_secondary.txt TS1_Dist.txt 100 1 1000 0 1 > output.txt
java learn EDL TS2000Grammar_secondary.txt TS1_Dist.txt 100 1 1000 0 1 | less

In order to do this you will need java and java runtime environment installed, and your computer will have to know where to find java.

If running GLA, the rest of the arguments are:

LEARNING RATE\\TODO: add learning rates to EDL, but keep separate
 - How much constraint ranking/weighting values get bumped when there’s an update
 - typical value is something like 0.1

 GRAMMAR TYPE\\TODO: move to second parameter
 - set this to OT, HG, or ME (maxent)

LEARNER\\TODO: move to first parameter
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

To run GLA, use this syntax at the command prompt. You may want to redirect the output to a file or pipe it to less.

java learn GLA TS2000Grammar_secondary.txt TS1_Dist.txt 1000 1 .1 OT EIP 2 0

You may want to redirect the output to a file or pipe it to less like this:
java learn GLA TS2000Grammar_secondary.txt TS1_Dist.txt 1000 1 .1 OT EIP 2 0 > output.txt
java learn GLA TS2000Grammar_secondary.txt TS1_Dist.txt 1000 1 .1 OT EIP 2 0 | less

In order to do this you will need java and java runtime environment installed, and your computer will have to know where to find java.