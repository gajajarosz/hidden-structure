package learner;
//Support code for the EDL learner

import java.text.NumberFormat;

public class RandomExtension {

    public double[][] grammar;   //contains mutual ranking probabilities - the grammar
    private String[] constraints;
    private int[] init;
    private int[] i_cor; // these store indices of all constraint pairs
    private int[] j_cor;
	private Writer writer;

    public RandomExtension(GrammarFile gf, Writer writer) {
	this.uni_grammar(gf.constraints.length);
	this.constraints = gf.constraints;
	this.init = gf.init;
	this.init_pairs();
	this.writer = writer;
    }

    public RandomExtension() {
		this.writer = new SystemWriter();
    }

    public static void main(String[] argv) {
	RandomExtension re = new RandomExtension();
	re.rand_grammar(Integer.parseInt(argv[0]));
	re.writer.println(re);

	int n_re_es = Integer.parseInt(argv[1]);
	int k = 0;

	int n = Integer.parseInt(argv[2]);
	while (k < n_re_es) {
	    re = re.reEstimate(n);
	    re.writer.println(re);
	    k++;
	}
    }

    // code used to estimate effect of treating pairwise rankings are marginal probs
    public RandomExtension reEstimate(int n) {
	int k = 0;
	double[][] sums = new double[grammar.length][grammar.length];
	double err = 0.0;
	while (k < n) {
	    double[][] single = generate_extension(); //change back to do matrix sampling
	    for (int i = 0; i < grammar.length; i++) {
		for (int j = 0; j < grammar.length; j++) {
		    sums[i][j] += single[i][j];
		}
	    }
	    k++;
	}

	double[][] temp = new double[grammar.length][grammar.length];
	for (int i = 0; i < grammar.length; i++) {
	    for (int j = 0; j < grammar.length; j++) {
		temp[i][j] = (sums[i][j]) / (sums[i][j] + sums[j][i]);
		if (i != j) {
		    err += Math.abs(temp[i][j] - grammar[i][j]);
		}
	    }
	}

	err = err / ((grammar.length * (grammar.length - 1)) / 2);
	writer.println("Sampling Error is " + err);
	RandomExtension re = new RandomExtension();
	re.grammar = temp;
	re.constraints = constraints;
	return re;
    }

    // sets initial uniform grammar
    public void uni_grammar(int size) {
	grammar = new double[size][size];

	for (int i = 0; i < size; i++) {
	    for (int j = i + 1; j < size; j++) {
		mod_gram(grammar, i, j, .5);
	    }
	}
    }

    // this function initializes grammar with an initial bias based on constraint definitions in grammar file
    public void bias_grammar() {
	int size = grammar.length;

	for (int i = 0; i < size; i++) {
	    for (int j = i + 1; j < size; j++) {
		if (this.init[i] > this.init[j]) {
		    mod_gram(grammar, i, j, .9);
		} else if (this.init[j] > this.init[i]) {
		    mod_gram(grammar, j, i, .9);
		} else {
		    mod_gram(grammar, i, j, .5);
		}
	    }
	}
    }

    public void rand_grammar(int size) {
	grammar = new double[size][size];
	for (int i = 0; i < size; i++) {
	    for (int j = 0; j < size; j++) {
		grammar[i][j] = -1;
	    }
	}

	for (int i = 0; i < size; i++) {
	    for (int j = i + 1; j < size; j++) {
		if (grammar[i][j] == -1) {
		    mod_gram(grammar, i, j, Math.random());
		}
	    }
	}

	for (int i = 0; i < size; i++) {
	    for (int j = i + 1; j < size; j++) {
		if ((grammar[i][j] > 0.0) && (grammar[i][j] < 1.0)) {
		    if (Math.random() < .1) {
			mod_gram(grammar, i, j, 0);
		    } else if (Math.random() < .1) {
			mod_gram(grammar, i, j, 1);
		    }
		}
	    }
	}

	constraints = new String[size];
	for (int i = 0; i < size; i++) {
	    constraints[i] = "XXXX ";
	}
    }

    //this method sets the grammar to the mode setting for each parameter
    public double[][] mode_grammar() {
	int size = grammar.length;
	double[][] mode = cloneGrammar();
	for (int r = 0; r < size; r++) {
	    for (int c = 0; c < r; c++) {
		if (mode[r][c] > .55) {
		    mode[r][c] = 1;
		    mode[c][r] = 0;
		} else if (mode[r][c] < .45) {
		    mode[r][c] = 0;
		    mode[c][r] = 1;
		}
	    }
	}
	return mode;
    }

