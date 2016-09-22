// usage: java STOTEM grammar_file dist_file sample_size num_samples learning_rate model learner noise NegOK?
// learner - {EIP, RIP, randRIP, RRIP}
// model - {OT, HG, ME}

public class STOTEM {

	//class variables
	public static DistFile df;
	public static GrammarFile gf;
	public static STOT gr;
	public static double rate;
	public static String model = "OT";
	public static boolean NegOK = true;
	public static String learner = "EIP";
	public static double noise = 1; 
	private static int sample_size = 0;
	private static int num_samples = 0;

	public static void main(String[] args) {
		if (args.length != 9) {
			System.out.println("usage: java STOTEM grammar_file dist_file sample_size num_samples learning_rate model learner noise NegOK?");
			System.exit(-1);
		}

		// read in a grammar_file
		gf = new GrammarFile(args[0]);

		// read in i_o_file
		df = new DistFile(args[1]);
		//	df.phono = false;
		System.out.println("\nLEXICON:\n" + df);

		sample_size = Integer.parseInt(args[2]);
		num_samples = Integer.parseInt(args[3]);
		rate = Double.parseDouble(args[4]);
		model = args[5];
		learner = args[6];
		noise = Double.parseDouble(args[7]);
		NegOK = Boolean.parseBoolean(args[8]);

		//initialize to uniform grammar
		gr = new STOT(gf);
		System.out.println("\nGRAMMAR:\n" + gr.gramToString(gr.grammar));

		learn_new_RIP();
	}

