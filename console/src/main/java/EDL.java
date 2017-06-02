package learner;
//This is the main file for the EDL learner
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
	public static int maxdepth = 100;
    public static HashMap<String, GrammarFile.Tableau> tabtable = new HashMap<String, GrammarFile.Tableau>();
	public static HashMap<String, PrefixTree> intable = new HashMap<String,PrefixTree>();
	public static Writer writer = new SystemWriter();


	public static void main(String[] args) {
		if (args.length < 7) {
			writer.println("usage: java EDL grammar_file dist_file iterations final_eval_sample learn_type gram_sample_size ranking_bias (print args) (maxdepth)");
			System.exit(-1);
		}

		// read in a grammar_file
		gf = new GrammarFile(args[0]);

		// read in i_o_file
		df = new DistFile(args[1]);
		//	df.phono = false;

		iterations = Integer.parseInt(args[2]);
		final_eval_sample = Integer.parseInt(args[3]);
		int learner = Integer.parseInt(args[4]);
		gram_sample_size = Integer.parseInt(args[5]);
		int init_bias = Integer.parseInt(args[6]);
		if (args.length > 13) {
			print_input = Integer.parseInt(args[7]);
			final_eval = Integer.parseInt(args[8]);
			mini_eval = Integer.parseInt(args[9]);
			mini_eval_freq = Integer.parseInt(args[10]);
			mini_eval_sample = Integer.parseInt(args[11]);
			quit_early = Integer.parseInt(args[12]);
			quit_early_sample = Integer.parseInt(args[13]);
			if (args.length == 15){
				maxdepth = Integer.parseInt(args[14]);
			}
		}
		if (print_input == 0) {
			writer.println("\nLEXICON:\n" + df);
		}
		// initialize grammar to uniform - make ll_grammar
		gr = new RandomExtension(gf);
		prior = new RandomExtension(gf);
		prior.bias_grammar();
		if (init_bias == 1) {
			gr.bias_grammar();
		}
		if (print_input == 0) {
			writer.println("\nGRAMMAR:\n" + gr);
		}
		//for (int i = 0; i < gf.tableaux.length; i++) {
			for (int j = 0; j < gf.tableaux[0].cands.length; j++) {
				writer.println(Arrays.toString(gf.tableaux[0].cands[j].violations));
			}
		//}


		if (learner == 1) {
			EDL_batch();
		} else if (learner == 2) {
			EDL_online();
		}
	}

	public static void EDL_batch() {
		// there are i iterations of EM

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
						//writer.println("Parameter Setting " + gr.constraints[r] + " - " + r + " >> " + gr.constraints[c] + " - " + c);
						gr.grammar[r][c] = 1.0;
						gr.grammar[c][r] = 0.0;

						double[][] ext = gr.cloneGrammar();
						gr.makeMeConsistent(ext);

						double[][] single;

						for (int s = 0; s < gram_sample_size; s++) {
							single = gr.generate_extension(ext);
							if (single != null) {
								int[] rank = gr.find_order(single);
								//writer.println("\t\tSampled the ranking: " + gr.rankToString(rank));
								//go through each output
								for (int o = 0; o < df.outputs.length; o++) {
									DistFile.Output output = df.outputs[o];
									//go through each UR for the output
									for (int in = 0; in < output.dist.length; in++) {
										String input = output.inputs[in];
										GrammarFile.Tableau tab = find_tab(input); //find the tableau
										if((tab.constraints[r]!=-5)&&(tab.constraints[c]!=-5)) {
											String winner = optimize(input, tab, rank);
											if (winner.equals(output.form)) {
												sum[r][c][o]++;
											}
										}
									}
								}
							} //else{
							//writer.println("Found inconsistent sample for parameters " + r + " >> " + c + "----Grammar:\n" + gr);
							//}
						}
						//done sampling r >> c...

						//temporarily set c >> r
						//writer.println("Parameter Setting " + gr.constraints[c] + " - " + c + " >> " + gr.constraints[r] + " - " + r);

						gr.grammar[r][c] = 0.0;
						gr.grammar[c][r] = 1.0;
						ext = gr.cloneGrammar();
						gr.makeMeConsistent(ext);

						for (int s = 0; s < gram_sample_size; s++) {
							single = gr.generate_extension(ext);
							if (single != null) {
								int[] rank = gr.find_order(single);
								//writer.println("\t\tSampled the ranking: " + gr.rankToString(rank));
								//go through each output
								for (int o = 0; o < df.outputs.length; o++) {
									DistFile.Output output = df.outputs[o];
									//go through each UR for the output
									for (int in = 0; in < output.dist.length; in++) {
										String input = output.inputs[in];
										GrammarFile.Tableau tab = find_tab(input); //find the tableau
										if((tab.constraints[r]!=-5)&&(tab.constraints[c]!=-5)) {
											String winner = optimize(input, tab, rank);
											if (winner.equals(output.form)) {
												sum[c][r][o]++;
											}
										}
									}
								}
							} //else{
							//writer.println("Found inconsistent sample for parameters " + c + " >> " + r + "----Grammar:\n" + gr);
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
					//writer.println("UPDATING " + r + " vs " + c);
					double rc_sum = 0.0;
					double cr_sum = 0.0;
					for (int o = 0; o < df.outputs.length; o++) {
						double o_prob = ((double) sum[r][c][o] + .0001) * gr.grammar[r][c] + ((double) sum[c][r][o] + .0001) * gr.grammar[c][r];
						double o_rc = (((double) sum[r][c][o] + .0001) * ((double) df.outputs[o].freq)) / ((double) o_prob);
						double o_cr = (((double) sum[c][r][o] + .0001) * ((double) df.outputs[o].freq)) / ((double) o_prob);
						//writer.println("\tUPDATES for " + df.outputs[o].form + " are " + o_rc*gr.grammar[r][c] + " vs " + o_cr*gr.grammar[c][r] );
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
				writer.println("Entire Grammar is Inconsistent --- Exiting:\n" + gr);
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

		//now going to examine resulting grammar
		if (final_eval == 0 || final_eval == 1) {
			writer.println("------------------EVALUATING-------------FINAL----------------GRAMMAR--------------------");
			writer.println("The final grammar is:\n" + gr);
			evaluate_grammar(final_eval_sample, i);
		}
	}

	public static void EDL_online() {

		// there are i iterations of sampling and updating
		//double corr_tot = 0;
		double[][] single = gr.generate_extension();
		int i = 0;
		double rate = .25;
		for (i = 0; i < iterations; i++) {
			if (i % mini_eval_freq == 0) {
				writer.println("Starting iteration " + i);
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

			double[][] output_counts = new double[gr.grammar.length][gr.grammar.length];

			for (int r = 0; r < gr.grammar.length; r++) {
				for (int c = 0; c < r; c++) {

					if ((gr.grammar[r][c] == 0) || (gr.grammar[r][c] == 1)) {
						output_counts[r][c] = gr.grammar[r][c];
						output_counts[c][r] = gr.grammar[c][r];
					} else {

						double old_rc = gr.grammar[r][c];
						double old_cr = gr.grammar[c][r];

						//temporarily set r >> c
						//writer.println("Setting " + gr.constraints[r] + " - " + r + " >> " + gr.constraints[c] + " - " + c);
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
								//writer.println("\t\tSampled the ranking: " + gr.rankToString(rank));

								if (rank != null) {
									//compute learner's winner and compare to actual output
									GrammarFile.Tableau tab = find_tab(input); //find the tableau
									if((tab.constraints[r]!=-5)&&(tab.constraints[c]!=-5)) {
										winner = optimize(input, tab, rank);
										//if equal, add matrix of ranking into collected samples
										if (winner.equals(output.form)) {
											//writer.println("\t\t" + output.form + " was correctly generated as " + winner);
											output_counts[r][c]++;
										}
									}
								} else {
									writer.println("Rank was nULL!");
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
								//writer.println("\t\tSampled the ranking: " + gr.rankToString(rank));
								if (rank != null) {
									//compute learner's winner and compare to actual output

									GrammarFile.Tableau tab = find_tab(input); //find the tableau
									if((tab.constraints[r]!=-5)&&(tab.constraints[c]!=-5)) {
										winner = optimize(input, tab, rank);
										//if equal, add matrix of ranking into collected samples
										if (winner.equals(output.form)) {
											//writer.println("\t\t" + output.form + " was correctly generated as " + winner);
											output_counts[c][r]++;
										}
									}
								} else {
									writer.println("Rank was nULL!");
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
				writer.println("Entire Grammar is Inconsistent --- Exiting:\n" + gr);
				System.exit(-1);
			}

			//bias = bias*(i+1)/(i+2);
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
					writer.println("Output " + output.form + " " + ((float) corr / tot) + " correct - actual freq is " + output.freq);
				}
			}
			if (i == iterations) {
				if (final_eval == 0) {
					writer.println("Output " + output.form + " " + ((float) corr / tot) + " correct - actual freq is " + output.freq);
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
				/*Object[] all = hm.entrySet().toArray();
				for(int k=0; k < all.length; k++) {
					writer.println(all[k]);
				}*/
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

	/*public static String prevFound(int[] rank, String input){
		String winner = "";
		if(intable.containsKey(input)) {
			//writer.println("Contains input");
			WinBundle bun = intable.get(input);
			int shortest = bun.start;
			int longest = bun.stop;
			HashMap<Ranking, String> wins = bun.ht;
			writer.println("Rank:" + Arrays.toString(rank));
			Set<Ranking> k = wins.keySet();
			for (Ranking key : k) {
				writer.println("key: " + Arrays.toString(key.ranking));
			}
			writer.println("Start: " + shortest);
			writer.println("Stop: " + longest);
			for (int i = shortest; i < (longest + 1); i++) { //Is this right???
				Ranking sub = new Ranking(Arrays.copyOfRange(rank, 0, i + 1));
				//writer.println(Arrays.toString(sub.ranking));

				if (wins.containsKey(sub)) {
					//writer.println("Found something!");
					winner = wins.get(sub);
					break;
				}
				int[] test = new int[2];
				test[0] = 9;
				test[1] = 8;
				if (wins.containsKey(new Ranking(test))){
					writer.println("FOund a key98");
				}
			}
		}
		return winner;
	}*/

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

