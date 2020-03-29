package pl.agh.edu.sr.lab3.administration;

import com.rabbitmq.client.*;
import pl.agh.edu.sr.lab3.ChannelFactory;
import pl.agh.edu.sr.lab3.Order;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

//TODO: sending messages to carrier or/and space

public class Administrator {
    private final Channel channel;
    public final static String ADMIN_QUEUE_NAME = "ADMIN_QUEUE";
    public final static String EXCHANGE_NAME = "ADMIN_QUEUE";

    public static void main(String []args) throws IOException, TimeoutException, InterruptedException {
        new Administrator().startListening();
    }

    public Administrator() throws IOException, TimeoutException {
        this.channel = new ChannelFactory().getChannel();
    }

    public void startListening() throws InterruptedException {
        Thread t1 = new Thread(this::initChannelAndStartListening);
        t1.start();
        t1.join();
    }

    private void initChannelAndStartListening() {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                Order order = new Order(body);
                System.out.println("New order: " + order);
            }
        };
        try {
            channel.queueDeclare(ADMIN_QUEUE_NAME, false, false, false, null);
            channel.basicConsume(ADMIN_QUEUE_NAME, true, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

