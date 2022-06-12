package server;

import common.Message;
import common.MessageType;
import common.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private ServerSocket ss = null;
    //创建一个集合，存放多个用户，只允许这些用户登录

    private static ConcurrentHashMap<String, User> validUsers = new ConcurrentHashMap<>();
    static {

        validUsers.put("100", new User("100", "123456"));
        validUsers.put("200", new User("200", "123456"));
        validUsers.put("300", new User("300", "123456"));
    }

    //验证用户是否有效的方法
    private boolean checkUser(String userId, String passwd) {

        User user = validUsers.get(userId);
        if(user == null) {
            return  false;
        }
        if(!user.getPasswd().equals(passwd)) {
            return false;
        }
        return  true;
    }

    public Server() {
        try {
            System.out.println("服务端在9999端口监听...");
            ss = new ServerSocket(9999);

            while (true) {
                Socket socket = ss.accept();
                ObjectInputStream ois =
                        new ObjectInputStream(socket.getInputStream());

                ObjectOutputStream oos =
                        new ObjectOutputStream(socket.getOutputStream());
                User u = (User) ois.readObject();//读取客户端发送的User对象
                Message message = new Message();
                if (checkUser(u.getUserId(), u.getPasswd())) {//登录通过
                    message.setMesType(MessageType.MESSAGE_LOGIN_SUCCEED);
                    oos.writeObject(message);
                    ServerConnectClientThread serverConnectClientThread =
                            new ServerConnectClientThread(socket, u.getUserId());
                    serverConnectClientThread.start();
                    ManageClientThreads.addClientThread(u.getUserId(), serverConnectClientThread);

                } else { // 登录失败
                    System.out.println("用户 id=" + u.getUserId() + " pwd=" + u.getPasswd() + " 验证失败");
                    message.setMesType(MessageType.MESSAGE_LOGIN_FAIL);
                    oos.writeObject(message);
                    //关闭socket
                    socket.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

