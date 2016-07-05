package ru.everypony.maud.utils;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 07:19 on 04/07/16
 *
 * @author cab404
 */
public class DrawerItemData {
    public DrawerItemData() {
    }

    public DrawerItemData(String name, int id, Type type, boolean deletable) {
        this.name = name;
        this.id = id;
        this.type = type;
        this.deletable = deletable;
    }

    public enum Type {
        BLOG, BUTTON
    }
    public boolean deletable = false;
    public String name;
    public String data;
    public Type type;
    public int id;

}
