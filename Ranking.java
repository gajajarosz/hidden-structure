import java.util.*;

public class Ranking{
    Ranking(int[] cons){
        ranking = cons;
    }
    public int[] ranking;

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        //if (!super.equals(object)) return false;

        Ranking ranking1 = (Ranking) object;

        if (!java.util.Arrays.equals(ranking, ranking1.ranking)) return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(ranking);
        return result;
    }
}