// read command
// usage: java POEM grammar_file i_o_file sample_size num_samples ranking_bias
// grammar_file contains all tableaux, i_o_file contains possible inputs, morphemes, outputs, & frequencies

public class POEM {

    public static DistFile df;
    public static GrammarFile gf;
    public static RandomExtension gr;
    public static RandomExtension prior;

    private static int sample_size = 0;
    private static int num_samples = 0;
    private static int rank_bias = 0;
    private static boolean speed_up = false;
    private static boolean verbose = false;

    public static void main(String[] args) {
	if (args.length < 8) {
	    System.out.println("usage: java POEM grammar_file dist_file sample_size iterations init_bias rank_bias learner_type speed_up verbose");
	    System.exit(-1);
	}

	// read in a grammar_file
	gf = new GrammarFile(args[0]);

	// read in i_o_file
	df = new DistFile(args[1]);
	//	df.phono = false;

	sample_size = Integer.parseInt(args[2]);
	num_samples = Integer.parseInt(args[3]);
        int init_bias = Integer.parseInt(args[4]);
	rank_bias = Integer.parseInt(args[5]);
        int learner = Integer.parseInt(args[6]);
	if(args.length >8) {
	    speed_up = (Integer.parseInt(args[7]) == 0) ? false : true;
	    verbose = (Integer.parseInt(args[8]) == 0) ? false : true;
	}
	if(verbose){
	    System.out.println("\nLEXICON:\n" + df);
	}
	// initialize grammar to uniform - make ll_grammar
	gr = new RandomExtension(gf);
	prior = new RandomExtension(gf);
	prior.bias_grammar();
	if (init_bias == 1){
	    gr.bias_grammar();
	}
	if (verbose){
	    System.out.println("\nGRAMMAR:\n" + gr);
	}
	if(learner == 1){
	    learn_batch_parameter_EM();
	}else if (learner == 2){
	    learn_sample_parameter();
	}else if (learner == 3){
	    learn();
	}else if (learner == 4){
	    learn_online_pos_neg();
	}

    }

