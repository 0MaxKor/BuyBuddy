package com.example.BuyBuddy.service;

import com.example.BuyBuddy.config.BotConfig;
import com.example.BuyBuddy.database.Database;
import com.example.BuyBuddy.database.Table;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.events.DTD;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


@Component
public class TelegramBot extends TelegramLongPollingBot {
    public String currentdbpath;

    String resourcesPath = "files/";
    Table tb_users;
    Table tb_requests;
    Table time;
    Database sqlitedatabase;
    ArrayList<long[]> ban;
    Connection sqlite_connection;
    Clock clock;
    // WebDriver webDriver;

    public boolean banOn = true;
    final BotConfig config;

    public TelegramBot(BotConfig botConfig) {


        config = botConfig;
        clock = Clock.systemDefaultZone();
        sqlitedatabase = new Database("botusers");
        time = new Table(sqlitedatabase, "time", "sqlite");
        tb_users = new Table(sqlitedatabase, "usersinfo", "sqlite");
        tb_requests = new Table(sqlitedatabase, "rqsts", "sqlite");
        List<BotCommand> menu = new ArrayList<>();
        menu.add(new BotCommand("/start", "Начать общение"));
        menu.add(new BotCommand("/city", "Ввести город для поиска"));
        menu.add(new BotCommand("/search", "Найти товар"));
        menu.add(new BotCommand("/profile", "Посмотреть профиль"));
        menu.add(new BotCommand("/help", "Написать в поддержку"));
        ban = new ArrayList<>();

        try {
            File directory = new File("/sqlite");
            directory.mkdirs();

            File file = new File(directory.getAbsolutePath() + "\\botusers.db");
            file.createNewFile();
            this.execute(new SetMyCommands(menu, new BotCommandScopeDefault(), null));
            currentdbpath = file.getAbsolutePath();
            sqlite_connection = DriverManager.getConnection("jdbc:sqlite:" + currentdbpath);
            updateTables(sqlite_connection);


        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } catch (TelegramApiException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        try {
            this.execute(new SetMyCommands(menu, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            System.out.println("exept");
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            long chatId = update.getMessage().getChatId();

            if (!banOn || !isBanned(chatId)) {
                long prevtime;
                long currentTime = clock.millis();
                ArrayList<String[]> timevals = time.selectFast(sqlite_connection, new String[]{"time", "spam"}, new String[]{"chatid"}, new String[]{"" + chatId});
                if (timevals.size() == 0) {
                    time.insertFast(sqlite_connection, new String[]{"chatid", "time", "spam"}, new String[]{chatId + "", currentTime + "", "0"});
                    prevtime = currentTime - 10000;
                    timevals = new ArrayList<>();
                    timevals.add(new String[]{prevtime + "", "0"});
                } else {
                    prevtime = Long.parseLong(timevals.get(0)[0]);

                }
                System.out.println(timevals.get(0).toString());
                if (banOn && Integer.parseInt(timevals.get(0)[1]) >= 30) {
                    sendMessage(chatId, "\uD83C\uDD98Вы заблокированы по причине \"спам\" на 30 сек\uD83C\uDD98");
                    ban.add(new long[]{chatId, (currentTime + 30000)});
                    time.updateFast(sqlite_connection, new String[]{"time", "spam"}, new String[]{clock.millis() + "", "0"}, new String[]{"chatid"}, new String[]{"" + chatId});

                } else {
                    if (currentTime - prevtime <= 1500) {
                        sendMessage(chatId, "⚠️Не так быстро⚠️");
                        time.updateFast(sqlite_connection, new String[]{"time", "spam    "}, new String[]{clock.millis() + "", (Integer.parseInt(timevals.get(0)[1]) + 1) + ""}, new String[]{"chatid"}, new String[]{"" + chatId});

                    } else {
                        ArrayList<String[]> uservalues = tb_users.selectFast(sqlite_connection, new String[]{"chatid", "lastmessage", "lastmessageargs", "city"}, new String[]{"chatid"}, new String[]{chatId + ""});


                        if (uservalues.isEmpty()) {
                            tb_users.insertFast(sqlite_connection, new String[]{"chatid", "lastmessage", "lastmessageargs"}, new String[]{chatId + "", "0", "noargs"});
                            uservalues = tb_users.selectFast(sqlite_connection, new String[]{"chatid", "lastmessage", "lastmessageargs"}, new String[]{"chatid"}, new String[]{chatId + ""});
                        }
                        int messageIndicator = Integer.parseInt(uservalues.get(0)[1]);

                        if (update.getMessage().hasText()) {
                            String messageText = update.getMessage().getText();

                            if (messageIndicator == 0) {

                                if (messageText.equals("/start")) {

                                    makeKeyboard(chatId, new String[][]{{"\uD83D\uDCCDВыбрать город\uD83D\uDCCD", "\uD83D\uDD0EНайти товар\uD83D\uDD0E"}, {"\uD83D\uDD38Написать в поддержку\uD83D\uDD38"}, {"\uD83D\uDC41Посмотреть профиль\uD83D\uDC41"}}, "\uD83D\uDD38Приветствую! \uD83D\uDD38\n" +
                                            "\uD83D\uDD3BЭтот бот поможет вам найти нужные товары и скажет, в каком магазине они находятся\n" +
                                            "\uD83D\uDD3BДавайте начнем поиск! Для начала выберите ваш город: /city. \nВыбраный город можно будет посмотреть в профиле");
                                } else if (messageText.equals("/cc")) {


                                } else if (messageText.equals("/help") || messageText.equals("\uD83D\uDD38Написать в поддержку\uD83D\uDD38")) {

                                    sendMessage(chatId, "\uD83D\uDE15Я не понимаю вас, " + update.getMessage().getChat().getFirstName() + "\uD83D\uDE15");
                                } else if (messageText.equals("/city") || messageText.equals("\uD83D\uDCCDВыбрать город\uD83D\uDCCD")) {

                                    sendMessage(chatId, "Введите город, в котором будет осуществляться поиск товаров");
                                    updateMessageStatus(sqlite_connection, chatId, "1", "0");

                                } else if (messageText.equals("/search") || messageText.equals("\uD83D\uDD0EНайти товар\uD83D\uDD0E")) {
                                    String city = tb_users.selectFast(sqlite_connection, new String[]{"city"}, new String[]{"chatid"}, new String[]{"" + chatId}).get(0)[0];
                                    if (city == null || city.isEmpty()) {
                                        sendMessage(chatId, "Сначала выберите город, в котором осуществлять поиск: \n /city");
                                    } else {
                                        sendMessage(chatId, "Введите название товара, который нужно искать");
                                        updateMessageStatus(sqlite_connection, chatId, "2", city);
                                    }

                                } else if (messageText.equals("/profile") || messageText.equals("\uD83D\uDC41Посмотреть профиль\uD83D\uDC41")) {
                                    String cityString = "";
                                    try {
                                        cityString = uservalues.get(0)[3].replace("|", ", ");
                                    } catch (Exception ignored) {

                                    }


                                    String profileText = "\uD83D\uDD3BИмя: " + update.getMessage().getChat().getFirstName() + "\n\uD83D\uDD39id: " + chatId + "\n" +
                                            "\uD83D\uDD39Город (село, деревня): " + cityString;
                                    if (cityString.isEmpty()) {
                                        profileText += "не выбран";
                                    }
                                    sendMessageWithButtons(chatId, profileText, new String[][]{{"Результаты последнего поиска"}}, new String[][]{{"последний поиск"}});
                                } else if (messageText.equals("/filter")) {
                                    tb_users.updateFast(sqlite_connection,new String[]{"sdata"},new String[]{"10"},new String[]{"chatid"},new String[]{""+chatId});
                                    sendMessageWithButtons(chatId, "Выберите фильтр", new String[][]{{"По возрастанию цены"}, {"По убыванию цены"}, {"Выбрать конкретные магазины"}, {"Отсортировать"}}, new String[][]{{"btn_costhigh"}, {"btn_costlow"}, {"btn_shops"}, {"btn_sort"}});
                                } else if (validateImg(messageText)) {
                                    String productData = tb_users.selectFast(sqlite_connection, new String[]{"lastsearch"}, new String[]{"chatid"}, new String[]{"" + chatId}).get(0)[0];
                                    if (productData == null || productData.isEmpty()) {
                                        sendMessage(chatId, "Ошибка");
                                    } else {
                                        String nums = "1234567890";
                                        int numberOfProduct = 0;
                                        for (int i = 0; i < messageText.length(); i++) {
                                            if (nums.contains(messageText.charAt(i) + "")) {
                                                numberOfProduct = Integer.parseInt(messageText.substring(i));
                                                break;
                                            }
                                        }
                                        List<Product> products = new ArrayList<>();
                                        int iteratorRight = productData.indexOf("#");
                                        System.out.println("productData:         --       " + productData);
                                        System.out.println("index: --- " + iteratorRight);
                                        int iteratorLeft = 0;
                                        products.add(Product.getProductFromString(productData.substring(0, iteratorRight)));
                                        for (int i = iteratorRight + 1; i < productData.length(); i++) {
                                            if (productData.charAt(i) == '#') {
                                                iteratorLeft = iteratorRight;
                                                iteratorRight = i;
                                                products.add(Product.getProductFromString(productData.substring(iteratorLeft + 1, iteratorRight)));
                                                System.out.println(Product.getProductFromString(productData.substring(iteratorLeft + 1, iteratorRight)));

                                            }

                                        }
                                        try {
                                            Product target = products.get(numberOfProduct);
                                            PrintProducts printProducts = new PrintProducts(this, new Product[]{target}, chatId);
                                            printProducts.start();
                                        } catch (Exception exception) {
                                            sendMessage(chatId, "Ошибка");
                                        }
                                    }

                                } else {
                                    sendMessage(chatId, "Я не понимаю вас, " + update.getMessage().getChat().getFirstName());
                                }


                            } else if (messageIndicator == 1) { // выбрать город
                                if (validateName(messageText.toLowerCase())) {
                                    updateMessageStatus(sqlite_connection, chatId, "0", "0");
                                    sendMessage(chatId, "Выберите один из городов из списка, который появится ниже");
                                    GetCity getCity = new GetCity(this, chatId, messageText, sqlite_connection, tb_users);
                                    getCity.start();
                                } else {
                                    sendMessage(chatId, "Некорректное название.\nУкажите имя города, используя только буквы a-z и а-я");
                                }


                            } else if (messageIndicator == 2) { // поиск товара, который ввел пользователь
                                if (validateName(messageText.toLowerCase())) {
                                    sendMessage(chatId, "Поиск запущен");
                                    String line = tb_users.selectFast(sqlite_connection, new String[]{"city"}, new String[]{"chatid"}, new String[]{"" + chatId}).get(0)[0];
                                    System.out.println(line);
                                    int rindex = line.indexOf('|');
                                    String cname = line.substring(0, rindex) + " " + line.substring(rindex + 1);
                                    updateMessageStatus(sqlite_connection, chatId, "3", "");
                                    GetGoods getGoods = new GetGoods(this, messageText.toLowerCase(), chatId, cname);
                                    getGoods.start();
                                } else {

                                    sendMessage(chatId, "Некорректное название\nВведите название, содержащее только буквы");

                                }
                            } else if (messageIndicator == 3) {// пока идет поиск
                                sendMessage(chatId, "Дождитесь завершения поиска");
                            } else if (messageIndicator == 4) {//когда поиск завершился
                                if (messageText.equals("/finish")) {
                                    sendMessage(chatId, "Поиск завершен");
                                    updateMessageStatus(sqlite_connection, chatId, "0", "");
                                } else if (messageText.equals("/filter")) {

                                    sendMessageWithButtons(chatId, "Выберите фильтр", new String[][]{{"По возрастанию цены"}, {"По убыванию цены"}, {"Выбрать конкретные магазины"}, {"Отсортировать"}}, new String[][]{{"btn_costhigh"}, {"btn_costlow"}, {"btn_shops"}, {"btn_sort"}});
                                    tb_users.updateFast(sqlite_connection,new String[]{"sdata"},new String[]{"10"},new String[]{"chatid"},new String[]{""+chatId});


                                } else {
                                    sendMessage(chatId, "Завершите поиск /finish или примените фильтры к найденным товарам /filter");
                                    updateMessageStatus(sqlite_connection, chatId, "0", "");

                                }
                            }


                        }
                        time.updateFast(sqlite_connection, new String[]{"time", "spam"}, new String[]{clock.millis() + "", "0"}, new String[]{"chatid"}, new String[]{"" + chatId});

                    }
                }


            }
            if (banOn) updateBanList();


        } else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callbackData = update.getCallbackQuery().getData();
            if (callbackData.equals("последний поиск")) {
                List<Product> products = getRecentProducts(chatId);
                if (products == null || products.isEmpty()) {
                    sendMessage(chatId, "Нет данных");
                } else {
                    String text = "";
                    int counter = 0;
                    for (Product p : products) {

                        text += ("\uD83D\uDD37" + p.name + "\n➖Цена: " + ("" + p.price) + " руб.\n➖Магазин: " + p.shop + "\n" + "➖Изображение товара: /img" + generateCharSequence() + ("" + counter) + "\n");
                        counter++;
                    }
                    sendMessage(chatId, text);
                }


            } else if (callbackData.equals("btn_costhigh")) {

                String line = tb_users.selectFast(sqlite_connection, new String[]{"sdata"}, new String[]{"chatid"}, new String[]{"" + chatId}).get(0)[0];
                if (line==null){
                    line = "10";
                }
                ManageFilters manageFilters = new ManageFilters(line);
                manageFilters.setHighCostSorting();
                manageFilters.send(this,chatId);
                sendMessage(chatId, "Выбран фильтр \"Сортировать по возрастанию цены\"");
            } else if (callbackData.equals("btn_costlow")) {

                String line = tb_users.selectFast(sqlite_connection, new String[]{"sdata"}, new String[]{"chatid"}, new String[]{"" + chatId}).get(0)[0];
                if (line==null){
                    line = "10";
                }
                ManageFilters manageFilters = new ManageFilters(line);
                manageFilters.setLowCostSorting();
                manageFilters.send(this,chatId);
                sendMessage(chatId, "Выбран фильтр \"Сортировать по убыванию цены\"");
            } else if (callbackData.equals("btn_shops")) {
                Product[] products = getRecentProducts(chatId).toArray(new Product[0]);
                List<String> shops = new ArrayList<>();
                for (int i = 0; i < products.length; i++) {
                    String s = products[i].shop;
                    if (!shops.contains(s)) {
                        shops.add(s);
                    }
                }

                int size = shops.size();
                String[][] snames = new String[size + 1][1], sdata = new String[size + 1][1];
                for (int i = 0; i < size; i++) {
                    snames[i][0] = shops.get(i);
                    sdata[i][0] = "btn_shop_" + shops.get(i);
                }
                snames[size] = new String[]{"✅Готово✅"};
                sdata[size] = new String[]{"btn_confirm"};

                sendMessageWithButtons(chatId, "Выберите магазины", snames, sdata);


            } else if (callbackData.equals("btn_sort")) {
                String line = tb_users.selectFast(sqlite_connection, new String[]{"sdata"}, new String[]{"chatid"}, new String[]{"" + chatId}).get(0)[0];
                if (line==null||line.isEmpty()||line.charAt(0)!='0'&&   line.charAt(0)!='1') {
                    sendMessage(chatId, "Пожалуйста, выберите какой-нибудь из фильтров");
                } else {
                    ManageFilters manageFilters = new ManageFilters(line);
                    EditMessageText editMessageText = new EditMessageText();
                    editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                    editMessageText.setChatId(chatId);
                    editMessageText.setText("Список продуктов появится ниже");
                    try {
                        this.execute(editMessageText);
                    } catch (TelegramApiException e) {
                        System.err.println(e.getMessage());
                    }
                    List<String> shops = manageFilters.getShops();
                    boolean islow = manageFilters.isLow();
                    List<Product> products = getRecentProducts(chatId);

                    List<Product>newProducts = new ArrayList<>();
                    if (shops==null||shops.isEmpty()){
                        newProducts=products;
                    }else {
                        for (int i = 0; i < products.size(); i++) {
                            if(shops.contains(products.get(i).shop)){
                                Product p = products.get(i);
                                p.imgIndex = i;
                                newProducts.add(p);
                            }
                        }

                    }
                    if(islow){
                        newProducts.sort(Product::compareTo);
                    }else {
                        newProducts.sort(Product::compareTo1);
                    }


                    String text = "";
                    int counter = 0;
                    for (Product p : newProducts) {

                        text += ("\uD83D\uDD37" + p.name + "\n➖Цена: " + ("" + p.price) + " руб.\n➖Магазин: " + p.shop + "\n" + "➖Изображение товара: /img" + generateCharSequence() + ("" + p.imgIndex) + "\n");
                        counter++;
                    }
                    sendMessage(chatId, text);


                }
            } else if (callbackData.equals("btn_confirm")) {
                String line = tb_users.selectFast(sqlite_connection, new String[]{"sdata"}, new String[]{"chatid"}, new String[]{"" + chatId}).get(0)[0];
                if (line==null||line.isEmpty()||line.charAt(0)!='0'&&   line.charAt(0)!='1') {
                    sendMessage(chatId, "Пожалуйста, выберите какой-нибудь из фильтров");
                } else {
                    EditMessageText editMessageText = new EditMessageText();
                    editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                    editMessageText.setChatId(chatId);
                    editMessageText.setText("Нажмите кнопку выше, чтобы отсортировать товары");
                    try {
                        this.execute(editMessageText);
                    } catch (TelegramApiException e) {
                        System.err.println(e.getMessage());
                    }
                }
            } else if (callbackData.contains("btn_shop_")) {
                String shop = callbackData.substring(9);
                sendMessage(chatId, "Вы добавили в фильтр магазин \"" + shop + "\"");
                String line = tb_users.selectFast(sqlite_connection, new String[]{"sdata"}, new String[]{"chatid"}, new String[]{"" + chatId}).get(0)[0];
                if (line==null||line.isEmpty()||line.charAt(0)!='0'&&line.charAt(0)!='1') {
                    line = "10" + shop + "*";
                    ManageFilters manageFilters = new ManageFilters(line);
                    manageFilters.send(this, chatId);
                } else {
                    ManageFilters manageFilters = new ManageFilters(line);
                    manageFilters.addShop(shop);
                    manageFilters.send(this, chatId);
                }

            } else if (callbackData.contains("bc_")) {

                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                System.out.println(editMessageText);
                String data = tb_users.selectFast(sqlite_connection, new String[]{"lastmessageargs"}, new String[]{"chatid"}, new String[]{"" + chatId}).get(0)[0];
                int num = Integer.parseInt(update.getCallbackQuery().getData().substring(3, 4));
                int beginIndex = 0;
                int counter = 0;
                String string = "";
                System.out.println(data);
                for (int i = 0; i < data.length(); i++) {
                    if (data.charAt(i) == '&') {
                        counter += 1;
                        if (counter - 1 == num) {
                            string = data.substring(beginIndex, i);
                            break;
                        } else {

                            beginIndex = i + 1;
                        }
                    }
                }
                System.out.println(string);
                String city = string.substring(0, string.indexOf('|'));
                String cityDescription = string.substring(string.indexOf('|') + 1);

                editMessageText.setText("Выбранное поселение: \n" + city + ", " + cityDescription);
                tb_users.updateFast(sqlite_connection, new String[]{"city"}, new String[]{string}, new String[]{"chatid"}, new String[]{"" + chatId});
                try {
                    this.execute(editMessageText);
                } catch (TelegramApiException e) {
                    System.err.println(e.getMessage());
                }
            }
        }

    }


    public void sendMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage(chatId + "", text);
        try {
            this.execute(sendMessage);
        } catch (TelegramApiException e) {
            int index = text.length() / 2;
            String substring1 = text.substring(index);
            int firstIndexOfNewLineSymbol = substring1.indexOf("\n");
            String finalSubstring1 = text.substring(0, index + firstIndexOfNewLineSymbol);
            String finalSubstring2 = text.substring(index + firstIndexOfNewLineSymbol);
            sendMessage(chatId, finalSubstring1);
            sendMessage(chatId, finalSubstring2);
        }
    }

    public void sendMessage(SendMessage sendMessage) {
        try {
            this.execute(sendMessage);
        } catch (TelegramApiException e) {

        }
    }

    public void sendPhoto(long chatId, String filepath) {
        InputFile inputFile = new InputFile();
        inputFile.setMedia(new File(filepath));
        SendPhoto sendPhoto = new SendPhoto(chatId + "", inputFile);
        try {
            this.execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
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
            this.execute(message);
        } catch (TelegramApiException e) {
            System.err.println(e.getMessage());
        }
    }

    public SendMessage addButtonsToSendMessage(SendMessage message, String[][] buttonsName, String[][] buttonsCallbackData) {
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
            this.execute(message);
        } catch (TelegramApiException e) {

        }
        return message;
    }

