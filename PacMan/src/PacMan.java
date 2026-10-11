import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U D L R
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            switch (this.direction) {
                case 'U':
                    this.velocityX = 0;
                    this.velocityY = -tileSize / 4;
                    break;
                case 'D':
                    this.velocityX = 0;
                    this.velocityY = tileSize / 4;
                    break;
                case 'L':
                    this.velocityX = -tileSize / 4;
                    this.velocityY = 0;
                    break;
                case 'R':
                    this.velocityX = tileSize / 4;
                    this.velocityY = 0;
                    break;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;

    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;   
    private Image redGhostImage;
    private Image scaredGhostImage;

    private Image cherryImage;

    private Image heartImage;
    private Image heartBlackImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "XbXX XXX X XXX XXpX",
        "X        C        X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XX XX X XXXX",
        "T        P        T",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X           X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "XrXXXXXX X XXXXXXoX",
        "X        C        X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> cherrys;
    HashSet<Block> ghosts;
    HashSet<Block> teleports;
    Block pacman;

    Timer gameLoop;
    TimerTask ghostScaredTimer;
    char[] directions = {'U', 'D', 'L', 'R'}; //up down left right
    Random random = new Random();
    int score = 0;
    int lives = 3;
    int livesBlack = 0;
    int cherrySpawnTime = 30;
    boolean gameOver = false;
    boolean ghostScared = false;

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        //load images
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();

        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();
        scaredGhostImage = new ImageIcon(getClass().getResource("./scaredGhost.png")).getImage();

        cherryImage = new ImageIcon(getClass().getResource("./cherry.png")).getImage();

        heartImage = new ImageIcon(getClass().getResource("./heart.png")).getImage();
        heartBlackImage = new ImageIcon(getClass().getResource("./heartBlack.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadMap();
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        //how long it takes to start timer, milliseconds gone between frames
        gameLoop = new Timer(50, this); //20fps (1000/50)
        gameLoop.start();
    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        cherrys = new HashSet<Block>();
        ghosts = new HashSet<Block>();
        teleports = new HashSet<Block>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c * tileSize;
                int y = r * tileSize;

                switch (tileMapChar) {
                    case 'X': //block wall
                        Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                        walls.add(wall);
                        break;
                    case 'b': //blue ghost
                        Block blueGhost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                        ghosts.add(blueGhost);
                        break;
                    case 'o': //orange ghost
                        Block orangeGhost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                        ghosts.add(orangeGhost);
                        break;
                    case 'p': //pink ghost
                        Block pinkGhost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                        ghosts.add(pinkGhost);
                        break;
                    case 'r': //red ghost
                        Block redGhost = new Block(redGhostImage, x, y, tileSize, tileSize);
                        ghosts.add(redGhost);
                        break;
                    case 'P': //pacman
                        pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                        break;
                    case ' ': //food
                        Block food = new Block(null, x + 14, y + 14, 4, 4);
                        foods.add(food);
                        break;
                    case 'T': //teleport
                        Block teleport = new Block(null, x, y, tileSize, tileSize);
                        teleports.add(teleport);
                        break;
                    case 'C': //cherry
                        Block cherry = new Block(cherryImage, x, y, tileSize, tileSize);
                        cherrys.add(cherry);
                        break;
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts) {
            if (ghostScared) {
                g.drawImage(scaredGhostImage, ghost.x, ghost.y, ghost.width, ghost.height, null);
            } else {
                g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
            }
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        for (Block cherry : cherrys) {
            g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.height, null);
        }

        for (Block food : foods) {
            g.setColor(Color.WHITE);
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        //score
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf(score) + " (Press 'R' to restart)", tileSize / 2, tileSize / 2);
            for (int j = livesBlack; j > 0; j--) {
                g.drawImage(heartBlackImage, 590 - (j * 30), 0, tileSize, tileSize, null);
            }
        } else {
            g.drawString("Score: " + String.valueOf(score), tileSize / 2, tileSize / 2);
            for (int i = lives - livesBlack; i > 0; i--) {
                g.drawImage(heartImage, 470 + (i * 30), 0, tileSize, tileSize, null);
            }
            for (int j = livesBlack; j > 0; j--) {
                g.drawImage(heartBlackImage, 590 - (j * 30), 0, tileSize, tileSize, null);
            }
        }
    }

    public void move() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        //check wall collisions
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        //check teleport conditions
        for (Block teleport : teleports) {
            if (collision(pacman, teleport)) {
                if (pacman.x == 0) {
                    pacman.x = 18 * tileSize;
                } else if (pacman.x == 18 * tileSize) {
                    pacman.x = 0;
                }
                break;
            }
        }

        //check ghost collisions
        for (Block ghost : ghosts) {
            if (collision(pacman, ghost)) {
                if (ghostScared) {
                    //ghost scared, pacman eats ghost
                    score += 100;
                    ghost.reset();
                } else {
                    //ghost hit, lose life
                    lives--;
                    pacman.reset();
                    if (lives <= 0) {
                        gameOver = true;
                    }
                    break;
                }
            }
        }

        //check food collisions
        HashSet<Block> toRemove = new HashSet<Block>();
        for (Block food : foods) {
            if (collision(pacman, food)) {
                toRemove.add(food);
                score += 10;
            }
        }
        foods.removeAll(toRemove);

        //check cherry collision
        for (Block cherry : cherrys) {
            if (collision(pacman, cherry)) {
                toRemove.add(cherry);
                score += 50;
                startGhostScaredTimer();
            }
        }
        cherrys.removeAll(toRemove);
    }

    public void startGhostScaredTimer() {
        if (!ghostScared) {
            ghostScared = true;
            ghostScaredTimer = new TimerTask() {
                @Override
                public void run() {
                    ghostScared = false;
                }
            };
            new Timer().schedule(ghostScaredTimer, 10000); // Scared for 10 seconds
        }
    }

    public boolean collision(Block b1, Block b2) {
        Rectangle r1 = new Rectangle(b1.x, b1.y, b1.width, b1.height);
        Rectangle r2 = new Rectangle(b2.x, b2.y, b2.width, b2.height);
        return r1.intersects(r2);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_UP:
                pacman.updateDirection('U');
                break;
            case KeyEvent.VK_DOWN:
                pacman.updateDirection('D');
                break;
            case KeyEvent.VK_LEFT:
                pacman.updateDirection('L');
                break;
            case KeyEvent.VK_RIGHT:
                pacman.updateDirection('R');
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}

