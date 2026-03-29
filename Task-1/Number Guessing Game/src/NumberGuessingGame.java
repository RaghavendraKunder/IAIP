
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.io.*;

public class NumberGuessingGame extends JFrame implements ActionListener {

    JLabel title, info, timerLabel, scoreLabel, highScoreLabel;
    JTextField input;
    JButton guessBtn, resetBtn;

    int number, attemptsLeft, maxAttempts, score, highScore = 0;
    int timeLeft;
    Timer timer;
    String difficulty;

    Random rand = new Random();

    public NumberGuessingGame() {
        setTitle("Number Guessing Game 🎯");
        setSize(400, 350);
        setLayout(new GridLayout(8, 1));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        title = new JLabel("Number Guessing Game", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        info = new JLabel("Select difficulty to start", JLabel.CENTER);

        timerLabel = new JLabel("Time: 0", JLabel.CENTER);
        scoreLabel = new JLabel("Score: 0", JLabel.CENTER);
        highScoreLabel = new JLabel("High Score: 0", JLabel.CENTER);

        input = new JTextField();
        guessBtn = new JButton("Guess");
        resetBtn = new JButton("Restart");

        guessBtn.addActionListener(this);
        resetBtn.addActionListener(e -> setupGame());

        add(title);
        add(info);
        add(timerLabel);
        add(scoreLabel);
        add(highScoreLabel);
        add(input);
        add(guessBtn);
        add(resetBtn);

        loadHighScore();
        chooseDifficulty();

        setVisible(true);
    }

    void chooseDifficulty() {
        String[] options = {"Easy", "Medium", "Hard"};
        difficulty = (String) JOptionPane.showInputDialog(
                this,
                "Choose Difficulty",
                "Difficulty",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        setupGame();
    }

    void setupGame() {
        int range = 100;

        switch (difficulty) {
            case "Easy":
                maxAttempts = 10;
                timeLeft = 60;
                break;
            case "Medium":
                maxAttempts = 7;
                timeLeft = 40;
                break;
            case "Hard":
                maxAttempts = 5;
                timeLeft = 25;
                break;
        }

        number = rand.nextInt(range) + 1;
        attemptsLeft = maxAttempts;
        score = 0;

        info.setText("Guess number (1-100)");
        scoreLabel.setText("Score: 0");

        startTimer();
    }

    void startTimer() {
        timerLabel.setText("Time: " + timeLeft);

        timer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("Time: " + timeLeft);

            if (timeLeft <= 0) {
                timer.stop();
                endGame(false);
            }
        });
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            int guess = Integer.parseInt(input.getText());

            attemptsLeft--;

            if (guess == number) {
                score = attemptsLeft * 10 + timeLeft;
                timer.stop();
                endGame(true);
            } else if (guess < number) {
                info.setText("Too Low! Attempts left: " + attemptsLeft);
            } else {
                info.setText("Too High! Attempts left: " + attemptsLeft);
            }

            if (attemptsLeft <= 0) {
                timer.stop();
                endGame(false);
            }

        } catch (Exception ex) {
            info.setText("Enter valid number!");
        }

        input.setText("");
    }

    void endGame(boolean win) {
        if (win) {
            info.setText("🎉 You Won! Number: " + number);

            if (score > highScore) {
                highScore = score;
                saveHighScore();
                highScoreLabel.setText("High Score: " + highScore);
            }

            scoreLabel.setText("Score: " + score);

        } else {
            info.setText("❌ Lost! Number was: " + number);
        }

        int option = JOptionPane.showConfirmDialog(this, "Play Again?");
        if (option == JOptionPane.YES_OPTION) {
            chooseDifficulty();
        } else {
            System.exit(0);
        }
    }

    void saveHighScore() {
        try {
            FileWriter fw = new FileWriter("highscore.txt");
            fw.write(String.valueOf(highScore));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void loadHighScore() {
        try {
            File file = new File("highscore.txt");
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                highScore = Integer.parseInt(br.readLine());
                br.close();
                highScoreLabel.setText("High Score: " + highScore);
            }
        } catch (Exception e) {
            highScore = 0;
        }
    }

    public static void main(String[] args) {
        new NumberGuessingGame();
    }
}
