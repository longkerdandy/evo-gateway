package com.github.longkerdandy.evo.adapter.wemo.handler;

import com.github.longkerdandy.evo.adapter.wemo.WeMoConst;
import com.github.longkerdandy.evo.adapter.wemo.storage.WeMoRedisStorage;
import com.github.longkerdandy.evo.api.message.*;
import com.github.longkerdandy.evo.api.mq.Publisher;
import com.github.longkerdandy.evo.api.mq.Topics;
import com.github.longkerdandy.evo.api.protocol.DeviceType;
import com.github.longkerdandy.evo.api.protocol.Evolution;
import com.github.longkerdandy.evo.api.protocol.OverridePolicy;
import com.github.longkerdandy.evo.api.protocol.ProtocolType;
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
                logger.info("Received event from device (switch) {} with sequence id {}", this.deviceId, sequence);

                // update device
                Map<String, StateVariableValue> values = subscription.getCurrentValues();
                String state = String.valueOf(values.get("BinaryState").getValue());
                logger.debug("Device (switch) {} state now is {}", this.deviceId, state);

                // when subscription established
                if (sequence.equals("0")) {
                    // update switch state
                    this.storage.updateDeviceAttr(this.deviceId, WeMoConst.ATTRIBUTE_SWITCH_STATE, state);
                    // send connect message
                    sendConnectMessage(this.deviceId);
                }
                // after subscription established
                else {
                    // compare with current state in storage
                    String currentState = this.storage.getDeviceAttr(deviceId, WeMoConst.ATTRIBUTE_SWITCH_STATE);
                    if (!state.equals(currentState)) {
                        // update switch state
                        this.storage.updateDeviceAttr(this.deviceId, WeMoConst.ATTRIBUTE_SWITCH_STATE, state);
                        // compare and send trigger message
                        sendTriggerMessage(deviceId, state);
                    }
                }
            }
        };
    }

    /**
     * Forge and push Trigger Message to message queue
     *
     * @param deviceId Device Id
     * @param state    Switch state
     */
    public void sendTriggerMessage(String deviceId, String state) {
        // attribute
        Map<String, Object> attr = new HashMap<>();
        attr.put(WeMoConst.ATTRIBUTE_SWITCH_STATE, NumberUtils.toInt(state));
        // trigger id
        String triggerId = WeMoConst.TRIGGER_SWITCH_STATE_CHANGED;
        // message
        Message<Trigger> msg = MessageFactory.newTriggerMessage(
                ProtocolType.TCP_1_0, DeviceType.DEVICE,
                deviceId, Evolution.ID,
                triggerId, OverridePolicy.UPDATE_IF_NEWER, attr);
        // push to mq
        this.publisher.sendMessage(Topics.DEVICE_EVENT, msg);
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
                ProtocolType.TCP_1_0, DeviceType.DEVICE,
                deviceId, Evolution.ID, null,
                null, null,
                OverridePolicy.REPLACE, newAttr);
        // push to mq
        publisher.sendMessage(Topics.DEVICE_EVENT, msg);
    }

    @Override
    public void sendDisconnectMessage(String deviceId) {
        // message
        Message<Disconnect> msg = MessageFactory.newDisconnectMessage(
                ProtocolType.TCP_1_0, DeviceType.DEVICE,
                deviceId, Evolution.ID);
        // push to mq
        publisher.sendMessage(Topics.DEVICE_EVENT, msg);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void executeActionMessage(RemoteDevice device, Message<Action> msg) {
        Action action = msg.getPayload();

        // check action id
        if (!WeMoConst.ACTION_SWITCH_SET_STATE.equals(action.getActionId())) {
            logger.warn("Unknown action id {}, message {} dropped", action.getActionId(), msg.getMsgId());
            return;
        }

        // check state
        String state = String.valueOf(msg.getPayload().getAttributes().get(WeMoConst.ATTRIBUTE_SWITCH_STATE));
        if (!("1".equals(state) || "0".equals(state))) {
            logger.warn("Unknown state value {}, message {} dropped", state, msg.getMsgId());
            return;
        }

        // WeMo basic event service
        Service service = device.findService(new ServiceId("Belkin", "basicevent1"));

        // SetBinaryState action
        ActionInvocation setBinaryStateInvocation = new ActionInvocation(service.getAction("SetBinaryState"));
        setBinaryStateInvocation.setInput("BinaryState", state);
        ActionCallback setBinaryStateCallback = new ActionCallback(setBinaryStateInvocation) {

            @Override
            public void success(ActionInvocation invocation) {
                logger.info("Successful set device (switch) {} state to {}", msg.getTo(), state);
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                logger.info("Failed to set device (switch) {} state to {}: {}", msg.getTo(), state, defaultMsg);
            }
        };

        // execute action
        upnpService.getControlPoint().execute(setBinaryStateCallback);
    }
}
