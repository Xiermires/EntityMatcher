package org.matcher.operator;


public class Functor extends Selector {

    public Functor(String symbol) {
	super(symbol);
    }
    
    @Override
    public String resolve(String tableColumn) {
	return getSymbol() + "(" + tableColumn + ")";
    }
}
