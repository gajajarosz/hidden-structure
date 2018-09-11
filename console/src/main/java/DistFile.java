package learner;
//Support code for parsing the distribution file for both the GLA and EDL learner

import java.io.*;
import java.util.regex.*;
import java.util.TreeSet;
import java.util.Set;
import java.util.Vector;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Iterator;


public class DistFile {
    private Writer writer;
    private Vector morphVec;
    private Vector morphSets;
    private boolean ur_learning = false;

    DistFile(String fn, Writer writer) {
	// Load the data from a file
	BufferedReader stream;
	this.writer = writer;
	this.ur_learning = ur_learning;
	morphVec = new Vector();
	morphSets = new Vector();

	try {
	    stream = new BufferedReader(new FileReader(fn));
	} catch (IOException ioe) {
	    writer.println(ioe + "\nUnable to find the distribution file with the name " + fn + ". Exiting...");
	    System.exit(0);
	    return;
	}

	Vector morphVec = new Vector();
	Vector morphSets = new Vector();

	try {
	    String line;

	    Pattern numpairs = Pattern.compile("^([0-9]+)\\s+pairs.*$");
	    Pattern pairs = Pattern.compile("\"(.*)\"\\s+\"(.*)\"\\s+([0-9]+)\\s+([0-9,]+)\\s+(.*)$");

	    int curPair = 0;
	    while ((line = stream.readLine()) != null) {
		Matcher m1 = numpairs.matcher(line);
		Matcher m2 = pairs.matcher(line);
		if (m1.matches()) {
		    int p = Integer.valueOf(m1.group(1));
		    outputs = new Output[p];
		    //System.out.println("" + p + " pairs.");
		} else if (m2.matches()) {
		    outputs[curPair] = new Output();
		    outputs[curPair].form = m2.group(2);
		    outputs[curPair].freq = Integer.valueOf(m2.group(3));
		    //System.out.println("output: " + outputs[curPair].form + " freq: " + outputs[curPair].freq);
		    //System.out.println("morphs: " + m2.group(4));
		    //System.out.println("inputs: " + m2.group(6));

		    //now setting target underlying forms - leaves the option to have multiple target URs available
		    outputs[curPair].inputs = m2.group(5).split("\\s+");
		    outputs[curPair].dist = new double[outputs[curPair].inputs.length];
		    for (int i = 0; i < outputs[curPair].inputs.length; i++) {
			outputs[curPair].dist[i] = ((double) 1.0) / ((double) outputs[curPair].inputs.length);
		    }
		    // this variable is used for a unique target UR
		    outputs[curPair].input = outputs[curPair].inputs[0];

		    //now making morphs and assigning them outputs
		    String m[] = m2.group(4).split(",");
		    outputs[curPair].morphs = new Morph[m.length];
		    for (int mo = 0; mo < m.length; mo++) {
			int mname = Integer.valueOf(m[mo]);
			//writer.println("mname is '" + mname + "' and size is " + morphVec.size() + " AND vector is: " + morphVec.toString()); 

			morphVec.ensureCapacity(mname+1);
			morphSets.ensureCapacity(mname+1);
			Morph cm = null;
			if (mname < morphVec.size()) {
			    cm = (Morph) morphVec.get(mname);
			} else {
			    morphVec.setSize(mname+1);
			    morphSets.setSize(mname+1);
			    //writer.println("setting to size " + (mname+1));
			}
			//writer.println("cm is '" + cm); 

			if (cm == null) {
			    cm = new Morph();
			    cm.name = mname;
			    //writer.println("Adding to morphVec " + mname);
			    morphVec.set(mname, cm);
			    morphSets.add(mname, new TreeSet());
			}

			//System.out.println("figuring out underlying forms for morpheme " + mname);
			outputs[curPair].morphs[mo] = cm;
		    }

		    curPair++;
		} else {
			writer.println("The following line from the distribution file do not match the specified format and will be ignored: \n>>>" + line);
		}
	    }
	} catch (IOException ioe) {
	    writer.println(ioe + "\nError reading the dist file: " + fn + ". Exiting...");
	    System.exit(-1);
	}

	//now that we've gone through all the data. Collect all morphs into the morphs list
	int numMorphs = 0;
	for (int i = 0; i< morphVec.size(); i++) {
		if (morphVec.get(i) != null) numMorphs++;
	}
	this.morphs = new Morph[numMorphs+1];
	//writer.println("Making morphs, it has size: " + (numMorphs+1));
	//setting up 'morphs' to point to morph objects we created above
        numMorphs = 0;
        for (int i = 0; i< morphVec.size(); i++) {
 	    if (morphVec.get(i) != null) {
	        Morph cm = (Morph) morphVec.get(i);
	        this.morphs[++numMorphs] = cm;
	    }
        }

	int totalfreq = 0;
	for (int o = 0; o < outputs.length; o++) {
	    totalfreq += outputs[o].freq;
	}
	for (int o = 0; o < outputs.length; o++) {
	    outputs[o].relfreq = (double) outputs[o].freq / (double) totalfreq;
	}

    }

