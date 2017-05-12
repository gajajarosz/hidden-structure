package learner;
//This is support code for the GUI; it implements a Writer to display output from the learner
import javafx.application.Platform;
import javafx.scene.text.*;
import java.util.*;

public class GuiWriter implements Writer {
        private Text text;
        final ArrayList<String> alist = new ArrayList<>();            

        public GuiWriter(Text text) { this.text = text; }
        
        public void println(Object line) {
            alist.add(line.toString());
            Platform.runLater(new Thread() {
                public void run() {
                    text.setText(String.join("\n", alist));
                }
            });
        }
    }
