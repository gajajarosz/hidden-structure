package learner;
// This is the main file for the EDL learner
// usage: java EDL grammar_file dist_file iterations final_eval_sample learn_type sample_size ranking_bias (print args) (maxdepth)
// grammar_file contains all tableaux, dist_file contains possible inputs, morphemes, outputs, & frequencies
import java.util.*;
import java.io.*;
import java.util.regex.*;


public class EDL {
    
    public static DistFile df;
    public static String distdirectory;
    public static String urdirectory;
    public static GrammarFile gf;
    public static RandomExtension gr;
    public static RandomExtension prior;
    
    private static int sample_size = 50;
    private static int iterations = 100;
    public static double rate = .1;
    public static int final_eval = 0;
    public static int final_eval_sample = 1000;
    public static int mini_eval = 1;
    public static int mini_eval_freq = 1;
    public static int mini_eval_sample = 100;
    public static int quit_early = 100;
    public static int quit_early_sample = 100;
    public static int print_input = 0;
    public static int maxdepth = 8;
    public static int learner = 1;
    public static int init_bias = 0;
    public static boolean ur_learning = false;
    public static int phono_iterations = 0;
    public static boolean phono = false;
    public static HashMap<String, GrammarFile.Tableau> tabtable = new HashMap<String, GrammarFile.Tableau>();
    public static HashMap<String, PrefixTree> intable = new HashMap<String,PrefixTree>();
    public static Writer writer = new SystemWriter();
    public static BufferedReader stream;
	//BEGIN HS CODE
	public static boolean harmSerial = false;
	public static String confile;
	public static String genfile;
	public static Map<String, Map<String, String>> GEN = new HashMap<>();
	public static Map<String, Constraint> CON = new  HashMap<>();
	public static String[] GEN_functions;
	public static Set<String> CON_set;
	public static String[] CON_names;
	public static int CON_num;	
	public static int func_num;
	public static String[][] changeSegLists;
    //END HS CODE
	
