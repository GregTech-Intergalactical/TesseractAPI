package tesseract.api.fluid;

/**
 * Enumerator used for classification of events for node/pipes.
 */
public enum FluidStatus {
    SUCCESS,

    FAIL_TEMP,
    FAIL_LEAK,
    FAIL_PRESSURE,
    FAIL_CAPACITY,
}
