package pl.agh.edu.sr.lab3;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import pl.agh.edu.sr.lab3.carrier.OrderType;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class ChannelFactory {

    public Channel getChannel() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();

        return connection.createChannel();
    }

    public static String getQueueName(OrderType orderType){
        switch (orderType){
            case PEOPLE:
                return "people";
            case SATELLITE:
                return "satellite";
            default:
                return "cargo";
        }
    }
}
