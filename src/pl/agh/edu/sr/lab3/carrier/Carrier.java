package pl.agh.edu.sr.lab3.carrier;

import com.rabbitmq.client.*;
import pl.agh.edu.sr.lab3.ChannelFactory;
import pl.agh.edu.sr.lab3.Order;
import pl.agh.edu.sr.lab3.administration.AdministrativeQueueListener;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Carrier {
    // PROCESSING_TIME - how long does it take to process each order, in milliseconds
    private final int PROCESSING_TIME = 1000;
    private final Channel channel;
    private final OrderType firstService;
    private final OrderType secondService;
    private static final String ADMIN_QUEUE_KEY = "*.carrier";

    public static void main(String []args) throws IOException, TimeoutException {

        System.out.print("Select two services:\nC - Cargo\nP - People\nS - Satellite\nWrite two letters without space," +
                " in ex. CP - cargo and people\n> ");
        String input = new Scanner(System.in).nextLine().trim();
        while(input.length() != 2){
            System.err.println("Wrong input, write two letters for two services, no more signs. Wrong input length.");
            System.out.print("> ");
            input = new Scanner(System.in).nextLine();
        }

        new Carrier(getService(input.charAt(0)), getService(input.charAt(1))).start();
    }
    private static OrderType getService(char input){
        switch(input){
            case 'P':
                return OrderType.PEOPLE;
            case 'S':
                return OrderType.SATELLITE;
            default:
                return OrderType.CARGO;
        }
    }

    public Carrier(OrderType firstService, OrderType secondService) throws IOException, TimeoutException {
        ChannelFactory channelFactory = new ChannelFactory();
        this.firstService = firstService;
        this.secondService = secondService;
        channel = channelFactory.getChannel();
    }

    public void start() throws IOException, TimeoutException {
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
        new AdministrativeQueueListener().subscribeOn(ADMIN_QUEUE_KEY);
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
                    Thread.sleep(PROCESSING_TIME);
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

}
