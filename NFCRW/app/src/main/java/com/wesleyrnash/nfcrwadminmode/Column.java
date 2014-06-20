package com.wesleyrnash.nfcrwadminmode;

/**
 * Created by Wesley on 6/20/2014.
 */
public class Column {
    private static final Column INSTANCE = new Column();
    private String[] column;

    private Column() {};

    public static Column getInstance() {
        return INSTANCE;
    }

    public void setColumn(String[] _column){
        column = _column;
    }

    public String[] getColumn(){
        return column;
    }
}