	public static void learn_new_RIP (){
		double r = rate;
		int fail = 0;
		double[] single = gr.sample(NegOK, noise);
		double[] orig_single = single;
		// there are i iterations of sampling and updating
		int i = 0;

		for (i = 0; i < num_samples; i++) {
			System.out.println("Starting iteration " + i);
			//each iteration consists of s samples
			fail = 0;
			for (int s = 0; s < sample_size; s++) {

				//sample an output form: output
				DistFile.Output output = null;
				GrammarFile.Candidate winner = null;
				boolean update = false;
				double rand = Math.random();
				for(int o=0; o < df.outputs.length; o++){
					rand -= df.outputs[o].relfreq;
					if(rand <= 0){
						output = df.outputs[o];
						break;
					}
				}
				//System.out.println("Sampled output form: " + output.form);

				//sample a ur for the output form: input
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

				//sample the current grammar's output for this form: optimal
				GrammarFile.Candidate optimal = null;
				single = gr.sample(NegOK, noise);
				if(learner.equals("Baseline")){
					single = gr.grammar;
				}
				//System.out.println("\nSampled:\n" + gr.gramToString(single));
				int[] rank = gr.find_order(single);
				if(model.equals("OT")){
					optimal = optimize(null, input, rank);
				}else if(model.equals("HG")){
					optimal = optimizeHG(null, input, single);
				}else{
					optimal = optimizeME(null, input, gr.grammar);
				}
				//System.out.println("The current optimal form is " + optimal.oform);
				//System.out.println("The current grammar is:\n" + gr.gramToString(gr.grammar));

				if((learner.equals("RIP")) || (learner.equals("RRIP"))){
					//make a temporary tableau for this output
					GrammarFile.Tableau tab = new GrammarFile.Tableau();
					GrammarFile.Candidate[] temp_cands = new GrammarFile.Candidate[3136];
					int cand_length = 0;
					for (int k=0; k < gf.tableaux.length; k++){
						for (int j=0; j < gf.tableaux[k].cands.length; j++){
							if (gf.tableaux[k].cands[j].oform.equals(output.form)){
								temp_cands[cand_length] = gf.tableaux[k].cands[j];
								cand_length++;
							}
						}
					}
					tab.cands = new GrammarFile.Candidate[cand_length];
					for(int k=0; k < cand_length; k++){
						tab.cands[k] = temp_cands[k];
						//System.out.println("Found matching candidate: " + tab.cands[k].oform);
					}
					GrammarFile.Candidate wouldhavebeen = null;
					if(model.equals("OT")){
						wouldhavebeen = optimize(tab, input, rank);
					}else if(model.equals("HG")){
						wouldhavebeen = optimizeHG(tab, input, single);
					}else{
						wouldhavebeen = optimizeME(tab, input, gr.grammar);
					}

					winner = wouldhavebeen;

					if(learner.equals("RRIP")){
						//			while(winner.form.equals(wouldhavebeen.form)){
						//RIP...	
						single =gr.sample(NegOK, noise);//resample
						//System.out.println("\nSampled:\n" + gr.gramToString(single));
						rank = gr.find_order(single);
						//System.out.println("\t\tSampled the ranking: " + gr.gramToString(rank));
						if(model.equals("OT")){
							winner = optimize(tab, input, rank);
						}else if(model.equals("HG")){
							winner = optimizeHG(tab, input, single);
						}else{
							winner = optimizeME(tab, input, gr.grammar);
						}	    
					}

					if(winner.form.equals(wouldhavebeen.form)){
						//System.out.println("Resampling found SAME winner - old: " + wouldhavebeen.form + " vs resampled:" + winner.form);
					}else{
						//System.out.println("Resampling found DIFFERENT overt form - old: " + wouldhavebeen.form + " vs resampled:" + winner.form);
					}


					if(optimal.form.equals(winner.form)){
						//System.out.println("RIPPed parse = " + winner.form + " matches Optimal = " + optimal.form);
					}else{
						if(learner.equals("RIP")){
							//there's an error so update grammar
							update = true;
						}else{ //this learner only counts overt form mismatches as errors
							if(!(optimal.oform.equals(winner.oform))){
								update = true;
							}else{
								//System.out.println("Structural Mismatch Only - optimal:" + optimal.form + " vs ripped:" + winner.form);
							}
						}
					}

				}else{ // This is either EIP or randRIP or Baseline
					if (optimal.oform.equals(output.form)){
						//do nothing, it matched..
						//System.out.println("Output = " + output.form + " matches optimal = " + optimal.oform);
					}else{
						//this is an error 
						if((learner.equals("EIP"))){
							//determine a correct parse of the output form by sampling until a match is found
							boolean looking = true;
							int count = 0;
							while ((looking) && (count < 1000)){
								single = gr.sample(NegOK, noise);
								//System.out.println("\nSampled:\n" + gr.gramToString(single));
								rank = gr.find_order(single);
								//System.out.println("\t\tSampled the ranking: " + gr.gramToString(rank));

								//compute learner's winner and compare to actual output
								if(model.equals("OT")){
									winner = optimize(null, input, rank);
								}else if(model.equals("HG")){
									winner = optimizeHG(null, input, single);
								}else{
									winner = optimizeME(null, input, gr.grammar);
								}

								count++;
								//System.out.println("The current winner form is " + winner.oform);

								//if equal, exit and keep the ranking
								if (winner.oform.equals(output.form)){
									//System.out.println("Matched form = " + output.form);
									//System.out.println("\twith sample:" + gr.gramToString(single));
									looking = false;
								}
							}

							if(looking == false){
								//found a matching parse, update grammar
								//System.out.println("UPDATING: Output = " + output.form + "parse = " + winner.form);
								update = true;
							}else{
								System.out.println("Never found a parse");
								fail++;
								if((fail > 20) && (i > 100)){
									System.out.println("stuck in bad grammer ----- exiting now");
									break;
								}
							}

						}else{ 
							if(learner.equals("randRIP")){
								//use random RIP
								//choose a random output for the input
								single = gr.sample(NegOK, noise);
								//System.out.println("\nSampled:\n" + gr.gramToString(single));
								rank = gr.find_order(single);
								//System.out.println("\t\tSampled the ranking: " + gr.gramToString(rank));

								if(model.equals("OT")){
									winner = optimize(null, input, rank);
								}else if(model.equals("HG")){
									winner = optimizeHG(null, input, single);
								}else{
									winner = optimizeME(null, input, gr.grammar);
								}

								//now update grammar
								update = true;
							}else{ //this will be the baseline
								gr.uni_grammar(gr.constraints.length);
								single = gr.sample(NegOK, noise);
								gr.grammar = single;
							}
						}
					}
				}
				// THIS IS THE NORMAL GLA (SYMMETRIC) UPDATE
				if(update){
					for(int c=0; c < gr.grammar.length; c++){
						if(model.equals("OT")){
							if(winner.violations[c] > optimal.violations[c]){
								gr.grammar[c] -= r;
								if(!(NegOK)){
									if(gr.grammar[c] < 0){
										gr.grammar[c] = 0;
									}
								}
							}else{ 
								if(winner.violations[c] < optimal.violations[c]){
									gr.grammar[c] += r;
								}
							}
						}else{ //update rule for HG is weighted by difference...
							gr.grammar[c] += r * (double)(optimal.violations[c] - winner.violations[c]);
							if(!(NegOK)){
								if(gr.grammar[c] < 0){
									gr.grammar[c] = 0;
								}
							}
						}
					}
				}


			}

			if((fail > 20) && (i > 100)){
				break;
			}
			System.out.println("The new grammar is:\n" + gr.gramToString(gr.grammar));
			if ((!(learner.equals("Baseline"))) && evaluate_grammar(100, i, noise)){
				System.out.println("-reached perfection early ----- exiting now");
				break;
			}
			System.out.println("Now evaluating nonnoisy grammar");
			if(evaluate_grammar(10, i, 0) && learner.equals("Baseline")){
				System.out.println("Baseline reached perfection ---- exiting now");
				break;
			}
		}
		System.out.println("------------------EVALUATING-------------FINAL----------------GRAMMAR--------------------");
		System.out.print("FINAL ");
		evaluate_grammar(1000, i, noise);
		System.out.print("FINAL ");
		evaluate_grammar(1000, i, 0);
	}

