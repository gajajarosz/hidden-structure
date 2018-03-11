package learner;
// This is the main file for the EDL learner
// usage: java EDL grammar_file dist_file iterations final_eval_sample learn_type gram_sample_size ranking_bias (print args) (maxdepth)
// grammar_file contains all tableaux, dist_file contains possible inputs, morphemes, outputs, & frequencies
import java.util.*;

public class EDL {

    public static DistFile df;
    public static GrammarFile gf;
    public static RandomExtension gr;
    public static RandomExtension prior;

    private static int gram_sample_size = 1;
    private static int iterations = 0;
    public static int final_eval = 0;
    public static int final_eval_sample = 1000;
    public static int mini_eval = 1;
    public static int mini_eval_freq = 100;
    public static int mini_eval_sample = 100;
    public static int quit_early = 100;
    public static int quit_early_sample = 100;
    public static int print_input = 0;
    public static int maxdepth = 8;
    public static HashMap<String, GrammarFile.Tableau> tabtable = new HashMap<String, GrammarFile.Tableau>();
    public static HashMap<String, PrefixTree> intable = new HashMap<String,PrefixTree>();
    public static Writer writer = new SystemWriter();


    public static void main(String[] args) {
	if (args.length < 7) {
	    writer.println("Too few arguments have been provided to run the program. Exiting...\nusage: run grammar_file dist_file iterations final_eval_sample learn_type gram_sample_size ranking_bias (print args) (maxdepth)");
	    System.exit(0);
	}

	// read in a grammar_file
	writer.println("Opening grammar file: " + args[0] + "...");
	gf = new GrammarFile(args[0]);

	// read in i_o_file
	writer.println("Opening distribution file: " + args[1] + "...");
	df = new DistFile(args[1]);
	//	df.phono = false;

	writer.println("Now parsing remaining arguments");
	writer.println("Setting iterations to: " + args[2]);
	iterations = Integer.parseInt(args[2]);
	writer.println("Setting Final Evaluation Sample Size to: " + args[3]);
	final_eval_sample = Integer.parseInt(args[3]);
	writer.println("Setting learner to: " + args[4]);
	int learner = Integer.parseInt(args[4]);
	writer.println("Setting grammar sample size to: " + args[5]);
	gram_sample_size = Integer.parseInt(args[5]);
	writer.println("Setting initial bias to: " + args[6]);
	int init_bias = Integer.parseInt(args[6]);
	if (args.length > 13) {
	    writer.println("Setting print_input? to: " + args[7]);
	    print_input = Integer.parseInt(args[7]);
	    writer.println("Setting final-eval to: " + args[8]);
	    final_eval = Integer.parseInt(args[8]);
	    writer.println("Setting mini-eval to: " + args[9]);
	    mini_eval = Integer.parseInt(args[9]);
	    writer.println("Setting mini-eval-freq to: " + args[10]);
	    mini_eval_freq = Integer.parseInt(args[10]);
	    writer.println("Setting mini-eval-sample to: " + args[11]);
	    mini_eval_sample = Integer.parseInt(args[11]);
	    writer.println("Setting quit_early? to: " + args[12]);
	    quit_early = Integer.parseInt(args[12]);
	    writer.println("Setting quit_early_sample? to: " + args[13]);
	    quit_early_sample = Integer.parseInt(args[13]);
	    if (args.length == 15){
		writer.println("Setting max-depth to: " + args[14]);
		maxdepth = Integer.parseInt(args[14]);
	    }
	}
	writer.println("Finished parsing all the arguments");
	
	if (print_input == 0) {
	    writer.println("\nSTARTING LEXICON:\n" + df);
	}
	// initialize grammar to uniform - make ll_grammar
	gr = new RandomExtension(gf);
	prior = new RandomExtension(gf);
	prior.bias_grammar();
	if (init_bias == 1) {
	    gr.bias_grammar();
	}
	if (print_input == 0) {
	    writer.println("\nSTARTING GRAMMAR:\n" + gr);
	}
	for (int j = 0; j < gf.tableaux[0].cands.length; j++) {
	    writer.println(Arrays.toString(gf.tableaux[0].cands[j].violations));
	}

	if (learner == 1) {
	    EDL_batch();
	} else if (learner == 2) {
	    EDL_online();
	}
    }

