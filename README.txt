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

FINAL-EVAL_SAMPLE
-How many samples are used to evaluate in the final evaluation
-Default: 1000

 ---------------------------------------------------------------------------------------------------------------------------------------

EDL

If running EDL, the rest of the arguments are as follows:

LEARNER
1 - batch Expectation Driven Learner in Jarosz (submitted). Runs the learning function EDL_batch().
2 - online Expectation Driven Learner in Jarosz (submitted). Runs the learning function EDL_online().

GRAMMAR SAMPLE SIZE
-this is the number of times that a grammar is sampled during each round of learning
- reasonable values are 50-1000

INITIAL BIAS//TODO: add to GLA; start constraints ranked high
- If your grammar file encodes which constraints are markedness and which are faithfulness, you can set this to 1 to begin with an M >> F grammar.
- You can encode M vs. F in your grammar file in the 6th field of the constraint names section. In the sample file they are all set to 1.
- set this to 0 by default

To run EDL, use this syntax at the command prompt. You may want to redirect the output to a file or pipe it to less.

java learn EDL TS2000Grammar_secondary.txt TS1_Dist.txt 100 1000 1 1000 0

You may want to redirect the output to a file or pipe it to less like this:
java learn EDL TS2000Grammar_secondary.txt TS1_Dist.txt 100 1000 1 1000 0 > output.txt
java learn EDL TS2000Grammar_secondary.txt TS1_Dist.txt 100 1000 1 1000 0 | less

In order to do this you will need java and java runtime environment installed, and your computer will have to know where to find java.

---------------------------------------------------------------------------------------------------------------------------------------

GLA

If running GLA, the rest of the arguments are:

LEARNER
 EIP - Jarosz (2013)
 RIP - is the original RIP as proposed for Stochastic OT by Boersma (2003)
 RRIP - is what I called RRIP in Jarosz (2013)
 randRIP - baseline model without parsing; when there’s an error it generates a random output as the ‘winner’ for the update.

 GRAMMAR TYPE
 - set this to OT, HG, or ME (maxent)

LEARNING RATE\\TODO: add learning rates to EDL, but keep separate
 - How much constraint ranking/weighting values get bumped when there’s an update
 - typical value is something like 0.1

 NOISE
 - what’s the variance around the ranking/weighting value
 - typical setting is something like 2

 NEGOK?
 - Indicates whether the learner be allowed to use negative weights.
 - Set to 0 to keep weights non-negative.

To run GLA, use this syntax at the command prompt. You may want to redirect the output to a file or pipe it to less.

java learn GLA TS2000Grammar_secondary.txt TS1_Dist.txt 1000 1000 EIP OT .1 2 0

You may want to redirect the output to a file or pipe it to less like this:
java learn GLA TS2000Grammar_secondary.txt TS1_Dist.txt 1000 1000 EIP OT .1 2 0 > output.txt
java learn GLA TS2000Grammar_secondary.txt TS1_Dist.txt 1000 1000 EIP OT .1 2 0 | less

In order to do this you will need java and java runtime environment installed, and your computer will have to know where to find java.

---------------------------------------------------------------------------------------------------------------------------------------

PRINT OPTIONS

PRINT_INPUT?
- 0 : prints grammar and input
- 1 : doesn't print input

FINAL-EVAL
- 0 : prints final grammar; accuracy on each output; total error and log likelihood
- 1 : prints final grammar; total error and log likelihood
- Default: 0

MINI-EVAL
- 0: prints grammar; accuracy on each output; total error and log likelihood
- 1: prints grammar; total error and log likelihood
- 2: prints nothing
- Default: 1

MINI-EVAL_FREQ
- How often a mini-evaluation round is performed
- Default: 100
- In order to not perform any intermediate evaluation, set to the same number as iterations

MINI-EVAL_SAMPLE
-How many samples are used to evaluate in a mini-evaluation
-Default: 100

QUIT_EARLY?
-How often the program checks to see if it can quit early
-Quits if the learner has already learned everything
-Default: 100
- In order to not try to quit early, set to the same number as iterations

QUIT_EARLY?_SAMPLE
-How many samples are used to evaluate whether the learner is done learning
-The fewer samples, the faster the program, but the greater the risk of quitting before the learner has really finished learning
-Default: 100

For instance, if you want to run GLA and print as much as possible, run:

java learn GLA TS2000Grammar_secondary.txt TS1_Dist.txt 1000 1000 EIP OT .1 2 0 0 0 0 1 100 100 100

If you want to run GLA as quickly as possible, run:

java learn GLA TS2000Grammar_secondary.txt TS1_Dist.txt 1000 1000 EIP OT .1 2 0 1 1 2 1000 100 1000 100