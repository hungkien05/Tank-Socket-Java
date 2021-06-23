package client.protocol;

import client.bean.Dir;
import client.bean.Tank;
import client.client.TankClient;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class TankNewMsg implements Msg{
    private int msgType = Msg.TANK_NEW_MSG;
    private Tank tank;
    private TankClient tc;

    public TankNewMsg(Tank tank){
        this.tank = tank;
        this.tc = tc;
    }

    public TankNewMsg(TankClient tc){
        this.tc = tc;
        tank = tc.getMyTank();
    }

    public void send(DatagramSocket ds, String IP, int UDP_Port){
        ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
        DataOutputStream dos = new DataOutputStream(baos); //doi dinh dang goi tin gui di
        try {
            dos.writeInt(msgType);
            dos.writeInt(tank.id);
            dos.writeInt(tank.getX());
            dos.writeInt(tank.getY());
            dos.writeInt(tank.getDir().ordinal());
            dos.writeBoolean(tank.isGood());

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

    public void parse(DataInputStream dis){
        try{
            int id = dis.readInt();
            if(id == this.tc.getMyTank().id || !this.tank.isLive()){
                return;
            }

            int x = dis.readInt();
            int y = dis.readInt();
            Dir dir = Dir.values()[dis.readInt()];
            boolean good = dis.readBoolean();
            // vi tank moi khong co thong tin cua cac tank cu nen cac tank cu can send TankNewMsg den tank moi

            boolean exist = false; // neu tank da exist thi thoi ko can add, con chua exist thi add vao getTanks
            for (Tank t : tc.getTanks()){
                if(id == t.id){
                    exist = true;
                    break;
                }
            }
            if(!exist) { //exist =false => tank t la tank moi
                TankNewMsg msg = new TankNewMsg(tc);
                if (tc.getMyTank().isLive()) tc.getNc().send(msg); // gui msg ve su ton tai cua minh den tank moi
                Tank t = new Tank(x, y, good, dir, tc);
                t.id = id;
                tc.getTanks().add(t); // add new tank vao getTanks cua tank cu
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
