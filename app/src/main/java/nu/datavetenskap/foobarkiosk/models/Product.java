package nu.datavetenskap.foobarkiosk.models;

/**
 * Created by alexis on 2017-06-24.
 */

public class Product {
    String id;
    String name;
    String code;
    String description;
    Number price;
    boolean active;
    Number qty;
    String image;


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public Number getPrice() {
        return price;
    }

    public boolean isActive() {
        return active;
    }

    public Number getQty() {
        return qty;
    }


}