    //given output index and input index, get probability of that input
    public double get_i_prob(int o_index, int i_index) {
	if (!this.ur_learning) {
	    return this.outputs[o_index].dist[i_index];
	} else {
	    //no longer in phonotactic learning
	    //this doesn't make sense for parameterized URs
	    return (double) 0.0;
	}
    }

    public String toString() {
	String toret = "";
	if (!this.ur_learning) {
	    for (int o = 0; o < outputs.length; o++) {
		toret += "Surface Representation: " + outputs[o].form;
		for (int i = 0; i < outputs[o].inputs.length; i++) {
		    toret += "\tUnderlying Representation: " + outputs[o].inputs[i] + "\tFrequency: " + outputs[o].freq + "\n";
		}
	    }
	} else {
	for (int m=1; m < morphs.length; m++){
		toret += "Morpheme " + morphs[m].name + " Dist:\n";
		Iterator settings = ((Set) morphs[m].UR_map.keySet()).iterator();
		while(settings.hasNext()){
		    Vector cv = (Vector) settings.next();
		    double prob = 1.0;
		    for (int p=0; p < cv.size(); p++){
			if(cv.get(p).equals(Integer.valueOf(1))){
			    prob *= morphs[m].dist[p];
			}else{
			    prob *= (1.0-morphs[m].dist[p]);
			}
		    }
		    toret += "\t" + morphs[m].UR_map.get(cv) + " - " + prob + "\n";
		}
	    }
	}
	return toret;
    }

