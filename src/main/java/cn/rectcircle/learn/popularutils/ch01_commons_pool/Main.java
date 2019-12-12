package cn.rectcircle.learn.popularutils.ch01_commons_pool;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Main
 */
public class Main {

    // 或者继承 org.apache.commons.pool2.BasePooledObjectFactory
    public static class RPCClientFactory implements PooledObjectFactory<RPCClient> {

        private String ip;
        private int port;

        public RPCClientFactory(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void destroyObject(PooledObject<RPCClient> p) throws Exception {
            p.getObject().close();
        }

        @Override
        public PooledObject<RPCClient> makeObject() throws Exception {
            // 构造器
            return new DefaultPooledObject<>(RPCClient.build(ip, port));
        }

        @Override
        public boolean validateObject(PooledObject<RPCClient> p) {
            // 验证对象是否有效
            return p.getObject().isConnected();
        }

        @Override
        public void activateObject(PooledObject<RPCClient> p) throws Exception {
            // 对象被取出 （objectPool.borrowObject 调用）时，执行的操作
            System.out.println("对象被取出，执行的操作");
        }

        @Override
        public void passivateObject(PooledObject<RPCClient> p) throws Exception {
            // 对象被取出 （objectPool.returnObject 调用）时，执行的操作
            System.out.println("对象被归还，执行的操作");
        }
    }

    public void test() {
        String ip = "127.0.0.1";
        int port = 5432;

        GenericObjectPoolConfig<RPCClient> config = new GenericObjectPoolConfig<>();
        // ===== 对象池大小相关配置 =====
        // 最大空闲数
        config.setMaxIdle(5);
        // 最小空闲数, 池中只有一个空闲对象的时候，池会在创建一个对象，并借出一个对象，从而保证池中最小空闲数为1
        config.setMinIdle(1);
        // 最大池对象总数
        config.setMaxTotal(20);
        // ===== 逐出相关配置 =====
        // 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
        config.setMinEvictableIdleTimeMillis(1800000);
        // 当idle池的大于MinIdle部分的对象的 逐出连接的最小空闲时间 默认-1，永久不会逐出
        config.setSoftMinEvictableIdleTimeMillis(1800000);
        // 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        config.setTimeBetweenEvictionRunsMillis(1800000 * 2L);
        // 每次逐出检查时 逐出的最大数目 默认3
        config.setNumTestsPerEvictionRun(3);
        // ===== 放逐配置 ======
        // ===== 有效性测试相关配置 =====
        // 在获取对象的时候检查有效性, 默认false
        config.setTestOnBorrow(true);
        // 在归还对象的时候检查有效性, 默认false
        config.setTestOnReturn(false);
        // 在空闲时检查有效性, 默认false
        config.setTestWhileIdle(false);
        // ====== 其他配置 =====
        // 最大等待时间， 默认的值为-1，表示无限等待。
        config.setMaxWaitMillis(5000);
        // 是否启用后进先出, 默认true
        config.setLifo(true);
        // 连接耗尽时是否阻塞, false报异常,true阻塞直到超时, 默认true
        config.setBlockWhenExhausted(true);
        ObjectPool<RPCClient> pool = new GenericObjectPool<>(new RPCClientFactory(ip, port), config);
        for (int i = 0; i < 20; i++) {
            if (i % 3 == 0) {
                try {
                    pool.clear();
                } catch (UnsupportedOperationException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            RPCClient client = null;
            try {
                client = pool.borrowObject();
                client.call("测试", null, void.class);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (client != null) {
                    try {
                        pool.returnObject(client);
                        System.out.println("---");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        pool.close(); // 关闭对象池
    }

    public static void main(String[] args) {
        new Main().test();
    }
}
