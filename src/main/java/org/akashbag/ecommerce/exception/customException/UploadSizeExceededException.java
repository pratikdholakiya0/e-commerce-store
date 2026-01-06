package org.akashbag.ecommerce.exception.customException;

public class UploadSizeExceededException extends RuntimeException {
    public UploadSizeExceededException(String message) {
        super(message);
    }
    public UploadSizeExceededException() {

    }
}
