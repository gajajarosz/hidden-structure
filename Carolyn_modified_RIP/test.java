public class Test {

	public static DistFile df;

	public static void main(String[] args) {
		df = new DistFile(args[0]);
		System.out.println(df);
	}
}