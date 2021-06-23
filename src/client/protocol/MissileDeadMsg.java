package client.protocol;

import client.bean.Explode;
import client.bean.Missile;
import client.client.TankClient;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class MissileDeadMsg implements Msg {
    private int msgType = Msg.MISSILE_DEAD_MESSAGE;
    private TankClient tc;
    private int tankId;
    private int id;
    private boolean hitTank;

    public MissileDeadMsg(int tankId, int id, boolean hitTank){
        this.tankId = tankId;
        this.id = id;
        this.hitTank=hitTank;
    }

    public MissileDeadMsg(TankClient tc){
        this.tc = tc;
    }

    @Override
    public void send(DatagramSocket ds, String IP, int UDP_Port) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(100);//指定大小, 免得字节数组扩容占用时间
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeInt(msgType);
            dos.writeInt(tankId);
            dos.writeInt(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] buf = baos.toByteArray();
        try{
            DatagramPacket dp = new DatagramPacket(buf, buf.length, new InetSocketAddress(IP, UDP_Port));
            ds.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void parse(DataInputStream dis) {
        try{
            int tankId = dis.readInt();
            int id = dis.readInt();
            for(Missile m : tc.getMissiles()){
                if(tankId == tc.getMyTank().id && id == m.getId()){
                    TankClient.hitCount++;
                    m.setLive(false);
                    tc.getExplodes().add(new Explode(m.getX(), m.getY(), tc));
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
