package com.github.longkerdandy.evo.adapter.wemo.handler;

import com.github.longkerdandy.evo.adapter.wemo.WeMoConst;
import com.github.longkerdandy.evo.adapter.wemo.storage.WeMoRedisStorage;
import com.github.longkerdandy.evo.api.message.*;
import com.github.longkerdandy.evo.api.mq.Publisher;
import com.github.longkerdandy.evo.api.mq.Topics;
import com.github.longkerdandy.evo.api.protocol.Const;
import com.github.longkerdandy.evo.api.protocol.DeviceType;
import com.github.longkerdandy.evo.api.protocol.OverridePolicy;
import com.github.longkerdandy.evo.api.util.UuidUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * WeMo Switch Handler
 */
public class WeMoSwitchHandler implements WeMoHandler {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(WeMoSwitchHandler.class);

    private final UpnpService upnpService;              // UPnP service
    private final WeMoRedisStorage storage;             // Storage
    private final Publisher publisher;                  // Message queue publisher

    public WeMoSwitchHandler(UpnpService upnpService, WeMoRedisStorage storage, Publisher publisher) {
        this.upnpService = upnpService;
        this.storage = storage;
        this.publisher = publisher;
    }

    @Override
    public String getModel() {
        return WeMoConst.MODEL_SWITCH;
    }


    @Override
    public WeMoSubscriptionCallback getDeviceSubscription(String deviceId, Registry registry, RemoteDevice device) {
        // WeMo basic event service
        Service service = device.findService(new ServiceId("Belkin", "basicevent1"));

        return new WeMoSubscriptionCallback(deviceId, UuidUtils.shortUuid(), this.storage, service, 600) {
            @Override
            @SuppressWarnings("unchecked")
            protected void eventReceived(GENASubscription subscription) {
                String sequence = String.valueOf(subscription.getCurrentSequence().getValue());
                logger.info("Received event from WeMo Switch {} with sequence id {}", this.deviceId, sequence);

                // update device
                Map<String, StateVariableValue> values = subscription.getCurrentValues();
                String state = String.valueOf(values.get("BinaryState").getValue());
                logger.debug("WeMo Switch {} state now is {}", this.deviceId, state);

                // send message
                if (sequence.equals("0")) {
                    this.storage.updateDeviceAttr(this.deviceId, WeMoConst.ATTRIBUTE_SWITCH_STATE, state);
                    sendConnectMessage(this.deviceId);
                } else {
                    sendTriggerMessage(deviceId, state);
                    this.storage.updateDeviceAttr(this.deviceId, WeMoConst.ATTRIBUTE_SWITCH_STATE, state);
                }
            }
        };
    }

    @Override
    public void sendConnectMessage(String deviceId) {
        // attribute
        Map<String, String> currentAttr = storage.getDeviceAttr(deviceId);
        Map<String, Object> newAttr = new HashMap<>();
        newAttr.put(WeMoConst.ATTRIBUTE_NAME, currentAttr.get(WeMoConst.ATTRIBUTE_NAME));
        newAttr.put(WeMoConst.ATTRIBUTE_MANUFACTURER, currentAttr.get(WeMoConst.ATTRIBUTE_MANUFACTURER));
        newAttr.put(WeMoConst.ATTRIBUTE_MODEL, currentAttr.get(WeMoConst.ATTRIBUTE_MODEL));
        newAttr.put(WeMoConst.ATTRIBUTE_SWITCH_STATE, NumberUtils.toInt(currentAttr.get(WeMoConst.ATTRIBUTE_SWITCH_STATE)));
        // message
        Message<Connect> msg = MessageFactory.newConnectMessage(
                Const.PROTOCOL_TCP_1_0, DeviceType.DEVICE, deviceId, Const.PLATFORM_ID,
                null, null, null, OverridePolicy.REPLACE, newAttr);
        // push to mq
        publisher.sendMessage(Topics.CLOUD_ADAPTER, msg);
    }

    @Override
    public void sendDisconnectMessage(String deviceId) {
        // message
        Message<Disconnect> msg = MessageFactory.newDisconnectMessage(
                Const.PROTOCOL_TCP_1_0, DeviceType.DEVICE, deviceId, Const.PLATFORM_ID);
        // push to mq
        publisher.sendMessage(Topics.CLOUD_ADAPTER, msg);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void executeActionMessage(RemoteDevice device, Message<Action> msg) {
        // WeMo basic event service
        Service service = device.findService(new ServiceId("Belkin", "basicevent1"));

        // SetBinaryState action
        String state = String.valueOf(msg.getPayload().getAttributes().get(WeMoConst.ATTRIBUTE_SWITCH_STATE));
        ActionInvocation setBinaryStateInvocation = new ActionInvocation(service.getAction("SetBinaryState"));
        setBinaryStateInvocation.setInput("BinaryState", state);
        ActionCallback setBinaryStateCallback = new ActionCallback(setBinaryStateInvocation) {

            @Override
            public void success(ActionInvocation invocation) {
                logger.debug("Successful set WeMo Switch {} state to {}", msg.getTo(), state);
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                logger.debug("Failed to set WeMo Switch {} state: {}", msg.getTo(), defaultMsg);
            }
        };

        // execute action
        upnpService.getControlPoint().execute(setBinaryStateCallback);
    }

    public void sendTriggerMessage(String deviceId, String state) {
        // compare with current state in storage
        String currentState = this.storage.getDeviceAttr(deviceId, WeMoConst.ATTRIBUTE_SWITCH_STATE);
        if (!state.equals(currentState)) {
            // attribute
            Map<String, Object> attr = new HashMap<>();
            attr.put(WeMoConst.ATTRIBUTE_SWITCH_STATE, NumberUtils.toInt(state));
            // trigger id
            String triggerId = state.equals("1") ? WeMoConst.TRIGGER_SWITCH_ON : WeMoConst.TRIGGER_SWITCH_OFF;
            // message
            Message<Trigger> msg = MessageFactory.newTriggerMessage(
                    Const.PROTOCOL_TCP_1_0, DeviceType.DEVICE, deviceId, Const.PLATFORM_ID,
                    triggerId, OverridePolicy.UPDATE_IF_NEWER, attr);
            // push to mq
            this.publisher.sendMessage(Topics.CLOUD_ADAPTER, msg);
        }
    }
}
