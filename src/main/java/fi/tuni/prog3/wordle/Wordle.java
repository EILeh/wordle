
package fi.tuni.prog3.wordle;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;


/**
 * JavaFX App
 */

public class Wordle extends Application
{
    // Globaalit vakiot
    private final int COMPLETE_WORD_TRIES_HIGH_LIMIT = 7;
    private ArrayList<ArrayList<Label>> wordleGameBoardLabel = new ArrayList<>(6);
    private ArrayList<Label> listOfWordsInputByThePlayer;
    private StringBuilder currentInput = new StringBuilder();
    private ArrayList<Label> guessedLettersOfCurrentRound = new ArrayList<>();
    private int roundCounter = 0;
    private int letterIndexCurrentInput = 0;
    private int amountStartNewGameHasBeenPressed = 0;
    private boolean isGameActive = true;
    private boolean isGameLost = false;
    private String wordBeingGuessed;
    private String wordBeingGuessedAsUppercase;

    public Wordle()
    {
    }

    @Override
    public void start(Stage stage)
    {
        BorderPane borderPane = new BorderPane();
        HBox topBox = new HBox(20);
        GridPane gameWindow = new GridPane();
        Scene gameScene = new Scene(borderPane, 800, 600);

        stage.setScene(gameScene);
        stage.setTitle("Wordle");

        gameWindow.setHgap(6);
        gameWindow.setVgap(6);
        gameWindow.setPadding(new Insets(10, 10, 10, 10));

        Button newGameBtn = new Button("Start new game");
        newGameBtn.setId("newGameBtn");

        Label gameStatusText = new Label("");
        gameStatusText.setId("infoBox");
        gameStatusText.setFont(Font.font("Neue Helvetica", FontWeight.NORMAL, 20));

        topBox.getChildren().addAll(newGameBtn, gameStatusText);

        borderPane.setPadding(new Insets(5, 10, 10, 50));
        borderPane.setTop(topBox);

        VBox centerBox = new VBox();

        centerBox.getChildren().add(gameWindow);
        borderPane.setCenter(centerBox);

        File wordsFile = new File("words.txt");

        try
        {
            wordBeingGuessed = parseWordsFile(wordsFile);
            wordBeingGuessedAsUppercase = wordBeingGuessed.toUpperCase();
            initializeGameBoard(wordBeingGuessedAsUppercase, gameWindow, guessedLettersOfCurrentRound);
        }
        catch (IOException fileOpeningException)
        {
            throw new RuntimeException(fileOpeningException);
        }

        newGameBtn.addEventFilter(KeyEvent.KEY_PRESSED, event ->
        {

            if (event.getCode() == KeyCode.ENTER)
            {
                // Tilanteessa, jossa pelaaja painaa enter ennen kuin on antanut arvauksen, joka olisi oikean sanan
                // mittainen, tulostetaan error.
                if (isGameActive && (currentInput.length() < wordBeingGuessed.length()))
                {
                    gameStatusText.setText("Give a complete word before pressing Enter!");
                }

                else if ((letterIndexCurrentInput == wordBeingGuessed.length()) && (isGameActive))
                {
                    // Nollataan nykyinen indeksi, sillä tähän tultaessa syöte on arvattavan sanan mittainen eli tehdään
                    // tarkistus siitä, arvasiko pelaaja sanan oikein
                    letterIndexCurrentInput = 0;

                    isGameActive = checkInput(wordBeingGuessedAsUppercase);
                    guessedLettersOfCurrentRound.clear();

                    if ((!isGameActive) && (!isGameLost))
                    {
                        gameStatusText.setText("Congratulations, you won!");
                    }
                    else if (isGameLost)
                    {
                        gameStatusText.setText("Game over, you lost! Correct word was " + wordBeingGuessed + "!");
                    }


                    else
                    {
                        gameStatusText.setText("");
                    }

                }
                event.consume();
            }
        });

        newGameBtn.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                try
                {
                    gameWindow.getChildren().clear();
                    borderPane.setTop(topBox);
                    borderPane.setCenter(centerBox);
                    stage.setScene(gameScene);

                    amountStartNewGameHasBeenPressed++;
                    wordBeingGuessed = parseWordsFile(wordsFile);
                    wordBeingGuessedAsUppercase = wordBeingGuessed.toUpperCase();

                    wordleGameBoardLabel.clear();
                    listOfWordsInputByThePlayer.clear();
                    gameStatusText.setText("");

                    initializeGameBoard(wordBeingGuessedAsUppercase, gameWindow, guessedLettersOfCurrentRound);

                    roundCounter = 0;
                    letterIndexCurrentInput = 0;
                    currentInput.setLength(0);
                    guessedLettersOfCurrentRound.clear();
                    isGameActive = true;
                    isGameLost = false;

                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        gameScene.setOnKeyPressed(keyEvent ->
        {
            KeyCode keyCode = keyEvent.getCode();
            keyEvent.consume();

            if (keyCode == KeyCode.BACK_SPACE)
            {
                if ((letterIndexCurrentInput - 1) >= 0)
                {
                    currentInput.setLength(letterIndexCurrentInput - 1);
                    System.out.println(currentInput);
                    Label letterPos = new Label("");
                    letterPos = wordleGameBoardLabel.get(roundCounter).get(letterIndexCurrentInput - 1);
                    letterPos.setText("");
                    letterIndexCurrentInput--;
                }
            }

            else if ((keyCode != KeyCode.ENTER) && (currentInput.length() < wordBeingGuessed.length()) && (isGameActive))
            {
                gameStatusText.setText("");
                currentInput.append(keyCode.toString());
                System.out.println(currentInput);
                Label keyAsALabel = new Label(keyCode.getName());
                guessedLettersOfCurrentRound.add(keyAsALabel);
                Label letterPos = new Label("");
                letterPos = wordleGameBoardLabel.get(roundCounter).get(letterIndexCurrentInput);
                letterPos.setAlignment(Pos.CENTER); // Center the text horizontally and vertically
                letterPos.setFont(Font.font("Neue Helvetica", FontWeight.BOLD, 50));
                letterPos.setText(keyAsALabel.getText());
                letterIndexCurrentInput++;
            }
        });

        stage.show();
    }

    private boolean checkInput(String wordAsUppercase)
    {
        if (currentInput.toString().equals(wordAsUppercase))
        {
            for (int i = 0; i < wordAsUppercase.length(); i++)
            {
                Label letter = new Label("");
                letter = wordleGameBoardLabel.get(roundCounter).get(i);
                letter.setText(String.valueOf(wordAsUppercase.charAt(i)));
                letter.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
            }
            return false;
        }

        else
        {
            for (int i = 0; i <wordAsUppercase.length(); i++)
            {
                Label letter = new Label("");
                letter = wordleGameBoardLabel.get(roundCounter).get(i);
                String letterStr = letter.getText();

                if (wordAsUppercase.contains(letterStr))
                {
                    if (letterStr.equals(String.valueOf(wordAsUppercase.charAt(i))))
                    {
                        letter.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY,
                                Insets.EMPTY)));
                    }

                    else
                    {
                        letter.setBackground(new Background(new BackgroundFill(Color.ORANGE, CornerRadii.EMPTY,
                                Insets.EMPTY)));
                    }
                }

                else
                {
                    letter.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY,
                            Insets.EMPTY)));
                }
            }
        }

        if (roundCounter == 5)
        {
            isGameLost = true;
            return false;
        }

        roundCounter++;
        currentInput.setLength(0);

        return true;
    }

    private String parseWordsFile(File wordsFile) throws IOException
    {
        ArrayList<String> words = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(wordsFile)))
        {
            String fileLine;
            while ((fileLine = br.readLine()) != null)
            {
                words.add(fileLine);
            }
        }
        return words.get(amountStartNewGameHasBeenPressed);
    }

    private void initializeGameBoard(String word, GridPane gameBoard, ArrayList<Label> guessedLetters)
    {
        for (int i = 1; i < COMPLETE_WORD_TRIES_HIGH_LIMIT; i++)
        {
            listOfWordsInputByThePlayer = new ArrayList<>(word.length());
            for (int j = 1; j <= word.length(); j++)
            {
                Label letterPos = new Label("");
                int indexIStartingFromZero = i - 1;
                int indexJStartingFromZero = j - 1;
                letterPos.setId(indexIStartingFromZero + "_" + indexJStartingFromZero);
                listOfWordsInputByThePlayer.add(j - 1, letterPos);

                letterPos.setPrefSize(80, 80);
                letterPos.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                letterPos.setStyle("-fx-border-color: black;");
                gameBoard.add(letterPos, j, i);
            }
            wordleGameBoardLabel.add(i - 1, listOfWordsInputByThePlayer);
        }
    }

    public static void main(String[] args)
    {
        launch();
        Wordle currentGame = new Wordle();
    }
}