    public void read_URs(String fn){
	writer.println("Reading URs from " + fn);
    	this.ur_learning = true;
	BufferedReader stream;
	try {
	    stream = new BufferedReader(new FileReader(fn));
	} catch (IOException ioe) {
	    this.writer.println(ioe + "\nUnable to find the UR distribution file with the name " + fn + ". Exiting...");
	    System.exit(0);
	    return;
	}

	try {
	    String line;

	    Pattern nummorphs = Pattern.compile("^([0-9]+) morphs.*$");
	    Pattern morphs = Pattern.compile("^morph\\s+([0-9]+).*$");
	    //Pattern morphs = Pattern.compile("^([0-9]+)((\\s+(.*)-([0-9]+,?)+)+)$");
	    Pattern urs = Pattern.compile("^\\s*UR\\s+\\[([0-9]+)\\]\\:\\s+([^\\s]+)\\s+(([0-9]+\\s*)+)$");

	    int curMorph = 0;
	    Morph cm = this.morphs[0];
	    while ((line = stream.readLine()) != null) {
		Matcher m3 = nummorphs.matcher(line);
		Matcher m4 = morphs.matcher(line);
		Matcher m5 = urs.matcher(line);
		if (m3.matches()){
		    int morphlen = Integer.valueOf(m3.group(1));
		    if ((morphlen+1) != this.morphs.length){
		    	this.writer.println("The number of morphemes in the distribution file does not match the number of morphemes in the UR file. Exiting...");
	    		System.exit(0);
	    		return;
		    }
		} else if (m4.matches()){
		    //update the morpheme we're looking at
		    curMorph = Integer.valueOf(m4.group(1));
		    //writer.println("Processing URs for Morph " + curMorph);
		    cm = this.morphs[curMorph];
		} else if (m5.matches()){
		    String u = m5.group(2);
		    String params = m5.group(3);
		    cm.add_form(u, params);
		} else {
	 	    writer.println("The following line from the UR file do not match the specified format and will be ignored: \n>>>" + line);
		}
	    }
	} catch (IOException ioe) {
	    writer.println(ioe + "\nError reading the UR file: " + fn + ". Exiting...");
	    System.exit(-1);
	}
	
	//going through and setting all distributions to uniform, and to 1 if only one UR is there for a morph
	for (int i = 1; i < morphs.length; i++) {
	    Morph cm = morphs[i];
	    //writer.println("Current morph is " + morphs[i].name + " and number of morphs is " + morphs.length);
	    for (int p = 0; p < cm.dist.length; p++){
		if(cm.max_values[p] == 0){  //this is a UR that has only one form
		    cm.dist[p] = 0;
		}else{
		    cm.dist[p] = 0.5;
		}
	    }
	}
    }

    public class Morph {
	public int name;
	public int[] max_values;
	//given the parameter settings, returns the form
	public Map<Vector<Integer> ,String> UR_map = new HashMap();
	public double[] dist;
	//these are used for learning
	public double[] one_counts;
	public double[] zero_counts;

	/*
	public double[] newdist;
	public String[] forms;
	public Output[] outputs;
	*/
	public void add_form(String f, String params){
	    String[] p_split = params.split("\\s+");
	    Vector<Integer> v = new Vector(p_split.length);
	    
	    for (int i=0; i < p_split.length; i++) {
		v.add(i, Integer.valueOf(p_split[i]));
	    }
	    if ((dist != null) && (dist.length != v.size())) {
		System.err.printf("Differing number of parameters for morph %d (%d vs %d)\n", name, dist.length, v.size());
		System.exit(-1);
	    }
	    if (dist== null) {
		max_values = new int[v.size()];
		dist = new double[v.size()];
	    }
	   
	    for (int i=0; i<v.size(); i++) {
		if (max_values[i] < v.get(i)) {
		    max_values[i] = v.get(i);
		}
	    }
	    //System.out.println("\t\tAdding vector: " + v + " , form: " + f);
	    UR_map.put(v, f);
	}
    }

    public class Output {
	public int freq;
	public double relfreq = 0.0;
	public String form;
	// this stores the assumed single (first) target UR
	public String input;
	// this store morphs that make up each output, if we're doing UR learning
	public Morph[] morphs;
	// these are left in case we want separate phonotactic and morphophonological UR spaces	
	public double[] dist;
	public String[] inputs;

	public String sample_UR(){
	    //for each morpheme in the output, sample each parameter settings, locate corresponding form, then concatenate
	    String input = "";
	    for(int m = 0; m < this.morphs.length; m++){
		if(m > 0){
		    input += "-";
		}
		Vector<Integer> cv = new Vector(this.morphs[m].dist.length);
		//System.out.println("\t\tLooking at morph " + output.morphs[m].name);
		for(int p = 0; p < this.morphs[m].dist.length; p++){
		    double rand = Math.random();
		    if(rand < this.morphs[m].dist[p]){
			cv.add(p,Integer.valueOf(1)); 
		    }else{
			cv.add(p,Integer.valueOf(0)); 
		    }
		    //System.out.println("Current vector is " + cv);
		}
		String m_form = this.morphs[m].UR_map.get(cv);
		input += "" + m_form;
	    }
	    return input;
	}
    }

    public Output[] outputs;
    public Morph[] morphs;
}