    // makes sure that transitively implied rankings are set correctly
    public boolean makeMeConsistent() {
	int size = grammar.length;
	for (int i = 0; i < size; i++) {
	    for (int j = i + 1; j < size; j++) {
		if ((grammar[i][j] == 0) || (grammar[i][j] == 1)) {
		    //System.out.println("\tCurrent grammar has a set ranking at " + i + ", " + j);
		    if (!makeConsistent(grammar, i, j)) {
			return false;
		    }
		}
	    }
	}
	return true;
    }

    // makes sure that transitively implied rankings are set correctly
    public boolean makeMeConsistent(double[][] gram) {
	int size = gram.length;
	for (int i = 0; i < size; i++) {
	    for (int j = i + 1; j < size; j++) {
		if ((gram[i][j] == 0) || (gram[i][j] == 1)) {
		    //System.out.println("\tCurrent grammar has a set ranking at " + i + ", " + j);
		    if (!makeConsistent(gram, i, j)) {
			return false;
		    }
		}
	    }
	}
	return true;
    }

    public void init_pairs() {
	int size = grammar.length;

	// Generate the pairs that we will use for later shuffling
	int num_pairs = ((size * size) - size) / 2;
	i_cor = new int[num_pairs];
	j_cor = new int[num_pairs];

	int count = 0;
	int unset = 0;
	for (int i = 0; i < size; i++) {
	    for (int j = i + 1; j < size; j++) {
		i_cor[count] = i;
		j_cor[count] = j;
		count++;
	    }
	}
    }

    public double[][] generate_extension() {
	return generate_extension(grammar);
    }

    public double[][] generate_extension(double[][] gram) {
	int size = grammar.length;
	double[][] ext = cloneGrammar(gram);

	//System.out.println("The current grammar is:\n" + this.toString(ext));

	// Shuffle the pairs.  We don't use Random.shuffle because we want to shuffle i_cor and j_cor together
	for (int k = 0; k < (i_cor.length - 1); k++) {
	    // select a random position and swap constraint nums with it
	    int pos = ((int) Math.floor(Math.random() * (i_cor.length - k))) + k;
	    int tmp_i = i_cor[k];
	    int tmp_j = j_cor[k];
	    i_cor[k] = i_cor[pos];
	    j_cor[k] = j_cor[pos];
	    i_cor[pos] = tmp_i;
	    j_cor[pos] = tmp_j;
	}

	// i_cor[0],j_cor[0] is the first position we examine
	// i_cor[1],j_cor[1] is the second position we examine
	//going through cells in random order and setting them
	for (int k = 0; k < i_cor.length; k++) {
	    int i_pos = i_cor[k];
	    int j_pos = j_cor[k];
	    if ((ext[i_pos][j_pos] != 0) && (ext[i_pos][j_pos] != 1)) { //this is an unset mutual ranking
		//ratio determines how strong of a "flattening" bias there is, if any

		if (((ext[i_pos][j_pos])) < Math.random()) {
		    if (!mod_gram(ext, i_pos, j_pos, 0)) {
			//System.out.println("Tried to sample for (" + i_pos + ", " + j_pos + ") -- setting to 0");
			return null;
		    }
		} else {
		    if (!mod_gram(ext, i_pos, j_pos, 1)) {
			//System.out.println("Tried to sample for (" + i_pos + ", " + j_pos + ") -- setting to 1");
			return null;
		    }
		}
		//System.out.println("\tGenerateExtention :: Setting (" + i_pos + ", " + j_pos + "):\n" +ext);
		if (!makeConsistent(ext, i_pos, j_pos)) {
		    return null;
		}
	    }
	}

	//System.out.println(toString(ext));

	return ext;
    }

    public int[] find_order(double[][] ext) {
	int size = ext.length;
	int[] to_ret = new int[size];
	for (int c = 0; c < size; c++) {
	    int wins = 1;
	    for (int i = 0; i < size; i++) {
		if ((c != i) && (ext[c][i] > 0.99)) {
		    wins++;
		}
	    }
	    to_ret[size - wins] = c;
	}
	return to_ret;
    }

    // make a copy of the grammar
    public double[][] cloneGrammar() {
	return cloneGrammar(grammar);
    }

    // make a copy of the grammar
    public double[][] cloneGrammar(double[][] ext) {
	double[][] to_ret = ext.clone();
	for (int i = 0; i < ext.length; i++) {
	    to_ret[i] = ext[i].clone();
	}
	return to_ret;
    }