    public static void learn_batch_parameter_EM() {
	// there are i iterations of EM

	long startTime = System.currentTimeMillis();
	double prev_error = 1000.0;
	int i = 0;
	for (i = 0; i < num_samples; i++) {
	    long startIterTime = System.currentTimeMillis();
	    if (verbose) {
		System.out.println("Starting iteration " + i);
	    }
	    //to store successes for each output
	    double[][][] sum = new double[gr.grammar.length][gr.grammar.length][df.outputs.length];

	    //for each parameter value ...
	    for(int r=0; r < gr.grammar.length; r++){
		for(int c=0; c < r; c++){
		    double old_rc = gr.grammar[r][c];
		    double old_cr = gr.grammar[c][r];

		    if (gr.grammar[r][c] != 0.0 && gr.grammar[r][c] != 1.0){
			//temporarily set r >> c
			//System.out.println("Parameter Setting " + gr.constraints[r] + " - " + r + " >> " + gr.constraints[c] + " - " + c);
			gr.grammar[r][c] = 1.0;
			gr.grammar[c][r] = 0.0;

			double[][] ext = gr.cloneGrammar();
			gr.makeMeConsistent(ext);
			//burn in sampler...
			//double[][] seed = gr.generate_extension();
			//double[][] single = gr.generate_extension1(seed);
			//for (int b = 0; b < 10000; b++){
			//single = gr.generate_extension1(seed);
			//}
			//need to sample s rankings for r >> c

			double[][] single;

			for (int s = 0; s < sample_size; s++) {
			    single = gr.generate_extension(ext);
			    if(single != null){
				int[] rank = gr.find_order(single);
				//System.out.println("\t\tSampled the ranking: " + gr.rankToString(rank));
				//go through each output
				for(int o=0; o < df.outputs.length; o++){
				    DistFile.Output output = df.outputs[o];
				    //go through each UR for the output
				    for(int in=0; in < output.dist.length; in++){
					String input = output.inputs[in];
					String winner = optimize(input, rank);
					if (winner.equals(output.form)){
					    sum[r][c][o]++;
					}
				    }
				}
			    } //else{
			    //System.out.println("Found inconsistent sample for parameters " + r + " >> " + c + "----Grammar:\n" + gr.gramToString(gr.grammar));
			    //}
			}
			//done sampling r >> c...

			//temporarily set c >> r
			//System.out.println("Parameter Setting " + gr.constraints[c] + " - " + c + " >> " + gr.constraints[r] + " - " + r);

			gr.grammar[r][c] = 0.0;
			gr.grammar[c][r] = 1.0;
			ext = gr.cloneGrammar();
			gr.makeMeConsistent(ext);

			for (int s = 0; s < sample_size; s++) {
			    single = gr.generate_extension(ext);
			    if(single != null){
				int[] rank = gr.find_order(single);
				//System.out.println("\t\tSampled the ranking: " + gr.rankToString(rank));
				//go through each output
				for(int o=0; o < df.outputs.length; o++){
				    DistFile.Output output = df.outputs[o];
				    //go through each UR for the output
				    for(int in=0; in < output.dist.length; in++){
					String input = output.inputs[in];
					String winner = optimize(input, rank);
					if (winner.equals(output.form)){
					    sum[c][r][o]++;
					}
				    }
				}
			    } //else{
			    //System.out.println("Found inconsistent sample for parameters " + c + " >> " + r + "----Grammar:\n" + gr.gramToString(gr.grammar));
			    //}
			}
			//done sampling c >> r...

			//done sampling for this parameter, reset grammar
			gr.grammar[r][c] = old_rc;
			gr.grammar[c][r] = old_cr;
            }
		}
	    } //done going through parameters

	    //now going to reset the grammar...
	    double[][][] output_probs = new double[df.outputs.length][gr.grammar.length][gr.grammar.length];
	    for(int r=0; r < gr.grammar.length; r++){
		for(int c=0; c < r; c++){
		    //System.out.println("UPDATING " + r + " vs " + c);
		    double rc_sum = 0.0;
		    double cr_sum = 0.0;
		    for(int o=0; o < df.outputs.length; o++){
			double o_prob = ((double)sum[r][c][o] +.0001)* gr.grammar[r][c] + ((double)sum[c][r][o] +.0001)* gr.grammar[c][r];
			double o_rc = (((double)sum[r][c][o] +.0001)* ((double)df.outputs[o].freq))/((double)o_prob);
			double o_cr = (((double)sum[c][r][o] +.0001)* ((double)df.outputs[o].freq))/((double)o_prob);
			//System.out.println("\tUPDATES for " + df.outputs[o].form + " are " + o_rc*gr.grammar[r][c] + " vs " + o_cr*gr.grammar[c][r] );
			    rc_sum += o_rc;
			    cr_sum += o_cr;
		    }

		    double new_rc = ((double)rc_sum*gr.grammar[r][c])/((double)rc_sum*gr.grammar[r][c]+(double)cr_sum*gr.grammar[c][r]);
		    double new_cr = 1 - new_rc;
		    if ( ! speed_up) {
			gr.grammar[r][c] = new_rc;
			gr.grammar[c][r] = 1-gr.grammar[r][c];
		    }
		    /*
		    // this skews the re-estimate toward 0 and 1
		    double ratio = (new_rc+.01)/(new_cr+.01);
		    gr.grammar[r][c] = .2*ratio*ratio/(ratio*ratio+1) + .8*gr.grammar[r][c];
		    gr.grammar[c][r] = 1-gr.grammar[r][c];
		    */


		    if (speed_up) {
			// this is an attempt to increase the learning rate...
			if (new_rc > gr.grammar[r][c]) { //bring closer to 1
			    gr.grammar[r][c] = .5*1 + .5*new_rc;
			    gr.grammar[c][r] = 1-gr.grammar[r][c];
			}else if (gr.grammar[r][c] > new_rc){ //bring closer to 0
			    gr.grammar[r][c] = .5*new_rc + .5*0;
			    gr.grammar[c][r] = 1-gr.grammar[r][c];
			}
		    }
		}
	    }

	    if (! gr.makeMeConsistent()) {
		System.out.println ("Entire Grammar is Inconsistent --- Exiting:\n" + gr);
		System.exit(-1);
	    }

	    if(verbose){
		System.out.println("The new grammar is:\n" + gr.gramToString(gr.grammar));
	    }
	    if(i % 1 == 0){

		//now going to examine resulting grammar
		if (verbose){
		    System.out.println("------------------EVALUATING--------------GRAMMAR------AT-------ITERATION-------" + i);
		    if (evaluate_grammar(1000, i, true)){
			System.out.println("-reached perfection early ----- exiting now");
			break;
		    }
		}
		else{
		    if (evaluate_grammar(100, i)){
			System.out.println("-reached perfection early ----- exiting now");
			break;
		    }
		}

		/*if (i % 50 == 0) {
		    System.out.println("CHECKING LEARNING: current error is " + recent_error + " and 50 iterations ago is was " + prev_error + " and ratio was " + prev_error/recent_error);

		    if (prev_error/recent_error < 0.9 && i > 100) {
			System.out.println("INSUFFICIENT LEARNING: current error is " + recent_error + " and 50 iterations ago is was " + prev_error);
			System.exit(0);
		    }
		    prev_error = recent_error;
		    }*/
		/*System.out.println("Now evaluating mode grammar---");
		double[][] temp = gr.grammar;
		gr.grammar = gr.mode_grammar();
		evaluate_grammar(100, i);
		gr.grammar = temp;
		*/
	    }



	    long endIterTime = System.currentTimeMillis();
	    if (verbose){
		System.out.println("TIMING:: Iteration " + i + " took " + ((endIterTime-startIterTime)/1000.0) + " sec.  Total: " + ((endIterTime-startTime)/1000.0) + " sec. Average: " + ((endIterTime-startTime)/1000.0)/(i+1) + " sec.");
	    }
	} //end of iterations

	//now going to examine resulting grammar
	System.out.println("------------------EVALUATING-------------FINAL----------------GRAMMAR--------------------");
	System.out.println("The final grammar is:\n" + gr.gramToString(gr.grammar));
	evaluate_grammar(10000, i, true);
	/*System.out.println("Now evaluating mode grammar---");
	double[][] temp = gr.grammar;
	gr.grammar = gr.mode_grammar();
	evaluate_grammar(100, i);
	*/
    }


public static void learn_batch_maximization() {
	// there are i iterations of maximization
	int i = 0;
	for (i = 0; i < num_samples; i++) {

	    double[][] odds = new double[gr.grammar.length][gr.grammar.length];

	    //for each parameter value ...
	    for(int r=0; r < gr.grammar.length; r++){
		for(int c=0; c < r; c++){
		    double old_rc = gr.grammar[r][c];
		    double old_cr = gr.grammar[c][r];

		    //temporarily set r >> c
		    //System.out.println("Setting " + gr.constraints[r] + " - " + r + " >> " + gr.constraints[c] + " - " + c);
		    gr.grammar[r][c] = 1.0;
		    gr.grammar[c][r] = 0.0;

		    //to store successes for each output
		    double[] sum_rc = new double[df.outputs.length];
		    double[] sum_cr = new double[df.outputs.length];

		    double[][] single = gr.generate_extension();

		    for (int s = 0; s < sample_size; s++) {
			single = gr.generate_extension();
			if(single != null){
			    int[] rank = gr.find_order(single);
			    //System.out.println("\t\tSampled the ranking: " + gr.rankToString(rank));
			    //go through each output
			    for(int o=0; o < df.outputs.length; o++){
				DistFile.Output output = df.outputs[o];
				//go through each UR for the output
				for(int in=0; in < output.dist.length; in++){
				    String input = output.inputs[in];
				    String winner = optimize(input, rank);
				    if (winner.equals(output.form)){
					sum_rc[o]++;
				    }
				}
			    }
			}
		    }
		    //done sampling r >> c...
		    for (int o=0; o < df.outputs.length; o++){
			//System.out.println("total successes for " + gr.constraints[r] + " - " + r + " >> " + gr.constraints[c] + " - " + c + " for output " + df.outputs[o].form +  " = " + sum_rc[o]);
			odds[r][c] += Math.log(sum_rc[o] + 1) * ((double)df.outputs[o].freq);
		    }

		    //temporarily set c >> r
		    //System.out.println("Setting " + gr.constraints[c] + " - " + c + " >> " + gr.constraints[r] + " - " + r);

		    gr.grammar[r][c] = 0.0;
		    gr.grammar[c][r] = 1.0;

		    for (int s = 0; s < sample_size; s++) {
			single = gr.generate_extension();
			if(single != null){
			    int[] rank = gr.find_order(single);
			    //System.out.println("\t\tSampled the ranking: " + gr.rankToString(rank));
			    //go through each output
			    for(int o=0; o < df.outputs.length; o++){
				DistFile.Output output = df.outputs[o];
				//go through each UR for the output
				for(int in=0; in < output.dist.length; in++){
				    String input = output.inputs[in];
				    String winner = optimize(input, rank);
				    if (winner.equals(output.form)){
					sum_cr[o]++;
				    }
				}
			    }
			}
		    }
		    //done sampling c >> r...
		    for (int o = 0; o < df.outputs.length; o++){
			//System.out.println("total successes for " + gr.constraints[c] +  " - " + c + " >> " + gr.constraints[r] + " - " + r + " for output " + df.outputs[o].form +  " = " + sum_cr[o]);
			odds[c][r] += Math.log(sum_cr[o] + 1) * ((double)df.outputs[o].freq) ;
		    }
		    //done sampling for this parameter, reset grammar
		    gr.grammar[r][c] = old_rc;
		    gr.grammar[c][r] = old_cr;

		    //incorporate the prior probs of rc vs cr
		    //System.out.println("numerator for " + gr.constraints[r] + " - " + r + " >> " + gr.constraints[c] + " - " + c + " = " + odds[r][c]);
		    //System.out.println("numerator for " + gr.constraints[c] + " - " + c + " >> " + gr.constraints[r] + " - " + r + " = " + odds[c][r]);


		    odds[r][c] = (odds[r][c]) + Math.log(gr.grammar[r][c]+.0001);
		    //System.out.println("numerator w/ prior for " + gr.constraints[r] + " - " + r + " >> " + gr.constraints[c] + " - " + c + " = " + odds[r][c]);
		    odds[c][r] = (odds[c][r]) + Math.log(gr.grammar[c][r]+.0001);
		    //System.out.println("numerator w/ prior for " + gr.constraints[c] + " - " + c + " >> " + gr.constraints[r] + " - " + r + " = " + odds[c][r]);
		}
	    } //done going through parameters

	    //now going to reset the grammar...
	    for(int r=0; r < gr.grammar.length; r++){
		for(int c=0; c < r; c++){
		    double log_odds = odds[r][c] - odds[c][r];
		    double log_p = log_odds - Math.log(1 + Math.exp(log_odds));
		    gr.grammar[r][c] = Math.exp(log_p);
		    gr.grammar[c][r] = 1 - gr.grammar[r][c];
		}
	    }

	    if (! gr.makeMeConsistent()) {
		System.out.println ("Entire Grammar is Inconsistent --- Exiting:\n" + gr);
		System.exit(-1);
	    }

	    System.out.println("The new grammar is:\n" + gr.gramToString(gr.grammar));
    	    //now going to examine resulting grammar
	    System.out.println("------------------EVALUATING--------------GRAMMAR------AT-------ITERATION-------" + i);
	    if (evaluate_grammar(1000, i)){
		System.out.println("-reached perfection early ----- exiting now");
		break;
	    }

	} //end of iterations

	//now going to examine resulting grammar
	System.out.println("------------------EVALUATING-------------FINAL----------------GRAMMAR--------------------");
	evaluate_grammar(10000, i);
    }

