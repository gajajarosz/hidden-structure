// read command
// usage: java POEM grammar_file i_o_file verbose gram_sample_size iterations ranking_bias learner_type
// grammar_file contains all tableaux, i_o_file contains possible inputs, morphemes, outputs, & frequencies

public class POEM {

	public static DistFile df;
	public static GrammarFile gf;
	public static RandomExtension gr;
	public static RandomExtension prior;

	private static int gram_sample_size = 1;
	private static int iterations = 0;
	private static boolean verbose = false;

	public static void main(String[] args) {
		if (args.length < 6) {
			System.out.println("usage: java POEM grammar_file dist_file verbose grammar_sample_size iterations init_bias learner_type");
			System.exit(-1);
		}

		// read in a grammar_file
		gf = new GrammarFile(args[0]);

		// read in i_o_file
		df = new DistFile(args[1]);
		//	df.phono = false;

		verbose = (Integer.parseInt(args[2]) == 0) ? false : true;
		gram_sample_size = Integer.parseInt(args[3]);
		iterations = Integer.parseInt(args[4]);
		int init_bias = Integer.parseInt(args[5]);
		int learner = Integer.parseInt(args[6]);
		if (verbose) {
			System.out.println("\nLEXICON:\n" + df);
		}
		// initialize grammar to uniform - make ll_grammar
		gr = new RandomExtension(gf);
		prior = new RandomExtension(gf);
		prior.bias_grammar();
		if (init_bias == 1) {
			gr.bias_grammar();
		}
		if (verbose) {
			System.out.println("\nGRAMMAR:\n" + gr);
		}
		if (learner == 1) {
			EDL_batch();
		} else if (learner == 2) {
			EDL_online();
		}
	}

