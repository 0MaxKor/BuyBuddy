package com.example.BuyBuddy.service;

import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class PrintProducts extends Thread {
    TelegramBot telegramBot;
    Product[] products;
    long chatId;

    public PrintProducts(TelegramBot telegramBot, Product[] products, long chatId) {
        super();
        this.chatId = chatId;
        this.products = products;
        this.telegramBot = telegramBot;
    }

    public void run() {
        for (int i = 0; i < products.length; i++) {
            Product p = products[i];
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chatId);

            InputFile inputFile = new InputFile();
            inputFile.setMedia("https://"+p.imageUrl);

            sendPhoto.setPhoto(inputFile);
            String text;
            if (p.price == -1) {
                text = "\uD83D\uDD38" + p.name + "\n";
                for (int j = 0; j < p.prices.length; j++) {
                    text += ("\uD83D\uDD39" + p.shops[j] + ": " + p.prices[j] + " руб.\n");
                }
            } else {
                text = "\uD83D\uDD38" + p.name + "\n" + "\uD83D\uDD39Цена: " + p.price + " руб.\n" + "\uD83D\uDD3BМагазин: " + p.shop;

            }
            sendPhoto.setCaption(text);


            try {
                telegramBot.execute(sendPhoto);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
