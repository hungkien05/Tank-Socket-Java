package client.bean;
import client.client.TankClient;
import client.bean.Tank;

import java.awt.*;

public class Obstacle {
    private int x1, y1,x2,y2;
    private boolean live = true;
    private TankClient tc;
    private int step = 0;
    public Obstacle(int x1, int y1, int x2, int y2){
        this.x1=x1;
        this.y1=y1;
        this.x2=x2;
        this.y2=y2;
    }

    public void blockTank(Tank t){
        //if(x + WIDTH > TankClient.GAME_WIDTH) x = TankClient.GAME_WIDTH - WIDTH;
        //if(y + HEIGHT > TankClient.GAME_HEIGHT) y = TankClient.GAME_HEIGHT - HEIGHT;
        int tx=t.getX();
        int ty=t.getY();
        System.out.println("tank(x,y)= "+tx+", "+ty);
        System.out.println("tankObs(x,y)= "+( tx+Tank.WIDTH)+", "+( ty+t.HEIGHT));
        if (tx + Tank.WIDTH>x1 && tx + 0*Tank.WIDTH<x2 && ty + Tank.HEIGHT >y1 && ty + 0*Tank.HEIGHT<y2) {
            if (tx - Tank.WIDTH< (x1+x2)/2) t.setX(x1 - Tank.WIDTH);
            else t.setX(x2 + Tank.WIDTH);
            if (ty - Tank.HEIGHT< (y1+y2)/2) t.setY(y1 -Tank.HEIGHT);
            else t.setY(y2 +Tank.HEIGHT);
        }
    }

    public boolean checkTank(Tank t){
        int tx=t.getX();
        int ty=t.getY();
        if (tx + Tank.WIDTH>x1 && tx + 0*Tank.WIDTH<x2 && ty + Tank.HEIGHT >y1 && ty + 0*Tank.HEIGHT<y2) {
            return true;
        }
        return false;
    }

    public void draw(Graphics g){
        if(!live) {
            tc.getObstacles().remove(this);
            return;
        }

        Color c = g.getColor();
        g.setColor(Color.BLUE);
        g.fillRect(x1,y1,x2-x1,y2-y1);
        g.setColor(c);

        //move();
    }

    public Rectangle getRect() {
        return new Rectangle(x1, y1, x2-x1, y2-y1);
    }
}
