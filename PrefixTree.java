
public class PrefixTree {
    Node root;

    class Branch implements Node{
        Node[] succs;
        Branch(int s){
            succs = new Node[s];
        }
        public String find(int[] k, int i){
            Node next = succs[k[i]];
            System.out.println("At node: "+k[i]);
            if(next==null){
                return null;
            }else{
                return next.find(k,i+1);
            }
        }
        public void put(int[] pre, int i, String v){
            if(i==pre.length-1){
                succs[pre[i]]=new Value(v);
            }else{
                if(succs[pre[i]]==null){
                    succs[pre[i]] = new Branch(succs.length);
                }
                succs[pre[i]].put(pre,i+1,v);
            }
        }
    }

    class Value implements Node{
        String value;
        Value(String v){
            value = v;
        }
        public String find(int[] k, int i){
            return value;
        }
        public void put(int[] pre, int i, String v){
            throw new RuntimeException("Cannot add branch to value!");
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
}