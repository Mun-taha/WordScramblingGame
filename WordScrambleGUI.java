package com.muntaha.wordscramble;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

public class WordScrambleGUI extends JFrame {

    private Map<String, List<String>> levels = new HashMap<>();
    private String currentLevel = "Easy";
    private String currentWord;
    private String scrambledWord;
    private int timeLeft;
    private Timer countdownTimer;
    private JLabel wordLabel;
    private JTextField inputField;
    private JButton checkButton;
    private JLabel resultLabel;
    private JLabel timerLabel;
    private JLabel[] scoreLabels;
    private JLabel playerTurnLabel;
    private JComboBox<String> levelSelector;
    private int numberOfPlayers = 2;
    private String[] playerNames = new String[2];
    private int[] scores = new int[2];
    private int[] times = new int[2];
    private int currentPlayer = 0;
    private Set<String> usedWords = new HashSet<>();
    private int totalWordsPerPlayer = 5;
    private int[] wordsGuessed = new int[2];

    public WordScrambleGUI() {
        setTitle("Word Scramble Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(11, 1));

        levels.put("Easy", new ArrayList<>(Arrays.asList("cat", "dog", "sun", "pen", "bat", "cup", "hat", "fan", "bed", "box")));
        levels.put("Medium", new ArrayList<>(Arrays.asList("apple", "plant", "mouse", "river", "table", "chair", "house", "light", "stone", "brush")));
        levels.put("Hard", new ArrayList<>(Arrays.asList("keyboard", "computer", "elephant", "umbrella", "mountain", "airplane", "building", "diamond", "festival", "strategy")));

        JOptionPane.showMessageDialog(this, "Welcome to the Word Scramble Game!");

        playerNames[0] = JOptionPane.showInputDialog(this, "Enter name for Player 1:");
        playerNames[1] = JOptionPane.showInputDialog(this, "Enter name for Player 2:");
        if (playerNames[0] == null || playerNames[1] == null || playerNames[0].trim().isEmpty() || playerNames[1].trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid player names. Exiting game.");
            System.exit(0);
        }

        scores = new int[numberOfPlayers];
        times = new int[numberOfPlayers];
        wordsGuessed = new int[numberOfPlayers];
        scoreLabels = new JLabel[numberOfPlayers];

        levelSelector = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        int levelChoice = JOptionPane.showOptionDialog(this, levelSelector, "Select Game Level", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (levelChoice == JOptionPane.CLOSED_OPTION) {
            JOptionPane.showMessageDialog(this, "No level selected. Exiting game.");
            System.exit(0);
        }
        currentLevel = levelSelector.getSelectedItem().toString();

        wordLabel = new JLabel("", SwingConstants.CENTER);
        wordLabel.setFont(new Font("Arial", Font.BOLD, 24));
        inputField = new JTextField();
        checkButton = new JButton("Check");
        resultLabel = new JLabel("", SwingConstants.CENTER);
        timerLabel = new JLabel("Time: 00", SwingConstants.CENTER);
        playerTurnLabel = new JLabel("", SwingConstants.CENTER);

        add(playerTurnLabel);
        add(wordLabel);
        add(inputField);
        add(checkButton);
        add(resultLabel);

        for (int i = 1; i < numberOfPlayers; i++) {
            scoreLabels[i] = new JLabel(playerNames[i] + " Score: 0", SwingConstants.CENTER);
            add(scoreLabels[i]);
        }

        add(timerLabel);

        checkButton.addActionListener(this::checkAnswer);

        loadNextWord();
    }

    private void setTimeByLevel() {
        switch (currentLevel) {
            case "Easy":
                timeLeft = 30;
                break;
            case "Medium":
                timeLeft = 20;
                break;
            case "Hard":
                timeLeft = 15;
                break;
        }
        timerLabel.setText("Time: " + timeLeft);
    }

    private void loadNextWord() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        if (wordsGuessed[currentPlayer] >= totalWordsPerPlayer) {
            switchPlayer();
            if (wordsGuessed[currentPlayer] >= totalWordsPerPlayer) {
                endGame();
                return;
            }
        }

        setTimeByLevel();

        List<String> wordSet = levels.get(currentLevel);
        List<String> availableWords = new ArrayList<>(wordSet);
        availableWords.removeAll(usedWords);

        if (availableWords.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No more unique words available in this level.");
            endGame();
            return;
        }

        currentWord = availableWords.get(new Random().nextInt(availableWords.size()));
        usedWords.add(currentWord);

        scrambledWord = scramble(currentWord);
        wordLabel.setText("Scrambled: " + scrambledWord);
        inputField.setText("");
        resultLabel.setText("");
        timerLabel.setText("Time: " + timeLeft);
        playerTurnLabel.setText(playerNames[currentPlayer] + "'s Turn");

        countdownTimer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("Time: " + timeLeft);
            if (timeLeft <= 0) {
                countdownTimer.stop();
                resultLabel.setText("Time's up! Word was: " + currentWord);
                inputField.setEnabled(false);
                checkButton.setEnabled(false);
                wordsGuessed[currentPlayer]++;
                switchPlayer();
                loadNextWord();
            }
        });
        countdownTimer.start();

        inputField.setEnabled(true);
        checkButton.setEnabled(true);
    }

    private void checkAnswer(ActionEvent e) {
        String guess = inputField.getText().trim();
        if (guess.equalsIgnoreCase(currentWord)) {
            resultLabel.setText("Correct!");
            scores[currentPlayer]++;
            times[currentPlayer] += (getInitialTime() - timeLeft);
            scoreLabels[currentPlayer].setText(playerNames[currentPlayer] + " Score: " + scores[currentPlayer]);
            countdownTimer.stop();
            inputField.setEnabled(false);
            checkButton.setEnabled(false);
            wordsGuessed[currentPlayer]++;
            switchPlayer();
            loadNextWord();
        } else {
            resultLabel.setText("Wrong! Try again.");
        }
    }

    private int getInitialTime() {
        switch (currentLevel) {
            case "Easy":
                return 30;
            case "Medium":
                return 20;
            case "Hard":
                return 15;
            default:
                return 30;
        }
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer + 1) % numberOfPlayers;
    }

    private void endGame() {
        String winnerMessage;
        if (scores[0] > scores[1]) {
            winnerMessage = "Winner: " + playerNames[0];
        } else if (scores[1] > scores[0]) {
            winnerMessage = "Winner: " + playerNames[1];
        } else {
            if (times[0] < times[1]) {
                winnerMessage = "Scores tied. Winner by time: " + playerNames[0];
            } else if (times[1] < times[0]) {
                winnerMessage = "Scores tied. Winner by time: " + playerNames[1];
            } else {
                winnerMessage = "It's a tie!";
            }
        }
        JOptionPane.showMessageDialog(this, winnerMessage);
        System.exit(0);
    }

    private String scramble(String word) {
        List<String> letters = Arrays.asList(word.split(""));
        Collections.shuffle(letters);
        StringBuilder sb = new StringBuilder();
        for (String letter : letters) {
            sb.append(letter);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WordScrambleGUI().setVisible(true));
    }
}
