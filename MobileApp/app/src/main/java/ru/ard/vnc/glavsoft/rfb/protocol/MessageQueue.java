package ru.ard.vnc.glavsoft.rfb.protocol;

import ru.ard.vnc.glavsoft.rfb.client.ClientToServerMessage;

import java.util.ArrayDeque;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {
//    private final BlockingQueue<ClientToServerMessage> queue = new LinkedBlockingQueue();
    private final Queue<ClientToServerMessage> queue = new ConcurrentLinkedQueue<>();

    public void put(ClientToServerMessage message) {
        this.queue.offer(message);
    }

    public ClientToServerMessage get() throws InterruptedException {
//        return this.queue.take();
        return this.queue.poll();
    }
}
