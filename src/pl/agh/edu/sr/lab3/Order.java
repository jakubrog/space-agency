package pl.agh.edu.sr.lab3;

import pl.agh.edu.sr.lab3.carrier.OrderType;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class Order {
    private String orderID;
    private OrderType orderType;
    private String agencyName;

    public Order(String agencyName, String orderID, OrderType orderType) {
        this.orderID = orderID;
        this.orderType = orderType;
        this.agencyName = agencyName;
    }

    public Order(byte []bytes) throws UnsupportedEncodingException {
        String order = new String(bytes, "utf-8");
        Scanner scanner = new Scanner(order).useDelimiter(";");
        agencyName = scanner.next();
        orderID = scanner.next();
        orderType = Order.getOrderType(scanner.next());
    }

    public byte[] getBytes(){
        String result = agencyName + ";" + orderID + ";" + Order.getOrderType(orderType);
        return result.getBytes();
    }

    public String getOrderID() {
        return orderID;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public static OrderType getOrderType(String serviceType) {
        switch(serviceType.trim()){
            case "PEOPLE":
            case "P":
                return OrderType.PEOPLE;
            case "SATELLITE":
            case "S":
                return OrderType.SATELLITE;
            default:
                return OrderType.CARGO;
        }
    }

    private static String getOrderType(OrderType orderType) {
        switch(orderType){
            case PEOPLE:
                return "PEOPLE";
            case SATELLITE:
                return "SATELLITE";
            default:
                return "CARGO";
        }
    }
    @Override
    public String toString(){
        return agencyName + " - " + getOrderType(orderType) + " id: " + orderID;
    }
}
