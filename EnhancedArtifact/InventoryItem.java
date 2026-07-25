package com.zybooks.projecttwo;

public class InventoryItem {
    private final long id;
    private final String itemNumber;
    private final String description;
    private final String location;
    private final int quantity;
    private final int minimumStock;

    //added the description and minimum stock values for search/display and low-stock filter options
    public InventoryItem(long id, String itemNumber, String description,
                         String location, int quantity,int minimumStock) {
        this.id = id;
        this.itemNumber = itemNumber;
        this.description = description;
        this.location = location;
        this.quantity = quantity;
        this.minimumStock = minimumStock;
    }


    public long getId() {
        return id;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public String getDescription() { return description;}

    public String getLocation() {
        return location;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getMinimumStock() { return minimumStock; }
}

