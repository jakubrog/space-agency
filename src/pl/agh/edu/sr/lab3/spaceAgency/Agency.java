package pl.agh.edu.sr.lab3.spaceAgency;

import com.rabbitmq.client.*;
import pl.agh.edu.sr.lab3.ChannelFactory;
import pl.agh.edu.sr.lab3.Order;
import pl.agh.edu.sr.lab3.administration.Administrator;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

//TODO: selecting agency name and refactor duplicated code

public class Agency implements Runnable{

    private final OrderSender orderSender;
    private static final String ADMIN_QUEUE_KEY = "agency.*";

    public Agency() throws IOException, TimeoutException {
          orderSender = new OrderSender();
    }

    public static void main(String []args) throws IOException, TimeoutException {
        new Thread(new Agency()).start();
    }

    @Override
    public void run(){
        new Thread(() -> {
            try {
                administrativeQueueStartListening();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        }).start();

        System.out.print("Enter agency name: ");
        Scanner input = new Scanner("SOME AGENCY");
        OrderCreator orderCreator = new OrderCreator("CO JEST KURWA");
        System.out.println("Commands:\nPEOPLE <orderID>\nSATELLITE <orderID>\nCARGO <orderID>\nquit\nAgency is running\n");
        while (true) {
            System.out.print("order> ");
            input = new Scanner(System.in);
            String command = input.nextLine();

            // break condition
            if (command.equals("quit")) {
                break;
            }
            Order order = orderCreator.createOrder(command);
            try {
                orderSender.send(order);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            orderSender.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void administrativeQueueStartListening() throws IOException, TimeoutException {
        Channel channel = new ChannelFactory().getChannel();

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

}
