package com.ergasia_android_teliki;

public class Product {
    private final String title;
    private final double price;
    private int availability;
    private final String imageName;

    public Product(String title, double price, int availability, String imageName) {
        this.title = title;
        this.price = price;
        this.availability = availability;
        this.imageName = imageName;
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


}
