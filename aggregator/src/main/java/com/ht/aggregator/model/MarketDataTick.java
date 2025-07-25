// MarketDataTick.java
package com.ht.aggregator.model;

public class MarketDataTick {
    private String symbol;
    private double price;
    private long timestamp;

    public MarketDataTick() {}

    public MarketDataTick(String symbol, double price, long timestamp) {
        this.symbol = symbol;
        this.price = price;
        this.timestamp = timestamp;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
