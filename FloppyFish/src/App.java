import javax.swing.*;

public class App {
    public static void main(String[] args) throws Exception {
        int boardWidth = 504;
        int boardHeight = 504;

        JFrame frame = new JFrame("Flappy Fish");
         //frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
    
        FloppyFish flappyFish = new FloppyFish();
        frame.add(flappyFish);
        frame.pack();
        flappyFish.requestFocus();
        frame.setVisible(true);
    }
}
