package game;

import animations.AnimationRunner;
import animations.MenuAnimation;
import animations.HighScoresAnimation;
import animations.GameLevel;
import animations.StopScreenDecorator;
import animations.EndScreen;


import animations.Task;

import animations.Menu;
import biuoop.DialogManager;

import biuoop.DrawSurface;
import biuoop.KeyboardSensor;
import geometry.Rectangle;
import sprites.LiveIndicator;
import sprites.ScoreIndicator;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 * The GameFlow class controls the different levels of the game.
 *
 * @author Matan Ben Noach Nir Ben Shalom
 * @version 1.0 22 May 2016
 */
public class GameFlow {
    private AnimationRunner ar;
    private KeyboardSensor ks;
    private int lives;
    private ScoreIndicator score;
    private HighScoresTable scoresTable;


    /**
     * Constructor for the GameFlow class.
     *
     * @param ar          The Animation Runner of the game.
     * @param ks          The KeyboardSensor of the game.
     * @param lives       is the number of live.
     * @param scoresTable is the HighScores table of the game.
     */
    public GameFlow(AnimationRunner ar, KeyboardSensor ks, int lives, HighScoresTable scoresTable) {
        this.ar = ar;
        this.ks = ks;
        this.lives = lives;
        this.score = new ScoreIndicator();
        this.scoresTable = scoresTable;
    }

    /**
     * chooseTask creates the tasks of the game, calls the menu to display them,
     * and runs the task that the user chooses.
     */
    public void chooseTask() {
        Menu<Task<Void>> menu = new MenuAnimation<Task<Void>>(ks, ar, "please choose:");

        //Anonymous classes that defines each task's run method.
        Task<Void> playGame = new Task<Void>() {
            @Override
            public Void run() {
                DrawSurface d = ar.getGui().getDrawSurface();
                List<LevelInformation> levels = new ArrayList<LevelInformation>();
                levels.add(new Level(new Rectangle(0, 0, d.getWidth(), d.getHeight())));
                runLevels(levels);
                return null;
            }
        };

        Task<Void> hiScores = new Task<Void>() {
            @Override
            public Void run() {
                ar.run(new HighScoresAnimation(scoresTable, "i", ks));
                if (ks.isPressed(KeyboardSensor.SPACE_KEY)) {
                    chooseTask();
                }
                return null;
            }
        };

        Task<Void> quitGame = new Task<Void>() {
            @Override
            public Void run() {
                Runtime.getRuntime().exit(1);
                return null;
            }
        };

        // Adding the tasks to the tasks list.
        menu.addSelection("h", "High scores", hiScores);
        menu.addSelection("s", "Play", playGame);
        menu.addSelection("q", "Quit", quitGame);

        while (true) {
            ar.run(menu);
            // wait for user selection
            Task<Void> task = menu.getStatus();
            task.run();
        }
    }

    /**
     * runLevels method runs a given list of levels.
     *
     * @param theLevels a list of levels.
     */
    public void runLevels(List<LevelInformation> theLevels) {
        LiveIndicator liveIndicator = new LiveIndicator(lives);
        score = new ScoreIndicator();
        for (LevelInformation levelInfo : theLevels) {
            GameLevel level = new GameLevel(levelInfo, this.ks, this.ar);
            level.initialize(liveIndicator, score);
            while (level.getBlockCounter().getValue() != 0 && liveIndicator.getValue() != 0) {
                level.playOneTurn();
            }

            if (level.getBlockCounter().getValue() == 0) {
                score.increase(100);
            }

            if (liveIndicator.getValue() == 0) {
                break;
            }
        }
        ar.run(new StopScreenDecorator(ks, "o", new EndScreen(ks, liveIndicator, score)));
        DialogManager dialog = ar.getGui().getDialogManager();
        String name = dialog.showQuestionDialog("Name", "What is your name?", "");
        scoresTable.add(new ScoreInfo(name, score.getScore().getValue()));
        ar.run(new StopScreenDecorator(ks, "t", new HighScoresAnimation(scoresTable, "t", ks)));
        try {
            scoresTable.save(new File("highscores"));
        } catch (IOException e) {
            System.out.println("Error saving file");
        }
        this.chooseTask();
    }



}