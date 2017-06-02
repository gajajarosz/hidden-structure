package learner;
//Support code for parsing the distribution file for both the GLA and EDL learner

import java.io.*;
import java.util.regex.*;
import java.util.TreeSet;
import java.util.Set;
import java.util.Vector;

public class DistFile {
	DistFile(String fn) {
		// Load the data from a file
		BufferedReader stream;
		try {
			stream = new BufferedReader(new FileReader(fn));
		} catch (IOException ioe) {
			System.out.println(ioe);
			return;
		}

		Vector morphVec = new Vector();
		Vector morphSets = new Vector();

		try {
			String line;

			Pattern numpairs = Pattern.compile("^([0-9]+) pairs.*$");
			Pattern pairs = Pattern.compile("\"(.*)\"\\s+\"(.*)\"\\s+([0-9]+)\\s+(([0-9]+,?)+)\\s+(.*)$");

			int curPair = 0;
			phono = true;
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

					//now doing phonotactic underlying forms
					outputs[curPair].inputs = m2.group(6).split("\\s+");
					outputs[curPair].dist = new double[outputs[curPair].inputs.length];
					for (int i = 0; i < outputs[curPair].inputs.length; i++) {
						outputs[curPair].dist[i] = ((double) 1.0) / ((double) outputs[curPair].inputs.length);
					}

					//now doing morpheme underlying forms
					String m[] = m2.group(4).split(",");
					outputs[curPair].morphs = new Morph[m.length];
					for (int mo = 0; mo < m.length; mo++) {
						int mname = Integer.valueOf(m[mo]);
						morphVec.ensureCapacity(mname + 1);
						morphSets.ensureCapacity(mname + 1);
						Morph cm = null;
						if (mname < morphVec.size()) {
							cm = (Morph) morphVec.get(mname);
						} else {
							morphVec.setSize(mname + 1);
							morphSets.setSize(mname + 1);
						}
						if (cm == null) {
							cm = new Morph();
							cm.name = mname;
							morphVec.add(mname, cm);
							morphSets.add(mname, new TreeSet());
						}
						Set mset = (Set) morphSets.get(mname);

						//System.out.println("figuring out underlying forms for morpheme " + mname);
						outputs[curPair].morphs[mo] = cm;
						String[] t_forms = new String[outputs[curPair].inputs.length];
						//have to go through all inputs to create morpheme urs
						for (int i = 0; i < outputs[curPair].inputs.length; i++) {
							String[] temp = outputs[curPair].inputs[i].split("-");
							//if input and output have the same number of morphemes
							if (temp.length == m.length) {
								mset.add(temp[mo]);
							}
						}
					}

					curPair++;
				}
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
			return;
		}
		int numMorphs = 0;
		for (int i = 0; i < morphVec.size(); i++) {
			if (morphVec.get(i) != null) numMorphs++;
		}
		morphs = new Morph[numMorphs];

		numMorphs = 0;
		for (int i = 0; i < morphVec.size(); i++) {
			if (morphVec.get(i) != null) {
				Morph cm = (Morph) morphVec.get(i);
				morphs[numMorphs++] = cm;
				int numforms = ((Set) morphSets.get(i)).size();
				cm.forms = new String[numforms];
				// now for through all ur forms for morpheme and set to equal probs
				cm.dist = new double[numforms];
				cm.newdist = new double[numforms];
				for (int j = 0; j < numforms; j++) {
					cm.dist[j] = ((double) 1.0) / ((double) numforms);
					cm.newdist[j] = ((double) 1.0) / ((double) numforms);
				}
				cm.forms = (String[]) ((Set) morphSets.get(i)).toArray(cm.forms);
			}
		}

		for (int i = 0; i < morphs.length; i++) {
			int numOut = 0;
			for (int j = 0; j < outputs.length; j++) {
				for (int k = 0; k < outputs[j].morphs.length; k++) {
					if (outputs[j].morphs[k] == morphs[i]) numOut++;
				}
			}
			morphs[i].outputs = new Output[numOut];
			numOut = 0;
			for (int j = 0; j < outputs.length; j++) {
				for (int k = 0; k < outputs[j].morphs.length; k++) {
					if (outputs[j].morphs[k] == morphs[i]) {
						morphs[i].outputs[numOut++] = outputs[j];
					}
				}
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
		if (phono) {
			return this.outputs[o_index].dist[i_index];
		} else {
			//no longer in phonotactic learning
			return (double) 0.0;
		}
	}

	public String toString() {
		String toret = "";
		if (phono) {
			for (int o = 0; o < outputs.length; o++) {
				toret += outputs[o].form + " distribution:\n";
				for (int i = 0; i < outputs[o].inputs.length; i++) {
					toret += "\t" + outputs[o].inputs[i] + " - " + outputs[o].dist[i] + "\n";
				}
			}
		} else {
			for (int m = 0; m < morphs.length; m++) {
				toret += "Morpheme " + morphs[m].name + " Dist:\n";
				for (int u = 0; u < morphs[m].forms.length; u++) {
					toret += "\t" + morphs[m].forms[u] + " - " + morphs[m].dist[u] + "\n";
				}
			}
		}
		return toret;
	}

	public class Morph {
		public int name;
		public double[] dist;
		public double[] newdist;
		public String[] forms;
		public Output[] outputs;
	}

	public class Output {
		public int freq;
		public double relfreq = 0.0;
		public String form;
		public double[] dist;
		public Morph[] morphs;
		public String[] inputs;
	}

	public Output[] outputs;
	public Morph[] morphs;
	public boolean phono;
}
