package com.example.BuyBuddy.service;

import java.util.Arrays;

public class Product implements Comparable<Product> {
    String id;
    double price = -1;
    String name;
    String shop;
    String imageUrl;
    String[] shops;
    double[] prices;
    int imgIndex = -1;


    public Product(String name, String id, String imageUrl) {
        this.name = name;
        this.id = id;
        this.imageUrl = imageUrl;
    }

    public Product(String id, double price, String name, String shop, String imageUrl) {
        this.id = id;
        this.price = price;
        this.name = name;
        this.shop = shop;
        this.imageUrl = imageUrl;
    }

    public Product(String name, String id, String imageUrl, String shop, double price) {
        this.imageUrl = imageUrl;
        this.shop = shop;
        this.name = name;
        this.price = price;
        this.id = id;
    }

    @Override
    public String toString() {
        return
                id + '|' + price +
                        "|" + name + '|' + shop + '|' + imageUrl;
    }

    public static Product getProductFromString(String targetString) {//id|price|name|shop|imageUrl
        String id = null;
        double price = -1;
        String name = null;
        String shop = null;
        String imageUrl = null;
        int iteratorLeft = targetString.indexOf('|');
        int iteratorRight = 0;
        id = targetString.substring(0, iteratorLeft);
        int counter = 0;
        for (int i = iteratorLeft + 1; i < targetString.length(); i++) {
            if (i == targetString.length() - 1) {
                imageUrl = targetString.substring(iteratorRight + 1);
            } else if (targetString.charAt(i) == '|') {
                if (counter == 0) {
                    iteratorRight = i;
                    price = Double.parseDouble(targetString.substring(iteratorLeft + 1, iteratorRight));
                } else {
                    iteratorLeft = iteratorRight;
                    iteratorRight = i;
                    if (counter == 1) {
                        name = targetString.substring(iteratorLeft + 1, iteratorRight);
                    } else if (counter == 2) {
                        shop = targetString.substring(iteratorLeft + 1, iteratorRight);
                    } else if (counter == 3) {

                    }
                }
                counter++;
            }

        }


        return new Product(id, price, name, shop, imageUrl);
    }


    @Override
    public int compareTo(Product product1) {
        int i = 1;
        if (!(price < product1.price)) {
            i = -1;
        }
        return i;
    }
    public int compareTo1(Product product1) {
        int i = 1;
        if ((price < product1.price)) {
            i = -1;
        }
        return i;
    }
}
