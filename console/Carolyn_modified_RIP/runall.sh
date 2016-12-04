#!/bin/bash

ITERATIONS=100
mkdir -p target/trouble2
mkdir -p target/trouble1
mkdir -p target/eng
mkdir -p target/polish
mkdir -p target/dutch
mkdir -p target/covert
mkdir -p target/nc
mkdir -p target/cn

for i in `seq 1 $ITERATIONS`; do
	java STOTEM syllgram_short.txt trouble_2_dist.txt 1 1000 .1 OT newRIP 2 0 target/trouble2/$i-trouble2
	java STOTEM syllgram_short.txt trouble_2_dist.txt 1 1000 .1 OT randRIP 2 0 target/trouble2/$i-trouble2
	java STOTEM syllgram_short.txt trouble_2_dist.txt 1 1000 .1 HG newRIP 2 0 target/trouble2/$i-trouble2
	java STOTEM syllgram_short.txt trouble_2_dist.txt 1 1000 .1 HG randRIP 2 0 target/trouble2/$i-trouble2
	java STOTEM syllgram_short.txt trouble_1_dist.txt 1 1000 .1 OT newRIP 2 0 target/trouble1/$i-trouble1
	java STOTEM syllgram_short.txt trouble_1_dist.txt 1 1000 .1 OT randRIP 2 0 target/trouble1/$i-trouble1
	java STOTEM syllgram_short.txt trouble_1_dist.txt 1 1000 .1 HG newRIP 2 0 target/trouble1/$i-trouble1
	java STOTEM syllgram_short.txt trouble_1_dist.txt 1 1000 .1 HG randRIP 2 0 target/trouble1/$i-trouble1
	java STOTEM syllgram_short.txt eng_short_dist.txt 1 1000 .1 OT newRIP 2 0 target/eng/$i-english
	java STOTEM syllgram_short.txt eng_short_dist.txt 1 1000 .1 OT randRIP 2 0 target/eng/$i-english
	java STOTEM syllgram_short.txt eng_short_dist.txt 1 1000 .1 HG newRIP 2 0 target/eng/$i-english
	java STOTEM syllgram_short.txt eng_short_dist.txt 1 1000 .1 HG randRIP 2 0 target/eng/$i-english
	java STOTEM syllgram_short.txt polish_short_dist.txt 1 1000 .1 OT newRIP 2 0 target/polish/$i-polish
	java STOTEM syllgram_short.txt polish_short_dist.txt 1 1000 .1 OT randRIP 2 0 target/polish/$i-polish
	java STOTEM syllgram_short.txt polish_short_dist.txt 1 1000 .1 HG newRIP 2 0 target/polish/$i-polish
	java STOTEM syllgram_short.txt polish_short_dist.txt 1 1000 .1 HG randRIP 2 0 target/polish/$i-polish
	java STOTEM syllgram_short.txt dutch_short_dist.txt 1 1000 .1 OT newRIP 2 0 target/dutch/$i-dutch
	java STOTEM syllgram_short.txt dutch_short_dist.txt 1 1000 .1 OT randRIP 2 0 target/dutch/$i-dutch
	java STOTEM syllgram_short.txt dutch_short_dist.txt 1 1000 .1 HG newRIP 2 0 target/dutch/$i-dutch
	java STOTEM syllgram_short.txt dutch_short_dist.txt 1 1000 .1 HG randRIP 2 0 target/dutch/$i-dutch
	java STOTEM syllgram_long.txt long_covert_dist.txt 1 1000 .1 OT newRIP 2 0 target/covert/$i-covert
	java STOTEM syllgram_long.txt long_covert_dist.txt 1 1000 .1 OT randRIP 2 0 target/covert/$i-covert
	java STOTEM syllgram_long.txt long_covert_dist.txt 1 1000 .1 HG newRIP 2 0 target/covert/$i-covert
	java STOTEM syllgram_long.txt long_covert_dist.txt 1 1000 .1 HG randRIP 2 0 target/covert/$i-covert
	java STOTEM syllgram_long_overt_n_c.txt nocoda_cons_overt_dist.txt 1 1000 .1 OT newRIP 2 0 target/nc/$i-nc
	java STOTEM syllgram_long_overt_n_c.txt nocoda_cons_overt_dist.txt 1 1000 .1 OT randRIP 2 0 target/nc/$i-nc
	java STOTEM syllgram_long_overt_n_c.txt nocoda_cons_overt_dist.txt 1 1000 .1 HG newRIP 2 0 target/nc/$i-nc
	java STOTEM syllgram_long_overt_n_c.txt nocoda_cons_overt_dist.txt 1 1000 .1 HG randRIP 2 0 target/nc/$i-nc
	java STOTEM syllgram_long_overt_c_n.txt cons_nocoda_overt_dist.txt 1 1000 .1 OT newRIP 2 0 target/cn/$i-cn
	java STOTEM syllgram_long_overt_c_n.txt cons_nocoda_overt_dist.txt 1 1000 .1 OT randRIP 2 0 target/cn/$i-cn
	java STOTEM syllgram_long_overt_c_n.txt cons_nocoda_overt_dist.txt 1 1000 .1 HG newRIP 2 0 target/cn/$i-cn
	java STOTEM syllgram_long_overt_c_n.txt cons_nocoda_overt_dist.txt 1 1000 .1 HG randRIP 2 0 target/cn/$i-cn
done
