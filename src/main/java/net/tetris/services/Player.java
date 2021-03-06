package net.tetris.services;

public class Player {
    private String name;
    private String callbackUrl;
    private PlayerScores scores;

    public Player() {
    }

    public Player(String name, String callbackUrl, PlayerScores scores) {
        this.name = name;
        this.callbackUrl = callbackUrl;
        this.scores = scores;
    }

    public String getName() {
        return name;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getScore() {
        return scores.getScore();
    }
}
