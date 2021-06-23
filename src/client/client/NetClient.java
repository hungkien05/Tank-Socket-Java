package client.client;

import client.protocol.*;
import server.TankServer;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

public class NetClient {
    private String serverIP;
    private int serverUDPPort;
    private TankClient tc;
    private int UDP_PORT;
    private DatagramSocket ds = null;

    public void setUDP_PORT(int UDP_PORT) {
        this.UDP_PORT = UDP_PORT;
    }

    public NetClient(TankClient tc){
        this.tc = tc;
    }

    /**
     * @param ip server IP
     * @param port  server TCP port
     */
    public void connect(String ip, int port){ // bat dau giao thuc TCP de gui ip va udp port len server
        serverIP = ip;
        Socket s = null;
        try {
            ds = new DatagramSocket(UDP_PORT); // try to connect with UDP
            s = new Socket(ip, port); // // try to connect with TCP
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            dos.writeInt(UDP_PORT); //Send your own UDP port number to the server
            DataInputStream dis = new DataInputStream(s.getInputStream());
            int id = dis.readInt(); //Get the id number assigned to your tank by the server
            this.serverUDPPort = dis.readInt(); //Get the UDP port number of the server
            tc.getMyTank().id = id;
            tc.getMyTank().setGood((id & 1) == 0 ? true : false);
            //tc.getMyTank().setGood(false);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                if(s != null) s.close(); // hoan thanh trao doi thong tin, dong socket TCP lai
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        TankNewMsg msg = new TankNewMsg(tc.getMyTank());
        send(msg);

        new Thread(new UDPThread()).start();
    }

    public void send(Msg msg){
        msg.send(ds, serverIP, serverUDPPort);
    }

    public class UDPThread implements Runnable{

        byte[] buf = new byte[1024];

        @Override
        public void run() {
            while(null != ds){
                DatagramPacket dp = new DatagramPacket(buf, buf.length);  //tao packet dp chua buf
                try{
                    ds.receive(dp); // receive UDP packet
                    parse(dp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void parse(DatagramPacket dp) {
            ByteArrayInputStream bais = new ByteArrayInputStream(buf, 0, dp.getLength());
            DataInputStream dis = new DataInputStream(bais); // phan tich goi tin dp qua bais roi dua vao dis ( vi truoc do goi tin duoc goi duoi dang baos roi goi tiep duoi dang dos)
//            System.out.println("dp = "+dp.getData() +" - bais = "+bais);
            int msgType = 0;
            try {
                msgType = dis.readInt();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Msg msg = null;
            switch (msgType){
                case Msg.TANK_NEW_MSG :
                    msg = new TankNewMsg(tc); // lay TankNewMsg cho tc
                    msg.parse(dis); // xu ly goi tin TankNewMsg nay
                    break;
                case  Msg.TANK_MOVE_MSG :
                    msg = new TankMoveMsg(tc);
                    msg.parse(dis);
                    break;
                case Msg.MISSILE_NEW_MESSAGE :
                    msg = new MissileNewMsg(tc);
                    msg.parse(dis);
                    break;
                case Msg.TANK_DEAD_MESSAGE :
                    msg = new TankDeadMsg(tc);
                    msg.parse(dis);
                    break;
                case Msg.MISSILE_DEAD_MESSAGE :
                    msg = new MissileDeadMsg(tc);
                    msg.parse(dis);
                    break;
            }
        }
    }
}
