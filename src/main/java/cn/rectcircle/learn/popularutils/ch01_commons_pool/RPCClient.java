package cn.rectcircle.learn.popularutils.ch01_commons_pool;

import java.util.Random;

/**
 * RPCClient：模拟一个RPC的客户端，存在生命周期： <br/>
 * 初始化、连接 -> 远程调用 -> 断连、销毁 <br/>
 * 初始化、连接过程比较昂贵，所以需要对象池进行缓存复用这个RPC链接 <br/>
 */
public class RPCClient {
    private String ip;
    private int port;
    private boolean connected = false;

    private RPCClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public static RPCClient build(String ip, int port) {
        RPCClient client = new RPCClient(ip, port);
        client.connect();
        return client;
    }

    // 模拟网络延迟
    private void netDelay() {
        try {
            Thread.sleep((new Random().nextInt(200)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 模拟连接
    private boolean connect() {
        this.netDelay();
        this.connected = (new Random()).nextDouble() >= 0.05; // 模拟连接失败
        System.out.println(String.format("连接到 %s:%d", ip, port));
        return this.connected;
    }

    // 模拟远程方法调用
    public <T> T call(String method, Object arg, Class<T> retType) {
        this.netDelay();
        System.out.println(String.format("调用 %s 方法", method));
        return (T) null;
    }

    // 模拟关闭连接
    public void close() {
        this.netDelay();
        System.out.println(String.format("从 %s:%d 断开连接", ip, port));
        this.connected = false;
    }

    public boolean isConnected() {
        return connected;
    }
}
