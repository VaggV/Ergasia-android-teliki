package com.ergasia_android_teliki;

import androidx.annotation.Nullable;

public class Product {
    private final int id;
    private final String title;
    private final double price;
    private int availability;
    private final String imageName;

    public Product(String title, double price, int availability, String imageName, int id) {
        this.title = title;
        this.price = price;
        this.availability = availability;
        this.imageName = imageName;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    public int getAvailability() {
        return availability;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }

    public String getImageName() {
        return imageName;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Product other = (Product) obj;
        if (id != other.id)
            return false;
        if (!title.equals(other.title))
            return false;
        if (price != other.price)
            return false;
        if (availability != other.availability)
            return false;
        if (!imageName.equals(other.imageName))
            return false;

        return true;
    }
}
