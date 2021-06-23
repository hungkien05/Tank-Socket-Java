package client.client;

import client.bean.*;
import client.protocol.MissileDeadMsg;
import client.protocol.TankDeadMsg;
import server.TankServer;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TankClient extends Frame {
    public static final int GAME_WIDTH = 800;
    public static final int GAME_HEIGHT = 600;
    private Image offScreenImage = null;
    Random rand = new Random();
    int ranNum = rand.nextInt(4)+1;
    private Tank myTank;
    private NetClient nc = new NetClient(this);
    private ConDialog dialog = new ConDialog();

    private List<Missile> missiles = new ArrayList<>();
    private List<Explode> explodes = new ArrayList<>();
    private List<Obstacle> obstacles = new ArrayList<Obstacle>();
    private List<Tank> tanks = new ArrayList<>();

    public static int missilesCount=0;
    public static int hitCount=0;

    @Override
    public void paint(Graphics g) {
        //g.drawString("missiles count:" + missiles.size(), 10, 50);
        //g.drawString("explodes count:" + explodes.size(), 10, 70);
        g.drawString("tanks  count:" + tanks.size(), 10, 90);

        for(int i = 0; i < missiles.size(); i++) {
            Missile m = missiles.get(i);
            if(m.hitTank(myTank)){
                TankDeadMsg msg = new TankDeadMsg(myTank.id);
                nc.send(msg);
                MissileDeadMsg mmsg = new MissileDeadMsg(m.getTankId(), m.getId(),true);
                nc.send(mmsg);
            }

            for (int j = 0; j < obstacles.size(); j++) {
                if (m.hitObstacle(obstacles.get(j))) {
                    MissileDeadMsg mmsg = new MissileDeadMsg(m.getTankId(), m.getId(),false);
                    nc.send(mmsg);
                }
            }
            m.draw(g);
        }
        for(int i = 0; i < explodes.size(); i++) {
            Explode e = explodes.get(i);
            e.draw(g);
        }
        for(int i = 0; i < obstacles.size(); i++) {
            Obstacle o = obstacles.get(i);
            o.draw(g);
        }

        for(int i = 0; i < tanks.size(); i++) {
            Tank t = tanks.get(i);
            t.draw(g);
        }
        myTank.draw(g);
        drawMenu(g);
    }
    static Font font = new Font("Verdana", Font.BOLD, 32);
    private static Toolkit tk = Toolkit.getDefaultToolkit();
    static Image tankImage = tk.getImage(Tank.class.getClassLoader().getResource("client/images/tank/broke.png"));
    static Image missileImage = tk.getImage(Tank.class.getClassLoader().getResource("client/images/tank/missile.png"));

    void drawMenu(Graphics g){
        g.setColor(Color.BLACK);
        g.fillRect(GAME_WIDTH,0,250,GAME_HEIGHT);
        g.drawImage(tankImage,850,275,null);
        g.setColor(Color.WHITE);
        g.setFont(font);
        g.drawString(hitCount+"",900,300);

        g.drawImage(missileImage,850,375,null);
        g.drawString(missilesCount+"",900,400);
    }

    @Override
    public void update(Graphics g) {
        if(offScreenImage == null) {
            offScreenImage = this.createImage(800+250, 600);
        }
        Graphics gOffScreen = offScreenImage.getGraphics();
        Color c = gOffScreen.getColor();
        gOffScreen.setColor(Color.GRAY); // background color
        gOffScreen.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        gOffScreen.setColor(c);
        paint(gOffScreen);
        g.drawImage(offScreenImage, 0, 0, null);
    }

    public void launchFrame() {
        this.setLocation(400, 200);
        this.setSize(GAME_WIDTH+250, GAME_HEIGHT);
        this.setTitle("TankWar");
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        this.setResizable(false);
        this.setBackground(Color.GREEN);  // background color

        this.addKeyListener(new KeyMonitor());

        this.setVisible(true);

        switch (ranNum) {
            case 1:
                myTank = new Tank(50, 50, true, Dir.STOP, this);
                break;
            case 2:
                myTank = new Tank(750, 50, true, Dir.STOP, this);
                break;
            case 3:
                myTank = new Tank(750, 550, true, Dir.STOP, this);
                break;
            default:
                myTank = new Tank(20, 550, true, Dir.STOP, this);
                break;
        }


        //adding Obstacle
        obstacles.add(new Obstacle(100,100,200,200));
        obstacles.add(new Obstacle(200,100,300,150));
        obstacles.add(new Obstacle(230,286,510,320));
        obstacles.add(new Obstacle(70,300,140,520));
        obstacles.add(new Obstacle(70,520,310,540));
        obstacles.add(new Obstacle(690,84,730,543));
        obstacles.add(new Obstacle(400,180,700,200));
        obstacles.add(new Obstacle(520,430,700,450));
        new Thread(new PaintThread()).start();

        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        TankClient tc = new TankClient();

        tc.launchFrame();
    }


    class PaintThread implements Runnable {

        public void run() {
            while(true) {
                repaint();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class KeyMonitor extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {
            myTank.keyReleased(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            myTank.keyPressed(e);
        }
    }

    class ConDialog extends Dialog{
        Button b = new Button("connect to server");
        TextField tfIP = new TextField("127.0.0.1", 15);
        TextField tfPort = new TextField("" + TankServer.TCP_PORT, 4);
        TextField tfMyUDPPort = new TextField("1000", 4);

        public ConDialog() {
            super(TankClient.this, true);
            this.setLayout(new FlowLayout());
            this.add(new Label("IP:"));
            this.add(tfIP);
            this.add(new Label("Port:"));
            this.add(tfPort);
            this.add(new Label("My UDP Port:"));
            this.add(tfMyUDPPort);
            this.add(b);
            this.setLocation(400, 400);
            this.pack();
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setVisible(false);
                }
            });
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String IP = tfIP.getText().trim();
                    int port = Integer.parseInt(tfPort.getText().trim());
                    int myUDPPort = Integer.parseInt(tfMyUDPPort.getText().trim());
                    nc.setUDP_PORT(myUDPPort);
                    nc.connect(IP, port);
                    setVisible(false);
                }
            });
        }
    }

    public List<Missile> getMissiles() {
        return missiles;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public void setMissiles(List<Missile> missiles) {
        this.missiles = missiles;
    }

    public List<Explode> getExplodes() {
        return explodes;
    }

    public void setExplodes(List<Explode> explodes) {
        this.explodes = explodes;
    }

    public List<Tank> getTanks() {
        return tanks;
    }

    public void setTanks(List<Tank> tanks) {
        this.tanks = tanks;
    }

    public Tank getMyTank() {
        return myTank;
    }

    public void setMyTank(Tank myTank) {
        this.myTank = myTank;
    }

    public NetClient getNc() {
        return nc;
    }

    public void setNc(NetClient nc) {
        this.nc = nc;
    }
}