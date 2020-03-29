package pl.agh.edu.sr.lab3.spaceAgency;

import pl.agh.edu.sr.lab3.Order;
import pl.agh.edu.sr.lab3.carrier.OrderType;

import java.util.Scanner;

public class OrderCreator {
    private final String agencyName;

    public OrderCreator(String agencyName) {
        this.agencyName = agencyName;
    }

    public Order createOrder(String orderCommand){
        Scanner input = new Scanner(orderCommand);
        input.useDelimiter(" ");
        OrderType orderType = Order.getOrderType(input.next());
        String orderID = input.next();

        return new Order(agencyName, orderID, orderType);
    }
}
