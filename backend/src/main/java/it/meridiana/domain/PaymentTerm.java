package it.meridiana.domain;

import java.util.List;

public enum PaymentTerm {
    IMMEDIATE(List.of(0)),
    D30(List.of(30)),
    D30_60(List.of(30, 60)),
    D30_60_90(List.of(30, 60, 90)),
    D60(List.of(60));

    private final List<Integer> offsets;

    PaymentTerm(List<Integer> offsets) {
        this.offsets = offsets;
    }

    public List<Integer> offsets() {
        return offsets;
    }
}
