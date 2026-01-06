package org.akashbag.ecommerce.exception.customException;

public class ResourceNotFound extends RuntimeException{
    public ResourceNotFound(String message){
        super(message);
    }
}
