package com.example.BuyBuddy.service;

import java.util.ArrayList;
import java.util.List;

public class ManageFilters {
    private String line;

    public ManageFilters(String line) {
        this.line = line;
    }

    //00 & & & & & & & &
    public String getLine() {
        return line;
    }

    public void setHighCostSorting() {
        line = "10" + line.substring(2);
    }

    public void setLowCostSorting() {
        line = "01" + line.substring(2);
    }

    public void addShop(String shopName) {
        if (!line.contains(shopName)) {
            line += (shopName + "&");
        }
    }

    public List<String> getShops(){
        List<String>shops1 = new ArrayList<>();
        int itl = 2;
        int itr = 2;
        var cnt=0;
        for (int i = 2; i < line.length(); i++) {
            if(line.charAt(i)=='&'){
                if (cnt==0){
                    itr = i;
                    shops1.add(line.substring(itl,i));
                }else {
                    itl = itr;
                    itr = i;
                    shops1.add(line.substring(itl+1,itr));
                }
                cnt++;
            }
        }
        return shops1;
    }
    public boolean isLow(){
        if (line.charAt(1)=='1'){
            return true;
        }
        return false;
    }
    public void send(TelegramBot telegramBot,long chatId){
        telegramBot. tb_users.updateFast(telegramBot.sqlite_connection,new String[]{"sdata"},new String[]{line},new String[]{"chatid"},new String[]{""+chatId});
    }


}
