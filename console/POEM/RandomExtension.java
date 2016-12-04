import java.text.NumberFormat;

public class RandomExtension {

    public double[][] grammar;   //contains mutual ranking probabilities - the grammar
    public String[] constraints;
    public int[] faith;
    public int[] i_cor; // these store indices of all constraint pairs
    public int[] j_cor;

    public RandomExtension (GrammarFile gf) {
	this.uni_grammar(gf.constraints.length);
	this.constraints = gf.constraints;
	this.faith = gf.faith;
	this.init_pairs();
    }

    public RandomExtension () {
    }

    public static void main (String[] argv){
	RandomExtension re = new RandomExtension();
	re.rand_grammar(Integer.parseInt(argv[0]));
	System.out.println(re);

	int n_re_es = Integer.parseInt(argv[1]);
	int k=0;

	int n = Integer.parseInt(argv[2]);
	while (k < n_re_es){
	    re = re.reEstimate(n);
	    System.out.println(re);
	    k++;
	}

	//while ( k < n_re_es){
	//    re.generate_extension();
	//    k++;
	//}
    }

    public RandomExtension reEstimate_pairwise (int n) {
	int k = 0;
	double[][] sums = new double[grammar.length][grammar.length];

	//generate an ordering to start with
	double[][] seed = generate_extension();
	double[][] burn = generate_extension1(seed);
	for (int b = 0; b < 10000; b++){
	    burn = generate_extension1(seed);
	}
	while (k < n){
	    double[][] single = generate_extension1(burn); //change back to do matrix sampling
	    for(int i=0; i < grammar.length; i++){
		for(int j=0; j < grammar.length; j++){
		    sums[i][j] += single[i][j];
		}
	    }
	    k++;
	}

	double[][] temp = new double[grammar.length][grammar.length];
	for(int i=0; i < grammar.length; i++){
	    for(int j=0; j < grammar.length; j++){
		temp[i][j] = (sums[i][j])/(sums[i][j] + sums[j][i]);
	    }
	}

	RandomExtension re = new RandomExtension();
	re.grammar = temp;
	re.constraints = constraints;
	return re;
	
    }

    public RandomExtension reEstimate (int n) {
	int k = 0;
	double[][] sums = new double[grammar.length][grammar.length];
	double err = 0.0;
	while (k < n){
	    double[][] single = generate_extension(); //change back to do matrix sampling
	    for(int i=0; i < grammar.length; i++){
		for(int j=0; j < grammar.length; j++){
		    sums[i][j] += single[i][j];
		}
	    }
	    k++;
	}

	double[][] temp = new double[grammar.length][grammar.length];
	for(int i=0; i < grammar.length; i++){
	    for(int j=0; j < grammar.length; j++){
		temp[i][j] = (sums[i][j])/(sums[i][j] + sums[j][i]);
		if (i != j){
		    err += Math.abs(temp[i][j] - grammar[i][j]);
		}
	    }
	}

	err = err/((grammar.length*(grammar.length-1))/2);
	System.out.println("Sampling Error is " + err);
	RandomExtension re = new RandomExtension();
	re.grammar = temp;
	re.constraints = constraints;
	return re;
    }

    public void uni_grammar (int size) {
	grammar = new double[size][size];

	for (int i=0; i < size; i++){
	    for (int j=i+1; j < size; j++) {
		mod_gram(grammar,i,j,.5);
	    }
	}
    
    }

    public void bias_grammar () {
	int size = grammar.length;

	for (int i=0; i < size; i++){
	    for (int j=i+1; j < size; j++) {
		//if (this.faith[i] > this.faith[j]){
		//    mod_gram(grammar,i,j,.1);
		//}else 
		if (this.faith[i] < this.faith[j]){
		    mod_gram(grammar,i,j,.99);
		}else if (this.faith[j] < this.faith[i]){
		    mod_gram(grammar,j,i,.99);
		}else{
		    mod_gram(grammar,i,j,.5);
		}
	    }
	}
    }

