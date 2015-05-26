package com.github.longkerdandy.evo.adapter.wemo.handler;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * WeMo Switch Handler
 */
public class WeMoSwitchHandler implements WeMoHandler {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(WeMoSwitchHandler.class);

    @Override
    public String getType() {
        return "controllee";
    }

    @Override
    public void deviceAdded(UpnpService upnpService, Registry registry, RemoteDevice device) {
        // subscribe GENA
        Service service = device.findService(new ServiceId("Belkin", "basicevent1"));
        WeMoSubscriptionCallback callback = new WeMoSubscriptionCallback(service);
        upnpService.getControlPoint().execute(callback);
    }

    @Override
    public void deviceUpdated(UpnpService upnpService, Registry registry, RemoteDevice device) {

    }

    @Override
    public void deviceRemoved(UpnpService upnpService, Registry registry, RemoteDevice device) {

    }

    public class WeMoSubscriptionCallback extends SubscriptionCallback {

        protected WeMoSubscriptionCallback(Service service) {
            super(service);
        }

        @Override
        protected void failed(GENASubscription subscription, UpnpResponse responseStatus, Exception exception, String defaultMsg) {
            logger.warn("WeMo Switch GENA subscribe failed: {}", defaultMsg);
        }

        @Override
        protected void established(GENASubscription subscription) {
            logger.debug("WeMo Switch GENA established with subscription id {}", subscription.getSubscriptionId());
        }

        @Override
        protected void ended(GENASubscription subscription, CancelReason reason, UpnpResponse responseStatus) {
            logger.debug("WeMo Switch GENA ended with reason: {}", reason);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void eventReceived(GENASubscription subscription) {
            logger.info("WeMo Switch received event with sequence id {}", subscription.getCurrentSequence().getValue());
            Map<String, StateVariableValue> values = subscription.getCurrentValues();
            StateVariableValue state = values.get("BinaryState");
            logger.debug("WeMo Switch state now is {}", subscription.getCurrentSequence().getValue());
        }

        @Override
        protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {
            logger.debug("WeMo Switch GENA missed {} events", numberOfMissedEvents);
        }
    }
}
