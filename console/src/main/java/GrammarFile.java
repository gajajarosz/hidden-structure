package learner;
//Support code for parsing the grammar file for both the EDL and GLA learner
import java.io.*;
import java.util.regex.*;
import java.util.*;

public class GrammarFile {
   private Writer writer;

    GrammarFile(String fn, Writer writer) {
	// Load the data from a file
	BufferedReader stream;
	boolean ignored = false;
	this.writer = writer;
	try {
	    stream = new BufferedReader(new FileReader(fn));
	} catch (IOException ioe) {
	    writer.println(ioe + "\nUnable to find the grammar file with the name " + fn + ". Exiting...");
	    System.exit(-1);
	    return;
	}

	try {
	    String line;

	    Pattern numcon = Pattern.compile("^([0-9]+)\\s+constraints.*$");
	    Pattern con = Pattern.compile("^constraint\\s+\\[([0-9]+)\\]\\:\\s+\\\"(.+)\\\"\\s+([0-9]+).*$");
	    Pattern numtab = Pattern.compile("^([0-9]+)\\s+tableaus.*$");
	    Pattern inline = Pattern.compile("input\\s+\\[([0-9]+)\\]:\\s+\"(.*)\"\\s+([0-9]+).*$");
	    Pattern candline = Pattern.compile("\\s+candidate\\s+\\[([0-9]+)\\]:\\s+\"(.*)\"\\s+([0-9\\s]+).*$");
	    Pattern struct = Pattern.compile("^structure symbols:\\s+(.*)$");

	    int curTab = 0;
	    while ((line = stream.readLine()) != null) {
		Matcher m1 = numcon.matcher(line);
		Matcher m2 = con.matcher(line);
		Matcher m3 = numtab.matcher(line);
		Matcher m4 = inline.matcher(line);
		Matcher m5 = candline.matcher(line);
		Matcher m6 = struct.matcher(line);
		if (m1.matches()) {
		    int c = Integer.valueOf(m1.group(1));
		    constraints = new String[c];
		    faith = new int[c];
		    //System.out.println("" + c + " constraints.");
		} else if (m2.matches()) {
		    int c = Integer.valueOf(m2.group(1)) - 1;
		    constraints[c] = m2.group(2);
		    faith[c] = Integer.valueOf(m2.group(3));
		    //System.out.println("Constraint " + c + " is " + constraints[c]);
		} else if (m3.matches()) {
		    int c = Integer.valueOf(m3.group(1));
		    tableaux = new Tableau[c];
		    //System.out.println("" + c + " tableaux");
		    if (structors == null) {
			structors = new String[0];
		    }
		} else if (m4.matches()) {
		    curTab = Integer.valueOf(m4.group(1)) - 1;
		    //System.out.println("Creating Tableau " + curTab + " " + m4.group(2)
		    //	       + " " + m4.group(3));
		    tableaux[curTab] = new Tableau();
		    this.tableaux[curTab].uf = m4.group(2);
		    int nc = Integer.valueOf(m4.group(3));
		    tableaux[curTab].cands = new Candidate[nc];
		    int l = this.constraints.length;
		    tableaux[curTab].constraints = new int[l];
		    for(int i=0;i<l;i++){
			tableaux[curTab].constraints[i] = -1;
		    }
		} else if (m5.matches()) {
		    int ci = Integer.valueOf(m5.group(1)) - 1;
		    tableaux[curTab].cands[ci] = new Candidate(m5.group(2));
		    String vio = m5.group(3);
		    String[] vios = vio.split("\\s+");
		    tableaux[curTab].cands[ci].violations = new int[vios.length];
		    int[] checkc = tableaux[curTab].constraints;
		    //System.out.println(Arrays.toString(vios));
		    for(int l=0;l<checkc.length;l++){
			int v = Integer.parseInt(vios[l]);
			if(checkc[l]==-1){
			    tableaux[curTab].constraints[l]=v;
			}else{
			    if(checkc[l]!=v){
				tableaux[curTab].constraints[l]=-2;
			    }
			}
		    }
		    //System.out.println(Arrays.toString(tableaux[curTab].constraints));
		    for (int i = 0; i < vios.length; i++) {
			tableaux[curTab].cands[ci].violations[i] = Integer.valueOf(vios[i]);
		    }
		    //System.out.println("Creating Candidate " + ci + " for " + curTab + ": " + tableaux[curTab].cands[ci].form + " " + tableaux[curTab].cands[ci].oform + " " + vio);
		} else if (m6.matches()) {
		    structors = m6.group(1).split("\\s+");
		} else {
		    if (ignored) {
			writer.println(">>>" + line);
		    } else {
			writer.println("The following lines from the grammar file do not match the specified format and will be ignored: \n>>>" + line);			
		    }
		    ignored = true;
		}
	    }

	} catch (IOException ioe) {
	    writer.println(ioe + "\nError reading the grammar file: " + fn + ". Exiting...");
	    System.exit(-1);
	}
    }

    public class Candidate {
	Candidate(String form) {
	    this.form = form;
	    this.oform = form;
	    if (structors != null) {
		for (int i = 0; i < structors.length; i++) {
		    oform = oform.replace(structors[i], "");
		}
	    }
	}

	public int[] violations;
	public String form;
	public String oform;
    }

    public static class Tableau {
	public String uf;
	public Candidate[] cands;
	public int[] constraints;
    }

    public String[] constraints;
    public int[] faith;
    public Tableau[] tableaux;
    public String[] structors;
}
