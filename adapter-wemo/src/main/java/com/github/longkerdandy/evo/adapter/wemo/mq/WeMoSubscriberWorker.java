package com.github.longkerdandy.evo.adapter.wemo.mq;

import com.github.longkerdandy.evo.adapter.wemo.WeMoConst;
import com.github.longkerdandy.evo.adapter.wemo.handler.WeMoHandler;
import com.github.longkerdandy.evo.adapter.wemo.storage.WeMoRedisStorage;
import com.github.longkerdandy.evo.api.message.Message;
import com.github.longkerdandy.evo.api.protocol.MessageType;
import com.github.longkerdandy.evo.api.storage.Scheme;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.UDN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;
import java.util.List;

/**
 * Message Queue Subscriber for WeMo
 */
public class WeMoSubscriberWorker extends JedisPubSub {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(WeMoSubscriberWorker.class);

    private final UpnpService upnpService;              // UPnP service
    private final WeMoRedisStorage storage;             // Storage
    private final List<WeMoHandler> handlers;           // Handlers

    public WeMoSubscriberWorker(UpnpService upnpService, WeMoRedisStorage storage, List<WeMoHandler> handlers) {
        this.upnpService = upnpService;
        this.storage = storage;
        this.handlers = handlers;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(String channel, String message) {
        try {
            // parse json
            Message msg = Message.parseMessage(message);

            logger.info("Received message {} {} on topic {}", msg.getMsgType(), msg.getMsgId(), channel);

            String deviceId = msg.getTo();

            // check device model
            String deviceModel = this.storage.getDeviceAttr(deviceId, WeMoConst.ATTRIBUTE_MODEL);
            if (StringUtils.isBlank(deviceModel)) {
                logger.warn("Device {} not exist, message {} dropped", deviceId, msg.getMsgId());
                return;
            }

            // check handler
            WeMoHandler handler = WeMoConst.findHandlerByModel(this.handlers, deviceModel);

            // check device online
            if (!"1".equals(this.storage.getDeviceConn(deviceId, Scheme.DEVICE_CONN_STATE))) {
                logger.warn("Device offline, message {} dropped", msg.getMsgId());
                return;
            }

            // check UPnP registry
            RemoteDevice device = this.upnpService.getRegistry().getRemoteDevice(new UDN(this.storage.getDeviceAttr(deviceId, WeMoConst.ATTRIBUTE_UDN)), false);
            if (device == null) {
                logger.warn("Device {} not existed in UPnP service stack, message {} dropped", deviceId, msg.getMsgId());
                return;
            }

            // let handler deal with the message
            switch (msg.getMsgType()) {
                case MessageType.ACTION:
                    handler.executeActionMessage(device, msg);
                    break;
            }
        } catch (IOException e) {
            logger.warn("Parse json message with error: {}", ExceptionUtils.getMessage(e));
        }
    }
}
