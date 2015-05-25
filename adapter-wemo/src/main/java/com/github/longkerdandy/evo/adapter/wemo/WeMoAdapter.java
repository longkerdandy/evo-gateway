package com.github.longkerdandy.evo.adapter.wemo;

import com.github.longkerdandy.evo.adapter.wemo.storage.WeMoRedisStorage;
import com.github.longkerdandy.evo.adapter.wemo.upnp.WeMoRegistryListener;
import com.github.longkerdandy.evo.adapter.wemo.upnp.WeMoUpnpServiceConfiguration;
import com.github.longkerdandy.evo.api.mq.Topics;
import com.github.longkerdandy.evo.api.storage.Scheme;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.DeviceTypeHeader;
import org.fourthline.cling.model.message.header.ServiceTypeHeader;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;

import java.util.HashMap;
import java.util.Map;

/**
 * Belkin WeMo adapter
 */
public class WeMoAdapter {

    // Adapter Id
    public static final String ID = "wemo";
    public static final String NAME = "Belkin WeMo Adapater";
    public static final String VERSION = "1.0";
    public static final String CALLBACK = Topics.ADAPTER(ID);

    public static void main(String[] args) throws Exception {
        // redis storage
        WeMoRedisStorage storage = new WeMoRedisStorage();

        // register adapter
        Map<String, String> info = prepareAdapterInfo();
        storage.updateAdapter(ID, info);

        // This will create necessary network resources for UPnP right away
        System.out.println("Starting Cling...");
        UpnpService upnpService = new UpnpServiceImpl(new WeMoUpnpServiceConfiguration());
        upnpService.getRegistry().addListener(new WeMoRegistryListener(upnpService));

        // Send a search message to belkin wemo devices, they should respond soon
        upnpService.getControlPoint().search(new ServiceTypeHeader(new ServiceType("Belkin", "basicevent", 1)));  // common
        upnpService.getControlPoint().search(new DeviceTypeHeader(new DeviceType("Belkin", "controllee", 1)));  // switch
        upnpService.getControlPoint().search(new DeviceTypeHeader(new DeviceType("Belkin", "sensor", 1)));  // motion
    }

    protected static Map<String, String> prepareAdapterInfo() {
        Map<String, String> map = new HashMap<>();
        map.put(Scheme.ADAPTER_ID, ID);
        map.put(Scheme.ADAPTER_NAME, NAME);
        map.put(Scheme.ADAPTER_VERSION, VERSION);
        map.put(Scheme.ADAPTER_CALLBACK, CALLBACK);
        return map;
    }
}
