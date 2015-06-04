package com.github.longkerdandy.evo.adapter.evo;

import com.github.longkerdandy.evo.adapter.evo.mq.EvoSubscriberWorkerFactory;
import com.github.longkerdandy.evo.adapter.evo.storage.EvoRedisStorage;
import com.github.longkerdandy.evo.adapter.evo.tcp.TcpClient;
import com.github.longkerdandy.evo.adapter.evo.tcp.TcpClientHandler;
import com.github.longkerdandy.evo.api.mq.Publisher;
import com.github.longkerdandy.evo.api.mq.Subscriber;
import com.github.longkerdandy.evo.api.mq.Topics;
import com.github.longkerdandy.evo.api.storage.Scheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Evolution Platform Adapter
 */
public class EvoAdapter {

    // Adapter Information
    public static final String ID = "evo";
    public static final String NAME = "Evolution Platform Adapter";
    public static final String VERSION = "1.0";
    public static final String CALLBACK = Topics.DEVICE_ADAPTER(ID);
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(EvoAdapter.class);

    public static void main(String[] args) throws Exception {
        // redis storage
        EvoRedisStorage storage = new EvoRedisStorage();

        // register adapter
        Map<String, String> info = prepareAdapterInfo();
        storage.updateAdapter(ID, info);
        logger.info("Evolution Platform adapter registered");

        // create message queue publisher
        Publisher publisher = new Publisher();
        logger.info("Message queue publisher started");

        // create message queue subscriber
        EvoSubscriberWorkerFactory factory = new EvoSubscriberWorkerFactory();
        Subscriber subscriber = new Subscriber();
        subscriber.subscribe(new String[] {Topics.DEVICE_EVENT, CALLBACK}, factory);
        logger.info("Message queue subscriber started");

        // start tcp client
        TcpClient tcp = new TcpClient("", 0, new TcpClientHandler(storage, publisher));
        Thread thread = new Thread(tcp);
        thread.start();
    }

    /**
     * Prepare Evo adapter information
     */
    protected static Map<String, String> prepareAdapterInfo() {
        Map<String, String> map = new HashMap<>();
        map.put(Scheme.ADAPTER_ID, ID);
        map.put(Scheme.ADAPTER_NAME, NAME);
        map.put(Scheme.ADAPTER_VERSION, VERSION);
        map.put(Scheme.ADAPTER_CALLBACK, CALLBACK);
        return map;
    }
}