	public static boolean evaluate_grammar(int s, int i, double noi) {
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
				double[] single = gr.sample(NegOK, noi);
				if (single != null){
					int[] rank = gr.find_order(single);

					//compute learner's winner and compare to actual output
					if(model.equals("OT")){
						winner = optimize(null, input, rank).oform;
					}else if(model.equals("HG")){
						winner = optimizeHG(null, input, single).oform;
					}else{
						winner = optimizeME(null, input, gr.grammar).oform;
					}
					//if equal, add matrix of ranking into collected samples
					if (winner.equals(output.form)){
						corr++;
					}
					tot++;
				} //else{
				//System.out.println("BAD::Found inconsistent sample during evaluation for grammar:\n" + gr.gramToString(gr.grammar));
				//}
			}
			if(i % 100 ==0){
				System.out.println("Output " + output.form + " " + ((float)corr/tot) + " correct - actual freq is " + output.freq);
			}
			log_likelihood += Math.log(((float)corr/tot))*output.freq;
			error += (((double)tot-(double)corr)/tot)*(double)output.freq;
			corr=0;
			tot=0;
		}
		System.out.println("ITERATION " + i + ":: Total error is " + error + " and log likelihood is " + log_likelihood);
		if (error == 0.0){
			return true;
		}else{
			return false;
		}
	}


	public static GrammarFile.Candidate optimize (GrammarFile.Tableau tab, String input, int[] rank) {

		//find the tableau
		if(tab == null){
			for (int i=0; i < gf.tableaux.length; i++){
				//System.out.println("Found UF = " + gf.tableaux[i].uf);
				if (gf.tableaux[i].uf.equals(input)){
					//System.out.println("found a match");
					tab = gf.tableaux[i];
				}
			}
		}

		//create array that stores information about which candidates are still in the running
		int[] survivors = new int[tab.cands.length];
		int num_sur = survivors.length;
		for (int i=0; i < survivors.length; i++) {
			survivors[i] = 1;
		}
		for (int j=0; (j < rank.length) && (num_sur > 1); j++) {
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
		}

		if (num_sur > 1) {
			System.out.println("WARNING.  Multiple winners (" + num_sur + ") in optimize (input=" + input + ")");
		} else if (num_sur == 0) {
			System.out.println("WARNING.  No Survivors! (" + num_sur + ") in optimize (input=" + input + ")");
		}

		GrammarFile.Candidate winner = null;
		for (int i=0; i < survivors.length; i++) {
			if (survivors[i] == 1) {
				winner = tab.cands[i];
				//System.out.println("winner for input " + input + " and rank " + gr.rankToString(rank) + " is " + winner);
			}
		}

		return winner;
	}
	public static GrammarFile.Candidate optimizeHG (GrammarFile.Tableau tab, String input, double[] vector) {

		//find the tableau
		if(tab == null){
			for (int i=0; i < gf.tableaux.length; i++){
				if (gf.tableaux[i].uf.equals(input)){
					tab = gf.tableaux[i];
				}
			}
		}

		//create array to store weights of candidates
		double[] weights = new double[tab.cands.length];

		//go through constraints and add their violations*weight for each candidate
		double min = -1;
		int index = -1;
		for(int i=0; i < vector.length; i++){
			min = -1;
			index = -1;
			for(int j = 0; j < tab.cands.length; j++){
				weights[j] += vector[i] * tab.cands[j].violations[i];
				//System.out.println("Candidate " + tab.cands[j].form + " has " + tab.cands[j].violations[i] + " violations of constraint " + i);
				//System.out.println("current weight = " + weights[j]);
				//keeping track of the minimum weight on each iteration
				if((weights[j] < min) || (min == -1)){
					min = weights[j];
					index = j;
				}
			}
		}
		GrammarFile.Candidate winner = null;
		winner = tab.cands[index];
		return winner;
	}

	public static GrammarFile.Candidate optimizeME (GrammarFile.Tableau tab, String input, double[] gram) {

		//find the tableau
		if(tab == null){
			for (int i=0; i < gf.tableaux.length; i++){
				if (gf.tableaux[i].uf.equals(input)){
					tab = gf.tableaux[i];
				}
			}
		}

		//create array to store candidate unnormalized probabilities
		double[] probs = new double[tab.cands.length];

		//go through constraints and add their violations*weight for each candidate
		double sum = 0.0;
		for(int j = 0; j < tab.cands.length; j++){ //go through each candidate j
			double harmony = 0.0;
			for(int i=0; i < gram.length; i++){ // go through each constraint i
				harmony += gram[i] * tab.cands[j].violations[i];
			}
			double exp = Math.pow(2, - harmony);
			sum += exp;
			probs[j] = exp;
			//System.out.println("harmony, candidate = " + probs[j] + ", " + tab.cands[j].form);
		}

		double rand = sum*Math.random();

		//find which candidate rand corresponds to
		int index = -1;
		for(int j = 0; j < tab.cands.length; j++){ //go through each candidate j
			rand -= probs[j];
			if(rand < 0){
				index = j;
				break;
			}
		}

		GrammarFile.Candidate winner = null;
		winner = tab.cands[index];
		return winner;
	}


}
