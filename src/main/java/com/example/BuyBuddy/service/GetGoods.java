package com.example.BuyBuddy.service;

import com.example.BuyBuddy.database.Table;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GetGoods extends Thread {
    TelegramBot telegramBot;
    Table users;
    Connection connection;
    String targetProduct;
    WebDriver webDriver;
    long chatId;
    String cityName;

    public GetGoods(TelegramBot telegramBot, String targetProduct, long chatId, String cityName) {
        super();
        this.telegramBot = telegramBot;
        this.targetProduct = targetProduct;
        users = telegramBot.tb_users;
        connection = telegramBot.sqlite_connection;
        this.webDriver = new ChromeDriver();
        webDriver.manage().window().maximize();
        this.chatId = chatId;
        this.cityName = cityName;
    }

    public void run() {
        webDriver.manage().timeouts().implicitlyWait(7, TimeUnit.SECONDS);


        webDriver.get("https://edadeal.ru/moscow/offers/search?keywords=" + targetProduct);
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {

            WebElement element2 = webDriver.findElement(By.xpath("/html/body/div[2]/div/div[2]/section/div[1]/div/section/span[1]/span/button/span"));
            element2.click();
            WebElement textBoxCity = webDriver.findElement(By.xpath("/html/body/div[2]/div/div[2]/section/div[1]/div/div[2]/span/span/span/span[2]/input"));
            textBoxCity.sendKeys(cityName);
        }catch (Exception e){

        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            WebElement cityButton = webDriver.findElement(By.xpath("/html/body/div[2]/div/div[2]/section/div[1]/div/div[4]/ul/li[1]/button"));
            cityButton.click();
        }catch (Exception e){

        }
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {

            WebElement yes = webDriver.findElement(By.xpath("/html/body/div[2]/div/div[2]/section/div[1]/div/section/span[2]/span/button"));
            yes.click();
        }catch (Exception e){

        }
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String ptext="";
        WebElement result = webDriver.findElement(By.xpath("/html/body/div[1]/div[2]/div[1]/div[3]/main/main/div[1]/div"));
        Document doc = Jsoup.parse(result.getAttribute("outerHTML"));

        if (doc.select("div[data-test-ref=\"emptyStub\"]").isEmpty()) {
            Elements elem = Jsoup.parse(webDriver.getPageSource()).select("#root-component > div.p-root__root-wrapper.page-wrapper.u11b99ae2e0e3b2 > div.p-root__view.u11b99ae2e0e3b2 > div.p-root__body.p-root__body_island_true.u11b99ae2e0e3b2 > main > main > div.p-dsk-offers-search__body.uf66b4d8a10406 > div.p-dsk-offers-search__content.uf66b4d8a10406 > div.i-block-helper.uea371e7ae384a.b-dsk-grid.b-dsk-grid_is-skeleton-visible_false.b-dsk-grid_progress_false.b-dsk-grid_theme_light.p-dsk-offers-search__grid.uf66b4d8a10406 > div.b-dsk-grid__main-content.uea371e7ae384a.p-dsk-offers-search__grid-container");
            String[] names = (elem.select("div[data-analytics-path=\"discountCard\"]>div>div.b-srch-card__content-container>div.b-srch-card__title")).eachText().toArray(new String[0]);
            Elements elems1 = elem.select("div[data-analytics-path=\"discountCard\"]");
            List<Product>unhandledProducts = new ArrayList<>();
            List<Product>handledProducts = new ArrayList<>();
            for (int i = 0; i < names.length; i++) {
                String productLink = elems1.get(i).select("a").eachAttr("href").get(0);
                String productName = elems1.get(i).select("div > div.b-srch-card__content-container > div.b-srch-card__title").eachText().get(0);
                String imageUrl = elems1.get(i).select(" > div > div.b-srch-card__image-container > span > img").eachAttr("src").get(0);
                Product product;
                if (!elems1.get(i).select("div > div.b-srch-card__content-container > div.b-srch-card__all-offers-wrapper").eachText().isEmpty()) {
                    product = new Product(productName,productLink,imageUrl.substring(8));
                    unhandledProducts.add(product);
                } else {
                    String shop = elems1.get(i).select("div > div.b-srch-card__content-container > div.b-srch-card__retailer").eachText().get(0);
                    String firstMoneyUnit1 = elems1.get(i).select("div > div.b-srch-card__content-container > div.b-srch-card__price > div.b-money_line-through_false> div > span.b-money__baseunit").eachText().get(0);
                    String firstMoneyUnit = "";
                    for (int j = 0; j < firstMoneyUnit1.length(); j++) {
                        if (firstMoneyUnit1.charAt(j)=='0'||firstMoneyUnit1.charAt(j)=='1'||firstMoneyUnit1.charAt(j)=='2'||firstMoneyUnit1.charAt(j)=='3'||firstMoneyUnit1.charAt(j)=='4'||firstMoneyUnit1.charAt(j)=='5'||firstMoneyUnit1.charAt(j)=='6'||firstMoneyUnit1.charAt(j)=='7'||firstMoneyUnit1.charAt(j)=='8'||firstMoneyUnit1.charAt(j)=='9'){
                            firstMoneyUnit+=firstMoneyUnit1.charAt(j);
                        }
                    }
                    System.out.println(firstMoneyUnit);
                    List<String> secondMoneyUnit2 = elems1.get(i).select("div > div.b-srch-card__content-container > div.b-srch-card__price > div.b-money_line-through_false> div > span.b-money__subunit").eachText();
                    String secondMoneyUnit = "";
                    if (!secondMoneyUnit2.isEmpty()) {
                        secondMoneyUnit = secondMoneyUnit2.get(0);
                    }
                    double price = Double.parseDouble(firstMoneyUnit + "." + secondMoneyUnit + "0");
                    product = new Product(productLink,price,productName,shop,imageUrl.substring(8));
                    handledProducts.add(product);
                    ptext+=(product.toString());
                    ptext+="#";
                    System.out.println(ptext);




                }
            }
            PrintProducts printHandledProducts = new PrintProducts(telegramBot,handledProducts.toArray(new Product[0]), chatId);
            printHandledProducts.start();


            for(int o = 0; o<unhandledProducts.size(); o++){
                Product unhandledProduct = unhandledProducts.get(o);
                webDriver.get("https://edadeal.ru"+unhandledProduct.id);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                WebElement webElement = webDriver.findElement(By.xpath("/html/body/div[1]/div[2]/div[1]/div[3]/main/main/div[1]/div[2]/div[1]/div/div[1]/div/div/div[2]/div/div/div"));
                //                                                                  "/html/body/div[1]/div[2]/div[1]/div[3]/main/main/div[1]/div[2]/div[1]/div/div[1]/div/div/div[2]/div/div/div"
                Document htmlDoc = Jsoup.parse(webElement.getAttribute("outerHTML"));
                String[]shops = htmlDoc.select("div>div>div.b-srch-price-offer__card-content>div>div>div.b-srch-price-offer__card-info-title>a").eachText().toArray(new String[0]);
                double[]prices = new double[shops.length];
                Elements elements = htmlDoc.select("div.u6334c1da39594");
                System.out.println(elements.eachText()+"-------------------------------------------------"+elements.size());
                for (int i = 0; i < shops.length; i++) {
                    System.out.println();
                    Element element = elements.get(i);

                    System.out.println(element.text()+"--------------1----------------");

                        Elements moneyUnit = element.select("div > div > div.b-srch-price-offer__card-content > div > div > div.b-srch-price-offer__card-info-price > div.b-srch-price-offer__card-info-price-new.i-block-helper> div");
                        if (moneyUnit.eachText().isEmpty()){
                          moneyUnit   = element.select("div > div > div.b-srch-price-offer__card-content > div > div > div.b-srch-price-offer__card-info-price > div.b-srch-price-offer__price-new.i-block-helper > div");

                        }
                        String firstMoneyUnit1 = moneyUnit.select("span.b-money__baseunit").eachText().get(0);
                        System.out.println(firstMoneyUnit1);
                        String firstMoneyUnit = "";
                        for (int j = 0; j < firstMoneyUnit1.length(); j++) {
                            if (firstMoneyUnit1.charAt(j)=='0'||firstMoneyUnit1.charAt(j)=='1'||firstMoneyUnit1.charAt(j)=='2'||firstMoneyUnit1.charAt(j)=='3'||firstMoneyUnit1.charAt(j)=='4'||firstMoneyUnit1.charAt(j)=='5'||firstMoneyUnit1.charAt(j)=='6'||firstMoneyUnit1.charAt(j)=='7'||firstMoneyUnit1.charAt(j)=='8'||firstMoneyUnit1.charAt(j)=='9'){
                                firstMoneyUnit+=firstMoneyUnit1.charAt(j);
                            }
                        }
                        System.out.println(firstMoneyUnit);
                        List<String> secondMoneyUnit2 = moneyUnit.select("span.b-money__subunit").eachText();
                        String secondMoneyUnit = "";
                        if (!secondMoneyUnit2.isEmpty()) {
                            secondMoneyUnit = secondMoneyUnit2.get(0);
                        }
                        prices[i]= Double.parseDouble(firstMoneyUnit + "." + secondMoneyUnit + "0");





                }
                unhandledProduct.shops=shops;
                unhandledProduct.prices=prices;
                unhandledProducts.set(o,unhandledProduct);

                if (telegramBot.tb_users.selectFast(telegramBot.sqlite_connection, new String[]{"sdata"},new String[]{"chatid"},new String[]{""+chatId}).get(0)[0]==null){
                    telegramBot.tb_users.insertFast(telegramBot.sqlite_connection, new String[]{"sdata"},new String[]{"10"});
                }


                for (int i = 0; i < prices.length; i++) {
                    ptext+=(new Product(unhandledProduct.name, unhandledProduct.id, unhandledProduct.imageUrl, shops[i], prices[i]).toString());
                    ptext+="#";
                }

            }
            PrintProducts printUnhandledProducts = new PrintProducts(telegramBot,unhandledProducts.toArray(new Product[0]), chatId);
            printUnhandledProducts.start();
            ptext.replace("\""," ");
            users.updateFast(connection,new String[]{"lastsearch"},new String[]{ptext.replace("\""," ").replace("\'"," ")},new String[]{"chatid"},new String[]{""+chatId});



        } else {
            SendMessage sendMessage = new SendMessage("" + chatId, "К сожалению, не нашлось таких товаров.");
            try {
                telegramBot.execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        updateMessageStatus(connection,chatId,"4","");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        webDriver.close();

    }
    public void updateMessageStatus(Connection connection, long chatId, String status, String statusArgs) {
        if (statusArgs != null) {
            users.updateFast(connection, new String[]{"lastmessage", "lastmessageargs"}, new String[]{status, statusArgs}, new String[]{"chatid"}, new String[]{chatId + ""});
        } else {
            users.updateFast(connection, new String[]{"lastmessage"}, new String[]{status}, new String[]{"chatid"}, new String[]{chatId + ""});
        }
    }

}