    public static void main(String[] args) {
        if(args.length == 1) {
            writer.println("Opening parameter file: " + args[0] + "...");
            
            try {
                stream = new BufferedReader(new FileReader(args[0]));
            } catch (IOException ioe) {
                writer.println(ioe + "\nUnable to find the parameter file with the name " + args[0] + ". Exiting...");
                System.exit(-1);
                return;
            }
            
            try {
                String line;
                Pattern pattern = Pattern.compile("^\"(.+)\"\\s+:\\s+(.+).*$");
                
                while ((line = stream.readLine()) != null) {
                    Matcher m1 = pattern.matcher(line);
                    if (m1.matches()) {
                        String parameter = m1.group(1);
                        if (parameter.equals("GRAMMAR_FILE")) {
                            String gramdirectory = m1.group(2);
							if (gramdirectory.equals("HS")){
								continue;
							}
                            writer.println("Opening grammar file: " + gramdirectory + "...");
                            gf = new GrammarFile(gramdirectory, writer);
                        } else if (parameter.equals("DIST_FILE")) {
                            distdirectory = m1.group(2);
                            writer.println("Opening distribution file: " + distdirectory + "...");
                            df = new DistFile(distdirectory, writer);
                        } else if (parameter.equals("ITERATIONS")) {
                            iterations = Integer.valueOf(m1.group(2));
                            writer.println("Setting iterations to: " + iterations);
                        } else if (parameter.equals("LEARNER")) {
                            if (m1.group(2).equals("online") || m1.group(2).equals("2")){
                                learner = 2;
                                writer.println("Setting learner to: online");
                            } else {
                                writer.println("Setting learner to: batch");
                            } 
                        } else if (parameter.equals("UR_LEARNING")) {
                            if (m1.group(2).equals("true") || m1.group(2).equals("1")){
                                ur_learning = true;
                                writer.println("Setting UR learning to: true");
                            } else {
                                writer.println("Setting UR learning to: false");
                            }
                        } else if (parameter.equals("UR_FILE")) {
                            urdirectory = m1.group(2);
                            writer.println("Setting UR file to: " + urdirectory);
                        } else if (parameter.equals("PHONO_ITERATIONS")) {
                            phono_iterations = Integer.valueOf(m1.group(2));
                            writer.println("Setting phonotactic learning iterations to: " + phono_iterations);
                        } else if (parameter.equals("LEARNING_RATE")) {
                            rate = Double.parseDouble(m1.group(2));
                            writer.println("Setting learning rate to: " + rate);
                        } else if (parameter.equals("INITIAL_BIAS")) {
                            if (m1.group(2).equals("true") || m1.group(2).equals("1")){
                                init_bias = 1;
                                writer.println("Setting initial bias to: true");
                            } else {
                                writer.println("Setting initial bias to: false");                                
                            }
                        } else if (parameter.equals("SAMPLE_SIZE")) {
                            sample_size = Integer.valueOf(m1.group(2));
                            writer.println("Setting sample size to: " + sample_size);
                        } else if (parameter.equals("FINAL_EVAL_SAMPLE")) {
                            final_eval_sample = Integer.valueOf(m1.group(2));
                            writer.println("Setting final sample size to: " + final_eval_sample);
                        } else if (parameter.equals("FINAL_EVAL_ACC")) {
                            if (m1.group(2).equals("true") || m1.group(2).equals("0")){
                                final_eval = 0;
                                writer.println("Setting final evaluation accuracy to: true");
                            } else {
                                final_eval = 1;
                                writer.println("Setting final evaluation accuracy to: false");
                            }
                        } else if (parameter.equals("MINI_EVAL")) {
                            mini_eval = Integer.valueOf(m1.group(2));
                            writer.println("Setting mini evaluation size to: " + mini_eval);
                        } else if (parameter.equals("MINI_EVAL_FREQ")) {
                            mini_eval_freq = Integer.valueOf(m1.group(2));
                            writer.println("Setting mini evaluation frequency to: " + mini_eval_freq);
                        } else if (parameter.equals("MINI_EVAL_SAMPLE")) {
                            mini_eval_sample = Integer.valueOf(m1.group(2));
                            writer.println("Setting mini evaluation sample size to: " + mini_eval_sample);                            
                        } else if (parameter.equals("QUIT_EARLY_FREQ")) {
                            quit_early = Integer.valueOf(m1.group(2));
                            writer.println("Setting quit early frequency to: " + quit_early);                            
                        } else if (parameter.equals("QUIT_EARLY_SAMPLE")) {
                            quit_early_sample = Integer.valueOf(m1.group(2));
                            writer.println("Setting quit early sample to: " + quit_early_sample);
                        } else if (parameter.equals("PRINT_INPUT")) {
                            if (m1.group(2).equals("false") || m1.group(2).equals("1")){
                                // 1 means not printing input here
                                print_input = 1;
                                writer.println("Setting print input to: false");
                            } else {
                                writer.println("Setting print input to: true");
                            } 
                        } else if (parameter.equals("MAXDEPTH")) {
                            maxdepth = Integer.valueOf(m1.group(2));
                            writer.println("Setting MAXDEPTH to: " + maxdepth);
                        } 
						//BEGIN HS CODE
						else if (parameter.equals("HARMONIC_SERIALISM")){
                            if (m1.group(2).equals("true") || m1.group(2).equals("1")){
                                harmSerial = true;
                                writer.println("Setting Harmonic Serialism to: true");
                            } else {
                                writer.println("Setting Harmonic Serialism to: false");
                            }
						} else if (parameter.equals("CON_FILE")) {
                            confile = m1.group(2);
                            writer.println("Setting CON file to: " + confile);
                        } else if (parameter.equals("GEN_FILE")) {
                            genfile = m1.group(2);
                            writer.println("Setting GEN file to: " + genfile);
                        }
						//END HS CODE
                        else {
                            writer.println("The following lines from the parameter file do not match the specified format and will be ignored: \n>>>" + line);
                        }
                    }
                }
                
            } catch (IOException ioe) {
                writer.println(ioe + "\nError reading the parameter file: " + args[0] + ". Exiting...");
                System.exit(-1);
            }
			
			//BEGIN HS CODE
			if (harmSerial){
				//Grabs CON:		
				CON = BuildTab.get_CON(confile);
				Set<String> CON_set = CON.keySet();
				CON_names = CON_set.toArray(new String[CON_set.size()]);
				CON_num = CON_set.size();
				
				//Grabs GEN:
				GEN = BuildTab.get_GEN (genfile);
 				Set<String> GEN_set = GEN.keySet();
				GEN_functions = GEN_set.toArray(new String[GEN_set.size()]);

				func_num = GEN_functions.length;
				changeSegLists = new String[func_num][]; //need this to work
				for (int f = 0; f < GEN_functions.length; f++){
					Set<String> this_seg_set = GEN.get(GEN_functions[f]).keySet();
					String[] this_seg_array = this_seg_set.toArray(new String[this_seg_set.size()]);
					changeSegLists[f] = this_seg_array;
				}
				
				//Create a fake grammar file (kind of a hack)
				try {
					BufferedWriter dummy_File = new BufferedWriter(new FileWriter("sample_files/dummyFile.txt"));
					dummy_File.write(CON_num+"\tconstraints\n");
					for (int c = 0; c < CON_num; c++){
						dummy_File.write("constraint\t["+(c+1)+"]:\t\""+CON_names[c]+"\" 0\n");
					}
					dummy_File.write("1\ttableaus\n");
					dummy_File.write("input	[1]:	\"NONE\"	1\n");
					dummy_File.write("	candidate	[1]:	\"NONE\"	");
					for (int c = 0; c < CON_num; c++){
						dummy_File.write("0\t");
					}
			 
					dummy_File.close();
				}
				catch (IOException e) {}
				
				gf = new GrammarFile("sample_files/dummyFile.txt", writer);			
			//END HS CODE
			}
		
        }
        else if(args.length > 1) {
            
            // read in a grammar_file
            writer.println("Opening grammar file: " + args[0] + "...");
            gf = new GrammarFile(args[0], writer);
            
            // read in i_o_file
            distdirectory = args[1];
            writer.println("Opening distribution file: " + distdirectory + "...");
            df = new DistFile(distdirectory, writer);            
            writer.println("Now parsing remaining arguments");
            writer.println("Setting iterations to: " + args[2]);
            iterations = Integer.parseInt(args[2]);
            writer.println("Setting Final Evaluation Sample Size to: " + args[3]);
            final_eval_sample = Integer.parseInt(args[3]);
            writer.println("Setting learner to: " + args[4]);
            int learner = Integer.parseInt(args[4]);
            writer.println("Setting grammar sample size to: " + args[5]);
            sample_size = Integer.parseInt(args[5]);
            writer.println("Setting initial bias to: " + args[6]);
            int init_bias = Integer.parseInt(args[6]);
            writer.println("Setting learning rate to: " + args[7]);
            rate = Double.parseDouble(args[7]);
            writer.println("Setting ur learning to: " + args[8]);
            ur_learning = Boolean.valueOf(args[8]);
            if (ur_learning){
                writer.println("Setting UR file to: " + args[9]);
                urdirectory = args[9];
                writer.println("Setting phonotactic learning iterations to: " + args[10]);
                phono_iterations = Integer.valueOf(args[10]);
            }
            if (args.length > 17) {
                writer.println("Setting print_input? to: " + args[11]);
                print_input = Integer.parseInt(args[11]);
                writer.println("Setting final-eval to: " + args[12]);
                final_eval = Integer.parseInt(args[12]);
                writer.println("Setting mini-eval to: " + args[13]);
                mini_eval = Integer.parseInt(args[13]);
                writer.println("Setting mini-eval-freq to: " + args[14]);
                mini_eval_freq = Integer.parseInt(args[14]);
                writer.println("Setting mini-eval-sample to: " + args[15]);
                mini_eval_sample = Integer.parseInt(args[15]);
                writer.println("Setting quit_early? to: " + args[16]);
                quit_early = Integer.parseInt(args[16]);
                writer.println("Setting quit_early_sample? to: " + args[17]);
                quit_early_sample = Integer.parseInt(args[17]);
                if (args.length == 19){
                    writer.println("Setting max-depth to: " + args[18]);
                    maxdepth = Integer.parseInt(args[18]);
                }
            }
        }
        else {
            writer.println("\nNo args found. Exiting...");
            System.exit(-1);
            return;
        }
        writer.println("Finished parsing all the arguments");
        
        // if  ur learning, then we need to read URs in 
        if(ur_learning){
            df.read_URs(urdirectory);
        }

        if (phono_iterations > 0){
            phono = true;
        }

        if (print_input == 0) {
            writer.println("\nSTARTING LEXICON:\n" + df);
        }
        // initialize grammar to uniform - make ll_grammar
        gr = new RandomExtension(gf, writer);
        prior = new RandomExtension(gf, writer);
        prior.bias_grammar();
        if (init_bias == 1) {
            gr.bias_grammar();
        }
        if (print_input == 0) {
            writer.println("\nSTARTING GRAMMAR:\n" + gr);
        }
        
        writer.println("\nVIOLATION VECTORS FOR THE FIRST TABLEAU:");
        for (int j = 0; j < gf.tableaux[0].cands.length; j++) {//done
            writer.println(Arrays.toString(gf.tableaux[0].cands[j].violations));//done
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

            if (phono && phono_iterations == i){
                writer.println("ENDING PHONOTACTIC LEARNING AT ITERATION: " + i);
                phono = false;
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
                        
                        // generate sample_size samples from the grammar
                        for (int s = 0; s < sample_size; s++) {
                            single = gr.generate_extension(ext);
                            if (single != null) {
                                // find the total order corresponding to the sampled matrix
                                int[] rank = gr.find_order(single);
                                //go through each output
                                for (int o = 0; o < df.outputs.length; o++) {
                                    DistFile.Output output = df.outputs[o];

                                    String input = output.input;
                                    if (ur_learning){
                                        input = output.sample_UR();
                                    }
                                    GrammarFile.Tableau tab = find_tab(input);
                                    String winner = optimize(input, tab, rank);
                                    if (winner.equals(output.form)) {
                                        // this sum divided by a constant (s) estimates the joint probability of o and r>c
                                        sum[r][c][o]++;
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
                        
                        // generate sample_size samples from the grammar
                        for (int s = 0; s < sample_size; s++) {
                            single = gr.generate_extension(ext);
                            if (single != null) {
                                int[] rank = gr.find_order(single);
                                //go through each output
                                for (int o = 0; o < df.outputs.length; o++) {
                                    DistFile.Output output = df.outputs[o];

                                    String input = output.input;
                                    if (ur_learning){
                                        input = output.sample_UR();
                                    }
                                    GrammarFile.Tableau tab = find_tab(input);
                                    String winner = optimize(input, tab, rank);
                                    if (winner.equals(output.form)) {
                                        // this sum divided by a constant (s) estimates the joint probability of o and r>c
                                        sum[c][r][o]++;
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
            } 
            //done going through grammar parameters
            
            // now test lexicon parameters if ur learning is on and phonotactic learning is off
            if (ur_learning && !phono){
                //initialize all morph counts
                for(int m=1; m < df.morphs.length; m++){
                    df.morphs[m].one_counts = new double[df.morphs[m].dist.length];
                    df.morphs[m].zero_counts = new double[df.morphs[m].dist.length];
                }
                //go through each output
                for(int o=0; o < df.outputs.length; o++){
                    DistFile.Output output = df.outputs[o];
                    double[][] single = gr.generate_extension();
                    //for each morpheme
                    for (int m = 0; m < output.morphs.length; m++){
                    //for each UR parameter
                        for (int p=0; p < output.morphs[m].dist.length; p++){
                            // remember the current setting of the parameter before testing it both ways
                            double o_prob = output.morphs[m].dist[p];
                            double temp_zero = 0.0;
                            double temp_one = 0.0;
                            if(output.morphs[m].max_values[p] != 0){
                                //set it each way and sample
                                
                                //set it to 0
                                output.morphs[m].dist[p] = 0.0;

                                //Sample the other parameters sample_size times
                                for (int s = 0; s < sample_size; s++) {
                                    single = gr.generate_extension();
                                    if(single != null){
                                        int[] rank = gr.find_order(single);
                                        String input = output.sample_UR();
                                        GrammarFile.Tableau tab = find_tab(input);
                                        String winner = optimize(input, tab, rank);
                                        if (winner.equals(output.form)){
                                            //update the expected counts for the Lexicon
                                            temp_zero++;
                                        }
                                    }
                                }
                                
                                //set it to 1
                                output.morphs[m].dist[p] = 1.0;
                                
                                //Sample the other parameters sample_size times
                                for (int s = 0; s < sample_size; s++) {
                                    single = gr.generate_extension();
                                    if(single != null){
                                        int[] rank = gr.find_order(single);
                                        String input = output.sample_UR();
                                        GrammarFile.Tableau tab = find_tab(input);
                                        String winner = optimize(input, tab, rank);
                                        if (winner.equals(output.form)){
                                            //update the expected counts for the Lexicon
                                            temp_one++;
                                        }
                                    }
                                }
                                output.morphs[m].dist[p] = o_prob;
                                
                            } else {  //we're looking at a UR with only one form
                                //doesn't matter how many times it's successful since we'll just normalize
                                temp_zero = sample_size;
                            }
                            double temp_prob = (temp_one+.0001)*o_prob + (temp_zero+.0001)*(1.0-o_prob);
                            output.morphs[m].one_counts[p] += ((temp_one+.0001)*o_prob)/(temp_prob)*output.freq;
                            output.morphs[m].zero_counts[p] += ((temp_zero+.0001)*(1.0-o_prob))/(temp_prob)*output.freq;
                            
                        }
                    }
                }
            }

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
            
            //now going to reset the lexicon if doing UR learning
            if(ur_learning && !phono){
                for(int m=1; m < df.morphs.length; m++){
                    for(int p=0; p < df.morphs[m].dist.length; p++){
                    df.morphs[m].dist[p] = (df.morphs[m].one_counts[p])/(df.morphs[m].one_counts[p]+df.morphs[m].zero_counts[p]);
                    }
                }
            }

            if (i % mini_eval_freq == 0) {
                if (mini_eval == 0 || mini_eval == 1) {
                    writer.println("The new grammar is:\n" + gr);
                    if (ur_learning){
                        writer.println("The new lexicon is:\n" + df);
                    }
                }
                if (i % quit_early != 0) {
                    evaluate_grammar(mini_eval_sample, i);
                }
            }
            
            if (i % quit_early == 0) {
                if (evaluate_grammar(quit_early_sample, i)) {
                    writer.println("-reached perfection early ----- exiting now");
                    i = iterations;
                    break;
                }
            }
        } //end of iterations
        
        //now going to examine final grammar
        if (final_eval == 0 || final_eval == 1) {
            writer.println("------------------EVALUATING-------------FINAL----------------GRAMMAR--------------------");
            writer.println("The final grammar is:\n" + gr);
            if (ur_learning){
                writer.println("The final lexicon is:\n" + df);
            }
            evaluate_grammar(final_eval_sample, i);
        }
    }
    
    public static void EDL_online() {
        
        // sample a ranking
        double[][] single = gr.generate_extension();
        int i = 0;
        // there are i iterations of sampling and updating
        for (i = 0; i < iterations; i++) {
            if (i % mini_eval_freq == 0) {
                writer.println("Starting iteration " + i);
            }

            if (phono && phono_iterations == i){
                writer.println("ENDING PHONOTACTIC LEARNING AT ITERATION: " + i);
                phono = false;
            }

            //double[][] corr_ranks_samp = new double[gr.grammar.length][gr.grammar.length];
            
            //sample one output form
            DistFile.Output output = null;
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
                        
                        // take sample_size samples from temporary grammar and count matches
                        for (int s = 0; s < sample_size; s++) {
                            single = gr.generate_extension(ext);
                            if (single != null) {
                                int[] rank = gr.find_order(single);
                                if (rank != null) {
                                    String input = output.input;
                                    if (ur_learning){
                                        input = output.sample_UR();
                                    }
                                    //compute learner's winner and compare to actual output
									//BEGIN HS CODE
									String winner;
									if (harmSerial){
										winner = optimizeDerivation(input, rank);
									}
									else{
										GrammarFile.Tableau tab = find_tab(input);
										winner = optimize(input, tab, rank);
									}
									//END HS CODE
                                    //GrammarFile.Tableau tab = find_tab(input); //find the tableau
                                    //String winner = optimize(input, tab, rank);
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
                        
                        // take sample_size samples and count number of matches
                        for (int s = 0; s < sample_size; s++) {
                            single = gr.generate_extension(ext);
                            if (single != null) {
                                int[] rank = gr.find_order(single);
                                if (rank != null) {
                                    String input = output.input;
                                    if (ur_learning){
                                        input = output.sample_UR();
                                    }
                                    //compute learner's winner and compare to actual output
									//BEGIN HS CODE
									String winner;
									if (harmSerial){
										winner = optimizeDerivation(input, rank);
									}
									else{
										GrammarFile.Tableau tab = find_tab(input);
										winner = optimize(input, tab, rank);
									}
									//END HS CODE
                                    //GrammarFile.Tableau tab = find_tab(input); //find the tableau
                                    //String winner = optimize(input, tab, rank);
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
            // test UR parameters if doing UR learning...
            if (ur_learning && !phono){
 
                //for each morpheme in the output form
                for (int m = 0; m < output.morphs.length; m++){
                    //writer.println("Examining morpheme: " + output.morphs[m].name);
                    //initialize all morph counts
                    output.morphs[m].one_counts = new double[output.morphs[m].dist.length];
                    output.morphs[m].zero_counts = new double[output.morphs[m].dist.length];
                    //for each UR parameter in each morpheme
                    for (int p=0; p < output.morphs[m].dist.length; p++){
                        //writer.println("\tExamining parameter: " + p);
                        // remember the current setting of the parameter before testing it both ways
                        double o_prob = output.morphs[m].dist[p];
                        if(output.morphs[m].max_values[p] != 0){
                            //set it each way and sample
                            
                            //set it to 0
                            output.morphs[m].dist[p] = 0.0;

                            //Sample the other parameters sample_size times
                            for (int s = 0; s < sample_size; s++) {
                                single = gr.generate_extension();
                                if(single != null){
                                    int[] rank = gr.find_order(single);
                                    String input = output.sample_UR();
                                    GrammarFile.Tableau tab = find_tab(input);
                                    String winner = optimize(input, tab, rank);
                                    if (winner.equals(output.form)){
                                        //update the expected counts for the Lexicon
                                        output.morphs[m].zero_counts[p]++;
                                    }
                                }
                            }
                            
                            //set it to 1
                            output.morphs[m].dist[p] = 1.0;
                            
                            //Sample the other parameters sample_size times
                            for (int s = 0; s < sample_size; s++) {
                                single = gr.generate_extension();
                                if(single != null){
                                    int[] rank = gr.find_order(single);
                                    String input = output.sample_UR();
                                    GrammarFile.Tableau tab = find_tab(input);
                                    String winner = optimize(input, tab, rank);
                                    if (winner.equals(output.form)){
                                        //update the expected counts for the Lexicon
                                        output.morphs[m].one_counts[p]++;
                                    }
                                }
                            }
                            output.morphs[m].dist[p] = o_prob;
                            
                        } else {  //we're looking at a UR with only one form
                            //doesn't matter how many times it's successful since we'll just normalize
                            output.morphs[m].zero_counts[p] = sample_size;
                        }
                        //writer.println("\t\tZero matches: " + output.morphs[m].zero_counts[p]);
                        //writer.println("\t\tOne matches: " + output.morphs[m].one_counts[p]);
                        //double temp_prob = (temp_one+.01)*o_prob + (temp_zero+.01)*(1.0-o_prob);
                        //output.morphs[m].one_counts[p] = temp_one;
                        //output.morphs[m].zero_counts[p] = temp_zero;
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
            
            //now going to reset the lexicon if doing UR learning
            if(ur_learning && !phono){
                for(int m=0; m < output.morphs.length; m++){
                    for(int p=0; p < output.morphs[m].dist.length; p++){
                        double new_p = ((double)output.morphs[m].one_counts[p]+ .0001)*(output.morphs[m].dist[p])/(((double)output.morphs[m].one_counts[p]+ .0001)*(output.morphs[m].dist[p])+((double)output.morphs[m].zero_counts[p]+ .0001)*(1-output.morphs[m].dist[p]));
                        output.morphs[m].dist[p] = (1-rate)*output.morphs[m].dist[p]+rate*new_p;
                    }
                }
            }

            if (i % mini_eval_freq == 0) {
                if (mini_eval == 0 || mini_eval == 1) {
                    writer.println("The new grammar is:\n" + gr);
                    if (ur_learning){
                        writer.println("The new lexicon is:\n" + df);
                    }

                }
                if (i % quit_early != 0) {
                    evaluate_grammar(mini_eval_sample, i);
                }
            }
            if (i % quit_early == 0) {
                if (evaluate_grammar(quit_early_sample, i)) {
                    writer.println("-reached perfection early ----- exiting now");
                    i = iterations;
                    break;
                }
            }
        }
        
        //now going to examine resulting grammar
        if (final_eval == 0 || final_eval == 1) {
            writer.println("------------------EVALUATING-------------FINAL----------------GRAMMAR--------------------");
            writer.println("The final grammar is:\n" + gr);
            if (ur_learning){
                writer.println("The new lexicon is:\n" + df);
            }
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
                String input = output.input;
                if (ur_learning){
                    input = output.sample_UR();
                }
 
                //sample a random ranking
                double[][] single = gr.generate_extension();
                if (single != null) {
                    int[] rank = gr.find_order(single);
                    //compute learner's winner and compare to actual output
					//BEGIN HS CODE
					if (harmSerial){
						winner = optimizeDerivation(input, rank);
					}
					else{
						GrammarFile.Tableau tab = find_tab(input);
						winner = optimize(input, tab, rank);
					}
					//END HS CODE
                    //GrammarFile.Tableau tab = find_tab(input); //find the tableau
                    //winner = optimize(input, tab, rank);
                    //if equal, add matrix of ranking into collected samples
                    if (winner.equals(output.form)) {
                        corr++;
                    }
                    tot++;
                }
            }
            if (i % mini_eval_freq == 0) {
                if (mini_eval == 0 || (i == iterations && final_eval==0)) {
                    writer.println("Output " + output.form + " " + ((float) corr / tot) + " correct - observed freq is " + output.freq);
                }
            }else{
                if (i == iterations) {
                    if (final_eval == 0) {
                        writer.println("Output " + output.form + " " + ((float) corr / tot) + " correct - observed freq is " + output.freq);
                    }
                }
            }
            log_likelihood += Math.log(((float) corr / tot)) * output.freq;
            error += (((double) tot - (double) corr) / tot) * (double) output.freq;
            corr = 0;
            tot = 0;
        }
        if (i == iterations) {
            if (final_eval == 0 || final_eval == 1) {
                writer.println("FINAL ITERATION :: Total error is " + error + " and log likelihood is " + log_likelihood);
            }
        }else{
            if (i % mini_eval_freq == 0) {
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
        if (w!=""){
            return w;
        } else {
            List<Integer> winners = initializeList(tab.cands.length); //create array that stores information about which candidates are still in the running
            int stop = rank.length;
            
            for (int j = 0; j < rank.length; j++) {
                //figuring out minimum violation for remaining candidates
                int min_vios = -1;
                List<Integer> cwinners = new LinkedList<Integer>(); //tracks winning candidates for each constraint
                for (int i: winners) {
                    if (min_vios == -1) {
                        min_vios = tab.cands[i].violations[rank[j]];
                        cwinners.add(i); //add the first remaining candidate to cwinner
                    } else if (tab.cands[i].violations[rank[j]] < min_vios) {
                        //If you find a new minimum number of violations for this constraint, current candidate
                        //becomes the best candidate
                        min_vios = tab.cands[i].violations[rank[j]];
                        cwinners.clear(); //remove previous winners (they have more violations)
                        cwinners.add(i);
                    } else if (tab.cands[i].violations[rank[j]] == min_vios) {
                        //add current candidate to winners if it has an equal number of violations as the minimum
                        cwinners.add(i);
                    }
                }
                if (cwinners.size() > 0) {
                    winners = cwinners; //remove winners that are not winners on this constraint
                }
                if (winners.size() < 2 || cwinners.size() == 0) {
                    //If there is only one remaining candidate or all candidates have been eliminated
                    stop = j;
                    break;
                }
            }
            String winner = tab.cands[winners.get(0)].oform; //If there are more than one winners, this chooses the last one in tableau
            track(stop, rank, winner, input); //Add to prefix tree to avoid repeat calculations
            return winner;
        }
    }
//BEGIN HS CODE	
    public static String optimizeStep (String input, BuildTab.Tableau tab, int[] rank) {
        List<Integer> winners = initializeList(tab.cands.length); //create array that stores information about which candidates are still in the running
		int stop = rank.length;
		
		for (int j = 0; j < rank.length; j++) {
			//System.out.println(CON_names[j]);
			//figuring out minimum violation for remaining candidates
			int min_vios = -1;
			List<Integer> cwinners = new LinkedList<Integer>(); //tracks winning candidates for each constraint
			for (int i: winners) {
				//System.out.print("Violations for "+input+"->"+tab.cands[i].form+" ");
				//System.out.println(tab.cands[i].violations[rank[j]]);
				if (min_vios == -1) {
					min_vios = tab.cands[i].violations[rank[j]];
					cwinners.add(i); //add the first remaining candidate to cwinner
				} else if (tab.cands[i].violations[rank[j]] < min_vios) {
					//If you find a new minimum number of violations for this constraint, current candidate
					//becomes the best candidate
					min_vios = tab.cands[i].violations[rank[j]];
					cwinners.clear(); //remove previous winners (they have more violations)
					cwinners.add(i);
				} else if (tab.cands[i].violations[rank[j]] == min_vios) {
					//add current candidate to winners if it has an equal number of violations as the minimum
					cwinners.add(i);
				}
			}
			if (cwinners.size() > 0) {
				winners = cwinners; //remove winners that are not winners on this constraint
			}
			if (winners.size() < 2 || cwinners.size() == 0) {
				//If there is only one remaining candidate or all candidates have been eliminated
				stop = j;
				break;
			}
		}
		String winner = tab.cands[winners.get(0)].oform; //If there are more than one winners, this chooses the last one in tableau
		track(stop, rank, winner, input); //Add to prefix tree to avoid repeat calculations
		return winner;
     
    }
	
	public static String optimizeDerivation (String input, int[] rank){
		BuildTab.Tableau tab = BuildTab.get_tab(input, GEN, CON,
											GEN_functions, CON_names,
											CON_num, func_num, changeSegLists, false);
		String winner = optimizeStep(input, tab, rank);
		
		while (winner != input){
			input = winner;
			tab = BuildTab.get_tab(input, GEN, CON,
									GEN_functions, CON_names,
									CON_num, func_num, changeSegLists, false);
			winner = optimizeStep(input, tab, rank);
			//System.out.println("Input: "+input);
			//System.out.println("Winner: "+winner);
			//System.out.print("Ranking: ");
		}

		return winner;		
	}
//END HS CODE    

    public static String prevFound(int[] rank, String input) {
        //returns the previously found winner if there is one
        String winner = "";
        if (intable.containsKey(input)) {
            PrefixTree ptree = intable.get(input);
            winner = ptree.find(rank); //retrieve stored winner from prefixTree
            if(winner!=null){
                //writer.println("Found something!"+winner);
            } else{
                winner = "";
            }
        }
        return winner;
    }
    
    public static void track(int stop, int[] rank, String winner, String input){
        //Keeps track of the prefix tree for each input
        if(intable.containsKey(input)) {
            //writer.println("Already contains!");
        }else{
            //Add to table of input / prefix tree pairs if not already in table
            intable.put(input, new PrefixTree(rank.length));
        }
        PrefixTree ptree = intable.get(input);
        if (stop < maxdepth) {
            //If maxdepth not exceeded:
            int[] pre = Arrays.copyOfRange(rank, 0, stop + 1);
            ptree.put(pre, winner); //insert winner in prefix tree at prefix pre
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