    public void rand_grammar (int size) {
	grammar = new double[size][size];
	for (int i=0; i < size; i++){
	    for (int j=0; j < size; j++) {
		grammar[i][j] = -1;
	    }
	}

	for (int i=0; i < size; i++){
	    for (int j=i+1; j < size; j++) {
		if (grammar[i][j] == -1){
		    mod_gram(grammar,i,j,Math.random());
		}
	    }
	}

	for (int i=0; i < size; i++){
	    for (int j=i+1; j < size; j++) {
		if ((grammar[i][j] > 0.0) && (grammar[i][j] < 1.0)) {
		    if ( Math.random() < .1){
			mod_gram(grammar,i,j,0);
			//			makeConsistent(grammar, i, j);    		
		    }else if ( Math.random() < .1){
			mod_gram(grammar,i,j,1);
			//			makeConsistent(grammar, i, j);    		    
		    }
		}
	    }
	}
	
	constraints = new String[size];
	for (int i=0; i < size; i++){
	    constraints[i] = "XXXX ";
	}
    }

    //this method sets the grammar to the mode setting for each parameter
    public double[][] mode_grammar() {
	int size = grammar.length;
	double[][] mode = cloneGrammar();
	for (int r=0; r < size; r++){
	    for (int c=0; c < r; c++){
		if (mode[r][c] > .55){
		    mode[r][c] = 1;
		    mode[c][r] = 0;
		}else if (mode[r][c] < .45){
		    mode[r][c] = 0;
		    mode[c][r] = 1;
		}
	    }
	}
	return mode;
    }

    //this is an alternative sampling method using pair-wise rerankings
    public double[][] generate_extension1 (double[][] single) {
	int size = grammar.length;
	double[][] ext = cloneGrammar();
	//System.out.println("Current sample is:\n" + this.gramToString(single));

	//order[0] is the index of the top-ranked constraint
	int[] order = find_order(single);
	//System.out.println("current order = " + rankToString(order));
	    
	// this will store probs of each consecutive pairwise reranking
	double[] p_rerank = new double[size-1]; 
	double sum = 0.0;
	for (int pos = 0; pos < size - 1; pos++){
	    int c1 = order[pos];
	    int c2 = order[pos+1];
	    p_rerank[pos] = ext[c2][c1];
	    sum += p_rerank[pos];
	}

	double r = Math.random();
	int pos = -1; // position of reranking...
	while (r >= 0.0){
	    pos++;
	    r -=(double)1.0/((double)size-1);
	}

	// proposed sample
	int[] sample = new int[size];
	for (int i=0; i < size; i++){
	    sample[i] = order[i];
	}
	int c1 = order[pos];
	int c2 = order[pos+1];
	sample[pos] = c2;
	sample[pos+1]=c1;
	
	//System.out.println("proposed sample = " + rankToString(sample));
	//double a = (double)p_rerank[pos]/((double)1-p_rerank[pos]);
	double a = p_rerank[pos];
	
	//System.out.println("a is " + a);
	if((a > 1) || (Math.random() < a)){
	    mod_gram(single,c1,c2,0);
	    //System.out.println("accepted sample");
	    //System.out.println("new order is = " + rankToString(find_order(single)));
	    return single;	    
	}else{
	    return null;
	}
    }

    public boolean makeMeConsistent () {
	int size = grammar.length;
	for (int i = 0; i < size; i++) {
	    for (int j=i+1; j < size; j++) {
		if((grammar[i][j] == 0) || (grammar[i][j] == 1)){
		    //System.out.println("\tCurrent grammar has a set ranking at " + i + ", " + j);
		    if( ! makeConsistent(grammar, i, j)){
			return false;
		    }
		}
	    }
	}
	return true;
    }

    public boolean makeMeConsistent (double[][] gram) {
	int size = gram.length;
	for (int i = 0; i < size; i++) {
	    for (int j=i+1; j < size; j++) {
		if((gram[i][j] == 0) || (gram[i][j] == 1)){
		    //System.out.println("\tCurrent grammar has a set ranking at " + i + ", " + j);
		    if( ! makeConsistent(gram, i, j)){
			return false;
		    }
		}
	    }
	}
	return true;
    }

    public void init_pairs () {
	int size = grammar.length;

	// Generate the pairs that we will use for later shuffling
	int num_pairs = ((size * size) - size) / 2; 
	i_cor = new int[num_pairs];
	j_cor = new int[num_pairs];

	int count = 0;
	int unset = 0;
	for (int i = 0; i < size; i++) {
	    for (int j=i+1; j < size; j++) {
		i_cor[count] = i;
		j_cor[count] = j;
		count++;
	    }
	}

    }

