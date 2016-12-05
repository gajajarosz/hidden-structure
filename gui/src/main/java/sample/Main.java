package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import java.io.File;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.awt.Desktop;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.*;
import javafx.beans.value.*;
import javafx.scene.text.*;
import java.util.Arrays;
import java.io.PrintStream;
import learner.*;

public class Main extends Application {

    final ScrollBar sc = new ScrollBar();

    @Override
    public void start(Stage primaryStage) {

        Desktop desktop = Desktop.getDesktop();
        primaryStage.setTitle("HS Learner");
        //Insert explanatory text here

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(20);
        grid.setPadding(new Insets(20, 20, 20, 20));

        Text scenetitle = new Text("Welcome to Gaja's Hidden Structure Learners");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        Label gramlabel = new Label("Grammar file:");
        TextField gr = new TextField ();
        gr.setPrefColumnCount(8);
        HBox grammar = new HBox();
        grammar.getChildren().addAll(gramlabel,gr);
        grammar.setSpacing(5);
        grid.add(grammar, 0,1);

        final FileChooser grChooser = new FileChooser();
        final Button gramButton = new Button("Upload file");
        grid.add(gramButton,1,1);
        TextField grPath = new TextField();

        gramButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File gramfile = grChooser.showOpenDialog(primaryStage);
                        if (gramfile != null) {
                            //openFile(file);
                            gr.setText(gramfile.getName());
                            grPath.setText(gramfile.getAbsolutePath());
                        }
                    }
                });

        Label distlabel = new Label("Distribution File:");
        TextField dist = new TextField ();
        dist.setPrefColumnCount(8);
        HBox distfile = new HBox();
        distfile.getChildren().addAll(distlabel,dist);
        distfile.setSpacing(5);
        grid.add(distfile, 0,2);
        TextField distPath = new TextField();

        final FileChooser distChooser = new FileChooser();
        final Button distButton = new Button("Upload file");
        grid.add(distButton,1,2);

        distButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File distfile = distChooser.showOpenDialog(primaryStage);
                        if (distfile != null) {
                            //openFile(file);
                            dist.setText(distfile.getName());
                            distPath.setText(distfile.getAbsolutePath());
                        }
                    }
                });

        Label itlabel = new Label("Iterations:");
        TextField it = new TextField ("1000");
        it.setPrefColumnCount(4);
        HBox iterations = new HBox();
        iterations.getChildren().addAll(itlabel,it);
        iterations.setSpacing(5);
        grid.add(iterations, 0,3);

        ComboBox learner = new ComboBox(FXCollections.observableArrayList("EDL","GLA"));
        learner.setPromptText("Learner");
        grid.add(learner,0,4);

        TitledPane edlo = new TitledPane();
        GridPane edlogrid = new GridPane();
        edlogrid.setVgap(4);
        edlogrid.setPadding(new Insets(5, 5, 5, 5));
        ComboBox emodel = new ComboBox(FXCollections.observableArrayList("Online","Batch"));
        emodel.setPromptText("Learner");
        TextField ss = new TextField("1000");
        edlogrid.add(emodel,0,0);
        edlogrid.add(new Label("Sample Size: "), 0, 1);
        edlogrid.add(ss, 1, 1);
        edlo.setText("EDL Options");
        edlo.setContent(edlogrid);
        edlo.setExpanded(false);
        edlo.setVisible(false);
        grid.add(edlo,0,5);

        TitledPane glao = new TitledPane();
        GridPane glaogrid = new GridPane();
        glaogrid.setVgap(4);
        glaogrid.setPadding(new Insets(5, 5, 5, 5));
        ComboBox gmodel = new ComboBox(FXCollections.observableArrayList("RIP","RRIP","EIP","randRIP"));
        gmodel.setPromptText("Learner");
        glaogrid.add(gmodel,0,0);
        ComboBox gramtype = new ComboBox(FXCollections.observableArrayList("OT","HG","MaxEnt"));
        gramtype.setPromptText("Grammar Type");
        TextField lr = new TextField("0.1");
        TextField n = new TextField("2");
        glaogrid.add(gramtype,0,1);
        glaogrid.add(new Label("Learning Rate: "),0,2);
        glaogrid.add(lr,1,2);
        glaogrid.add(new Label("Noise: "),0,3);
        glaogrid.add(n,1,3);
        glao.setText("GLA Options");
        glao.setContent(glaogrid);
        glao.setExpanded(false);
        glao.setVisible(false);
        grid.add(glao,0,5);

        learner.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue ov, Number value, Number new_value) {
                        if(new_value.intValue()==0){
                            glao.setVisible(false);
                            glao.setExpanded(false);
                            edlo.setExpanded(true);
                            edlo.setVisible(true);
                        }else{
                            edlo.setVisible(false);
                            edlo.setExpanded(false);
                            glao.setExpanded(true);
                            glao.setVisible(true);
                        }
                    }
                });

        TitledPane ao = new TitledPane();
        GridPane aogrid = new GridPane();
        aogrid.setVgap(4);
        aogrid.setPadding(new Insets(5, 5, 5, 5));
        CheckBox nok = new CheckBox();
        CheckBox iBias = new CheckBox();
        TextField finEvalSample = new TextField("1000");
        aogrid.add(new Label("NegOK?"), 0, 0);
        aogrid.add(nok, 1, 0);
        aogrid.add(new Label("Initial Bias"), 0, 1);
        aogrid.add(iBias, 1, 1);
        aogrid.add(new Label("Final Evaluation Sampling: "),0,2);
        aogrid.add(finEvalSample,1,2);
        ao.setText("Advanced Options");
        ao.setContent(aogrid);
        ao.setExpanded(false);
        grid.add(ao,0,6);

        TitledPane eo = new TitledPane();
        GridPane eogrid = new GridPane();
        eogrid.setVgap(4);
        eogrid.setPadding(new Insets(5, 5, 5, 5));
        TextField quitFreq = new TextField("100");
        TextField quitSample = new TextField("100");
        TextField maxDepth = new TextField();
        eogrid.add(new Label("Check to Quit Early @ Iteration: "), 0, 0);
        eogrid.add(quitFreq, 1, 0);
        eogrid.add(new Label("Sampling for Quit Early: "), 0, 1);
        eogrid.add(quitSample, 1, 1);
        eogrid.add(new Label("MaxDepth of Ranking Tree: "), 0, 2);
        eogrid.add(maxDepth, 1, 2);
        eo.setText("Efficiency Options");
        eo.setContent(eogrid);
        eo.setExpanded(false);
        grid.add(eo,0,7);

        TitledPane po = new TitledPane();
        GridPane pogrid = new GridPane();
        pogrid.setVgap(4);
        pogrid.setPadding(new Insets(5, 5, 5, 5));
        CheckBox printInput = new CheckBox();
        pogrid.add(new Label("At beginning of program:"),0,0);
        pogrid.add(new Label("Print Input?"),0,1);
        pogrid.add(printInput,1,1);
        pogrid.add(new Separator(),0,2);

        pogrid.add(new Label("Intermediate evaluation:"),0,3);
        pogrid.add(new Label("Evaluate @ Iteration: "),0,4);
        TextField interEvalFreq = new TextField("100");
        CheckBox interEvalGram = new CheckBox();
        CheckBox interEvalAcc = new CheckBox();
        pogrid.add(interEvalFreq,1,4);
        pogrid.add(new Label("Print Grammar?"), 0, 5);
        pogrid.add(interEvalGram, 1, 5);
        pogrid.add(new Label("Print Accuracy Per Output?"), 0, 6);
        pogrid.add(interEvalAcc, 1, 6);
        pogrid.add(new Separator(),0,7);

        pogrid.add(new Label("Final evaluation:"),0,8);
        CheckBox finalGram =new CheckBox();
        CheckBox finalAcc = new CheckBox();
        pogrid.add(new Label("Print Grammar?"), 0, 9);
        pogrid.add(finalGram, 1, 9);
        pogrid.add(new Label("Print Accuracy Per Output?"), 0, 10);
        pogrid.add(finalAcc, 1, 10);
        po.setText("Print Options");
        po.setContent(pogrid);
        po.setExpanded(false);
        grid.add(po,0,8);

        //grid.setGridLinesVisible(true);

        Button btn = new Button("Run");
        grid.add(btn, 1, 4);

        final Text actiontarget = new Text();
        grid.add(actiontarget, 1, 5);

        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                actiontarget.setText("Running...");
                String chosenBias;
                if (iBias.isSelected()) {
                    chosenBias = "1";
                } else {
                    chosenBias = "0";
                }
                String chosenNeg;
                if (nok.isSelected()) {
                    chosenNeg = "1";
                } else {
                    chosenNeg = "0";
                }
                String chosenQuitFreq = quitFreq.getText();
                String chosenQuitSample = quitSample.getText();
                String chosenMaxDepth = maxDepth.getText();

                Boolean chosenPrintInput = printInput.isSelected();
                String chosenInterEvalFreq = interEvalFreq.getText();
                Boolean chosenInterEvalGram = interEvalGram.isSelected();
                Boolean chosenInterEvalAcc = interEvalAcc.isSelected();
                Boolean chosenFinalGram = finalGram.isSelected();
                Boolean chosenFinalAcc = finalAcc.isSelected();
                if(gr.getText().equals("")){
                    actiontarget.setText("Error: please upload grammar file!");
                } else {
                    String chosenGrammar = grPath.getText();
                    if(dist.getText().equals("")){
                        actiontarget.setText("Error: please upload distribution file!");
                    }else{
                        String chosenDist = distPath.getText();
                        if(it.getText().equals("")){
                            actiontarget.setText("Error: please specify iterations!");
                        } else {
                            String chosenIt = it.getText();
                            if(learner.getValue()==null){
                                actiontarget.setText("Error: please choose learner!");
                            } else {
                                String chosenLearner = learner.getValue().toString();
                                if(chosenLearner=="EDL") {
                                    System.out.println("EDL!");
                                    if(emodel.getValue()==null){
                                        actiontarget.setText("Error: please choose learner type!");
                                    } else{
                                        String chosenLearnerType = emodel.getValue().toString();
                                        String chosenLearnerNum;
                                        if(chosenLearnerType=="Online"){
                                            chosenLearnerNum = "2";
                                        }else{
                                            chosenLearnerNum = "1";
                                        }
                                        if(ss.getText().equals("")){
                                            actiontarget.setText("Error: please specify sample size!");
                                        }else{
                                            String chosenSampleSize = ss.getText();
                                            System.out.println("All EDL parameters ok!");
                                            String chosenFinEvalSample = finEvalSample.getText();//eventually move
                                            String[] args = {chosenGrammar, chosenDist, chosenIt, chosenFinEvalSample, chosenLearnerNum, chosenSampleSize,chosenBias};
                                            System.out.println(Arrays.toString(args));
                                            new Thread () {
                                                @Override public void run () {
                                                    EDL.main(args);
                                                }
                                            }.start();
                                        }
                                    }
                                } else{
                                    System.out.println("GLA!");
                                    if(gmodel.getValue()==null){
                                        actiontarget.setText("Error: please choose learner type!");
                                    }else{
                                        String chosenLearnerType = gmodel.getValue().toString();
                                        if(gramtype.getValue()==null){
                                            actiontarget.setText("Error: please choose grammar type!");
                                        }else{
                                            String chosenGrammarType = gramtype.getValue().toString();
                                            if(lr.getText().equals("")){
                                                actiontarget.setText("Error: please specify learning rate!");
                                            }else{
                                                String chosenLR = lr.getText();
                                                if(n.getText().equals("")){
                                                    actiontarget.setText("Error: please specify noise!");
                                                }else{
                                                    String chosenNoise = n.getText();
                                                    String chosenFinEvalSample = finEvalSample.getText();//eventually move
                                                    System.out.println("All GLA parameters ok!");
                                                    String[] args = {chosenGrammar, chosenDist, chosenIt, chosenFinEvalSample, chosenLearnerType, chosenGrammarType, chosenLR, chosenNoise, chosenBias,chosenNeg};
                                                    System.out.println(Arrays.toString(args));
                                                    TextArea results = new TextArea();
                                                    grid.add(results,3,3);
                                                    new Thread () {
                                                        @Override public void run () {
                                                            GLA.main(args);
                                                        }
                                                    }.start();


                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        TextArea ta = TextAreaBuilder.create()
                .prefWidth(800)
                .prefHeight(600)
                .wrapText(true)
                .build();

        Console console = new Console(ta);
        PrintStream ps = new PrintStream(console, true);
        System.setOut(ps);
        System.setErr(ps);

        grid.add(ta,3,3);
        Scene scene = new Scene(grid, 650, 550);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
