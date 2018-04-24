import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/** 
 * represent controller.
 * @version 1.0.
 */
public class Controller extends JPanel {
    private Aquarium tank;
    private Timer t;
    private Player player;
    /** 
     * JFrame.
     */
    public static JFrame f = new JFrame("Arkavquarium");
    /** 
     * prev time.
     */
    public static long prev = System.nanoTime();
    /** 
     * time.
     */
    public static long time = 1;
    /** 
     * constructor.
     */
    public Controller() {
       this.tank = new Aquarium();
       this.player = new Player();
       tank.addGuppy(new Guppy());
       tank.addGuppy(new Guppy());
    }
    /** 
     * buy egg.
     */
    public void buyEgg() {
        this.player.payEgg();
    }
    /**
     * buy guppy.
     */
    public void buyGuppy() {
        if(this.player.payGuppy()) {
            Guppy guppy = new Guppy();
            this.tank.addGuppy(guppy);
        }
    }
    /**
     * buy piranha.
     */
    public void buyPiranha() {
        if(this.player.payPiranha()) {
            Piranha piranha = new Piranha();
            this.tank.addPiranha(piranha);
        }
    }
    /**
     * buy food.
     * @param initPosition object Position of the food.
     */
    public void buyFood(Position initPosition) {
        if(this.player.payFood()) {
            FishFood food = new FishFood(initPosition);
            this.tank.addFishFood(food);
        }
    }

