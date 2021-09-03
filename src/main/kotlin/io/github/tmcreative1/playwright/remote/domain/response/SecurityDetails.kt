package com.playwright.remote.domain.response

data class SecurityDetails(
    /**
     * Common Name component of the Issuer field. from the certificate. This should only be used for informational purposes.
     * Optional.
     */
    val issuer: String,
    /**
     * The specific TLS protocol used. (e.g. {@code TLS 1.3}). Optional.
     */
    val protocol: String,
    /**
     * Common Name component of the Subject field from the certificate. This should only be used for informational purposes.
     * Optional.
     */
    val subjectName: String,
    /**
     * Unix timestamp (in seconds) specifying when this cert becomes valid. Optional.
     */
    val validForm: Double,
    /**
     * Unix timestamp (in seconds) specifying when this cert becomes invalid. Optional.
     */
    val validTo: Double,
)
