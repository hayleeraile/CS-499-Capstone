package com.zybooks.projecttwo;

public class InventoryItemActivity {
    //sets the variables
    private final long id;
    private final String itemNumber;
    private final String location;
    private final int quantity;

    //sets all properties for each item in the inventory
    //for the grid display, only itemnumber, location and qty are needed
    public InventoryItemActivity(long id, String itemNumber, String description,
                         String location, int quantity, int minStock) {
        this.id = id;
        this.itemNumber = itemNumber;
        this.location = location;
        this.quantity = quantity;
    }

    //getters for each property
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

