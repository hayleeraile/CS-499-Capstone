package com.zybooks.projecttwo;

public class InventoryItem {
    private final long id;
    private final String itemNumber;
    private final String location;
    private final int quantity;

    public InventoryItem(long id, String itemNumber,
                         String location, int quantity) {
        this.id = id;
        this.itemNumber = itemNumber;
        this.location = location;
        this.quantity = quantity;
    }


    public long getId() {
        return id;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public String getLocation() {
        return location;
    }

    public int getQuantity() {
        return quantity;
    }
}