    public static void learn_sample_parameter() {

	// there are i iterations of sampling and updating
	//double corr_tot = 0;
	double[][] single = gr.generate_extension();
	int i = 0;
	double rate = .25;
	for (i = 0; i < num_samples; i++) {
	    if (verbose) {
		System.out.println("Starting iteration " + i);
	    }
	    double[][] corr_ranks_samp = new double[gr.grammar.length][gr.grammar.length];
	    //each iteration consists of s samples
	    for (int s = 0; s < sample_size; s++) {

		//sample an output form
		DistFile.Output output = null;
		String winner = "tat";
		double rand = Math.random();
		int o_index = 0;
		for(int o=0; o < df.outputs.length; o++){
		    rand -= df.outputs[o].relfreq;
		    if(rand <= 0){
			output = df.outputs[o];
			o_index = o;
			break;
		    }
		}
		//System.out.println("Sampled output form: " + output.form);

		//sample a ur for the output form
		String input = "";
		rand = Math.random();
		for(int in=0; in < output.dist.length; in++){
		    rand -= output.dist[in];
		    if(rand <= 0){
			input = output.inputs[in];
			break;
		    }
		}
		//System.out.println("\tSampled a UR form: " + input);

		double[][] output_counts = new double[gr.grammar.length][gr.grammar.length];

		for(int r=0; r < gr.grammar.length; r++){
		    for(int c=0; c < r; c++){

			if ((gr.grammar[r][c] == 0) || (gr.grammar[r][c] == 1)){
			    if (verbose){
				//System.out.println(" r=" + r + " by c=" + c + " is set, not worth checking probs");
			    }
			    output_counts[r][c] = gr.grammar[r][c];
			    output_counts[c][r] = gr.grammar[c][r];
			}else{

			    double old_rc = gr.grammar[r][c];
			    double old_cr = gr.grammar[c][r];

			    //temporarily set r >> c
			    //System.out.println("Setting " + gr.constraints[r] + " - " + r + " >> " + gr.constraints[c] + " - " + c);
			    gr.grammar[r][c] = 1.0;
			    gr.grammar[c][r] = 0.0;

			    int count = 0;
			    double[][] ext = gr.cloneGrammar();
			    gr.makeMeConsistent(ext);
			    while (count < 50){
				//sample a random ranking
				count++;
				single = gr.generate_extension(ext);
				if(single != null){
				    int[] rank = gr.find_order(single);
				    //System.out.println("\t\tSampled the ranking: " + gr.rankToString(rank));

				    if(rank != null){
					//compute learner's winner and compare to actual output
					winner = optimize(input, rank);
					//if equal, add matrix of ranking into collected samples
					if (winner.equals(output.form)){
					    //System.out.println("\t\t" + output.form + " was correctly generated as " + winner);
					    output_counts[r][c]++;
					}
				    }else{
					System.out.println("Rank was nULL!");
				    }
				}
			    }

			    gr.grammar[r][c] = 0.0;
			    gr.grammar[c][r] = 1.0;

			    ext = gr.cloneGrammar();
			    gr.makeMeConsistent(ext);
			    count = 0;
			    while (count < 50){
				//sample a random ranking
				count++;

				single = gr.generate_extension(ext);
				if(single != null){
				    int[] rank = gr.find_order(single);
				    //System.out.println("\t\tSampled the ranking: " + gr.rankToString(rank));
				    if(rank != null){
					//compute learner's winner and compare to actual output
					winner = optimize(input, rank);
					//if equal, add matrix of ranking into collected samples
					if (winner.equals(output.form)){
					    //System.out.println("\t\t" + output.form + " was correctly generated as " + winner);
					    output_counts[c][r]++;
					}
				    }else{
					System.out.println("Rank was nULL!");
				    }
				}
			    }
			    //reset grammar
			    gr.grammar[r][c] = old_rc;
			    gr.grammar[c][r] = old_cr;
			}
		    }
		}
		for(int r=0; r < gr.grammar.length; r++){
		    for(int c=0; c < r; c++){
			double o_prob = ((double)output_counts[r][c]) * gr.grammar[r][c] + ((double)output_counts[c][r]) * gr.grammar[c][r];
			if (o_prob != 0){
			    corr_ranks_samp[r][c] += ((double)output_counts[r][c])/((double)o_prob);
			    corr_ranks_samp[c][r] += ((double)output_counts[c][r])/((double)o_prob);
			}
		    }
		}
	    }

	    for (int r=0; r < gr.grammar.length; r++) {
		for (int c=0; c < r; c++) {
		    double old_r = gr.grammar[r][c];
		    double new_rc;
		    if (corr_ranks_samp[r][c]+corr_ranks_samp[c][r] != 0){
			new_rc = (((double)corr_ranks_samp[r][c])*gr.grammar[r][c])/(((double)corr_ranks_samp[r][c])*gr.grammar[r][c] + ((double)corr_ranks_samp[c][r])*gr.grammar[c][r]);
			if ((new_rc > .5) && (new_rc < .5)) {
			    new_rc = gr.grammar[r][c];
			}
		    }else{
			new_rc = 0.5;
		    }

		    if(sample_size != 1){
			//making learning rate higher...
			if (new_rc > gr.grammar[r][c]) { //bring closer to 1
			    gr.grammar[r][c] = .5*1 + .5*new_rc;
			    gr.grammar[c][r] = 1-gr.grammar[r][c];
			}else if (gr.grammar[r][c] > new_rc){ //bring closer to 0
			    gr.grammar[r][c] = .5*new_rc + .5*0;
			    gr.grammar[c][r] = 1-gr.grammar[r][c];
			}

		    }else{ //make a small update since we're processing only one output per update
			//go part of the way that EM would want
			gr.grammar[r][c] = (1-rate)*gr.grammar[r][c] + rate*new_rc;
			gr.grammar[c][r] = 1-gr.grammar[r][c];
		    }
		}
	    }

	    if (! gr.makeMeConsistent()) {
		System.out.println ("Entire Grammar is Inconsistent --- Exiting:\n" + gr);
		System.exit(-1);
	    }

	    //bias = bias*(i+1)/(i+2);
	    if(i % 1 == 0){

		if (verbose) {
		    System.out.println("The new grammar is:\n" + gr.gramToString(gr.grammar));
		}
		//now going to examine resulting grammar

		if (verbose){
		    System.out.println("------------------EVALUATING--------------GRAMMAR------AT-------ITERATION-------" + i);
		    if (evaluate_grammar(100, i, true)){
			System.out.println("-reached perfection early ----- exiting now");
			break;
		    }
		}
		else{
		    if (evaluate_grammar(100, i)){
			System.out.println("-reached perfection early ----- exiting now");
			break;
		    }
		}
	    }
	}
	//now going to examine resulting grammar

	System.out.println("------------------EVALUATING-------------FINAL----------------GRAMMAR--------------------");
	System.out.println("The final grammar is:\n" + gr.gramToString(gr.grammar));
	evaluate_grammar(10000, i, true);
    }

