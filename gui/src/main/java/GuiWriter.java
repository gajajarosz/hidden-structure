package learner;
//This is support code for the GUI; it implements a Writer to display output from the learner
import javafx.application.Platform;
import javafx.scene.text.*;
import java.util.*;
import javafx.scene.control.TextArea;

public class GuiWriter implements Writer {
    private TextArea text;
    public StringBuilder builder;
    String displayText;

    public GuiWriter(TextArea text) {
	this.builder = new StringBuilder();
	this.text = text;
	this.displayText = "";
    }

    public String getText() {
	return builder.toString();
    }

    public void clear() {
	displayText = "";
    }

    public void println(Object line) {
	Platform.runLater(new Runnable() {
                public void run() {
                    builder.append(line.toString() + "\n");

                    displayText = displayText + line.toString() + "\n";
                    //If the text window in the GUI has more than 5000 characters displayed,
                    //crop output to avoid slowing the program
                    if (displayText.length() > 5000) {
                        displayText = displayText.substring(displayText.length() - 5000);
                    }
                    text.setText(displayText);
                    if (!text.isFocused()) {
                        text.setScrollTop(Double.MAX_VALUE);
                    }
                    //text.setText(line.toString());
		    //                     text.(line.toString()+"\n");
                }
            });
    }
}