    /**
     * run.
     */
    public void run(){

        f.add(this);
        f.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                boolean getCoin = false;
                boolean buyegg = false;
                if(e.getButton() == MouseEvent.BUTTON1){
                    if(Math.abs(e.getX()-584) < 15 && Math.abs(e.getY()-72) < 15){
                        buyEgg();
                        buyegg = true;
                    }else{
                        for(int coinCount=0;coinCount<tank.getListOfCoin().getSize();coinCount++){
                            if(tank.getListOfCoin().get(coinCount).getCurrentPosition().calculateDistance(new Position(e.getX()-18,e.getY()-33))<15){
                                player.increaseMoney(tank.getListOfCoin().get(coinCount).getValue());
                                tank.removeCoin(tank.getListOfCoin().get(coinCount));
                                getCoin = true;
                            }
                        }
                    }
                    if(!getCoin && !buyegg){
                        buyFood(new Position(e.getX()-10, e.getY()-20));
                    }
                }
            }
        });

        f.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_F){
                    buyGuppy();
                } else if (e.getKeyCode() == KeyEvent.VK_P){
                    buyPiranha();
                }
            }
        });

        f.setPreferredSize(new Dimension(Aquarium.DEFAULT_WIDTH, Aquarium.DEFAULT_HEIGHT));
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        t = new Timer(20, new ActionListener () {
            public void actionPerformed(ActionEvent event) {
                repaint();
            }
        });
        t.start();


    }

    /**
     * animate guppy.
     */
    public void animateGuppy() {
        for (int gupCount = 0; gupCount < tank.getListOfGuppy().getSize(); gupCount++) {
            Guppy curr = tank.getListOfGuppy().get(gupCount);
            if (curr.getIsAlive()) {
                curr.changeMovingStatus(tank.getListOfFishFood());
                curr.move(time, tank.getListOfFishFood());
                curr.setCoinTime(curr.getCoinTime()-0.02);
                for (int foodCount = 0; foodCount < tank.getListOfFishFood().getSize(); foodCount++) {
                    if (curr.isHungry() && curr.getCurrentPosition().calculateDistance(tank.getListOfFishFood().get(foodCount).getCurrentPosition()) < 15) {
                        tank.removeFishFood(tank.getListOfFishFood().get(foodCount));
                        curr.eatFood();
                        break;
                    }
                }
                if (curr.getCoinTime() < 0 && curr.getSize() > 1) {
                    tank.addCoin(curr.extractCoin());
                }
                curr.changeIsAlive();
            } else {
                tank.removeGuppy(curr);
            }
        }
    }

    /**
     * animate piranha.
     */
    public void animatePiranha() {
        for (int pirCount = 0; pirCount < tank.getListOfPiranha().getSize(); pirCount++) {
            Piranha curr = tank.getListOfPiranha().get(pirCount);
            if (curr.getIsAlive()) {
                curr.changeMovingStatus(tank.getListOfGuppy());
                curr.move(time, tank.getListOfGuppy());
                for (int gupCount = 0; gupCount < tank.getListOfGuppy().getSize(); gupCount++) {
                    if (curr.isHungry() && curr.getCurrentPosition().calculateDistance(tank.getListOfGuppy().get(gupCount).getCurrentPosition()) < 15) {
                        tank.addCoin( curr.extractCoin(tank.getListOfGuppy().get(gupCount).getSize()) );
                        tank.removeGuppy(tank.getListOfGuppy().get(gupCount));
                        curr.eatFood();
                        break;
                    }
                }
                curr.changeIsAlive();
            } else {
                tank.removePiranha(curr);
            }
        }
    }

    /**
     * animate FishFood.
     */
    public void animateFishFood() {
        for (int foodCount = 0; foodCount < tank.getListOfFishFood().getSize(); foodCount++) {
            tank.getListOfFishFood().get(foodCount).moveDown(time);
            if (tank.getListOfFishFood().get(foodCount).isBottom()){
                tank.removeFishFood(tank.getListOfFishFood().get(foodCount));
            }
        }
    }

    /**
     * animate Coin.
     */
    public void animateCoin() {
        for (int coinCount = 0; coinCount < tank.getListOfCoin().getSize(); coinCount++) {
            tank.getListOfCoin().get(coinCount).moveDown(time);
        }
    }

    /**
     * animate Snail.
     */
    public void animateSnail(){
        tank.getSnail().changeMovingStatus(tank.getListOfCoin());
        tank.getSnail().move(time,tank.getListOfCoin());
        for(int coinCount=0;coinCount<tank.getListOfCoin().getSize();coinCount++){
            if(tank.getListOfCoin().get(coinCount).getCurrentPosition().calculateDistance(tank.getSnail().getCurrentPosition()) < 15){
                player.increaseMoney(tank.getListOfCoin().get(coinCount).getValue());
                tank.removeCoin(tank.getListOfCoin().get(coinCount));
            }
        }
    }

    /**
     * animate egg.
     * @param g graphic.
     * @param t time.
     */
    public void animateEgg(Graphics g, Toolkit t){
        if(player.getEgg()==0){
            g.drawImage(t.getImage("images/Egg_L1.png"),560,20,null);
        }else if(player.getEgg()==1){
            g.drawImage(t.getImage("images/Egg_L2.png"),560,20,null);
        }else{
            g.drawImage(t.getImage("images/Egg_L3.png"),560,20,null);

        }
    }


    /**
     * paint.
     * @param g graphic.
     */
    @Override
    public void paintComponent(Graphics g) {
        Toolkit t = Toolkit.getDefaultToolkit();
        if(!player.isWin()) {
            tank.draw(g, t, null);
            Font font = new Font("Gill Sans Ultra Bold", Font.BOLD, 20);
            g.setColor(Color.YELLOW);
            g.setFont(font);
            g.drawImage(t.getImage("images/Money.png"),40,20,null);
            g.drawString(Double.toString(player.getMoney()), 70, 48);

            for (int gupCount = 0; gupCount < tank.getListOfGuppy().getSize(); gupCount++) {
                tank.getListOfGuppy().get(gupCount).draw(g, t, this);
            }

            for (int pirCount = 0; pirCount < tank.getListOfPiranha().getSize(); pirCount++) {
                tank.getListOfPiranha().get(pirCount).draw(g, t, this);
            }

            for (int foodCount = 0; foodCount < tank.getListOfFishFood().getSize(); foodCount++) {
                tank.getListOfFishFood().get(foodCount).draw(g, t, this);
            }

            for (int coinCount = 0; coinCount < tank.getListOfCoin().getSize(); coinCount++) {
                tank.getListOfCoin().get(coinCount).draw(g, t, this);
            }
            tank.getSnail().draw(g, t, this);
            animateGuppy();
            animateCoin();
            animatePiranha();
            animateFishFood();
            animateSnail();
            animateEgg(g,t);
            if(player.isLose(tank)){
                g.drawImage(t.getImage("images/Lose.png"),Aquarium.DEFAULT_WIDTH/2 - 170,Aquarium.DEFAULT_HEIGHT/2 - 50,null);
            }
        }else if(player.isWin()){
            g.drawImage(t.getImage("images/Win.png"),Aquarium.DEFAULT_WIDTH/2 - 270,Aquarium.DEFAULT_HEIGHT/2 - 75,null);
        }

    }
}