import java.util.*;

public class WinBundle {
    WinBundle(int f, int l) {
        start = f;
        stop = l;
        ht = new HashMap<List<int[]>, String>();
    }
    public int start;
    public int stop;
    public HashMap<List<int[]>, String> ht;
}