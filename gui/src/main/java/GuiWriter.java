package learner;
//This is support code for the GUI; it implements a Writer to display output from the learner
import javafx.application.Platform;
import javafx.scene.text.*;
import java.util.*;
import javafx.scene.control.TextArea;

public class GuiWriter implements Writer {
        private TextArea text;

        public GuiWriter(TextArea text) {
            this.text = text;
        }
        
        public void println(Object line) {
            Platform.runLater(new Runnable() {
                public void run() {
                    text.appendText(line.toString()+"\n");
                }
            });
        }
    }