    public static void learn_online_new() { // this simply rewards successful rankings

	// there are i iterations of sampling and updating
	double corr_tot = 0;
	double bias = .01 * sample_size;
	double[][] single = gr.generate_extension();
	/*for (int b = 0; b < 1000; b++){
	    single = gr.generate_extension1(seed);
	    }*/
	double rate = .01;
	int i = 0;
	for (i = 0; i < num_samples; i++) {
	    //System.out.println("Starting iteration " + i);
	    double[][] corr_ranks_samp = new double[gr.grammar.length][gr.grammar.length];
	    double corr_samp = 0;

	    double[][] sample_counts = new double[gr.grammar.length][gr.grammar.length];
	    //rate = rate * 0.8;
	    //each iteration consists of s samples
	    for (int s = 0; s < sample_size; s++) {

		//sample an output form
		DistFile.Output output = null;
		String winner = "tat";
		double rand = Math.random();
		int o_index = 0;
		for(int o=0; o < df.outputs.length; o++){
		    rand -= df.outputs[o].relfreq;
		    if(rand <= 0){
			output = df.outputs[o];
			o_index = o;
			break;
		    }
		}
		//System.out.println("Sampled output form: " + output.form);

		//sample a ur for the output form
		String input = "";
		rand = Math.random();
		for(int in=0; in < output.dist.length; in++){
		    rand -= output.dist[in];
		    if(rand <= 0){
			input = output.inputs[in];
			break;
		    }
		}
		//System.out.println("\tSampled a UR form: " + input);

		boolean looking = true;
		int count = 0;
		single = gr.generate_extension();
		/*for (int b = 0; b < 1000; b++){
		    double[][] temp = gr.generate_extension1(single);
		    if (temp != null){
			single = temp;
		    }
		}
		*/
		double[][] output_counts = new double[gr.grammar.length][gr.grammar.length];
		double acceptance = 0.0;
		while ((looking) && (count < 100)){
		    //sample a random ranking
		    single = gr.generate_extension();
		    int[] rank = gr.find_order(single);
		    //System.out.println("\t\tSampled the ranking: " + gr.rankToString(rank));

		    //compute learner's winner and compare to actual output
		    winner = optimize(input, rank);
		    count++;
		    //if equal, add matrix of ranking into collected samples
		    if (winner.equals(output.form)){
			looking = false;
			//System.out.println("\t\t" + output.form + " was correctly generated as " + winner);
			corr_tot++;
			corr_samp++;
			for(int r=0; r < gr.grammar.length; r++){
			    for(int c=0; c < r; c++){
				if(single[r][c] == 1){// this means r >> c in this ranking
				    //corr_ranks_samp[r][c]++;
				    output_counts[r][c]++;
				}else{
				    //corr_ranks_samp[c][r]++;
				    output_counts[c][r]++;
				}
			    }
			}
		    }
		}
		for(int r=0; r < gr.grammar.length; r++){
		    for(int c=0; c < r; c++){
			if(!(looking)){
			    //go 1/100th of the way that EM would want
			    if(output_counts[r][c] == 1){ //increase [r][c]
				gr.grammar[r][c] = gr.grammar[r][c]*(1-rate) + 1*rate;
				gr.grammar[c][r] = 1-gr.grammar[r][c];
			    }else{ //increase [c][r]...
				gr.grammar[c][r] = gr.grammar[c][r]*(1-rate) + 1*rate;
				gr.grammar[r][c] = 1-gr.grammar[c][r];
			    }
			}
		    }
		}
	    }

	    if (! gr.makeMeConsistent()) {
		System.out.println ("Entire Grammar is Inconsistent --- Exiting:\n" + gr);
		System.exit(-1);
	    }

	    if(i % 1000 == 0){
		System.out.println("The new grammar is:\n" + gr.gramToString(gr.grammar));
		//now going to examine resulting grammar
		if (evaluate_grammar(100, i)){
		    System.out.println("-reached perfection early ----- exiting now");
		    break;
		}
		System.out.println("Now evaluating mode grammar---");
		double[][] temp = gr.grammar;
		gr.grammar = gr.mode_grammar();
		evaluate_grammar(10, i);
		gr.grammar = temp;
	    }
	}
	//now going to examine resulting grammar
	System.out.println("------------------EVALUATING-------------FINAL----------------GRAMMAR--------------------");
	evaluate_grammar(1000, i);
	System.out.println("Now evaluating mode grammar---");
	double[][] temp = gr.grammar;
	gr.grammar = gr.mode_grammar();
	evaluate_grammar(100, i);
    }