    public EditMessageText addButtonsToEditMessageText(EditMessageText message, String[][] buttonsName, String[][] buttonsCallbackData) {
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
            this.execute(message);
        } catch (TelegramApiException e) {

        }
        return message;
    }


    public boolean isFileInFolder(String pathToFolder, String fileName) {
        File folder = new File(pathToFolder);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (file.getName().equals(fileName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public void updateMessageStatus(Connection connection, long chatId, String status, String statusArgs) {
        if (statusArgs != null) {
            tb_users.updateFast(connection, new String[]{"lastmessage", "lastmessageargs"}, new String[]{status, statusArgs}, new String[]{"chatid"}, new String[]{chatId + ""});
        } else {
            tb_users.updateFast(connection, new String[]{"lastmessage"}, new String[]{status}, new String[]{"chatid"}, new String[]{chatId + ""});
        }
    }

    public void updateTables(Connection innerdatabase) {
        String sql_create_table_time = "CREATE TABLE IF NOT EXISTS \"time\" (\n" +
                "\t\"spam\"\tINTEGER,\n" +
                "\t\"chatid\"\tTEXT,\n" +
                "\t\"time\"\tTEXT,\n" +
                "\tPRIMARY KEY(\"chatid\")\n" +
                ");";
        String sql_create_table_users = "CREATE TABLE IF NOT EXISTS \"usersinfo\" (\n" +
                "\t\"chatid\"\tTEXT,\n" +
                "\t\"lastmessage\"\tTEXT,\n" +
                "\t\"lastmessageargs\"\tTEXT,\n" +
                "\t\"city\"\tTEXT,\n" +
                "\t\"sdata\"\tTEXT,\n" +
                "\t\"lastsearch\"\tTEXT,\n" +
                "\tPRIMARY KEY(\"chatid\")\n" +
                ");";
        String sql_create_table_requests = "CREATE TABLE IF NOT EXISTS \"helprequests\" (\n" +
                "\t\"id\"\tserial,\n" +
                "\t\"chatid\"\tTEXT,\n" +
                "\t\"firstname\"\tTEXT,\n" +
                "\t\"lastname\"\tTEXT,\n" +
                "\t\"link\"\tTEXT,\n" +
                "\tPRIMARY KEY(\"id\")\n" +
                ");";


        try {
            Statement statement1 = innerdatabase.createStatement();
            Statement statement2 = innerdatabase.createStatement();
            statement1.execute(sql_create_table_time);
            statement2.execute(sql_create_table_users);
        } catch (SQLException e) {
            System.err.println(e);
        }
    }

    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public boolean isBanned(long chatid) {
        for (int i = 0; i < ban.size(); i++) {
            System.out.println(ban.get(i)[0] + " " + chatid);
            if (ban.get(i)[0] == chatid) {
                return true;
            }
        }
        return false;
    }

    public void updateBanList() {
        long currentTime = clock.millis();
        for (int i = 0; i < ban.size(); i++) {
            if (ban.get(i)[1] <= currentTime) {
                ban.remove(i);
                return;
            }

        }
    }

    public void makeKeyboard(long chatId, String[][] buttonsName, String messageText) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> buttons = new ArrayList<>();
        for (int i = 0; i < buttonsName.length; i++) {
            KeyboardRow keyboardRow = new KeyboardRow();
            for (int j = 0; j < buttonsName[i].length; j++) {
                keyboardRow.add(new KeyboardButton(buttonsName[i][j]));
            }
            buttons.add(keyboardRow);
        }
        markup.setKeyboard(buttons);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(messageText);
        sendMessage.setReplyMarkup(markup);
        try {
            this.execute(sendMessage);
        } catch (TelegramApiException e) {
            System.err.println(e.getMessage());
        }
    }


    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    public boolean validateName(String name) {

        System.out.println("validate".toUpperCase(Locale.ROOT) + "------------------------" + name);
        char[] chrs = new char[]{' ', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ',
                'ы', 'ь', 'э', 'ю', 'я'};

        for (char c : name.toCharArray()) {
            boolean flag = false;
            for (int i = 0; i < chrs.length; i++) {
                if (c == chrs[i]) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                return false;
            }

        }
        return true;
    }

    public boolean validateImg(String message) {
        String charset = "cDqWrRoPsS";
        String nums = "1234567890";
        if (!(message.contains("/img"))) {
            return false;

        }
        for (int i = 4; i < message.length(); i++) {
            if (!(charset.contains(message.charAt(i) + "") || nums.contains(message.charAt(i) + ""))) {
                return false;
            }
        }
        return true;
    }

    public String generateCharSequence() {
        String s = "";
        String charset = "cDqWrRoPsS";
        for (int i = 0; i < 6; i++) {
            s += charset.charAt((int) (Math.random() * 8));
        }
        return s;
    }

    List<Product> getRecentProducts(long chatId) {
        String productData = tb_users.selectFast(sqlite_connection, new String[]{"lastsearch"}, new String[]{"chatid"}, new String[]{"" + chatId}).get(0)[0];
        if (productData == null || productData.isEmpty()) {
            return null;
        } else {
            List<Product> products = new ArrayList<>();
            int iteratorRight = productData.indexOf("#");
            System.out.println("productData:         --       " + productData);
            System.out.println("index: --- " + iteratorRight);
            int iteratorLeft = 0;
            int cntry = 0;
            products.add(Product.getProductFromString(productData.substring(0, iteratorRight)));
            for (int i = iteratorRight + 1; i < productData.length(); i++) {
                if (productData.charAt(i) == '#') {
                    iteratorLeft = iteratorRight;
                    iteratorRight = i;
                    Product po = Product.getProductFromString(productData.substring(iteratorLeft + 1, iteratorRight));
                    po.imgIndex=cntry;
                    products.add(po);

                }

                cntry++;

            }
            return products;
        }

    }
}
