package pl.agh.edu.sr.lab3.administration;

import com.rabbitmq.client.*;
import pl.agh.edu.sr.lab3.ChannelFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class AdministrativeQueueListener {
    private final Channel channel;

    public AdministrativeQueueListener() throws IOException, TimeoutException {
        this.channel = new ChannelFactory().getChannel();
    }

    public void subscribeOn(String key){
        new Thread(() -> {
            try {
                administrativeQueueStartListening(key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void administrativeQueueStartListening(String key) throws IOException {
        channel.exchangeDeclare(Administrator.EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, Administrator.EXCHANGE_NAME, key);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received from admin: " + message);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
        channel.basicConsume(queueName, false, consumer);
    }
}