    public static void learn() { //updates by approximating P(ab), P(ba) over many samples

	// there are i iterations of sampling and updating
	double tot_err = 0.0;
	double corr_tot = 0;
	double bias = .01 * sample_size;
	double[][] single = gr.generate_extension();
	double rate = .5;
	int i = 0;
	for (i = 0; i < num_samples; i++) {
	    sample_size *= 1.5;

	    System.out.println("Starting iteration " + i);
	    double[][] corr_ranks_samp = new double[gr.grammar.length][gr.grammar.length];
	    double corr_samp = 0;

	    double[][] sample_counts = new double[gr.grammar.length][gr.grammar.length];
	    //each iteration consists of s samples
	    double[] sample_outputs = new double[df.outputs.length];
	    for (int s = 0; s < sample_size; s++) {
		//sample an output form
		DistFile.Output output = null;
		String winner = "tat";
		double rand = Math.random();
		int o_index = 0;
		for(int o=0; o < df.outputs.length; o++){
		    rand -= df.outputs[o].relfreq;
		    if(rand <= 0){
			output = df.outputs[o];
			o_index = o;
			sample_outputs[o]++;
			break;
		    }
		}
		//System.out.println("Sampled output form: " + output.form);

		//sample a ur for the output form
		String input = "";
		rand = Math.random();
		for(int in=0; in < output.dist.length; in++){
		    rand -= output.dist[in];
		    if(rand <= 0){
			input = output.inputs[in];
			break;
		    }
		}
		//System.out.println("\tSampled a UR form: " + input);

		boolean looking = true;
		int count = 0;
		single = gr.generate_extension();
		double[][] output_counts = new double[gr.grammar.length][gr.grammar.length];
		double acceptance = 0.0;
		while ((looking) && (count < 100)){
		    //sample a random ranking
		    single = gr.generate_extension();
		    int[] rank = gr.find_order(single);
		    //System.out.println("\t\tSampled the ranking: " + gr.rankToString(rank));

		    //compute learner's winner and compare to actual output
		    winner = optimize(input, rank);
		    count++;
		    //if equal, add matrix of ranking into collected samples
		    if (winner.equals(output.form)){
			looking = false;
			//System.out.println("\t\t" + output.form + " was correctly generated as " + winner);
			corr_tot++;
			corr_samp++;
			for(int r=0; r < gr.grammar.length; r++){
			    for(int c=0; c < r; c++){
				if(single[r][c] == 1){// this means r >> c in this ranking
				    //corr_ranks_samp[r][c]++;
				    output_counts[r][c]++;
				    //System.out.println("r,c = " + r + ", " + c + " should be 1 -" + single[r][c]);
				}else{
				    //corr_ranks_samp[c][r]++;
				    output_counts[c][r]++;
				    //System.out.println("c,r = " + c + ", " + r + " should be 1 -" + single[c][r]);
				}
			    }
			}
		    }
		    if(count == 1){
			    for(int r=0; r < gr.grammar.length; r++){
				for(int c=0; c < r; c++){
				    if(single[r][c] == 1){
					sample_counts[r][c] ++;
				    }else{
					sample_counts[c][r] ++;
				    }
				}
		   	 }
			}

		}
		if (looking){
		    //System.out.println("Never found a winner for UR->Output :: " + input + "->" + output.form);
		}

		//SILLY GAJA, put this in the found winner loop!
		for(int r=0; r < gr.grammar.length; r++){
		    for(int c=0; c < r; c++){
			if(!(looking)){
			    corr_ranks_samp[r][c] += ((double)output_counts[r][c])/((double)output_counts[r][c] + (double)output_counts[c][r]);
			    corr_ranks_samp[c][r] += ((double)output_counts[c][r])/((double)output_counts[c][r] + (double)output_counts[r][c]);
			}else{
			    corr_ranks_samp[r][c] += .5;
			    corr_ranks_samp[c][r] += .5;
			}
		    }
		}
		//System.out.println("For UR/Output:: " + input + "-->" + output.form + "-- The successful proportions are: \n" + gr.gramToString(output_counts));

		//System.out.println("Acceptance rate was :: " + (double)acceptance/1000);
	    }
	    double err = 0.0;
	    for(int r=0; r < gr.grammar.length; r++){
		for(int c=0; c < r; c++){
		    sample_counts[r][c] = sample_counts[r][c]/(sample_counts[r][c] + sample_counts[c][r]);
		    sample_counts[c][r] = 1-sample_counts[r][c];
		    err += Math.abs(sample_counts[r][c] - gr.grammar[r][c]);
		}
	    }
	    err = err/((gr.grammar.length*(gr.grammar.length-1))/2);
	    tot_err = ((tot_err * (i))+err)/(i+1);
	    System.out.println("Sampling Error is " + err);
	    System.out.println("Average Sampling Error per iteration is " + tot_err);

	    //System.out.println("The sampled grammar is:\n" + gr.gramToString(gr.grammar));
	    //System.out.println("The sampled proportions are: \n" + gr.gramToString(sample_counts));
	    for (int r=0; r < gr.grammar.length; r++) {
		for (int c=0; c < r; c++) {
		    double old_r = gr.grammar[r][c];
		    double new_rc = ((double)corr_ranks_samp[r][c])/((double)corr_ranks_samp[r][c] + (double)corr_ranks_samp[c][r]);
		    gr.grammar[r][c] = new_rc;//unbiased complete re-estimation
		    gr.grammar[c][r] = 1 - new_rc;

		    //slowing down learning rate
		    //gr.grammar[r][c] = (.5)*gr.grammar[r][c] + .5*new_rc;
		    //gr.grammar[c][r] = 1-gr.grammar[r][c];

		    /*
		    //making learning rate higher...
		    if (new_rc > gr.grammar[r][c]) { //bring closer to 1
			gr.grammar[r][c] = rate*1 + ((double)1-rate)*new_rc;
			gr.grammar[c][r] = 1-gr.grammar[r][c];
		    }else if (gr.grammar[r][c] > new_rc){ //bring closer to 0
			gr.grammar[r][c] = ((double)1-rate)*new_rc + rate*0;
			gr.grammar[c][r] = 1-gr.grammar[r][c];
		    }
		    */
		    /*
		    double ratio = (gr.grammar[r][c]+.01)/(gr.grammar[c][r]+.01);
		    gr.grammar[r][c] = .05*ratio*ratio/(ratio*ratio+1) + .95*gr.grammar[r][c];
		    gr.grammar[c][r] = 1-gr.grammar[r][c];
		    */
		    /*
		    if(gr.grammar[r][c] > .98){
			gr.grammar[r][c] = 1.0;
			gr.grammar[c][r] = 0.0;
		    }else if (gr.grammar[r][c] < .02){
			gr.grammar[r][c] = 0.0;
			gr.grammar[c][r] = 1.0;

		    }
		    */
		    if(rank_bias == 1){
			//biasing using beta prior with alpha=1, beta=2 or vice versa
			if(prior.grammar[r][c] > .5){
			    gr.grammar[r][c] = (corr_ranks_samp[r][c]+bias)/(corr_samp+bias);
			}else if (prior.grammar[r][c] < .5){
			    gr.grammar[r][c] = (corr_ranks_samp[r][c])/(corr_samp+bias);
			}
		    }
		}
	    }

	    if (! gr.makeMeConsistent()) {
		System.out.println ("Entire Grammar is Inconsistent --- Exiting:\n" + gr);
		System.exit(-1);
	    }

	    //bias = bias*(i+1)/(i+2);
	    if(i % 1 == 0){
		System.out.println("The new grammar is:\n" + gr.gramToString(gr.grammar));

		//now going to examine resulting grammar
		if (evaluate_grammar(100, i)){
		    System.out.println("-reached perfection early ----- exiting now");
		    break;
		}

		System.out.println("Now evaluating mode grammar---");
		double[][] temp = gr.grammar;
		gr.grammar = gr.mode_grammar();
		evaluate_grammar(10, i);
		gr.grammar = temp;

	    }
	}
	//now going to examine resulting grammar
	System.out.println("------------------EVALUATING-------------FINAL----------------GRAMMAR--------------------");
	evaluate_grammar(1000, i);
	System.out.println("Now evaluating mode grammar---");
	double[][] temp = gr.grammar;
	gr.grammar = gr.mode_grammar();
	evaluate_grammar(100, i);
    }