    public static void EDL_batch() {
	// there are i iterations (passes through the full data set)

	double prev_error = 1000.0;
	int i = 0;
	for (i = 0; i < iterations; i++) {
	    if (i % mini_eval_freq == 0 ) {
		writer.println("Starting iteration " + i);
	    }
	    //to store successes for each output
	    double[][][] sum = new double[gr.grammar.length][gr.grammar.length][df.outputs.length];

	    //for each parameter value ...
	    for (int r = 0; r < gr.grammar.length; r++) {
		for (int c = 0; c < r; c++) {
		    double old_rc = gr.grammar[r][c];
		    double old_cr = gr.grammar[c][r];

		    if (gr.grammar[r][c] != 0.0 && gr.grammar[r][c] != 1.0) {
			//temporarily set r >> c
			gr.grammar[r][c] = 1.0;
			gr.grammar[c][r] = 0.0;

			double[][] ext = gr.cloneGrammar();
			gr.makeMeConsistent(ext);

			double[][] single;

			// generate s samples from the grammar
			for (int s = 0; s < gram_sample_size; s++) {
			    single = gr.generate_extension(ext);
			    if (single != null) {
				// find the total order corresponding to the sampled matrix
				int[] rank = gr.find_order(single);
				//go through each output
				for (int o = 0; o < df.outputs.length; o++) {
				    DistFile.Output output = df.outputs[o];
				    //go through each UR for that output
				    for (int in = 0; in < output.dist.length; in++) {
					String input = output.inputs[in];
					GrammarFile.Tableau tab = find_tab(input); //find the tableau
					String winner = optimize(input, tab, rank);
					if (winner.equals(output.form)) {
					    // this sum divided by a constant (s) estimates the joint probability of o and r>c
					    sum[r][c][o]++;
					    
					}
				    }
				}
			    }
			}
			//done sampling r >> c...

			// now temporarily set c >> r
			gr.grammar[r][c] = 0.0;
			gr.grammar[c][r] = 1.0;
			ext = gr.cloneGrammar();
			gr.makeMeConsistent(ext);

			// generate s samples from the grammar
			for (int s = 0; s < gram_sample_size; s++) {
			    single = gr.generate_extension(ext);
			    if (single != null) {
				int[] rank = gr.find_order(single);
				//go through each output
				for (int o = 0; o < df.outputs.length; o++) {
				    DistFile.Output output = df.outputs[o];
				    //go through each UR for the output
				    for (int in = 0; in < output.dist.length; in++) {
					String input = output.inputs[in];
					GrammarFile.Tableau tab = find_tab(input); //find the tableau
					String winner = optimize(input, tab, rank);
					if (winner.equals(output.form)) {
					    // this sum divided by a constant (s) estimates the joint probability of o and c>r
					    sum[c][r][o]++;
					}
				    }
				}
			    } 
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
	    // for each pairwise ranking
	    for (int r = 0; r < gr.grammar.length; r++) {
		for (int c = 0; c < r; c++) {
		    double rc_sum = 0.0;
		    double cr_sum = 0.0;
		    // go through each output
		    for (int o = 0; o < df.outputs.length; o++) {
			// o_prob is proportional to the estimated probability of the output given current grammar
			// smoothing since sometimes with small samples observed output is never sampled, leading to 0 denominator
			double o_prob = ((double) sum[r][c][o] + .0001) * gr.grammar[r][c] + ((double) sum[c][r][o] + .0001) * gr.grammar[c][r];
			// estimate expected count of r>c given o multiplied out by frequency
			double o_rc = (((double) sum[r][c][o] + .0001) * ((double) df.outputs[o].freq)) / ((double) o_prob);
			// estimate expected count of c>r given o multiplied out by frequency
			double o_cr = (((double) sum[c][r][o] + .0001) * ((double) df.outputs[o].freq)) / ((double) o_prob);
			// sum up expected counts over all outputs
			rc_sum += o_rc;
			cr_sum += o_cr;
		    }

		    // update the grammar
		    gr.grammar[r][c] = ((double) rc_sum * gr.grammar[r][c]) / ((double) rc_sum * gr.grammar[r][c] + (double) cr_sum * gr.grammar[c][r]);
		    gr.grammar[c][r] = 1 - gr.grammar[r][c];
		}
	    }

	    if (!gr.makeMeConsistent()) {
		writer.println("Grammar is Inconsistent after updating --- Exiting:\n" + gr);
		System.exit(-1);
	    }

	    if (i % mini_eval_freq == 0) {
		if (mini_eval == 0) {
		    writer.println("The new grammar is:\n" + gr);
		}
		if (i % quit_early != 0) {
		    evaluate_grammar(mini_eval_sample, i);
		}
	    }
	    if (i % quit_early == 0) {
		if (evaluate_grammar(quit_early_sample, i)) {
		    writer.println("-reached perfection early ----- exiting now");
		    break;
		}
	    }
	} //end of iterations

	//now going to examine final grammar
	if (final_eval == 0 || final_eval == 1) {
	    writer.println("------------------EVALUATING-------------FINAL----------------GRAMMAR--------------------");
	    writer.println("The final grammar is:\n" + gr);
	    evaluate_grammar(final_eval_sample, i);
	}
    }

    public static void EDL_online() {

	// sample a ranking
	double[][] single = gr.generate_extension();
	int i = 0;
	double rate = .25;
	// there are i iterations of sampling and updating
	for (i = 0; i < iterations; i++) {
	    if (i % mini_eval_freq == 0) {
		writer.println("Starting iteration " + i);
	    }
	    double[][] corr_ranks_samp = new double[gr.grammar.length][gr.grammar.length];

	    //sample one output form
	    DistFile.Output output = null;
	    String winner = "tat";
	    double rand = Math.random();
	    int o_index = 0;
	    for (int o = 0; o < df.outputs.length; o++) {
		rand -= df.outputs[o].relfreq;
		if (rand <= 0) {
		    output = df.outputs[o];
		    o_index = o;
		    break;
		}
	    }
	    //writer.println("Sampled output form: " + output.form);

	    //sample a ur for the output form
	    String input = "";
	    rand = Math.random();
	    for (int in = 0; in < output.dist.length; in++) {
		rand -= output.dist[in];
		if (rand <= 0) {
		    input = output.inputs[in];
		    break;
		}
	    }
	    //writer.println("\tSampled a UR form: " + input);

	    // this keeps track of how many samples are successful at generating the observed output for each pairwise ranking
	    double[][] output_counts = new double[gr.grammar.length][gr.grammar.length];

	    // go through each pairwise ranking
	    for (int r = 0; r < gr.grammar.length; r++) {
		for (int c = 0; c < r; c++) {

		    // if this ranking hasn't already been categorically set, consider both pairwise rankings
		    if ((gr.grammar[r][c] == 0) || (gr.grammar[r][c] == 1)) {
			output_counts[r][c] = gr.grammar[r][c];
			output_counts[c][r] = gr.grammar[c][r];
		    } else {

			double old_rc = gr.grammar[r][c];
			double old_cr = gr.grammar[c][r];

			// temporarily set r >> c in a copied grammar ext
			gr.grammar[r][c] = 1.0;
			gr.grammar[c][r] = 0.0;
			double[][] ext = gr.cloneGrammar();
			gr.makeMeConsistent(ext);

			// take s samples from temporary grammar and count matches
			for (int s = 0; s < gram_sample_size; s++) {
			    single = gr.generate_extension(ext);
			    if (single != null) {
				int[] rank = gr.find_order(single);
				if (rank != null) {
				    //compute learner's winner and compare to actual output
				    GrammarFile.Tableau tab = find_tab(input); //find the tableau
				    winner = optimize(input, tab, rank);
				    if (winner.equals(output.form)) {
					output_counts[r][c]++;
				    }
				} else {
				    writer.println("Rank was nULL!");
				}
			    }
			}

			// temporarily set the grammar the other way
			gr.grammar[r][c] = 0.0;
			gr.grammar[c][r] = 1.0;
			ext = gr.cloneGrammar();
			gr.makeMeConsistent(ext);

			// take s samples and count number of matches
			for (int s = 0; s < gram_sample_size; s++) {
			    single = gr.generate_extension(ext);
			    if (single != null) {
				int[] rank = gr.find_order(single);
				if (rank != null) {
				    //compute learner's winner and compare to actual output
				    GrammarFile.Tableau tab = find_tab(input); //find the tableau
				    winner = optimize(input, tab, rank);
				    //if equal, add matrix of ranking into collected samples
				    if (winner.equals(output.form)) {
					//writer.println("\t\t" + output.form + " was correctly generated as " + winner);
					output_counts[c][r]++;
				    }
				} else {
				    writer.println("Rank was nULL!");
				}
			    }
			}
			//reset grammar to original state
			gr.grammar[r][c] = old_rc;
			gr.grammar[c][r] = old_cr;
		    }
		}
	    }

	    // update each pairwise ranking based on number of successes it got
	    for (int r = 0; r < gr.grammar.length; r++) {
		for (int c = 0; c < r; c++) {
		    double rc_o = 0.0;
		    double cr_o = 0.0;

		    // o_prob is proportional to estimated probability of the output
		    double o_prob = (((double) output_counts[r][c])+ .0001) * gr.grammar[r][c] + (((double) output_counts[c][r])+ .0001) * gr.grammar[c][r];

		    // estimated probability of r>>c given output
		    rc_o = ((double) output_counts[r][c]+ .0001) / ((double) o_prob);
		    // estimated probability of c>>r given output
		    cr_o = ((double) output_counts[c][r]+ .0001) / ((double) o_prob);
		  			
		    double new_rc;
		    new_rc = (((double) rc_o) * gr.grammar[r][c]) / (((double) rc_o) * gr.grammar[r][c] + ((double) cr_o) * gr.grammar[c][r]);
		     
		    //make a small update since we're processing only one output per update
		    //go part of the way that EM would want
		    gr.grammar[r][c] = (1 - rate) * gr.grammar[r][c] + rate * new_rc;
		    gr.grammar[c][r] = 1 - gr.grammar[r][c];
		}
	    }

	    if (!gr.makeMeConsistent()) {
		writer.println("Updated Grammar is Inconsistent --- Exiting:\n" + gr);
		System.exit(-1);
	    }

	    if (i % mini_eval_freq == 0) {
		if (mini_eval == 0 || mini_eval == 1) {
		    writer.println("The new grammar is:\n" + gr);
		    if (i % quit_early != 0) {
			evaluate_grammar(mini_eval_sample, i);
		    }
		}
	    }
	    if (i % quit_early == 0) {
		if (evaluate_grammar(quit_early_sample, i)) {
		    writer.println("-reached perfection early ----- exiting now");
		    break;
		}
	    }
	}

	//now going to examine resulting grammar
	if (final_eval == 0 || final_eval == 1) {
	    writer.println("------------------EVALUATING-------------FINAL----------------GRAMMAR--------------------");
	    writer.println("The final grammar is:\n" + gr);
	    evaluate_grammar(final_eval_sample, i);
	}
    }

    public static boolean evaluate_grammar(int s, int i) {
	//now going to examine resulting grammar
	double log_likelihood = 0;
	int tot = 0;
	int corr = 0;
	double error = 0.0;
	for (int o = 0; o < df.outputs.length; o++) { //for each output form

	    DistFile.Output output = null;
	    String winner = "";
	    double rand = Math.random();
	    output = df.outputs[o];

	    //sample s times to check distribution
	    for (int c = 0; c < s; c++) {
		//sample a ur for the output form
		String input = "";
		rand = Math.random();
		for (int in = 0; in < output.dist.length; in++) {
		    rand -= output.dist[in];
		    if (rand < 0) {
			input = output.inputs[in];
			break;
		    }
		}
		//sample a random ranking
		double[][] single = gr.generate_extension();
		if (single != null) {
		    int[] rank = gr.find_order(single);
		    //compute learner's winner and compare to actual output

		    GrammarFile.Tableau tab = find_tab(input); //find the tableau
		    winner = optimize(input, tab, rank);
		    //if equal, add matrix of ranking into collected samples
		    if (winner.equals(output.form)) {
			corr++;
		    }
		    tot++;
		}
	    }
	    if (i % mini_eval_freq == 0) {
		if (mini_eval == 0) {
		    writer.println("Output " + output.form + " " + ((float) corr / tot) + " correct - observed freq is " + output.freq);
		}
	    }
	    if (i == iterations) {
		if (final_eval == 0) {
		    writer.println("Output " + output.form + " " + ((float) corr / tot) + " correct - observed freq is " + output.freq);
		}
	    }
	    log_likelihood += Math.log(((float) corr / tot)) * output.freq;
	    error += (((double) tot - (double) corr) / tot) * (double) output.freq;
	    corr = 0;
	    tot = 0;
	}
	if (i % mini_eval_freq == 0) {
	    if (mini_eval == 0 || mini_eval == 1) {
		writer.println("ITERATION " + i + ":: Total error is " + error + " and log likelihood is " + log_likelihood);
	    }
	}
	if (i == iterations) {
	    if (final_eval == 0 || final_eval == 1) {
		writer.println("ITERATION " + i + ":: Total error is " + error + " and log likelihood is " + log_likelihood);
	    }
	}

	double recent_error = error;
	if (error == 0.0) {
	    return true;
	} else {
	    return false;
	}
    }

    public static String optimize(String input, GrammarFile.Tableau tab, int[] rank) {
        String w = prevFound(rank,input);
	//writer.println("What prevFound returned: "+w);
	if (w!=""){
	    //writer.println("Here is what the word was: "+w);
	    return w;
	} else {
	    List<Integer> winners = initializeList(tab.cands.length); //create array that stores information about which candidates are still in the running
	    int stop = rank.length;

	    for (int j = 0; j < rank.length; j++) {
		//figuring out minimum violation for remaining candidates
		int min_vios = -1;
		List<Integer> cwinners = new LinkedList<Integer>();
		for (int i: winners) {
		    if (min_vios == -1) {
			min_vios = tab.cands[i].violations[rank[j]];
			cwinners.add(i);
		    } else if (tab.cands[i].violations[rank[j]] < min_vios) {
			min_vios = tab.cands[i].violations[rank[j]];
			cwinners.clear();
			cwinners.add(i);
		    } else if (tab.cands[i].violations[rank[j]] == min_vios) {
			cwinners.add(i);
		    }
		}
		if (cwinners.size() > 0) {
		    winners = cwinners;
		}
		if (winners.size() < 2 || cwinners.size() == 0) {
		    stop = j;
		    break;
		}
	    }
	    String winner = tab.cands[winners.get(0)].oform; //If there are more than one winners, this chooses the last one in tableau
	    track(stop, rank, winner, input);
	    return winner;
	}
    }

    public static String prevFound(int[] rank, String input) {
	String winner = "";
	if (intable.containsKey(input)) {
	    //writer.println("Contains input");
	    //writer.println("Input is: "+input);
	    PrefixTree ptree = intable.get(input);
	    //writer.println("Rank:" + Arrays.toString(rank));
	    //writer.println(ptree.toString());
	    winner = ptree.find(rank);
	    if(winner!=null){
		//writer.println("Found something!"+winner);
	    } else{
		winner = "";
	    }
	}
	return winner;
    }

    public static void track(int stop, int[] rank, String winner, String input){
	//writer.println("Stop is: "+stop);
	if(intable.containsKey(input)) {
	    //writer.println("Already contains!");
	}else{
	    intable.put(input, new PrefixTree(rank.length));
	}
	PrefixTree ptree = intable.get(input);
	//writer.println(ptree.toString());
	if (stop < maxdepth) {
	    int[] pre = Arrays.copyOfRange(rank, 0, stop + 1);
	    //writer.println("adding prefix: "+Arrays.toString(pre));
	    ptree.put(pre, winner);
	    //writer.println(ptree.toString());
	}
    }

    public static List<Integer> initializeList(int l){
	List<Integer> winners = new ArrayList<Integer>();
	for (int k = 0; k < l; k++){
	    winners.add(k);
	}
	return winners;
    }

    public static GrammarFile.Tableau find_tab(String input) {
        //find the tableau
        GrammarFile.Tableau tab = null;
        if (tabtable.containsKey(input)){
            tab = tabtable.get(input);
        } else{
            for (int i = 0; i < gf.tableaux.length; i++) {
                if (gf.tableaux[i].uf.equals(input)) {
                    tab = gf.tableaux[i];
                }
            }
            tabtable.put(input, tab);
        }
	return tab;
    }
}