    public double[][] generate_extension () {
	return generate_extension(grammar);
    }

    public double[][] generate_extension (double[][] gram) {
	int size = grammar.length;
	double[][] ext = cloneGrammar(gram);

	//System.out.println("The current grammar is:\n" + this.gramToString(ext));

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

	//System.out.println("Starting at:\n" + gramToString(ext));

	//going through cells in random order and setting them
	for (int k = 0; k < i_cor.length; k++) {
	    int i_pos = i_cor[k];
	    int j_pos = j_cor[k];
	    if ((ext[i_pos][j_pos] != 0) && (ext[i_pos][j_pos] != 1)){ //this is an unset mutual ranking
		//ratio determines how strong of a "flattening" bias there is, if any

		if ( ((ext[i_pos][j_pos])) < Math.random() ){
		    if( ! mod_gram(ext,i_pos,j_pos,0)){
			//System.out.println("Tried to sample for (" + i_pos + ", " + j_pos + ") -- setting to 0");
			return null;
		    }
		    //compensate(ext, j_pos, i_pos, 0);
		}else{
		    if(!mod_gram(ext,i_pos,j_pos,1)){
			//System.out.println("Tried to sample for (" + i_pos + ", " + j_pos + ") -- setting to 1");
			return null;
		    }
		    //compensate(ext, i_pos, j_pos, 0);
		}
		//System.out.println("\tGenerateExtention :: Setting (" + i_pos + ", " + j_pos + "):\n" + gramToString(ext));
		if( ! makeConsistent(ext, i_pos, j_pos)){
		    return null;
		}
		//unBias(ext, i_pos, j_pos);
	    }	 
	}

	//System.out.println(gramToString(ext));
	//translating matrix into an order and printing
	/*
	int[] ord = find_order(ext);
	if (ord == null) {
	    //System.out.println("No consistent order found!");
	    ext = null;
	} else {
	    String toprint = "" + ord[0];
	    for (int i = 1; i < ord.length; i++) {
		toprint += " >> " + ord[i];
	    }
	    //System.out.println("Order is " + toprint);
	}
	*/
	return ext;
    }

    public int[] find_order (double[][] ext) {
	int size = ext.length;
	int[] to_ret = new int[size];
	//boolean[] consist = new boolean[size];
	for (int c = 0; c < size; c++) {
	    int wins = 1;
	    for (int i = 0; i < size; i++) {
		if ((c != i) && (ext[c][i] > 0.99)) {
		    wins++;
		}
	    }
	    to_ret[size - wins] = c;
	    //consist[size - wins] = true;
	}
	/*for (int c = 0; c < size; c++) {
	    if (consist[c] == false){
		return null;
	    }
	    }*/	
	return to_ret;
    }


    public int[] find_order_old (double[][] ext) {
	// return null if there is no order
	int size = ext.length;
	int[] to_ret = new int[size];
	int[] used = new int[size];
	//for (int i = 0; i < size; i++) {used[i] = 0;}
	
	for (int numused = 0; numused < size; numused++) { //goes through the constraints
	    int found_any = 0; //keeps track of whether a possible next constraint was found
	    for (int i = 0; (i < size) && (found_any == 0); i++) { // goes through the rows
		if (used[i] == 0) { // if this constraint hasn't been ranked yet and no constraint has been found...
		    // Checking to see if i is next
		    int ok = 1;
		    for (int k = 0; k < size; k++) {  // checking if all unranked constraints are lower than i
			if ((used[k] == 0) && (ext[k][i] >= 0.99) && (k != i)) {
			    ok = 0; // found an unranked constraint above i
			}
		    }
		    if (ok == 1) {  // constraint i has nothing above it!
			found_any = 1;
			used[i] = 1; //ranking constraint i
			to_ret[numused] = i; //constraint i is the next constraint
		    }
		}
	    }
	    if (found_any == 0) { //if all constraints have been considered but none can go next
		// No consistent order!
		return null;
	    }
	}
	return to_ret;
    }

    public double[][] cloneGrammar() {
	return cloneGrammar(grammar);
    }

    public double[][] cloneGrammar(double[][] ext) {
	double[][] to_ret = ext.clone();
	for (int i = 0; i < ext.length; i++) {
	    to_ret[i] = ext[i].clone();
	}
	return to_ret;
    }

