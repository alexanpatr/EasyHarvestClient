package com.www.client;

public class Item {
    String text = "";
    int image = 0;

    public Item(String text, int image) {
        this.text = text;
        this.image = image;
    }

    public Item(String text) {
        this.text = text;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
