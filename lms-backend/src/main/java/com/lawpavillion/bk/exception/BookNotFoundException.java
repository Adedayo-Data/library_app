package com.lawpavillion.bk.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(String message){
        super(message);
    }
}
