import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Arrays;

public class BuildTab { 
	//These keep track of whether GEN and CON do fancy stuff (will update automatically based on input files)
	public static boolean isInsertion = false;
	public static boolean isSMR = false;
	
	public static String join_array(String[] myArray, String connector){
		String output = "";
		for (int i = 0; i < myArray.length; i++) {
			if (i != myArray.length - 1){
				output = output + myArray[i] + connector;
			}
			else{
				output = output + myArray[i];
			}
		}
		return output;
	}
	
	public static Map<String, Map<String, String>> get_GEN (String file_name){
		//Initialize output dictionary to hold gen's mappings:
		Map<String, Map<String, String>> gen_mappings = new HashMap<String, Map<String, String>>();
		
		//Setting up buffered readers:
		BufferedReader gf_reader = null;
		BufferedReader cf_reader = null;
		try {
			//GEN
			File gf = new File(file_name);
			gf_reader = new BufferedReader(new FileReader(gf));
		} 
		catch (IOException e) {} 
		
		//Going line-by-line through the input file:
		try{
			//Read in the GEN functions:
			String g_line;
			String g_function;
			String gen_label = "";
			Map<String, String> this_mapping = new HashMap<String, String>();
			while ((g_line = gf_reader.readLine()) != null){
				String gen_columns[] = g_line.split("\t");
				if (gen_columns[0].equals("function:")){
					gen_label = gen_columns[1];
					this_mapping = new HashMap<String, String>();
				}
				else {
					String input_list[] = gen_columns[0].split(",");
					String output_list[] = gen_columns[1].split(",");
					for (int i=0; i < output_list.length; i++){
						this_mapping.put(input_list[i], output_list[i]);
						if (input_list[i].equals("_")){
							isInsertion = true;
						}
					}
					gen_mappings.put(gen_label, this_mapping);
				}
			}
		}
		catch (IOException e) {} 
		

		return gen_mappings;
	}
	
	public static Map<String, Constraint> get_CON (String file_name){
		//Initialize output dictionary to hold con's mappings:
		Map<String, Constraint> con_mappings = new HashMap<String, Constraint>();
		
		//Setting up buffered readers:
		BufferedReader cf_reader = null;
		try {
				//CON
				File cf = new File(file_name);
				cf_reader = new BufferedReader(new FileReader(cf));
			} 
		catch (IOException e) {} 
		
		//Going line-by-line through the input files:
		try{
			//Read in the constraints:
			String c_line;
			String con_family = "";	
			while ((c_line = cf_reader.readLine()) != null) {
				String con_columns[] = c_line.split("\t");			
				if (con_columns[0].equals("type:")){
					con_family = con_columns[1];
					if (con_family.equals("serialMarkedness")){
						//Keeps track of whether we have to worry about MSeqs
						isSMR = true; 
					}
				}
				else{
					Constraint c = new Constraint(
						con_columns[0],
						con_family,
						c_line.replace(con_columns[0]+"\t", "").split("\t")
					);
					con_mappings.put(c.label, c);
				}
			}
		}
		catch (IOException e) {} 

		return con_mappings;
	}

