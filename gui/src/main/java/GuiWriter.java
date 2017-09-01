package learner;
//This is support code for the GUI; it implements a Writer to display output from the learner
import javafx.application.Platform;
import javafx.scene.text.*;
import java.util.*;
import javafx.scene.control.TextArea;

public class GuiWriter implements Writer {
        private TextArea text;
        final ArrayList<String> alist = new ArrayList<>();            

        public GuiWriter(TextArea text) { this.text = text; }
        
        public void println(Object line) {
            alist.add(line.toString()+"\n");
            Platform.runLater(new Thread() {
                public void run() {
                    text.setText(alist+"\n");
                }
            });
        }
    }
