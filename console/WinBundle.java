import java.util.*;

public class WinBundle {
    WinBundle(int f, int l) {
        start = f;
        stop = l;
        ht = new HashMap<Ranking, String>();
    }
    public int start;
    public int stop;
    public HashMap<Ranking, String> ht;
}