	public static Tableau get_tab (String UR, String gen_file, String con_file, boolean verbose){ 
	//This function takes a UR and GEN/CON file names, then outputs
	//a Tableau object that's roughly equivalent to what GrammarFile.java
	//creates from grammar files.
		
		//Get our GEN and CON:
		Map<String, Map<String, String>> GEN = get_GEN(gen_file); //Dictionary of mappings from possible UR segs to SR segs
		Map<String, Constraint> CON = get_CON(con_file); //Dictionary of constraint names to Constraint objects 
		Set<String> GEN_functions = GEN.keySet();
		Set<String> CON_set = CON.keySet();
		String[] CON_names = CON_set.toArray(new String[CON_set.size()]);
		
		//Separate the phonological input from any MSeqs that the input may contain:
		String current_mSeq = "";
		if (isSMR){
			if (UR.contains("<")){
				String[] inputElements = UR.split("<");
				UR = inputElements[0];
				current_mSeq = "<"+inputElements[1];
			}
		}
		
		//Break input up into an array:
		String input_array[] = UR.split("");
		
		//Find the violation profile for the faithful candidate
		int candViols[] = new int[CON_names.length]; //Array that holds each candidate's violation profile
		String inputViols[] = new String[CON_names.length]; //Faithful candidate violation profile (+con types)
		for (int con_index = 0; con_index<CON_names.length;con_index++){
			candViols[con_index] = CON.get(CON_names[con_index]).get_viols(UR, UR);
			inputViols[con_index] = String.valueOf(candViols[con_index]);
			//The next bit it important for SMR constraints:
			if (CON.get(CON_names[con_index]).family.equals("markedness")){
				inputViols[con_index] = inputViols[con_index] + ",M";
			}
			else {
				inputViols[con_index] = inputViols[con_index] + ",-";
			}
		}
		
		//Fill in violProfiles for the non-faithful candidates:
		ArrayList<String> candidates = new ArrayList<String>(); //List of all the candidates
		candidates.add(UR); //Add the faithful candidate into the list
		ArrayList<int[]> violProfiles = new ArrayList<int[]>(); //All the violation profiles
		violProfiles.add(candViols.clone()); //Add the faithful candidate into the list
		for (String function : GEN_functions){
			//Step through each function that GEN has, applying it to each relevant segment
			//in the input (only one change per candidate--a la HS).
			Set<String> changable_segs = GEN.get(function).keySet();//All segments GEN can change
			for (String changable_seg : changable_segs){
				for (int input_i=0; input_i < input_array.length; input_i++){
					if (input_array[input_i].equals(changable_seg)){
						//If this segment in the input is changable by GEN, do that.
						String[] newCandidate_array = new String [input_array.length];
						for (int input_j=0; input_j < input_array.length; input_j++){
							if (input_j == input_i){
								newCandidate_array[input_i] = GEN.get(function).get(input_array[input_i]);
							}
							else{
								newCandidate_array[input_j] = input_array[input_j];
							}
						}
						String newCandidate = join_array(newCandidate_array, ""); //Convert cand to string

						//Add MSeqs to the candidate if we need them for this CON:
						if (isSMR){
							String new_mSeq = "";
							for (int con_index = 0; con_index < inputViols.length; con_index++){
								String[] this_viol = inputViols[con_index].split(",");
								if (this_viol[1].equals("M") && this_viol[0].equals("1")){
									//If the input violated this markedness constraint...
									if (CON.get(CON_names[con_index]).get_viols(UR, newCandidate) == 0){
										//...and this candidate doesn't...
										new_mSeq = new_mSeq + CON_names[con_index] + "&";//I use "&" instead of "+"
										//...Add this constraint to the MSeq.
									}
								}
							}
							new_mSeq = new_mSeq + ">";
							new_mSeq = new_mSeq.replaceAll("&>",">");
							if (current_mSeq == ""){
								new_mSeq = "<" + new_mSeq;
							}
							else{
								new_mSeq = current_mSeq.replaceAll(">",","+new_mSeq);
							}
							newCandidate = newCandidate + new_mSeq;
						}
						
						candidates.add(newCandidate); //Add to candidate list
						
						//Calculate candidate's constraint violations:
						for (int con_index = 0; con_index<CON_names.length;con_index++){
							int viol = CON.get(CON_names[con_index]).get_viols(UR, newCandidate);
							candViols[con_index] = viol;
						}
						violProfiles.add(candViols.clone()); //Add to viol profile list
					}
				}
			}
		}

		//This class can also print out pretty tableaux to the console if you want it to:
		if (verbose) {
			print_tab(UR, current_mSeq, CON_names, candidates, violProfiles);
		}
		
		//Convert everything to a format that EDL.java can use:
		Candidate[] output_cands = new Candidate[candidates.size()];
		for (int cand_i = 0; cand_i < candidates.size(); cand_i++){
			Candidate this_cand = new Candidate();
			this_cand.form = candidates.get(cand_i).replace("_","");
			this_cand.violations = violProfiles.get(cand_i);
			output_cands[cand_i] = this_cand;
		}
		Tableau output = new Tableau();
		output.uf = UR;
		output.cands = output_cands;
		
		return output;
	}
	
	public static void print_tab (String INPUT, String MSEQ, String[] CONSTS, ArrayList<String> CANDS, ArrayList<int[]> VIOLS){
		//Just print a tableau:
		System.out.print("/"+INPUT+MSEQ+"/\t"); //Input
		System.out.println(join_array(CONSTS, "\t"));//Constraints
		System.out.println("--------------------------------------------------------");
		for (int cand_i=0; cand_i < CANDS.size();cand_i++){ 
			System.out.print(CANDS.get(cand_i).replace("_","")+"\t");//Candidate
			for (int con_j=0; con_j < CONSTS.length; con_j++){
				System.out.print(VIOLS.get(cand_i)[con_j]);//Violation for each constraint
				System.out.print("\t");
			}
			System.out.print("\n");
		} 
		System.out.print("\n\n\n");
	}
	
   public static class Candidate {
		Candidate() {}

		public int[] violations; //Violation profile
		public String form; //SR
   }
	
	public static class Tableau {
		public String uf; //Underlying form (string)
		public Candidate[] cands; //Candidate object (see class above)
    }
	
}

