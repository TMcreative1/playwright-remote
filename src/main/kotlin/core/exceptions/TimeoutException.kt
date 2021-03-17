package core.exceptions

class TimeoutException : RuntimeException {

    constructor(message: String) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
}