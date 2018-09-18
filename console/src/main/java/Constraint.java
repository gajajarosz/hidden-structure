package learner;
import java.io.*;
import java.util.regex.*;

public class Constraint {
	String label;
	String family;
	String marked_regex;
	String[] faith_input;
	String[] faith_output;
	String mSeq;
	
	public Constraint(String this_label, String this_family, String[] loci){
		this.label = this_label; //Custom name--doesn't matter really
		this.family = this_family; //Right now this can only handle markedness, faithfulness, and SMR
		
		if (this.family.equals("markedness")){
			this.marked_regex = loci[0];
		}
		if (this.family.equals("faithfulness")){
			this.faith_input = loci[0].split(",");
			this.faith_output = loci[1].split(",");			
		}
		if (this.family.equals("serialMarkedness")){
			this.mSeq = loci[0];
		}

	}
	
	
	public int get_viols (String UR, String SR){
		int viol_count = 0;
		
		//Markedness constraints:
		if (this.family.equals("markedness")){
			SR = SR.replace("_", ""); //Get rid of empty symbols
			SR = SR.split("<")[0]; //Get rid of mSeqs
			SR = "_"+SR+"_"; //Add symbols to beginning and end
			Pattern my_regex = Pattern.compile(this.marked_regex);
			Matcher viol_detect = my_regex.matcher(SR);
			while (viol_detect.find()){
				viol_count++; //Count viols
			}
		}
		//Faithfulness constraints:
		else if (this.family.equals("faithfulness")){
			SR = SR.split("<")[0]; //Get rid of mSeqs
			UR = UR.split("<")[0]; //Get rid of mSeqs
			String UR_array[] = UR.split("");
			String SR_array[] = SR.split("");
			for (int faith_index = 0; faith_index < this.faith_input.length; faith_index++){
				for (int word_index = 0; word_index < UR_array.length; word_index++){
					if (UR_array[word_index].equals(this.faith_input[faith_index])){
						//If the segment in the UR belongs to the class relevant to this
						//Faith constraint...
						if (SR_array[word_index].equals(this.faith_output[faith_index])){
							//And if the segment in the SR is also relevant...
							viol_count ++;
						}
					}
				}
			}
		}
		//Serial Markedness Constraints
		else if (this.family.equals("serialMarkedness")){
			if (! SR.contains("<")){
				//If we're at the very beginning of the derivation, it's vacuous.
				viol_count = 0;
			}
			else {
				String relevantCons[] = this.mSeq.split(",");
				String SR_mSeq = SR.split("<")[1];
				String vacuousTracker = "";
				for (int con_i = 0; con_i < relevantCons.length; con_i++){
					if (SR_mSeq.contains(relevantCons[con_i])){
						//If a relevant constraint is present in the 
						//candidate's MSeq...
						vacuousTracker = vacuousTracker + "0";
					}
					else {
						//If a relevant constraint is missing from 
						//the candidate's MSeq...
						vacuousTracker = vacuousTracker + "1";
					}
				}
				if (vacuousTracker.contains("1")){
					//If all of the constraints that the SMR
					//constraint refers to aren't present in the 
					//candidate's MSeq, it's vacuous.
					viol_count = 0;
				}
				else{
					//If the SM constraint isn't vacuous, check
					//the ordering in the candidate's MSeq:
					String mSeqRegex = "\\Q"+this.mSeq.replace(",","\\E.*,.*\\Q")+"\\E";
					if (SR.matches(mSeqRegex)){
						viol_count = 0; //Correct ordering
					}
					else {
						viol_count = 1; //Incorrect ordering
					}
				}
			}
		}
		
		return viol_count;
	}
}