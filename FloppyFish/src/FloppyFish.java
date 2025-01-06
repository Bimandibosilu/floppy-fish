import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;
import java.awt.event.KeyListener;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;

public class FloppyFish extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 504;
    int boardHeight = 504;

    // Declare Clip objects for sound effects
    Clip jumpSound;
    Clip gameOversound;

    //Images
    Image backgroundImg;
    Image fishImg;
    Image downPipeImg;
    Image upPipeImg;
    Image foregroundImg;

    //Fish
    int fishX = 100;
    int fishY = 150;
    int fishWidth = 38;
    int fishHeight = 28;

    class Fish {
        int x = fishX;
        int y = fishY;
        int width = fishWidth;
        int height = fishHeight;
        Image img;

        Fish(Image img) {
            this.img = img;
        }
    }

    //pipes
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 400;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    //game logic
    Fish fish;
    int velocityX = -4; //move pipes to the left speed (simulates bird moving right)
    int velocityY = 0; //move fish up/down speed
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameOver = false;
    boolean gameStarted = false;
    double score = 0;
    double highScore = 0;

    FloppyFish() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        //setBackground(Color.blue);
        setFocusable(true);
        addKeyListener(this);

        //load images
        backgroundImg = new ImageIcon(getClass().getResource("./background.png")).getImage();
        fishImg = new ImageIcon(getClass().getResource("./fish.png")).getImage();
        upPipeImg = new ImageIcon(getClass().getResource("./upPipe.png")).getImage();
        downPipeImg = new ImageIcon(getClass().getResource("./downPipe.png")).getImage();
        foregroundImg = new ImageIcon(getClass().getResource("./foreground.png")).getImage();
 
        //fish
        fish = new Fish (fishImg);
        pipes = new ArrayList<Pipe>();

        //Sounds
        jumpSound = Sound.clipForLoopFactory("Jump.wav");
        gameOversound = Sound.clipForLoopFactory("gameOver.wav");


        //place pipes timer
        placePipesTimer = new Timer(1500,new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });

        placePipesTimer.start();


        //game Timer
        gameLoop = new Timer(1000/60, this); //1000/60 = 16.6
        gameLoop.start();
    }


     void placePipes() {
        //(0-1)*pipeHeight/2 --> (0-256)
        //128
        //0 - 128 - (0-256) --> pipeHeight/4 -> 3/4 pipeHeight

        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4;

        Pipe downPipe = new Pipe(downPipeImg);
        downPipe.y = randomPipeY;
        pipes.add(downPipe);

        Pipe upPipe = new Pipe(upPipeImg);
        upPipe.y = downPipe.y + pipeHeight + openingSpace;
        pipes.add(upPipe);
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);

    }

    public void draw(Graphics g) {
        //background
        g.drawImage(backgroundImg,0 ,0, boardWidth, boardHeight, null);
    
       //fish
       g.drawImage(fish.img, fish.x, fish.y, fish.width, fish.height, null);
    
       //pipes
       for (int i = 0; i <pipes.size(); i++) {
        Pipe pipe = pipes.get(i);
        g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
       }

       //foreground
       g.drawImage(foregroundImg, 0, boardHeight - 50, boardWidth, 50, null);
       
       //// Show welcome messages if the game hasn't started
    if (!gameStarted) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Purisa", Font.BOLD, 28));
        g.drawString("Welcome to Floppy Fish!", 80, 210);
        g.drawString("Press SPACE to swim", 110, 250);
    } else if (!gameStarted) {
        g.setColor(Color.WHITE);
            g.setFont(new Font("Purisa", Font.BOLD, 28));
            g.drawString("Press SPACE to swim", 110, 250);
    }
       //display high score
       g.setColor(Color.BLACK);
       g.setFont(new Font("Purisa", Font.BOLD, 18));
       g.drawString("High Score: ", 350, 30);
       g.drawString(String.valueOf((int) highScore), 460, 30);
         
       //score
       g.setColor(Color.white);
       g.setFont(new Font("Purisa", Font.BOLD, 32));
       if (gameOver) {
        g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
       }
       else {
        g.drawString(String.valueOf((int) score), 10, 35);
       }
    }

    public void move() {
        //fish
        velocityY += gravity;

        // Limit the maximum downward velocity to prevent excessive speed
        if (velocityY > 6) {
        velocityY = 6;  // You can adjust this value for smoother descent
        }

        fish.y += velocityY;
        fish.y = Math.max(fish.y, 0);

        //pipes
        for (int i=0; i <pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && fish.x > pipe.x + pipe.width) {
                score += 0.5; //because there are 2 pipes! so 0.5*2 = 1, 1 for each set of pipes
                pipe.passed = true;
            }

            if (collision(fish, pipe)) {
                gameOver = true;
            }
        }

        if (fish.y > boardHeight) {
            gameOver = true;
        } 
    }

    boolean collision(Fish a, Pipe b){
        return a.x < b.x + b.width && //a's top left corner doesn't reach b's top right corner
               a.x + a.width > b.x && //a's top right corner passes b's top left corner
               a.y < b.y + b.height && //a's top left corner doesn't reach b's bottom left corner
               a.y + a.height > b.y; //a's bottom left corner passes b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            if (gameOversound != null) {
                gameOversound.setFramePosition(0);
                gameOversound.start();
            }

            placePipesTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!gameStarted) {
                gameStarted = true; //Start the game
            }

            if (jumpSound != null){
                jumpSound.setFramePosition(0);
                jumpSound.start();
            }
            velocityY = -9;

            if (gameOver) {
                //restart the game by resetting the conditions
                if (score > highScore) {
                    highScore = score;
                }
                fish.y = fishY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameStarted = false;
                gameLoop.start();
                placePipesTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

}

class Sound {

    public static Clip clipForLoopFactory(String soundFileName) {
        try {
            // Load the sound file
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(Sound.class.getResource(soundFileName));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

