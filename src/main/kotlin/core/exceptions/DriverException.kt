package core.exceptions

import domain.message.SerializedError

class DriverException(error: SerializedError.Error) : PlaywrightException(error.toString()) {
}