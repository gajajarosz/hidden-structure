package learner;
//Support code for the GLA learner
import java.text.NumberFormat;
import java.util.Random;

public class STOT {

	public double[] grammar;   //contains mean ranking values for each constraint
	public String[] constraints;
	public int[] faith;

	public STOT(GrammarFile gf) {
		this.uni_grammar(gf.constraints.length);
		this.constraints = gf.constraints;
		this.faith = gf.faith;
	}

	public void uni_grammar(int size) {
		grammar = new double[size];

		for (int i = 0; i < size; i++) {
			grammar[i] = 10.0;
		}
	}

	public String gramToString(double[] grammar) {
		NumberFormat formatter = NumberFormat.getNumberInstance();
		formatter.setMaximumFractionDigits(2);
		formatter.setMinimumFractionDigits(2);
		formatter.setMinimumIntegerDigits(1);
		formatter.setMaximumIntegerDigits(4);

		String to_return = "";

		for (int i = 0; i < grammar.length; i++) {
			to_return += this.constraints[i] + " : " + formatter.format((double) grammar[i]) + "  ";
		}

		return to_return;
	}

	public void bias_grammar() {
		int size = grammar.length;
		for (int i = 0; i < size; i++) {
			if (this.faith[i] == 1) {
				grammar[i] -= 5.0;
			}
		}
	}

	public double[] sample(boolean NegOK, double noise) {
		Random gaussian = new Random();
		int size = grammar.length;
		double[] single = new double[size];

		for (int i = 0; i < grammar.length; i++) {
			single[i] = gaussian.nextGaussian() * noise + grammar[i];
			if (!(NegOK)) {
				if (single[i] < 0) {
					single[i] = 0;
				}
			}
		}
		return single;
	}

	public int[] find_order(double[] single) {

		int size = single.length;
		int[] rank = new int[size];
		int[] used = new int[size];
		for (int i = 0; i < size; i++) {
			used[i] = 0;
		}

		double max = -1.0;
		int max_c = -1;
		for (int c = 0; c < size; c++) { //goes through the constraints
			for (int i = 0; i < size; i++) { // goes through the rows
				if ((used[i] == 0) && ((max_c < 0) || (single[i] > max))) {
					max = single[i];
					max_c = i;
				}
			}
			rank[c] = max_c;
			used[max_c] = 1;
			max = -1.0;
			max_c = -1;
		}
		return rank;
	}
}
