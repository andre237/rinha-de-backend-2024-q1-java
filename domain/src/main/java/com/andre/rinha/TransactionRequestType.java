package com.andre.rinha;

public enum TransactionRequestType {

    CREDIT("c"), DEBIT("d");

    private final String symbol;

    TransactionRequestType(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static TransactionRequestType fromSymbol(String symbol) {
        return switch (symbol) {
            case "c" -> TransactionRequestType.CREDIT;
            case "d" -> TransactionRequestType.DEBIT;
            default -> null;
        };
    }

}
