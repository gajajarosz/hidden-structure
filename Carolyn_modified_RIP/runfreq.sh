#!/bin/bash

ITERATIONS=100
mkdir -p target/covert_freq
mkdir -p target/c_n_freq
mkdir -p target/n_c_freq

for i in `seq 1 $ITERATIONS`; do
	java STOTEM syllgram_long.txt covert_freq_dist.txt 1 1000 .1 OT newRIP 2 0 target/covert_freq/$i-covert_freq
	java STOTEM syllgram_long.txt covert_freq_dist.txt 1 1000 .1 OT randRIP 2 0 target/covert_freq/$i-covert_freq
	java STOTEM syllgram_long_overt_c_n.txt c_n_freq_dist.txt 1 1000 .1 OT newRIP 2 0 target/c_n_freq/$i-c_n_freq
	java STOTEM syllgram_long_overt_c_n.txt c_n_freq_dist.txt 1 1000 .1 OT randRIP 2 0 target/c_n_freq/$i-c_n_freq
	java STOTEM syllgram_long_overt_n_c.txt n_c_freq_dist.txt 1 1000 .1 OT newRIP 2 0 target/n_c_freq/$i-n_c_freq
	java STOTEM syllgram_long_overt_n_c.txt n_c_freq_dist.txt 1 1000 .1 OT randRIP 2 0 target/n_c_freq/$i-n_c_freq
done
