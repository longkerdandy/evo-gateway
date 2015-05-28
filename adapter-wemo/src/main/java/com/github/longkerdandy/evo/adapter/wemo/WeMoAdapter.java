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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Belkin WeMo adapter
 */
public class WeMoAdapter {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(WeMoAdapter.class);

    // Adapter Information
    public static final String ID = "wemo";
    public static final String NAME = "Belkin WeMo Adapater";
    public static final String VERSION = "1.0";
    public static final String CALLBACK = Topics.DEVICE_ADAPTER(ID);

    public static void main(String[] args) throws Exception {
        // redis storage
        WeMoRedisStorage storage = new WeMoRedisStorage();

        // register adapter
        Map<String, String> info = prepareAdapterInfo();
        storage.updateAdapter(ID, info);
        logger.info("Belkin WeMo adapter registered");

        // init message queue publisher & subscriber
        Publisher publisher = new Publisher();
        logger.info("Message queue publisher init completed");

        // create Cling UPnP service
        UpnpService upnpService = new UpnpServiceImpl(new WeMoUpnpServiceConfiguration());
        List<WeMoHandler> handlers = new ArrayList<>();
        handlers.add(new WeMoSwitchHandler(upnpService, storage, publisher));
        upnpService.getRegistry().addListener(new WeMoRegistryListener(upnpService, storage, handlers));
        logger.info("UPnP service started");

        WeMoSubscriberWorkerFactory factory = new WeMoSubscriberWorkerFactory(upnpService, storage, handlers);
        Subscriber subscriber = new Subscriber();
        subscriber.subscribe(CALLBACK, factory);
        logger.info("Message queue subscriber init completed");

        // send a ssdp search for belkin wemo devices
        upnpService.getControlPoint().search(new ServiceTypeHeader(new ServiceType("Belkin", "basicevent", 1)));  // common
        // upnpService.getControlPoint().search(new DeviceTypeHeader(new DeviceType("Belkin", "controllee", 1)));  // switch
        // upnpService.getControlPoint().search(new DeviceTypeHeader(new DeviceType("Belkin", "sensor", 1)));  // motion
        logger.debug("Send ssdp search message for belkin wemo devices");
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
