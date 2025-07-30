package be.esmay.pocketidsync.api;

import lombok.Getter;

@Getter
public class PocketIDException extends Exception {

    private final int statusCode;
    private final String responseBody;
    
    public PocketIDException(String message) {
        super(message);

        this.statusCode = -1;
        this.responseBody = null;
    }
    
    public PocketIDException(String message, Throwable cause) {
        super(message, cause);

        this.statusCode = -1;
        this.responseBody = null;
    }
    
    public PocketIDException(String message, int statusCode, String responseBody) {
        super(message);

        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

}