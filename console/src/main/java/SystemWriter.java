package learner;
//Support code for i/o: necessary for GUI compatibility
public class SystemWriter implements Writer {
    public void println(Object line) {
	System.out.println(line);
    }
    public String getText() { throw new RuntimeException("getText only implemented for GUI"); }
    public void clear() {throw new RuntimeException("clear only implemented for GUI"); }
}
    
