import java.util.Arrays;

public class learn {

    public static void main(String[] args) {
        if (args.length < 7) {
            System.out.println("usage: java main learner grammar_file dist_file verbose grammar_sample_size iterations init_bias learner_type");
            System.exit(-1);
        }
        String[] argslist = Arrays.copyOfRange(args,1,args.length);
        System.out.println(args[0]);
        if (args[0].equals("POEM")) {
            POEM.main(argslist);
        }else if (args[0].equals("STOTEM")){
            STOTEM.main(argslist);
        }else {
            System.out.println("usage: java main learner grammar_file dist_file verbose grammar_sample_size iterations init_bias learner_type");
            System.exit(-1);
        }
    }

}