package com.github.longkerdandy.evo.adapter.wemo;

import com.github.longkerdandy.evo.adapter.wemo.handler.WeMoHandler;
import com.github.longkerdandy.evo.adapter.wemo.handler.WeMoSwitchHandler;
import com.github.longkerdandy.evo.adapter.wemo.mq.WeMoSubscriberWorkerFactory;
import com.github.longkerdandy.evo.adapter.wemo.storage.WeMoRedisStorage;
import com.github.longkerdandy.evo.adapter.wemo.upnp.WeMoRegistryListener;
import com.github.longkerdandy.evo.adapter.wemo.upnp.WeMoUpnpServiceConfiguration;
import com.github.longkerdandy.evo.api.mq.Publisher;
import com.github.longkerdandy.evo.api.mq.Subscriber;
import com.github.longkerdandy.evo.api.mq.Topics;
import com.github.longkerdandy.evo.api.storage.Scheme;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.ServiceTypeHeader;
import org.fourthline.cling.model.types.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.*;

/**
 * Belkin WeMo adapter
 */
public class WeMoAdapter {

    // Adapter Information
    public static final String ID = "wemo";
    public static final String NAME = "Belkin WeMo Adapater";
    public static final String VERSION = "1.0";
    public static final String CALLBACK = Topics.DEVICE_ADAPTER(ID);
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(WeMoAdapter.class);

    public static void main(String[] args) throws Exception {
        // logger bridge
        // optionally remove existing handlers attached to j.u.l root logger
        SLF4JBridgeHandler.removeHandlersForRootLogger();  // (since SLF4J 1.6.5)
        // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
        // the initialization phase of your application
        SLF4JBridgeHandler.install();

        // redis storage
        WeMoRedisStorage storage = new WeMoRedisStorage();

        // register adapter
        Map<String, String> info = prepareAdapterInfo();
        storage.updateAdapter(ID, info);
        logger.info("Belkin WeMo adapter registered");

        // create message queue publisher
        Publisher publisher = new Publisher();
        logger.info("Message queue publisher started");

        // create Cling UPnP service
        UpnpService upnpService = new UpnpServiceImpl(new WeMoUpnpServiceConfiguration());
        logger.info("UPnP service started");

        // create WeMo handlers
        List<WeMoHandler> handlers = new ArrayList<>();
        handlers.add(new WeMoSwitchHandler(upnpService, storage, publisher));

        // create UPnP registry listener
        upnpService.getRegistry().addListener(new WeMoRegistryListener(upnpService, storage, handlers));

        // create message queue subscriber
        WeMoSubscriberWorkerFactory factory = new WeMoSubscriberWorkerFactory(upnpService, storage, handlers);
        Subscriber subscriber = new Subscriber();
        subscriber.subscribe(CALLBACK, factory);
        logger.info("Message queue subscriber started");

        // send a ssdp search for belkin wemo devices
        upnpService.getControlPoint().search(new ServiceTypeHeader(new ServiceType("Belkin", "basicevent", 1)));  // common
        logger.debug("Send SSDP search message for belkin wemo devices");

        // send scheduled ssdp search for belkin wemo devices
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                upnpService.getControlPoint().search(new ServiceTypeHeader(new ServiceType("Belkin", "basicevent", 1)));
                logger.debug("Send SSDP search message for belkin wemo devices");
            }
        };
        timer.scheduleAtFixedRate(task, 0, 60000);
        logger.debug("SSDP search timer task started");
    }

    /**
     * Prepare WeMo adapter information
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
