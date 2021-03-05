package core.exceptions


class PlaywrightException : RuntimeException {

    constructor(message: String) : super(message);

    constructor(message: String, cause: Throwable) : super(message, cause);
}