    public static void learn_online() { //supposed to be the stepwise EM with mini-batch in Liang & Klein

	// there are i iterations of sampling and updating

	//this number determines the starting counts, amount of 'smoothing'
	double init_count = 1;
	double corr_tot = init_count;
	double bias = .005*init_count;

	double[][] single = gr.generate_extension();
	double[][] rank_counts = new double[gr.grammar.length][gr.grammar.length];
	double[][] batch_counts = new double[gr.grammar.length][gr.grammar.length];

	int batch = 30;

	for (int r=0; r < gr.grammar.length; r++) {
	    for (int c=0; c < gr.grammar.length; c++) {
		rank_counts[r][c] = init_count/2;
	    }
	}
	int i = 0;
	for (i = 0; i < num_samples; i++) {

	    double[][] output_counts = new double[gr.grammar.length][gr.grammar.length];
	    //sample an output form
	    DistFile.Output output = null;
	    String winner = "tat";
	    double rand = Math.random();
	    int o_index = 0;
	    for(int o=0; o < df.outputs.length; o++){
		rand -= df.outputs[o].relfreq;
		if(rand < 0){
		    output = df.outputs[o];
		    o_index = o;
		    break;
		}
	    }
	    //System.out.println("Sampled output form: " + output.form);

	    //sample a ur for the output form
	    String input = "";
	    rand = Math.random();
	    for(int in=0; in < output.dist.length; in++){
		rand -= output.dist[in];
		if(rand < 0){
		    input = output.inputs[in];
		    break;
		}
	    }
	    //System.out.println("\tSampled a UR form: " + input);

	    boolean looking = true;
	    int count = 0;

	    while ((looking) && (count < 1000)){
		//sample a random ranking
		single = gr.generate_extension();
		if (single != null){
		    int[] rank = gr.find_order(single);
		    //System.out.println("\t\tSampled the ranking: " + gr.rankToString(rank));

		    //compute learner's winner and compare to actual output
		    winner = optimize(input, rank);
		    count++;
		    //if equal, update grammar, rewarding current ranking
		    if (winner.equals(output.form)){
			looking = false;
			//System.out.println("\t\t" + output.form + " was correctly generated as " + winner);
			corr_tot++;
			for(int r=0; r < gr.grammar.length; r++){
			    for(int c=0; c < gr.grammar.length; c++){
				if(single[r][c] == 1){// this means r >> c in this ranking
				    output_counts[r][c]++;
				    //add counts for the correct ranking into rank counts

				}
			    }
			}
		    }
		}
	    }

	    //if parsing was successful, update grammar
	    if (looking ==false){
		//update grammar using new counts
		for (int r=0; r < gr.grammar.length; r++) {
		    for (int c=0; c < gr.grammar.length; c++) {

			double eta = Math.pow((i + 2), -.5);

			double new_rc = ((double)output_counts[r][c])/((double)output_counts[r][c] + (double)output_counts[c][r]);
			double new_cr = ((double)output_counts[c][r])/((double)output_counts[c][r] + (double)output_counts[r][c]);
			if(looking){
			    new_rc = .5;
			    new_cr = .5;
			}

			batch_counts[r][c] += new_rc;
			batch_counts[c][r] += new_cr;

			if((i+1) % batch == 0){ //re-estimate after batch
			    new_rc = ((double)batch_counts[r][c])/((double)batch_counts[r][c] + (double)batch_counts[c][r] + .0001);
			    new_cr = ((double)batch_counts[c][r])/((double)batch_counts[c][r] + (double)batch_counts[r][c] + .0001);

			    rank_counts[r][c] = eta*new_rc + ((double)1.0-eta)*rank_counts[r][c];
			    rank_counts[c][r] = eta*new_cr + ((double)1.0-eta)*rank_counts[c][r];

			    gr.grammar[r][c] = (rank_counts[r][c])/(rank_counts[r][c] + rank_counts[c][r]);  //unbiased complete re-estimation
			    gr.grammar[c][r] = 1-gr.grammar[r][c];

			    double ratio = (gr.grammar[r][c]+.01)/(gr.grammar[c][r]+.01);
			    gr.grammar[r][c] = .025*ratio*ratio/(ratio*ratio+1) + .975*gr.grammar[r][c];
			    gr.grammar[c][r] = 1-gr.grammar[r][c];

			    batch_counts = new double[gr.grammar.length][gr.grammar.length];
			}
		    }
		}
	    }

	    if (! gr.makeMeConsistent()) {
		System.out.println ("Entire Grammar is Inconsistent --- Exiting:\n" + gr);
		System.exit(-1);
	    }

	    //now going to examine resulting grammar
	    if ((i % 100) == 0){
		System.out.println("Finished iteration " + i);
		System.out.println("The new grammar is:\n" + gr.gramToString(gr.grammar));
		if (evaluate_grammar(100, i)){
		    System.out.println("-reached perfection early ----- exiting now");
		    break;
		}
	    }

	}
	//now going to examine resulting grammar
	System.out.println("------------------EVALUATING-------------FINAL----------------GRAMMAR--------------------");
	evaluate_grammar(10000, i);
    }

public static void learn_online_pos_neg() {
    //this is the naive parameter learner from Yang (2002), linear update rule
       	//this number determines the learning rate
	double rate = .1;
	int i = 0;
	double[][] tries = new double[gr.grammar.length][gr.grammar.length];
	double[][] successes = new double[gr.grammar.length][gr.grammar.length];
	double[][] failures = new double[gr.grammar.length][gr.grammar.length];

	// there are i iterations of sampling and updating
	for (i = 0; i < num_samples; i++) {
	    //sample an output form
	    DistFile.Output output = null;
	    String winner = "tat";
	    double rand = Math.random();
	    int o_index = 0;
	    for(int o=0; o < df.outputs.length; o++){
		rand -= df.outputs[o].relfreq;
		if(rand < 0){
		    output = df.outputs[o];
		    o_index = o;
		    break;
		}
	    }
	    //System.out.println("Sampled output form: " + output.form);

	    //sample a ur for the output form
	    String input = "";
	    rand = Math.random();
	    for(int in=0; in < output.dist.length; in++){
		rand -= output.dist[in];
		if(rand < 0){
		    input = output.inputs[in];
		    break;
		}
	    }
	    //System.out.println("\tSampled a UR form: " + input);

	    //sample a random ranking
	    double[][] single = gr.generate_extension();
	    int[] rank = gr.find_order(single);
	    //System.out.println("\t\tSampled the ranking: " + gr.rankToString(rank));

	    //compute learner's winner and compare to actual output
	    winner = optimize(input, rank);

	    // Bayes' update
	    if (winner.equals(output.form)){
		for(int r=0; r < gr.grammar.length; r++){
		    for(int c=0; c < r; c++){
			if(single[r][c] == 1){ // this means r >> c in this ranking
			    successes[r][c] += 1;
			    tries[r][c] += 1;
			}else{ // this means c >> r in this ranking
			    successes[c][r] += 1;
			    tries[c][r] += 1;
			}
			if((i % sample_size) == 0){
			    double rc = successes[r][c]/tries[r][c];
			    double cr = successes[c][r]/tries[c][r];
			    if(rc > cr){
				gr.grammar[r][c] = gr.grammar[r][c] + (rc/(rc+cr))  * (1- gr.grammar[r][c]);
				gr.grammar[c][r] = 1- gr.grammar[r][c];
			    }else if (cr > rc){
				gr.grammar[c][r] = gr.grammar[c][r] + (cr/(rc+cr))  * (1- gr.grammar[c][r]);
				gr.grammar[r][c] = 1- gr.grammar[c][r];
			    }
			    successes[r][c] = 1;
			    successes[c][r] = 1;
			    tries[r][c] = 2;
			    tries[c][r] = 2;
			}

		    }
		}
	    } else{ //parsing was unsuccessful... punish ranking
		for(int r=0; r < gr.grammar.length; r++){
		    for(int c=0; c < r; c++){
			if(single[r][c] == 1){ // this means r >> c in this ranking
			    tries[r][c] +=1;
			}else{ // this means c >> r in this ranking
			    tries[c][r] +=1;
			}

			if((i % sample_size) == 0){
			    double rc = successes[r][c]/tries[r][c];
			    double cr = successes[c][r]/tries[c][r];
			    if(rc > cr){
				gr.grammar[r][c] = gr.grammar[r][c] + (rc/(rc+cr))  * (1- gr.grammar[r][c]);
				gr.grammar[c][r] = 1- gr.grammar[r][c];
			    }else if (cr > rc){
				gr.grammar[c][r] = gr.grammar[c][r] + (cr/(rc+cr))  * (1- gr.grammar[c][r]);
				gr.grammar[r][c] = 1- gr.grammar[c][r];
			    }
			    successes[r][c] = 1;
			    successes[c][r] = 1;
			    tries[r][c] = 2;
			    tries[c][r] = 2;
			}

		    }
		}
	    }
	    if (! gr.makeMeConsistent()) {
		System.out.println ("Entire Grammar is Inconsistent --- Exiting:\n" + gr);
		System.exit(-1);
	    }

	    //now going to examine resulting grammar
	    if ((i % 100000) == 0){
		System.out.println("Finished iteration " + i);
		//rate = rate * .8;
		System.out.println("The new grammar is:\n" + gr.gramToString(gr.grammar));
		if (evaluate_grammar(100, i)){
		    System.out.println("-reached perfection early ----- exiting now");
		    break;
		}
	    }

	}
	//now going to examine resulting grammar
	System.out.println("------------------EVALUATING-------------FINAL----------------GRAMMAR--------------------");
	evaluate_grammar(1000, i);
    }

