package edu.harvard.cs50.pokedex;

public class Pokemon {
    private String name;
    private String url;
    private int stat = 0;

    Pokemon(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public int getStat(){return stat;};
}
