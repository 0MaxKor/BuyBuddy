package com.example.BuyBuddy.service;

import com.example.BuyBuddy.database.Table;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GetCity extends Thread{
    TelegramBot telegramBot;
    long chatId;
    String targetText;
    Connection connection;
    Table table;
    WebDriver webDriver;

    public GetCity(TelegramBot telegramBot, long chatId, String targetText,Connection connection, Table table) {
        super();
        this.telegramBot = telegramBot;
        this.chatId = chatId;
        this.targetText = targetText;
        this.table = table;
        this.connection = connection;
        webDriver = new ChromeDriver();
        webDriver.manage().window().maximize();
    }
    public void run(){
        webDriver.manage().timeouts().implicitlyWait(7, TimeUnit.SECONDS);
        System.out.println("-------------------------------------------------------------2");
        webDriver.get("https://edadeal.ru/moskva/offers");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        WebElement element2 = webDriver.findElement(By.xpath("/html/body/div[2]/div/div[2]/section/div[1]/div/section/span[1]/span/button/span"));
        element2.click();
        WebElement textBoxCity = webDriver.findElement(By.xpath("/html/body/div[2]/div/div[2]/section/div[1]/div/div[2]/span/span/span/span[2]/input"));
        textBoxCity.clear();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        textBoxCity.sendKeys(targetText);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String text = "";
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Извините, но в таком городе невозможно осуществить поиск");
        try {


            WebElement result = webDriver.findElement(By.xpath("//*[@id=\"teleports\"]/div/div[2]/section/div[1]/div/div[4]/ul"));
            Document doc = Jsoup.parse(result.getAttribute("outerHTML"));
            webDriver.close();
            String[] cities = doc.select("li > button > div > div.b-geo-picker__locality-name.u3149432f6270a").eachText().toArray(new String[0]);
            String[] citiesInfo = doc.select("li > button > div > div.b-geo-picker__locality-region.u3149432f6270a").eachText().toArray(new String[0]);



            String[][] buttons = new String[cities.length][1];
            String[][] kbdata = new String[cities.length][1];
            String kbdata1 = "";
            for (int i = 0; i < cities.length; i++) {
                buttons[i]=new String[]{cities[i]+", "+citiesInfo[i]};
                kbdata[i]=new String[]{"bc_"+i+targetText};
                kbdata1+=(cities[i]+"|"+citiesInfo[i]+"&");
            }

            table.updateFast(connection,new String[]{"lastmessageargs"},new String[]{kbdata1},new String[]{"chatid"},new String[]{""+chatId});

            sendMessageWithButtons(chatId,"Список городов:",buttons,kbdata);



            sendMessage.setText(text);
        }catch (Exception e){
            webDriver.close();
            System.err.println(e.getMessage());
            sendMessage.setChatId(chatId);
            try {
                telegramBot.execute(sendMessage);
            } catch (TelegramApiException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
    public void sendMessageWithButtons(long chatId, String text, String[][] buttonsName, String[][] buttonsCallbackData) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        List<List<InlineKeyboardButton>> inlineKeyboardButtons = new ArrayList<>();
        for (int i = 0; i < buttonsName.length; i++) {
            List<InlineKeyboardButton> btns = new ArrayList<>();
            for (int j = 0; j < buttonsName[i].length; j++) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(buttonsName[i][j]);
                button.setCallbackData(buttonsCallbackData[i][j]);
                btns.add(button);
            }
            inlineKeyboardButtons.add(btns);
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(inlineKeyboardButtons);
        message.setReplyMarkup(inlineKeyboardMarkup);
        try {
            telegramBot.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