    public static boolean evaluate_grammar(int s, int i) {
	return evaluate_grammar(s, i, false);
    }

    public static boolean evaluate_grammar(int s, int i, boolean printout) {
	//now going to examine resulting grammar
	double log_likelihood = 0;
	int tot = 0;
	int corr = 0;
	double error = 0.0;
	for(int o=0; o < df.outputs.length; o++){ //for each output form

	    DistFile.Output output = null;
	    String winner = "";
	    double rand = Math.random();
	    output = df.outputs[o];

	    //sample s times to check distribution
	    for( int c=0; c<s; c++){
		//sample a ur for the output form
		String input = "";
		rand = Math.random();
		for(int in=0; in < output.dist.length; in++){
		    rand -= output.dist[in];
		    if(rand < 0){
			input = output.inputs[in];
			break;
		    }
		}
		//sample a random ranking
		double[][] single = gr.generate_extension();
		if (single != null){
		    int[] rank = gr.find_order(single);

		    //compute learner's winner and compare to actual output
		    winner = optimize(input, rank);

		    //if equal, add matrix of ranking into collected samples
		    if (winner.equals(output.form)){
			corr++;
		    }
		    tot++;
		}
	    }
	    if(printout){
		System.out.println("Output " + output.form + " " + ((float)corr/tot) + " correct - actual freq is " + output.freq);
	    }
	    log_likelihood += Math.log(((float)corr/tot))*output.freq;
	    error += (((double)tot-(double)corr)/tot)*(double)output.freq;
	    corr=0;
	    tot=0;
	}
	System.out.println("ITERATION " + i + ":: Total error is " + error + " and log likelihood is " + log_likelihood);

        double recent_error = error;
	if (error == 0.0){
	    return true;
	}else{
	    return false;
	}
    }