    public void unBias (double[][] ext, int i_pos, int j_pos) {
	int bo = (int)ext[i_pos][j_pos];

	//System.out.println("Making Unbiased for: (" + i_pos + ", " + j_pos + "):\n" + gramToString(ext));

	// go through the row and unbias
	for (int j=0; j < ext.length; j++){
	    if((j != j_pos) && (ext[i_pos][j] != 0) && (ext[i_pos][j] != 1)){
		mod_gram(ext, i_pos, j, (19*ext[i_pos][j]+1*.5)/(20));
	    }
	}

	// go through the column and unbias
	for (int i=0; i < ext.length; i++){
	    if((i != i_pos) && (ext[i][j_pos] != 0) && (ext[i][j_pos] != 1)){
		mod_gram(ext, i, j_pos, (19*ext[i][j_pos]+1*.5)/(20));
	    }
	}

	//System.out.println("Result: (" + i_pos + ", " + j_pos + "):\n" + gramToString(ext));

    }

    public boolean makeConsistent (double[][] ext, int i_pos, int j_pos) {
	int bo = (int)ext[i_pos][j_pos];
	//System.out.println("\tMaking (" + i_pos + ", " + j_pos + ") consistent for with val = " + bo);
	/*
	for (int x = 0; x < ext.length; x++) {
	    if ((x != i_pos) && (ext[x][i_pos] == bo)) {
		if( ! mod_gram(ext, x, j_pos, bo)){
		    System.out.println("Tried to make consistent for (" + i_pos + ", " + j_pos + ") -- setting to " + bo);
		}
	    }
	    if ((x != j_pos) && (ext[j_pos][x] == bo)) {
		if( ! mod_gram(ext, i_pos, x, bo)){
		    System.out.println("Tried to make consistent for (" + i_pos + ", " + j_pos + ") -- setting to " + bo);
		}
	    }
	    for (int y = 0; y < ext.length; y++) {
		if ((x!= i_pos) && (y != j_pos) && (x != y) &&
		    (ext[x][i_pos] == bo) && (ext[j_pos][y] == bo)) {
		    if( ! mod_gram(ext, x, y, bo)){
			System.out.println("Tried to make consistent for (" + i_pos + ", " + j_pos + ") -- setting to " + bo);
		    }
		}
	    }
	}
	*/
	//read ext(x,y) as "prob that x is above y"
	//this part checks whether something is higher/lower than constraint at j_pos
	for (int col = 0; col < ext.length; col++){
	    if ((ext[j_pos][col]==bo) && (ext[i_pos][col] != bo) && (j_pos != col) && (i_pos != col)) {
		if (ext[i_pos][col] == (1-bo)) {
		    //System.out.println("Resetting from 0-1 or 1-0");
		    //System.exit(-1);
		}
		if( ! mod_gram(ext,i_pos,col,bo)){
		    //System.out.println("Tried to make consistent for (" + i_pos + ", " + j_pos + ") -- setting to " + bo);
		    return false;
		}
		//System.out.println("Making Consistent: (" + i_pos + ", " + col + "):\n" + gramToString(ext));
		if(! makeConsistent(ext, i_pos, col)){
		    return false;
		}
	    }
	}

	//this part checks whether something is higher/lower than constraint at i_pos
	for (int col = 0; col < ext.length; col++){
	    if ((ext[i_pos][col]==(1-bo)) && (ext[j_pos][col] != (1-bo)) && (col != j_pos) && (j_pos != col)) {
		if (ext[j_pos][col] == bo) {
		    //System.out.println("Resetting from 0-1 or 1-0");
		    //System.exit(-1);
		}
		if ( ! mod_gram(ext,j_pos,col,1-bo)){
		    //System.out.println("Tried to make consistent for (" + i_pos + ", " + j_pos + ") -- setting to " + bo);
		    return false;
		}
		//System.out.println("Making Consistent: (" + j_pos + ", " + col + "):\n" + gramToString(ext));
		if (! makeConsistent(ext, j_pos, col)){
		    return false;
		}
	    }
	}
	return true;
    }