	public static void EDL_batch() {
		// there are i iterations of EM

		long startTime = System.currentTimeMillis();
		double prev_error = 1000.0;
		int i = 0;
		for (i = 0; i < iterations; i++) {
			long startIterTime = System.currentTimeMillis();
			if (verbose) {
				System.out.println("Starting iteration " + i);
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
						//System.out.println("Parameter Setting " + gr.constraints[r] + " - " + r + " >> " + gr.constraints[c] + " - " + c);
						gr.grammar[r][c] = 1.0;
						gr.grammar[c][r] = 0.0;

						double[][] ext = gr.cloneGrammar();
						gr.makeMeConsistent(ext);

						double[][] single;

						for (int s = 0; s < gram_sample_size; s++) {
							single = gr.generate_extension(ext);
							if (single != null) {
								int[] rank = gr.find_order(single);
								//System.out.println("\t\tSampled the ranking: " + gr.rankToString(rank));
								//go through each output
								for (int o = 0; o < df.outputs.length; o++) {
									DistFile.Output output = df.outputs[o];
									//go through each UR for the output
									for (int in = 0; in < output.dist.length; in++) {
										String input = output.inputs[in];
										String winner = optimize(input, rank);
										if (winner.equals(output.form)) {
											sum[r][c][o]++;
										}
									}
								}
							} //else{
							//System.out.println("Found inconsistent sample for parameters " + r + " >> " + c + "----Grammar:\n" + gr);
							//}
						}
						//done sampling r >> c...

						//temporarily set c >> r
						//System.out.println("Parameter Setting " + gr.constraints[c] + " - " + c + " >> " + gr.constraints[r] + " - " + r);

						gr.grammar[r][c] = 0.0;
						gr.grammar[c][r] = 1.0;
						ext = gr.cloneGrammar();
						gr.makeMeConsistent(ext);

						for (int s = 0; s < gram_sample_size; s++) {
							single = gr.generate_extension(ext);
							if (single != null) {
								int[] rank = gr.find_order(single);
								//System.out.println("\t\tSampled the ranking: " + gr.rankToString(rank));
								//go through each output
								for (int o = 0; o < df.outputs.length; o++) {
									DistFile.Output output = df.outputs[o];
									//go through each UR for the output
									for (int in = 0; in < output.dist.length; in++) {
										String input = output.inputs[in];
										String winner = optimize(input, rank);
										if (winner.equals(output.form)) {
											sum[c][r][o]++;
										}
									}
								}
							} //else{
							//System.out.println("Found inconsistent sample for parameters " + c + " >> " + r + "----Grammar:\n" + gr);
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
			for (int r = 0; r < gr.grammar.length; r++) {
				for (int c = 0; c < r; c++) {
					//System.out.println("UPDATING " + r + " vs " + c);
					double rc_sum = 0.0;
					double cr_sum = 0.0;
					for (int o = 0; o < df.outputs.length; o++) {
						double o_prob = ((double) sum[r][c][o] + .0001) * gr.grammar[r][c] + ((double) sum[c][r][o] + .0001) * gr.grammar[c][r];
						double o_rc = (((double) sum[r][c][o] + .0001) * ((double) df.outputs[o].freq)) / ((double) o_prob);
						double o_cr = (((double) sum[c][r][o] + .0001) * ((double) df.outputs[o].freq)) / ((double) o_prob);
						//System.out.println("\tUPDATES for " + df.outputs[o].form + " are " + o_rc*gr.grammar[r][c] + " vs " + o_cr*gr.grammar[c][r] );
						rc_sum += o_rc;
						cr_sum += o_cr;
					}

					double new_rc = ((double) rc_sum * gr.grammar[r][c]) / ((double) rc_sum * gr.grammar[r][c] + (double) cr_sum * gr.grammar[c][r]);
					double new_cr = 1 - new_rc;
					gr.grammar[r][c] = new_rc;
					gr.grammar[c][r] = 1 - gr.grammar[r][c];
			/*
		    // this skews the re-estimate toward 0 and 1
		    double ratio = (new_rc+.01)/(new_cr+.01);
		    gr.grammar[r][c] = .2*ratio*ratio/(ratio*ratio+1) + .8*gr.grammar[r][c];
		    gr.grammar[c][r] = 1-gr.grammar[r][c];

					if (speed_up) {
						// this is an attempt to increase the learning rate...
						if (new_rc > gr.grammar[r][c]) { //bring closer to 1
							gr.grammar[r][c] = .5 * 1 + .5 * new_rc;
							gr.grammar[c][r] = 1 - gr.grammar[r][c];
						} else if (gr.grammar[r][c] > new_rc) { //bring closer to 0
							gr.grammar[r][c] = .5 * new_rc + .5 * 0;
							gr.grammar[c][r] = 1 - gr.grammar[r][c];
						}
					}
				 */
				}
			}

			if (!gr.makeMeConsistent()) {
				System.out.println("Entire Grammar is Inconsistent --- Exiting:\n" + gr);
				System.exit(-1);
			}

			if (verbose) {
				System.out.println("The new grammar is:\n" + gr);
			}
			if (i % 1 == 0) {

				//now going to examine resulting grammar
				if (verbose) {
					System.out.println("------------------EVALUATING--------------GRAMMAR------AT-------ITERATION-------" + i);
					if (evaluate_grammar(1000, i, true)) {
						System.out.println("-reached perfection early ----- exiting now");
						break;
					}
				} else {
					if (evaluate_grammar(100, i)) {
						System.out.println("-reached perfection early ----- exiting now");
						break;
					}
				}
			}
			long endIterTime = System.currentTimeMillis();
			if (verbose) {
				System.out.println("TIMING:: Iteration " + i + " took " + ((endIterTime - startIterTime) / 1000.0) + " sec.  Total: " + ((endIterTime - startTime) / 1000.0) + " sec. Average: " + ((endIterTime - startTime) / 1000.0) / (i + 1) + " sec.");
			}
		} //end of iterations

		//now going to examine resulting grammar
		System.out.println("------------------EVALUATING-------------FINAL----------------GRAMMAR--------------------");
		System.out.println("The final grammar is:\n" + gr);
		evaluate_grammar(10000, i, true);
	}

	public static void EDL_online() {

		// there are i iterations of sampling and updating
		//double corr_tot = 0;
		double[][] single = gr.generate_extension();
		int i = 0;
		double rate = .25;
		for (i = 0; i < iterations; i++) {
			if (verbose) {
				System.out.println("Starting iteration " + i);
			}
			double[][] corr_ranks_samp = new double[gr.grammar.length][gr.grammar.length];
			//each iteration consists of s samples
			//for (int s = 0; s < sample_size; s++) {

				//sample an output form
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
				//System.out.println("Sampled output form: " + output.form);

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
				//System.out.println("\tSampled a UR form: " + input);

				double[][] output_counts = new double[gr.grammar.length][gr.grammar.length];

				for (int r = 0; r < gr.grammar.length; r++) {
					for (int c = 0; c < r; c++) {

						if ((gr.grammar[r][c] == 0) || (gr.grammar[r][c] == 1)) {
							if (verbose) {
								//System.out.println(" r=" + r + " by c=" + c + " is set, not worth checking probs");
							}
							output_counts[r][c] = gr.grammar[r][c];
							output_counts[c][r] = gr.grammar[c][r];
						} else {

							double old_rc = gr.grammar[r][c];
							double old_cr = gr.grammar[c][r];

							//temporarily set r >> c
							//System.out.println("Setting " + gr.constraints[r] + " - " + r + " >> " + gr.constraints[c] + " - " + c);
							gr.grammar[r][c] = 1.0;
							gr.grammar[c][r] = 0.0;

							int count = 0;
							double[][] ext = gr.cloneGrammar();
							gr.makeMeConsistent(ext);
							while (count < gram_sample_size) {
								//sample a random ranking
								count++;
								single = gr.generate_extension(ext);
								if (single != null) {
									int[] rank = gr.find_order(single);
									//System.out.println("\t\tSampled the ranking: " + gr.rankToString(rank));

									if (rank != null) {
										//compute learner's winner and compare to actual output
										winner = optimize(input, rank);
										//if equal, add matrix of ranking into collected samples
										if (winner.equals(output.form)) {
											//System.out.println("\t\t" + output.form + " was correctly generated as " + winner);
											output_counts[r][c]++;
										}
									} else {
										System.out.println("Rank was nULL!");
									}
								}
							}

							gr.grammar[r][c] = 0.0;
							gr.grammar[c][r] = 1.0;

							ext = gr.cloneGrammar();
							gr.makeMeConsistent(ext);
							count = 0;
							while (count < gram_sample_size) {
								//sample a random ranking
								count++;

								single = gr.generate_extension(ext);
								if (single != null) {
									int[] rank = gr.find_order(single);
									//System.out.println("\t\tSampled the ranking: " + gr.rankToString(rank));
									if (rank != null) {
										//compute learner's winner and compare to actual output
										winner = optimize(input, rank);
										//if equal, add matrix of ranking into collected samples
										if (winner.equals(output.form)) {
											//System.out.println("\t\t" + output.form + " was correctly generated as " + winner);
											output_counts[c][r]++;
										}
									} else {
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
				for (int r = 0; r < gr.grammar.length; r++) {
					for (int c = 0; c < r; c++) {
						double o_prob = ((double) output_counts[r][c]) * gr.grammar[r][c] + ((double) output_counts[c][r]) * gr.grammar[c][r];
						if (o_prob != 0) {
							corr_ranks_samp[r][c] += ((double) output_counts[r][c]) / ((double) o_prob);
							corr_ranks_samp[c][r] += ((double) output_counts[c][r]) / ((double) o_prob);
						}
					}
				}
			//}

			for (int r = 0; r < gr.grammar.length; r++) {
				for (int c = 0; c < r; c++) {
					double old_r = gr.grammar[r][c];
					double new_rc;
					if (corr_ranks_samp[r][c] + corr_ranks_samp[c][r] != 0) {
						new_rc = (((double) corr_ranks_samp[r][c]) * gr.grammar[r][c]) / (((double) corr_ranks_samp[r][c]) * gr.grammar[r][c] + ((double) corr_ranks_samp[c][r]) * gr.grammar[c][r]);
						if ((new_rc > .5) && (new_rc < .5)) {
							new_rc = gr.grammar[r][c];
						}
					} else {
						new_rc = 0.5;
					}

                    //make a small update since we're processing only one output per update
                    //go part of the way that EM would want
                    gr.grammar[r][c] = (1 - rate) * gr.grammar[r][c] + rate * new_rc;
                    gr.grammar[c][r] = 1 - gr.grammar[r][c];
				}
			}

			if (!gr.makeMeConsistent()) {
				System.out.println("Entire Grammar is Inconsistent --- Exiting:\n" + gr);
				System.exit(-1);
			}

			//bias = bias*(i+1)/(i+2);
			if (i % 1 == 0) {

				if (verbose) {
					System.out.println("The new grammar is:\n" + gr);
				}
				//now going to examine resulting grammar

				if (verbose) {
					System.out.println("------------------EVALUATING--------------GRAMMAR------AT-------ITERATION-------" + i);
					if (evaluate_grammar(100, i, true)) {
						System.out.println("-reached perfection early ----- exiting now");
						break;
					}
				} else {
					if (evaluate_grammar(100, i)) {
						System.out.println("-reached perfection early ----- exiting now");
						break;
					}
				}
			}
		}
		//now going to examine resulting grammar

		System.out.println("------------------EVALUATING-------------FINAL----------------GRAMMAR--------------------");
		System.out.println("The final grammar is:\n" + gr);
		evaluate_grammar(10000, i, true);
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
					winner = optimize(input, rank);
					//if equal, add matrix of ranking into collected samples
					if (winner.equals(output.form)) {
						corr++;
					}
					tot++;
				}
			}
			if (printout) {
				System.out.println("Output " + output.form + " " + ((float) corr / tot) + " correct - actual freq is " + output.freq);
			}
			log_likelihood += Math.log(((float) corr / tot)) * output.freq;
			error += (((double) tot - (double) corr) / tot) * (double) output.freq;
			corr = 0;
			tot = 0;
		}
		System.out.println("ITERATION " + i + ":: Total error is " + error + " and log likelihood is " + log_likelihood);

		double recent_error = error;
		if (error == 0.0) {
			return true;
		} else {
			return false;
		}
	}

	public static String optimize(String input, int[] rank) {

		//find the tableau
		GrammarFile.Tableau tab = null;
		for (int i = 0; i < gf.tableaux.length; i++) {
			if (gf.tableaux[i].uf.equals(input)) {
				tab = gf.tableaux[i];
			}
		}

		//create array that stores information about which candidates are still in the running
		int[] survivors = new int[tab.cands.length];
		int num_sur = survivors.length;
		for (int i = 0; i < survivors.length; i++) {
			survivors[i] = 1;
		}
		int size = rank.length;
		while ((num_sur > 1) && (size >= 0)) {
			for (int j = 0; j < rank.length; j++) {
				//figuring out minimum violation for remaining candidates
				int min_vios = -1;
				for (int i = 0; i < survivors.length; i++) {
					if (survivors[i] == 1) {
						if (min_vios == -1) {
							min_vios = tab.cands[i].violations[rank[j]];
						} else if (tab.cands[i].violations[rank[j]] < min_vios) {
							min_vios = tab.cands[i].violations[rank[j]];
						}
					}
				}

				//System.out.println("looking at constraint " + rank[j] + " whose minimum is " + min_vios);
				for (int i = 0; i < survivors.length; i++) {
					if ((tab.cands[i].violations[rank[j]] > min_vios) && (survivors[i] == 1)) {
						//System.out.println("candidate " + tab.cands[i].oform + " getting knocked out");
						survivors[i] = 0;
						num_sur -= 1;
					}
				}
				size--;
			}
		}
		String winner = "";
		for (int i = 0; i < survivors.length; i++) {
			if (survivors[i] == 1) {
				winner = tab.cands[i].oform;
				//System.out.println("winner for input " + input + " and rank " + gr.rankToString(rank) + " is " + winner);
			}
		}
		return winner;
	}
}
