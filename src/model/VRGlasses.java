package model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** VR-очки с ценой аренды за одну минуту. */
public final class VRGlasses {
  private final String name;
  private double pricePerMinute;

  public VRGlasses(String name, double pricePerMinute) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Название VR-очков не должно быть пустым.");
    }
    this.name = name.strip();
    setPricePerMinute(pricePerMinute);
  }

  public String getName() {
    return name;
  }

  public double getPricePerMinute() {
    return pricePerMinute;
  }

  public void setPricePerMinute(double pricePerMinute) {
    if (!Double.isFinite(pricePerMinute) || pricePerMinute < 0) {
      throw new IllegalArgumentException("Цена минуты должна быть конечной и неотрицательной.");
    }
    this.pricePerMinute = BigDecimal.valueOf(pricePerMinute)
        .setScale(2, RoundingMode.HALF_UP).doubleValue();
  }
}