    public void compensate (double[][] ext, int i_pos, int j_pos, double old_ij) {
	//this part attempts to compensate for choices made probabilistically ...
	//going to go through each third constraint and penalize the lower of the just ranked pai
	//System.out.println("Just set " +  i_pos + " and " + j_pos + ", used to be " + (double)old_ij + " now: " + ext[i_pos][j_pos]);
	for (int x=0; x < ext.length; x++){
	    if ((x != j_pos) && (x != i_pos) && (ext[j_pos][x] !=0) && (ext[j_pos][x] !=1)){
		//if ((x != j_pos) && (x != i_pos)){
		double xij = ext[x][i_pos]*ext[x][j_pos];
		double ixj = ext[i_pos][x]*ext[x][j_pos];
		double ijx = ext[i_pos][x]*ext[j_pos][x];
		double new_jx = ijx/(xij+ixj+ijx);
		//System.out.println("\tTarget: " + j_pos + " is above " + x + " with prob " +  ext[x][j_pos]);
		//System.out.println("\tBias: " + i_pos + " is above " + x + " with prob " +  ext[i_pos][x]);
		//System.out.println("\tChanging prob of target to " +  new_jx);

		double new_xi = xij/(xij+ixj+ijx);
		//System.out.println("Before Compensate:\n" + gramToString(ext));
		//mod_gram(ext, j_pos, x,new_jx);
		
		ext[j_pos][x] = 0.9* ext[j_pos][x] + 0.1 * new_jx;
		ext[x][j_pos] = 1-ext[j_pos][x];
		ext[x][i_pos] = 0.9 * ext[x][i_pos]+ 0.1 * new_xi;
		ext[i_pos][x] = 1-ext[x][i_pos];
		
		//System.out.println("After Compensate:\n" + gramToString(ext));

		/*
		ext[j_pos][x] = ext[j_pos][x]*.95;
		ext[x][j_pos] = 1-ext[j_pos][x];
		
		ext[x][i_pos] = ext[x][i_pos]*.95;
		ext[i_pos][x] = 1- ext[x][i_pos];
		*/

	    }
	}
    }

    /*old really badly written version
   public void makeConsistent (double[][] ext, int i_pos, int j_pos) {
	int bo = (int)ext[i_pos][j_pos];
	//read ext(x,y) as "prob that x is above y"
	//this part checks whether something is higher/lower than constraint at j_pos
	for (int col = j_pos+1; col < ext.length; col++){
	    if ((ext[j_pos][col]==bo) && (ext[i_pos][col] != bo)) {
		if (ext[i_pos][col] == (1-bo)) {
		    System.out.println("Resetting from 0-1 or 1-0");
		    //System.exit(-1);
		}
		mod_gram(ext,i_pos,col,bo);
		//System.out.println("Making Consistent: (" + i_pos + ", " + col + "):\n" + gramToString(ext));
		makeConsistent(ext, i_pos, col);
	    }
	}
	for (int row = 0; row < j_pos; row++) {
	    if ((row != i_pos) && (ext[row][j_pos] == (1-bo))) {
		if ((row < i_pos) && (ext[row][i_pos] != (1-bo))) {
		    if (ext[row][i_pos] == bo) {
			System.out.println("Resetting from 0-1 or 1-0");
			//System.exit(-1);
		    }
		    mod_gram(ext,row,i_pos,1-bo);
		    //System.out.println("Making Consistent: (" + row + ", " + i_pos + "):\n" + gramToString(ext));
		    makeConsistent(ext, row, i_pos);
		}else if ((row > i_pos) && (ext[i_pos][row] != bo)) { 
		    if (ext[i_pos][row] == (1-bo)) {
			System.out.println("Resetting from 0-1 or 1-0");
			//System.exit(-1);
		    }
		    mod_gram(ext,i_pos,row,bo);
		    //System.out.println("Making Consistent: (" + i_pos + ", " + row + "):\n" + gramToString(ext));
		    makeConsistent(ext, i_pos, row);
		}
	    }
	}

	//this part checks whether something is higher/lower than constraint at i_pos
	for (int col = i_pos+1; col < ext.length; col++){
	    if ((ext[i_pos][col]==(1-bo)) && (ext[j_pos][col] != (1-bo))) {
		if (ext[j_pos][col] == bo) {
		    System.out.println("Resetting from 0-1 or 1-0");
		    //System.exit(-1);
		}
		mod_gram(ext,j_pos,col,1-bo);
		//System.out.println("Making Consistent: (" + j_pos + ", " + col + "):\n" + gramToString(ext));
		makeConsistent(ext, j_pos, col);
	    }
	}
	for (int row = 0; row < i_pos; row++) {
	    if ((row != j_pos) && (ext[row][i_pos] == bo)) {
		if ((row < j_pos) && (ext[row][j_pos] != bo)) {
		    if (ext[row][j_pos] == (1-bo)) {
			System.out.println("Resetting from 0-1 or 1-0");
			//System.exit(-1);
		    }
		    mod_gram(ext,row,j_pos,bo);
		    //System.out.println("Making Consistent: (" + row + ", " + j_pos + "):\n" + gramToString(ext));
		    makeConsistent(ext, row, j_pos);
		}else if ((row > j_pos) && (ext[j_pos][row] != (1-bo))) { 
		    if (ext[j_pos][row] == bo) {
			System.out.println("Resetting from 0-1 or 1-0");
			//System.exit(-1);
		    }
		    mod_gram(ext,j_pos,row,1-bo);
		    //System.out.println("Making Consistent: (" + j_pos + ", " + row + "):\n" + gramToString(ext));
		    makeConsistent(ext, j_pos, row);
		}
	    }
	}
    }
*/   
   
