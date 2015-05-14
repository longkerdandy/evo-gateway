package com.github.longkerdandy.evo.adapter.wemo.upnp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.schedulers.Schedulers;

/** Lightweight SSDP */
public class Ssdp {

    public static class Info {
        public final String serviceType;
        public final String location;
        public final String host;

        Info(final String serviceType, final String location) {
            this.serviceType = serviceType;
            this.location = location;
            host = getHost(location);
        }

        private static String getHost(String rawUrl) {
            try {
                final URL url = new URL(rawUrl);
                return url.getHost() + ":" + url.getPort();
            } catch (MalformedURLException e) {
                // shouldn't happen
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof Info
                    && ((Info) other).serviceType.equals(serviceType)
                    && ((Info) other).location.equals(location);
        }

        @Override
        public int hashCode() {
            return location.hashCode();
        }

        @Override
        public String toString() {
            return String.format("ST: %s\r\nLOCATION: %s\r\n", serviceType, location);
        }

        public static Info from(String packet) {
            String serviceType = null, location = null;
            String[] parts = packet.split("\r\n");
            for (String part : parts) {
                if (part.startsWith("LOCATION:"))
                    location = part.substring("LOCATION: ".length()).trim();
                else if (part.startsWith("ST:"))
                    serviceType = part.substring("ST: ".length()).trim();
            }
            return new Info(serviceType, location);
        }

    }

    private final static String DISCOVER_MESSAGE =
            "M-SEARCH * HTTP/1.1\r\n"
                    + "HOST: %s\r\n"
                    + "MAN: \"ssdp:discover\"\r\n"
                    + "MX: %d\r\n"
                    + "ST: %s\r\n\r\n";

    private static final String DEFAULT_HOST = "239.255.255.250";
    private static final int DEFAULT_PORT = 1900;
    private static final int DEFAULT_MX = 3;

    /**
     * How long to wait for packets while discovering.
     *  We use a relatively short loop in case the
     *  user has gotten what they came for already
     *  and doesn't care to discovery any more devices
     */
    private static final int DISCOVER_RECEIVE_TIMEOUT = 2000;

    /**
     * If it's been more than this many ms since we've
     *  discovered anything and we're still searching,
     *  send another Discover packet, in case whomever
     *  we're looking for just missed it
     */
    private static final long DISCOVER_RESEND_PERIOD = 1500;

    private Executor executor = Executors.newSingleThreadExecutor();
    private MulticastSocket socket;
    InetAddress multicastAddress;

    final int port;
    final String host;

    public Ssdp() throws IOException {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public Ssdp(String host, final int port) throws IOException {
        this.port = port;
        this.host = host + ":" + port;

        try {
            multicastAddress = InetAddress.getByName(host);
            socket = new MulticastSocket(port);
            // socket = new MulticastSocket();
            socket.setReuseAddress(true);
            socket.setSoTimeout(130000);
            socket.joinGroup(multicastAddress);
        } catch (UnknownHostException e) {
            // shouldn't happen...?
            e.printStackTrace();
        }
    }

    /**
     * Returns an Observable that emits Ssdp.Info objects
     *  for the given serviceType.
     *
     * I don't know if it's good practice or not, but
     *  the observable will do its subscriptions on a separate
     *  Scheduler by default, so if you're only interested
     *  in the first thing found, you can get it immediately
     *  without waiting for the timeout to expire
     */
    public Observable<Info> discover(final String serviceType,
                                     final long timeout) {
        final Observable<Info> observable =
                Observable.create((subscriber) -> {

                    final long timeup = System.currentTimeMillis() + timeout;
                    try {
                        long lastDiscovery = 0;
                        String packet = null;
                        do {
                            if (System.currentTimeMillis() - lastDiscovery
                                    > DISCOVER_RESEND_PERIOD) {
                                send(newDiscoverMessage(serviceType));

                                // prevent spam
                                lastDiscovery = System.currentTimeMillis();
                            }

                            if (subscriber.isUnsubscribed()) {
                                // quit early; they've left
                                return;
                            }

                            // receive
                            packet = receive(DISCOVER_RECEIVE_TIMEOUT);
                            if (packet == null || !packet.startsWith("HTTP")) {
                                // nothing received, or it was
                                //  the one we sent
                                Thread.yield();
                                continue;
                            }

                            // publish!
                            lastDiscovery = System.currentTimeMillis();
                            subscriber.onNext(Info.from(packet));
                        } while (System.currentTimeMillis() < timeup);

                        // time's up!
                        subscriber.onCompleted();
                    } catch (IOException e) {
                        // something went terribly wrong
                        e.printStackTrace();
                        subscriber.onError(e);
                    }
                });

        return observable.subscribeOn(Schedulers.from(executor))
                .distinct(); // we ping periodically, so ensure distinctness
    }

    void send(final String packet) throws IOException {
        try {
            final byte[] data = packet.getBytes("UTF-8");
            final DatagramPacket datagramPacket = new DatagramPacket(data,
                    data.length, multicastAddress, port);
            socket.setBroadcast(true);
            socket.send(datagramPacket);
        } catch (UnsupportedEncodingException e) {
            // the universe exploded!
            new RuntimeException(e);
        }
    }

    String receive(int timeout) throws IOException {
        try {
            final byte[] rxbuf = new byte[8192];
            final DatagramPacket packet = new DatagramPacket(rxbuf, rxbuf.length);
            socket.setSoTimeout(timeout);
            socket.receive(packet);
            return new String(rxbuf, 0, packet.getLength());
        } catch (SocketTimeoutException e) {
            return null;
        }
    }

    String newDiscoverMessage(final String serviceType) {
        return newDiscoverMessage(host, serviceType, DEFAULT_MX);
    }

    static String newDiscoverMessage(final String host,
                                     final String serviceType, final int mx) {
        return String.format(DISCOVER_MESSAGE, host, mx, serviceType);
    }
}