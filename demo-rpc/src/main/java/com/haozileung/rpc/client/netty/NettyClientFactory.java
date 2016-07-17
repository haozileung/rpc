package com.haozileung.rpc.client.netty;

import com.haozileung.infra.utils.PropUtils;
import com.haozileung.rpc.client.IClient;
import org.apache.commons.lang3.RandomUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class NettyClientFactory {

    private ConcurrentHashMap<Class, List<IClient>> serviceClientMap = new ConcurrentHashMap<>();

    public IClient get(Class<?> targetInterface) {
        List<IClient> clients = serviceClientMap.get(targetInterface);
        if (clients != null && clients.size() > 0) {
            return clients.get(RandomUtils.nextInt(0, clients.size()));
        }
        clients = new ArrayList<>();
        Properties p = PropUtils.INSTANCE.get("rpc.properties");
        if (p != null) {
            String url = p.getProperty(targetInterface.getName());
            String[] configs = url.split(",");
            for (String config : configs) {
                String[] conf = config.split(":");
                String host = conf[0];
                Integer port = Integer.valueOf(conf[1]);
                IClient client = new NettyClient(new InetSocketAddress(host, port));
                client.connect();
                clients.add(client);
            }
            List<IClient> oldClients = serviceClientMap.putIfAbsent(targetInterface, clients);
            if (oldClients != null) {
                clients = oldClients;
            }
            return clients.get(RandomUtils.nextInt(0, clients.size()));
        }
        return null;
    }

    public void close() {
        for (List<IClient> clients : serviceClientMap.values()) {
            clients.forEach(IClient::close);
        }
    }
}
