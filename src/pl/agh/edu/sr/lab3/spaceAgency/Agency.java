package pl.agh.edu.sr.lab3.spaceAgency;

import com.rabbitmq.client.*;
import pl.agh.edu.sr.lab3.ChannelFactory;
import pl.agh.edu.sr.lab3.Order;
import pl.agh.edu.sr.lab3.administration.AdministrativeQueueListener;
import pl.agh.edu.sr.lab3.administration.Administrator;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;


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
        System.out.print("Enter agency name: ");
        Scanner input = new Scanner(System.in);
        OrderCreator orderCreator = new OrderCreator(input.next());
        try {
            new AdministrativeQueueListener().subscribeOn(ADMIN_QUEUE_KEY);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

        System.out.println("Commands:\nP <orderID> - People order\nS <orderID> - Satellite order\n" +
                "C <orderID> - Cargo order\nquit() - quit\n\n");

        while (true) {
            System.out.print("order> ");
            input = new Scanner(System.in);
            String command = input.nextLine().trim();

            if (command.equals("quit()")) {
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



}
