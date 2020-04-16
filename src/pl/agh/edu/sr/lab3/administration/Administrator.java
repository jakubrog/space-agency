package pl.agh.edu.sr.lab3.administration;

import com.rabbitmq.client.*;
import pl.agh.edu.sr.lab3.ChannelFactory;
import pl.agh.edu.sr.lab3.Order;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;


public class Administrator {
    private final Channel channel;
    private final Channel exchangeChannel;
    public final static String ADMIN_QUEUE_NAME = "ADMIN_QUEUE";
    public final static String EXCHANGE_NAME = "ADMIN_EXE_QUEUE";

    public static void main(String []args) throws IOException, TimeoutException, InterruptedException {
        new Administrator().startListening();
    }

    public Administrator() throws IOException, TimeoutException {
        this.channel = new ChannelFactory().getChannel();
        this.exchangeChannel = new ChannelFactory().getChannel();
        exchangeChannel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
    }

    public void startListening() throws InterruptedException {
        Thread t1 = new Thread(this::initChannelAndStartListening);
        Thread t2 = new Thread(this::messageSender);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
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
    private void messageSender() {
        System.out.println("Commands:\n" +
                "S <message body> - message to Space Agencies\n" +
                "C <message body> - message to Carriers\n" +
                "A <message body> - message to ALL\n" +
                "Q - quit");

        boolean run = true;
        String key = "";
        while(run){
            System.out.print(">");
            Scanner scanner = new Scanner(System.in).useDelimiter(" ");
            String command = scanner.next();
            String message = "";
            if(scanner.hasNext())
                message = scanner.nextLine();

            switch(command){
                case "S":
                    key = "agency.";
                    break;
                case "C":
                    key= ".carrier";
                    break;
                case "A":
                    key = "agency.carrier";
                    break;
                case "Q":
                    run = false;
                    break;
                default:
                    System.out.println("Wrong command");
                    break;
            }
            try {
                exchangeChannel.basicPublish(EXCHANGE_NAME, key, null, message.getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

