import java.util.*;
import java.util.stream.*;

interface Node{
    String find(int[] k, int i);
    void put(int[] pre, int i, String v);
    Stream<String> prettyPrint();
}