    // recursive function that sets rankings implied by transitivity
    public boolean makeConsistent(double[][] ext, int i_pos, int j_pos) {
	int bo = (int) ext[i_pos][j_pos];
	//System.out.println("\tMaking (" + i_pos + ", " + j_pos + ") consistent for with val = " + bo);
	//read ext(x,y) as "prob that x is above y"
	//this part checks whether something is higher/lower than constraint at j_pos
	for (int col = 0; col < ext.length; col++) {
	    if ((ext[j_pos][col] == bo) && (ext[i_pos][col] != bo) && (j_pos != col) && (i_pos != col)) {
		if (ext[i_pos][col] == (1 - bo)) {
		    //System.out.println("Resetting from 0-1 or 1-0");
		    //System.exit(-1);
		}
		if (!mod_gram(ext, i_pos, col, bo)) {
		    //System.out.println("Tried to make consistent for (" + i_pos + ", " + j_pos + ") -- setting to " + bo);
		    return false;
		}
		//System.out.println("Making Consistent: (" + i_pos + ", " + col + "):\n" +ext);
		if (!makeConsistent(ext, i_pos, col)) {
		    return false;
		}
	    }
	}

	//this part checks whether something is higher/lower than constraint at i_pos
	for (int col = 0; col < ext.length; col++) {
	    if ((ext[i_pos][col] == (1 - bo)) && (ext[j_pos][col] != (1 - bo)) && (col != j_pos) && (j_pos != col)) {
		if (ext[j_pos][col] == bo) {
		    //System.out.println("Resetting from 0-1 or 1-0");
		    //System.exit(-1);
		}
		if (!mod_gram(ext, j_pos, col, 1 - bo)) {
		    //System.out.println("Tried to make consistent for (" + i_pos + ", " + j_pos + ") -- setting to " + bo);
		    return false;
		}
		//System.out.println("Making Consistent: (" + j_pos + ", " + col + "):\n" +ext);
		if (!makeConsistent(ext, j_pos, col)) {
		    return false;
		}
	    }
	}
	return true;
    }

    // helper function for setting pairwise rankings
    public boolean mod_gram(double[][] gram, int i, int j, double val) {
	double old = gram[i][j];

	if (((val == 1.0) && (old == 0.0)) ||
	    ((val == 0.0) && (old == 1.0))) {
	    //System.out.println("\t\tTried resetting from " + old + " to " + val + " for constraints: " + i + ", " + j);
	    //System.out.println("gram was:\n" + gram);
	    return false;
	} else {
	    gram[i][j] = val;
	    gram[j][i] = 1.0 - val;
	    //System.out.println("\t\tJust set (" + i + ", " + j + ") to " + val);
	    return true;
	}
    }

    public String toString() {
	NumberFormat formatter = NumberFormat.getNumberInstance();
	int size = grammar.length;
	double[] weight = new double[size];
	for (int i = 0; i < size; i++) {
	    weight[i] = 1.0;
	}
	formatter.setMaximumFractionDigits(2);
	formatter.setMinimumFractionDigits(2);
	formatter.setMinimumIntegerDigits(2);
	formatter.setMaximumIntegerDigits(2);
	String to_return = "     ";

	for (int i = 0; i < size; i++) {//constraint names at top
	    to_return += " " + String.format("%-5s", this.constraints[i]).substring(0, 5);
	}
	to_return += "\n";
	for (int i = 0; i < size; i++) {
	    to_return += String.format("%-5s", this.constraints[i]).substring(0, 5);
	    for (int j = 0; j < i; j++) {
		if (grammar[i][j] <= 0.01) {
		    weight[i] *= .01;
		} else {
		    weight[i] *= grammar[i][j];
		}
		if (grammar[i][j] == 1.0) {
		    to_return += " " + "1    ";
		} else if (grammar[i][j] == 0.0) {
		    to_return += " " + "0    ";
		} else {
		    //					to_return += " " + formatter.format((double) grammar[i][j]);
		    to_return += " " + String.format("%-5.2f", (double) grammar[i][j]);
		}
	    }
	    to_return += "      ";
	    for (int j = i + 1; j < size; j++) {
		if (grammar[i][j] <= 0.01) {
		    weight[i] *= .01;
		} else {
		    weight[i] *= grammar[i][j];
		}
		if (grammar[i][j] == 1.0) {
		    to_return += " " + "1    ";
		} else if (grammar[i][j] == 0.0) {
		    to_return += " " + "0    ";
		} else {
		    to_return += " " + String.format("%-5.2f", (double) grammar[i][j]);
		    //					to_return += " " + formatter.format((double) grammar[i][j]);
		}
	    }
	    to_return += "\n";
	}

	/*now going to make a linearized version
	  for (int i = 0; i < size; i++) {
	  to_return += this.constraints[i] + " = " + formatter.format((double) (100.0 + (Math.log((double) weight[i])))) + "\t";
	  }*/
	return to_return;
    }

    public String rankToString(int[] rank) {
	String toprint = "" + rank[0];
	for (int i = 1; i < rank.length; i++) {
	    toprint += " >> " + rank[i];
	}
	return toprint;
    }

}