    public static String optimize (String input, int[] rank) {

	//find the tableau
	GrammarFile.Tableau tab = null;
	for (int i=0; i < gf.tableaux.length; i++){
	    if (gf.tableaux[i].uf.equals(input)){
		tab = gf.tableaux[i];
	    }
	}

	//create array that stores information about which candidates are still in the running
	int[] survivors = new int[tab.cands.length];
	int num_sur = survivors.length;
	for (int i=0; i < survivors.length; i++) {
	    survivors[i] = 1;
	}
	int size = rank.length;
	while ((num_sur > 1) && (size >= 0) ) {
	    for (int j=0; j < rank.length; j++) {
		//figuring out minimum violation for remaining candidates
		int min_vios = -1;
		for (int i=0; i < survivors.length; i++) {
		    if (survivors[i] == 1) {
			if (min_vios == -1){
			    min_vios = tab.cands[i].violations[rank[j]];
			}else if (tab.cands[i].violations[rank[j]] < min_vios) {
			    min_vios = tab.cands[i].violations[rank[j]];
			}
		    }
		}

		//System.out.println("looking at constraint " + rank[j] + " whose minimum is " + min_vios);
		for (int i=0; i < survivors.length; i++) {
		    if ((tab.cands[i].violations[rank[j]] > min_vios) && (survivors[i] ==1)) {
			//System.out.println("candidate " + tab.cands[i].oform + " getting knocked out");
			survivors[i] = 0;
			num_sur -= 1;
		    }
		}
		size--;
	    }
	}
	String winner = "";
	for (int i=0; i < survivors.length; i++) {
	    if (survivors[i] == 1) {
		winner = tab.cands[i].oform;
		//System.out.println("winner for input " + input + " and rank " + gr.rankToString(rank) + " is " + winner);
	    }
	}
	return winner;
    }
}
