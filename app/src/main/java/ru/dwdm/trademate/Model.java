package ru.dwdm.trademate;

class Model {
    private String description;
    private int idRes;

    public Model(String description, int idRes) {
        this.description = description;
        this.idRes = idRes;
    }

    public String getDescription() {
        return description;
    }

    public int getIdRes() {
        return idRes;
    }
}
