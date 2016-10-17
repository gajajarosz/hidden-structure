import java.util.Arrays;

public class learn {

    public static void main(String[] args) {
        if (args.length < 7) {
            System.out.println("usage: java main learner grammar_file dist_file iterations verbose learner_type grammar_sample_size init_bias");
            System.exit(-1);
        }
        String[] argslist = Arrays.copyOfRange(args,1,args.length);
        System.out.println(args[0]);
        if (args[0].equals("EDL")) {
            EDL.main(argslist);
        }else if (args[0].equals("GLA")){
            GLA.main(argslist);
        }else {
            System.out.println("usage: java main learner grammar_file dist_file iterations verbose learner_type grammar_sample_size init_bias");
            System.exit(-1);
        }
    }

}