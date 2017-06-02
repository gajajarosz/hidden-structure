package learner;
//Support code for the EDL learner
import java.util.*;
import java.util.stream.*;

public class PrefixTree {
    Node root;

    class Branch implements Node{
        Node[] succs;
        Branch(int s){
            succs = new Node[s];
        }
        public String find(int[] k, int i){
            Node next = succs[k[i]];
            //System.out.println("At node: "+k[i]);
            if(next!=null){
                return next.find(k,i+1);
            }else{
                return null;
            }
        }
        public void put(int[] pre, int i, String v){
            if(i==pre.length-1){
                //System.out.println("Got to pre.length-1: "+pre[i]);
                succs[pre[i]]=new Value(v);
            }else{
                if(succs[pre[i]]==null){
                    //System.out.println("adding new branch at: "+pre[i]);
                    succs[pre[i]] = new Branch(succs.length);
                }else{
                    //System.out.println("branch already existed: "+pre[i]);
                }
                succs[pre[i]].put(pre,i+1,v);

            }
        }

        public String toString(){
            String result = "<div>";
            for(int i=0; i < succs.length;i++){
                if(succs[i]!=null){
                    result+=i+"->"+succs[i].toString()+"<br>";
                }
            }
            result+="</div>";
            return result;
        }
        public Stream<String> prettyPrint(){
            return IntStream.range(0,succs.length).boxed().filter(i->succs[i]!=null).flatMap(i->succs[i].prettyPrint().map(s->i+","+s));
        }
    }

    class Value implements Node{
        String value;
        Value(String v){
            value = v;
        }
        public String find(int[] k, int i){
            //System.out.println("Found at :"+k[i]);
            return value;
        }
        public void put(int[] pre, int i, String v){
            throw new RuntimeException("Cannot add branch to value!");
        }
        public String toString(){
            return value;
        }
        public Stream<String> prettyPrint(){
            return java.util.stream.Stream.of(value);
        }
    }

    PrefixTree(int size){
        root = new Branch(size);
    }
    public String find(int[] k){
        return root.find(k,0);
    }
    public void put(int[] pre, String v){
        root.put(pre, 0, v);
    }
    public String toString(){
        String[] result = root.prettyPrint().toArray(String[]::new);
        return String.join("\n",result);
    }
}
