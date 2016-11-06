import java.util.*;

public class Ranking{
    Ranking(int[] cons){
        ranking = cons;
    }
    public int[] ranking;

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Ranking ranking1 = (Ranking) object;

        if (!java.util.Arrays.equals(ranking, ranking1.ranking)) return false;

        return true;
    }

    public int hashCode() {
        int result = Arrays.hashCode(ranking);
        return result;
    }
}