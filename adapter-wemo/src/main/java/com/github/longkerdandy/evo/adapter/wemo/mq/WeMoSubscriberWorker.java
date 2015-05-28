package com.github.longkerdandy.evo.adapter.wemo.mq;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.longkerdandy.evo.adapter.wemo.WeMoConst;
import com.github.longkerdandy.evo.adapter.wemo.handler.WeMoHandler;
import com.github.longkerdandy.evo.adapter.wemo.storage.WeMoRedisStorage;
import com.github.longkerdandy.evo.api.message.Action;
import com.github.longkerdandy.evo.api.message.Message;
import com.github.longkerdandy.evo.api.message.MessageFactory;
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

import static com.github.longkerdandy.evo.api.util.JsonUtils.ObjectMapper;

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
    public void onMessage(String channel, String message) {
        try {
            // parse json
            JavaType type = ObjectMapper.getTypeFactory().constructParametrizedType(Message.class, Message.class, JsonNode.class);
            Message<JsonNode> msg = ObjectMapper.readValue(message, type);
            // check device id not empty
            String deviceId = msg.getTo();
            if (StringUtils.isBlank(deviceId)) {
                logger.warn("Empty device id, message {} dropped", msg.getMsgId());
            } else {
                // check device model exist
                String deviceModel = this.storage.getDeviceAttr(deviceId, WeMoConst.ATTRIBUTE_MODEL);
                if (StringUtils.isBlank(deviceModel)) {
                    logger.warn("Unknown device {}, message {} dropped", deviceId, msg.getMsgId());
                } else {
                    // check matching handler
                    WeMoHandler handler = WeMoConst.getHandlerByModel(this.handlers, deviceModel);
                    if (handler == null) {
                        logger.warn("Unknown device model {}, message {} dropped", deviceModel, msg.getMsgId());
                    } else {
                        if (!"1".equals(this.storage.getDeviceConn(deviceId, Scheme.DEVICE_CONN_STATE))) {
                            logger.debug("Device offline, message {} cached", msg.getMsgId());
                        } else {
                            RemoteDevice device = this.upnpService.getRegistry().getRemoteDevice(
                                    // TODO: UDN
                                    new UDN(this.storage.getDeviceAttr(deviceId, WeMoConst.ATTRIBUTE_SERIAL_NUMBER)), false);
                            if (device == null) {
                                logger.error("Device {} not existed in UPnP service stack, message {} dropped", deviceId, msg.getMsgId());
                            } else {
                                switch (msg.getMsgType()) {
                                    case MessageType.ACTION:
                                        handler.executeActionMessage(device, MessageFactory.newMessage(msg, ObjectMapper.treeToValue(msg.getPayload(), Action.class)));
                                        break;
                                    default:
                                        logger.warn("Unexpected message type {}, message {} dropped", msg.getMsgType(), msg.getMsgId());
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Parse json message with error: {}", ExceptionUtils.getMessage(e));
        }
    }
}
