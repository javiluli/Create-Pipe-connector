package com.javiluli.createpipeconnector.feature.routing;

/** Estrategias de enrutado expuestas en el menu de opciones. */
public enum RoutePriority {
    AUTO(1),
    HORIZONTAL_FIRST(1),
    VERTICAL_FIRST(1),
    X_FIRST(1),
    Z_FIRST(1),
    AVOID_VERTICAL(8);

    private final int verticalCost;

    /** Crea una prioridad con su coste vertical asociado. */
    RoutePriority(int verticalCost) {
        this.verticalCost = verticalCost;
    }

    /** Devuelve el coste aplicado a cada movimiento vertical. */
    public int verticalCost() {
        return verticalCost;
    }
}
