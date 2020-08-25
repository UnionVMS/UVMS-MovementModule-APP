package eu.europa.ec.fisheries.uvms.movement.model.constants;

public enum SatId {
    AORE(1), AORW(0), POR(2), IOR(3);
    private final Integer value;

    SatId(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SatId fromInt(Integer code) {
        for (SatId type : SatId.values()) {
            if (type.value == code) {
                return type;
            }
        }
        return null;
    }
}
