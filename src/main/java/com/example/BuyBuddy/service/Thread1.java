package com.example.BuyBuddy.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Thread1 extends Thread{
    TelegramBot telegramBot;
    public Thread1(TelegramBot bot){
        super();
        telegramBot = bot;
    }



    public void run(){
        for (int i = 0; i < 1000; i++) {
            try {
                Thread.sleep(1000);
                System.out.println("sdsdsdsdsdsdsd");
                SendMessage sendMessage = new SendMessage("1990414292","meme");
                telegramBot.execute(sendMessage);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
