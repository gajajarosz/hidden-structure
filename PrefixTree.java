
public class PrefixTree {
    Node root;

    class Branch implements Node{
        Node[] succs;
        Branch(int s){
            succs = new Node[s];
        }
        String find(int[] k, int i){
            next = succs[k[i]];
            if(next==null|i==k.length){
                return null;
            }else{
                next.find(k,i+1);
            }
        }
        void put(int[] pre, int i, String v){
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
        String find(int[] k, int i){
            return value;
        }
        void put(int[] pre, int i, String v){
            throw new Exception("Cannot add branch to value!");
        }
    }

    PrefixTree(int size){
        root = new Branch(size);
    }
    String find(int[] k){
        return root.find(k,0);
    }
    void put(int[] pre, String v){
        return root.put(pre, 0, v);
    }
}