    public boolean mod_gram(double[][] gram, int i, int j, double val) {
	double old = gram[i][j];
	
	if (((val == 1.0) && (old == 0.0)) ||
	    ((val == 0.0) && (old == 1.0))) {
	    //System.out.println("\t\tTried resetting from " + old + " to " + val + " for constraints: " + i + ", " + j);
	    //System.out.println("gram was:\n" + gramToString(gram));
	    return false;
	}else{
	    gram[i][j] = val;
	    gram[j][i] = 1.0 - val;
	    //System.out.println("\t\tJust set (" + i + ", " + j + ") to " + val); 
	    return true;
	}
    }

    public String toString (){
	return gramToString(grammar);
    }

    public String rankToString(int[] rank) {
	String toprint = "" + rank[0];
	for (int i = 1; i < rank.length; i++) {
	    toprint += " >> " + rank[i];
	    }
	return toprint;
    }

    public String gramToString (double[][] grammar){
	NumberFormat formatter = NumberFormat.getNumberInstance();
	int size = grammar.length;
	double[] weight = new double[size];
	for (int i=0; i < size; i++){
	    weight[i] = 1.0;
	}
	formatter.setMaximumFractionDigits(2);
	formatter.setMinimumFractionDigits(2);
	formatter.setMinimumIntegerDigits(2);
	formatter.setMaximumIntegerDigits(2);
	String to_return = "     ";

	for (int i=0; i < size; i++){//constraint names at top
	    //	    to_return += " " + formatter.format((double) i);
	    to_return += " " + this.constraints[i];
	}	
	to_return += "\n";
	for (int i=0; i < size; i++){
	    //	    to_return += formatter.format((double) i);
	    to_return += this.constraints[i];
	    for (int j=0; j < i; j++) {
		if(grammar[i][j] <= 0.01){
		    weight[i] *= .01;
		}else{
		    weight[i] *= grammar[i][j];
		}
		if (grammar[i][j] == 1.0) {
		    to_return += " " + " 1   ";
		} else if (grammar[i][j] == 0.0) {
		    to_return += " " + " 0   ";
		} else {
		    to_return += " " + formatter.format((double) grammar[i][j]);
		}
		//to_return += "      ";
	    }
	    to_return += "      ";
	    for (int j=i+1; j < size; j++) {
		if(grammar[i][j] <= 0.01){
		    weight[i] *= .01;
		}else{
		    weight[i] *= grammar[i][j];
		}
		if (grammar[i][j] == 1.0) {
		    to_return += " " + " 1   ";
		} else if (grammar[i][j] == 0.0) {
		    to_return += " " + " 0   ";
		} else {
		    to_return += " " + formatter.format((double) grammar[i][j]);
		}
	    }
	    to_return += "\n";
	}

	//now going to make a linearized version
	for (int i=0; i < size; i++) {
	    to_return += this.constraints[i] + " = " + formatter.format((double) (100.0 +(Math.log((double)weight[i])))) + "\t";
	}
	
	return to_return;
    }

}

