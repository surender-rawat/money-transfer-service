package com.revolut.money.transfer.exceptions;

/**
 * Runtime exception which is used to be thrown when operation could not be executed. Most of time
 * when some unexpected SQLException occurred.
 */
public class InvalidOperationExecution extends RuntimeException {
    public InvalidOperationExecution(Throwable cause) {
        super(cause);
    }
}
