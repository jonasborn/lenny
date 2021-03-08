package de.jonasborn.lenny

class StreamNotFoundException  extends Exception {

    StreamNotFoundException(String message) {
        super(message)
    }

    StreamNotFoundException(String message, Throwable cause) {
        super(message, cause)
    }
}
