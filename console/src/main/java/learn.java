package learner;
//This is the console user interface to the EDL and GLA learners

import java.util.Arrays;

public class learn {

    public static void main(String[] args) {
        if (args.length < 8) {
            System.out.println("Too few arguments have been specified to run the program. Exiting...\nusage: run grammar_file dist_file iterations fin_sample learner_type grammar_sample_size init_bias (print args) (maxdepth)");
            System.exit(-1);
        }
        String[] argslist = Arrays.copyOfRange(args,1,args.length);
        System.out.println(args[0]);
        if (args[0].equals("EDL")) {
            EDL.main(argslist);
        }else if (args[0].equals("GLA")){
            GLA.main(argslist);
        }else {
            System.out.println("usage: run grammar_file dist_file iterations fin_sample learner_type grammar_sample_size init_bias (print args) (maxdepth)");
            System.exit(-1);
        }
    }

}
