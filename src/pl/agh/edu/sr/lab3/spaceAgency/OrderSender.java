package pl.agh.edu.sr.lab3.spaceAgency;

import com.rabbitmq.client.Channel;
import pl.agh.edu.sr.lab3.ChannelFactory;
import pl.agh.edu.sr.lab3.Order;
import pl.agh.edu.sr.lab3.administration.Administrator;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class OrderSender {
    private ChannelFactory channelFactory;
    private Channel channel;

    public OrderSender() throws IOException, TimeoutException {
        this.channelFactory = new ChannelFactory();
        this.channel = channelFactory.getChannel();
    }

    public void send(Order order) throws IOException {
        channel.basicPublish("", ChannelFactory.getQueueName(order.getOrderType()), null, order.getBytes());
        channel.basicPublish("", Administrator.ADMIN_QUEUE_NAME, null, order.getBytes());
    }

    public void close() throws Exception {
        channel.getConnection().close();
        channel.close();
    }
}
