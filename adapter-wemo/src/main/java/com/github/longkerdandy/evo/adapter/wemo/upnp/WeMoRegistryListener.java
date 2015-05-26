package com.github.longkerdandy.evo.adapter.wemo.upnp;

import com.github.longkerdandy.evo.adapter.wemo.handler.WeMoHandler;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.*;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * RegistryListener for WeMo Devices
 */
public class WeMoRegistryListener implements RegistryListener {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(WeMoRegistryListener.class);

    private final UpnpService upnpService;    // UPnP service
    private final List<WeMoHandler> handlers; // Handlers

    public WeMoRegistryListener(UpnpService upnpService, List<WeMoHandler> handlers) {
        this.upnpService = upnpService;
        this.handlers = handlers;
    }

    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
        System.out.println("Discovery started: " + device.getDisplayString());
    }

    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
        System.out.println("Discovery failed: " + device.getDisplayString() + " => " + ex);
    }

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        System.out.println("Remote device available: " + device.getDisplayString());
        if (device.getType().getType().equals("controllee")) {
            Service service = device.findService(new ServiceId("Belkin", "basicevent1"));

            Action getBinaryStateAction = service.getAction("GetBinaryState");
            ActionInvocation getBinaryStateInvocation = new ActionInvocation(getBinaryStateAction);
            ActionCallback getBinaryStateCallback = new ActionCallback(getBinaryStateInvocation) {

                @Override
                public void success(ActionInvocation invocation) {
                    ActionArgumentValue status  = invocation.getOutput("BinaryState");
                    System.out.println("WeMo Switch's state is : " + status.getValue());
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    System.err.println(defaultMsg);
                }
            };
            upnpService.getControlPoint().execute(getBinaryStateCallback);

//            Action setBinaryStateAction = service.getAction("SetBinaryState");
//            ActionInvocation setBinaryStateInvocation = new ActionInvocation(setBinaryStateAction);
//            setBinaryStateInvocation.setInput("BinaryState", "0");
//            ActionCallback setBinaryStateCallback = new ActionCallback(setBinaryStateInvocation) {
//
//                @Override
//                public void success(ActionInvocation invocation) {
//                    ActionArgumentValue[] output = invocation.getOutput();
//                    assert(output.length == 0);
//                }
//
//                @Override
//                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
//                    System.err.println(defaultMsg);
//                }
//            };
//            upnpService.getControlPoint().execute(setBinaryStateCallback);
        }
    }

    @Override
    public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
        System.out.println("Remote device updated: " + device.getDisplayString());
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        System.out.println("Remote device removed: " + device.getDisplayString());
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
        System.out.println("Local device added: " + device.getDisplayString());
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
        System.out.println("Local device removed: " + device.getDisplayString());
    }

    @Override
    public void beforeShutdown(Registry registry) {
        System.out.println("Before shutdown, the registry has devices: " + registry.getDevices().size());
    }

    @Override
    public void afterShutdown() {
        System.out.println("Shutdown of registry complete!");
    }
}
