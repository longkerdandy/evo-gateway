package com.github.longkerdandy.evo.adapter.wemo.upnp.scpd;

/**
 * SCPD
 */
@SuppressWarnings("unused")
public class AllowedValueRange {

    private String minimum;
    private String maximum;
    private String step;

    public AllowedValueRange(String minimum, String maximum, String step) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.step = step;
    }

    public String getMinimum() {
        return minimum;
    }

    public void setMinimum(String minimum) {
        this.minimum = minimum;
    }

    public String getMaximum() {
        return maximum;
    }

    public void setMaximum(String maximum) {
        this.maximum = maximum;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }
}
