package pl.agh.edu.sr.lab3.carrier;

import com.rabbitmq.client.*;
import pl.agh.edu.sr.lab3.ChannelFactory;
import pl.agh.edu.sr.lab3.Order;
import pl.agh.edu.sr.lab3.administration.Administrator;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class Carrier implements AutoCloseable{
    private final Channel channel;
    private final OrderType firstService;
    private final OrderType secondService;
    private static final String ADMIN_QUEUE_KEY = "*.carrier";

    public static void main(String []args) throws IOException, TimeoutException {
        new Carrier(OrderType.CARGO, OrderType.PEOPLE).start();
    }

    public Carrier(OrderType firstService, OrderType secondService) throws IOException, TimeoutException {
        ChannelFactory channelFactory = new ChannelFactory();
        this.firstService = firstService;
        this.secondService = secondService;
        channel = channelFactory.getChannel();
    }

    public void start(){
        new Thread(()-> {
            try {
                initChannelAndStartListening(firstService);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(()-> {
            try {
                initChannelAndStartListening(secondService);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(()-> {
            try {
                administrativeQueueStartListening();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void initChannelAndStartListening(OrderType orderType) throws IOException {
        String QUEUE_NAME = ChannelFactory.getQueueName(orderType);
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                Order order = new Order(body);
                System.out.println("Received order: " + order);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
        channel.basicQos(1);
        System.out.println("Ready for " + QUEUE_NAME + " orders");
        channel.basicConsume(QUEUE_NAME, false, consumer);
    }

    private void administrativeQueueStartListening() throws IOException {
        channel.exchangeDeclare(Administrator.EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, Administrator.EXCHANGE_NAME, ADMIN_QUEUE_KEY);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        // start listening
        System.out.println("Waiting for admin messages...");
        channel.basicConsume(queueName, false, consumer);
    }

    @Override
    public void close() throws Exception {
        channel.getConnection().close();
        channel.close();
    }

}
