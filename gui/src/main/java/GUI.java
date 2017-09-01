package sample;
//GUI code

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

public class GUI extends Application {

    @Override
    public void start(Stage primaryStage) {

        Desktop desktop = Desktop.getDesktop();
        primaryStage.setTitle("HS Learning");//title of window

        GridPane grid = new GridPane();//This is the grid that all elements must be added to
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(20);
        grid.setPadding(new Insets(20, 20, 20, 20));

        Text scenetitle = new Text("Hidden Structure Learning");//scene title
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        TextArea ta = new TextArea();
        grid.add(ta,2,3);

        Label gramlabel = new Label("Grammar file:");//Grammar file upload box
        Tooltip gramTooltip = new Tooltip();
        gramTooltip.setText("Grammar file stores...");
        Tooltip.install(gramlabel, gramTooltip);
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

        Label distlabel = new Label("Distribution File:");//Distibution file upload box
        Tooltip distTooltip = new Tooltip();
        distTooltip.setText("Distribution file stores...");
        Tooltip.install(distlabel, distTooltip);
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

        Label itlabel = new Label("Iterations:");//Iterations
        Tooltip itTooltip = new Tooltip();
        itTooltip.setText("Reasonable iterations are...");
        Tooltip.install(itlabel, itTooltip);
        TextField it = new TextField ("1000");
        it.setPrefColumnCount(4);
        HBox iterations = new HBox();
        iterations.getChildren().addAll(itlabel,it);
        iterations.setSpacing(5);
        grid.add(iterations, 0,3);

        ComboBox learner = new ComboBox(FXCollections.observableArrayList("EDL","GLA"));
        Tooltip learnTooltip = new Tooltip();
        learnTooltip.setText("The EDL learner is... The GLA learner is...");
        Tooltip.install(learner, learnTooltip);
        learner.setPromptText("Learner");
        grid.add(learner,0,4);

        TitledPane edlo = new TitledPane();//Options for the EDL learner
        GridPane edlogrid = new GridPane();
        edlogrid.setVgap(4);
        edlogrid.setPadding(new Insets(5, 5, 5, 5));
        ComboBox emodel = new ComboBox(FXCollections.observableArrayList("Online","Batch"));
        Tooltip emodelTooltip = new Tooltip();
        emodelTooltip.setText("Here is an explanation of the different algorithms...");
        Tooltip.install(emodel, emodelTooltip);
        emodel.setPromptText("Learner");
        TextField ss = new TextField("1000");
        Label ssl = new Label("Sample Size: ");
        Tooltip ssTooltip = new Tooltip();
        ssTooltip.setText("Here is an explanation of sample size:");
        Tooltip.install(ssl, ssTooltip);
        edlogrid.add(emodel,0,0);
        edlogrid.add(ssl, 0, 1);
        edlogrid.add(ss, 1, 1);
        edlo.setText("EDL Options");
        edlo.setContent(edlogrid);
        edlo.setExpanded(false);
        edlo.setVisible(false);
        grid.add(edlo,0,5);

        TitledPane glao = new TitledPane();//Options for the GLA learner
        GridPane glaogrid = new GridPane();
        glaogrid.setVgap(4);
        glaogrid.setPadding(new Insets(5, 5, 5, 5));
        ComboBox gmodel = new ComboBox(FXCollections.observableArrayList("RIP","RRIP","EIP","randRIP"));
        Tooltip gmodelTooltip = new Tooltip();
        gmodelTooltip.setText("Here is an explanation of the different algorithms...");
        Tooltip.install(gmodel, gmodelTooltip);
        gmodel.setPromptText("Learner");
        glaogrid.add(gmodel,0,0);
        ComboBox gramtype = new ComboBox(FXCollections.observableArrayList("OT","HG","MaxEnt"));
        Tooltip typeTooltip = new Tooltip();
        typeTooltip.setText("Here is an explanation of the different types...");
        Tooltip.install(gramtype, typeTooltip);
        gramtype.setPromptText("Grammar Type");
        TextField lr = new TextField("0.1");
        Label lrl = new Label("Learning Rate: ");
        Tooltip lrTooltip = new Tooltip();
        lrTooltip.setText("Reasonable learning rates are...");
        Tooltip.install(lrl, lrTooltip);
        TextField n = new TextField("2");
        Label nl = new Label("Noise: ");
        Tooltip nTooltip = new Tooltip();
        nTooltip.setText("Here is of noise:");
        Tooltip.install(nl, nTooltip);
        CheckBox nok = new CheckBox();
        Label nokl = new Label("NegOK?");
        Tooltip nokTooltip = new Tooltip();
        nokTooltip.setText("NegOK means that negative weights can be used.");
        Tooltip.install(nokl, nokTooltip);
        glaogrid.add(gramtype,0,1);
        glaogrid.add(lrl,0,2);
        glaogrid.add(lr,1,2);
        glaogrid.add(nl,0,3);
        glaogrid.add(n,1,3);
        glaogrid.add(nokl, 0, 4);
        glaogrid.add(nok, 1, 4);
        glao.setText("GLA Options");
        glao.setContent(glaogrid);
        glao.setExpanded(false);
        glao.setVisible(false);
        grid.add(glao,0,5);

        learner.getSelectionModel().selectedIndexProperty().addListener(//Display either EDL or GLA options based on which is selected
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

        TitledPane ao = new TitledPane();//Advanced options
        GridPane aogrid = new GridPane();
        aogrid.setVgap(4);
        aogrid.setPadding(new Insets(5, 5, 5, 5));
        CheckBox iBias = new CheckBox();
        Label ibl = new Label("Initial Bias");
        Tooltip ibTooltip = new Tooltip();
        ibTooltip.setText("Initial Bias means that faithfulness constraints are ranked above markedness constraints in the starting grammar.");
        Tooltip.install(ibl, ibTooltip);
        TextField finEvalSample = new TextField("1000");
        Label fesl = new Label("Final Evaluation Sampling: ");
        Tooltip fesTooltip = new Tooltip();
        fesTooltip.setText("Final Evaluation Sampling is the number of samples used to evaluate the final grammar. Reasonable values are...");
        Tooltip.install(fesl, fesTooltip);
        aogrid.add(ibl, 0, 1);
        aogrid.add(iBias, 1, 1);
        aogrid.add(fesl,0,2);
        aogrid.add(finEvalSample,1,2);
        ao.setText("Advanced Options");
        ao.setContent(aogrid);
        ao.setExpanded(false);
        grid.add(ao,0,6);

        TitledPane eo = new TitledPane();//Efficiency options
        GridPane eogrid = new GridPane();
        eogrid.setVgap(4);
        eogrid.setPadding(new Insets(5, 5, 5, 5));
        TextField quitFreq = new TextField("100");
        Label qfl = new Label("Check to Quit Early @ Iteration: ");
        Tooltip qfTooltip = new Tooltip();
        qfTooltip.setText("How often the program checks to see if a successful grammar has already been learned. If the learning problem is simple, setting a small number of generations will likely make the program more efficient; it the learning problem is difficult and early success if unlikely, then a high value here will increase efficiency.");
        Tooltip.install(qfl, qfTooltip);
        TextField quitSample = new TextField("100");
        Label qsl = new Label("Sampling for Quit Early: ");
        Tooltip qsTooltip = new Tooltip();
        qsTooltip.setText("Determines how many samples are used to evaluate whether a successful grammar has already been learned. High values, like X, will improve accuracy at the expense of efficient performance.");
        Tooltip.install(qsl, qsTooltip);
        TextField maxDepth = new TextField();
        Label mdl = new Label("MaxDepth of Ranking Tree: ");
        Tooltip mdTooltip = new Tooltip();
        mdTooltip.setText("Sets a maximum depth of the ranking tree; to increase efficiency, it is possible to cap the depth of data structures. A reasonable value may be X.");
        Tooltip.install(mdl, mdTooltip);
        eogrid.add(qfl, 0, 0);
        eogrid.add(quitFreq, 1, 0);
        eogrid.add(qsl, 0, 1);
        eogrid.add(quitSample, 1, 1);
        eogrid.add(mdl, 0, 2);
        eogrid.add(maxDepth, 1, 2);
        eo.setText("Efficiency Options");
        eo.setContent(eogrid);
        eo.setExpanded(false);
        grid.add(eo,0,7);

        TitledPane po = new TitledPane();//Output options
        GridPane pogrid = new GridPane();
        pogrid.setVgap(4);
        pogrid.setPadding(new Insets(5, 5, 5, 5));
        CheckBox printInput = new CheckBox();
        Label pil = new Label("Print Input?");
        Tooltip piTooltip = new Tooltip();
        piTooltip.setText("Print user input at the beginning of the program?");
        Tooltip.install(pil, piTooltip);
        pogrid.add(new Label("At beginning of program:"),0,0);
        pogrid.add(pil,0,1);
        pogrid.add(printInput,1,1);
        pogrid.add(new Separator(),0,2);

        pogrid.add(new Label("Intermediate evaluation:"),0,3);
        Label eil = new Label("Evaluate @ Iteration: ");
        Tooltip eiTooltip = new Tooltip();
        eiTooltip.setText("Print intermediate evaluations?");
        Tooltip.install(eil, eiTooltip);
        pogrid.add(eil,0,4);
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
        Label fil = new Label("Print Grammar?");
        Tooltip fiTooltip = new Tooltip();
        fiTooltip.setText("Print final evaluation?");
        Tooltip.install(fil, fiTooltip);
        CheckBox finalGram =new CheckBox();
        CheckBox finalAcc = new CheckBox();
        pogrid.add(fil, 0, 9);
        pogrid.add(finalGram, 1, 9);
        pogrid.add(new Label("Print Accuracy Per Output?"), 0, 10);
        pogrid.add(finalAcc, 1, 10);
        po.setText("Print Options");
        po.setContent(pogrid);
        po.setExpanded(false);
        grid.add(po,0,8);

        //grid.setGridLinesVisible(true);

        Button btn = new Button("Run");//Run the program
        Tooltip runTooltip = new Tooltip();
        runTooltip.setText("Your parameter preferences will automatically be saved.");//Not yet
        Tooltip.install(btn, runTooltip);
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
                                        if(ss.getText().equals("")){//Throw errors if something hasn't been specified
                                            actiontarget.setText("Error: please specify sample size!");
                                        }else{//Else run learner with given parameters
                                            String chosenSampleSize = ss.getText();
                                            System.out.println("All EDL parameters ok!");
                                            String chosenFinEvalSample = finEvalSample.getText();//eventually move
                                            String[] args = {chosenGrammar, chosenDist, chosenIt, chosenFinEvalSample, chosenLearnerNum, chosenSampleSize,chosenBias};
                                            System.out.println(Arrays.toString(args));
                                            EDL.writer = new GuiWriter(ta);//Create a writer to output results
                                            new Thread () {
                                                @Override public void run () {//Must create new thread so that the GUI doesn't freeze while the learner is running
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
                                                    GLA.writer = new GuiWriter(ta);
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


        ScrollPane sp = new ScrollPane();//Scroll for GUI
        sp.setContent(grid);
        Scene scene = new Scene(sp, 650, 550);
        primaryStage.setScene(scene);

        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }

}
