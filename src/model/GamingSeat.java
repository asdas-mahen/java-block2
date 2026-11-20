package model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Игровое место с тарифом и необязательной арендой VR-очков. */
public abstract class GamingSeat {
  private static final String DEFAULT_ZONE = "Standard Zone";
  private static final double STANDARD_MULTIPLIER = 1.0;
  private static final double VIP_MULTIPLIER = 1.4;
  private static final double BOOTCAMP_MULTIPLIER = 2.0;
  private static final int DEFAULT_VR_MINUTES = 1;
  private static final double FALLBACK_BASE_PRICE = 100.0;

  private final String name;
  private double basePrice;
  private String zone;
  private VRGlasses appliedOption = null;

  protected GamingSeat(String name, double basePrice) {
    this(name, basePrice, DEFAULT_ZONE);
  }

  protected GamingSeat(String name, double basePrice, String zone) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Название игрового места не должно быть пустым.");
    }
    this.name = name.strip();
    setBasePrice(basePrice);
    setZone(zone);
  }

  public String getName() {
    return name;
  }

  public double getBasePrice() {
    return basePrice;
  }

  public String getZone() {
    return zone;
  }

  public VRGlasses getAppliedOption() {
    return appliedOption;
  }

  public final void setBasePrice(double basePrice) {
    if (Double.isFinite(basePrice) && basePrice > 0) {
      double rounded = toMoney(BigDecimal.valueOf(basePrice));
      if (rounded > 0) {
        this.basePrice = rounded;
        return;
      }
    }
    System.out.println("Предупреждение: недопустимая базовая цена. Установлено "
        + FALLBACK_BASE_PRICE + " руб.");
    this.basePrice = FALLBACK_BASE_PRICE;
  }

  public final void setZone(String zone) {
    this.zone = zone == null ? "" : zone.strip();
  }

  public abstract double calculateBillingPrice();

  /** Возвращает стоимость заказа без аренды VR-очков. */
  public final double calculateFinalPrice() {
    return calculateBillingPrice();
  }

  /** Добавляет VR-очки на одну минуту и возвращает общую стоимость. */
  public double calculateFinalPrice(VRGlasses option) {
    return calculateFinalPrice(option, DEFAULT_VR_MINUTES);
  }

  /** Добавляет VR-очки на указанное число минут и возвращает общую стоимость. */
  public double calculateFinalPrice(VRGlasses option, int minutes) {
    if (option == null) {
      throw new IllegalArgumentException("Необходимо указать VR-очки.");
    }
    if (minutes <= 0) {
      throw new IllegalArgumentException("Время аренды VR должно быть больше нуля.");
    }
    BigDecimal vrPrice = BigDecimal.valueOf(option.getPricePerMinute())
        .multiply(BigDecimal.valueOf(minutes));
    double finalPrice = toMoney(BigDecimal.valueOf(calculateFinalPrice()).add(vrPrice));
    appliedOption = option;
    return finalPrice;
  }

  public double addVR(VRGlasses vr) {
    return calculateFinalPrice(vr);
  }

  public double addVR(VRGlasses vr, int minutes) {
    return calculateFinalPrice(vr, minutes);
  }

  protected final double calculateSeatsPrice(int seatCount) {
    double multiplier = switch (zone) {
      case DEFAULT_ZONE -> STANDARD_MULTIPLIER;
      case "VIP Zone" -> VIP_MULTIPLIER;
      case "BootCamp" -> BOOTCAMP_MULTIPLIER;
      default -> {
        System.out.println("Предупреждение: неизвестная зона «" + zone
            + "». Расчёт по тарифу " + DEFAULT_ZONE + ".");
        yield STANDARD_MULTIPLIER;
      }
    };
    BigDecimal price = BigDecimal.valueOf(basePrice).multiply(BigDecimal.valueOf(multiplier))
        .multiply(BigDecimal.valueOf(seatCount));
    return toMoney(price);
  }

  private static double toMoney(BigDecimal price) {
    double result = price.setScale(2, RoundingMode.HALF_UP).doubleValue();
    if (!Double.isFinite(result)) {
      throw new IllegalArgumentException("Рассчитанная цена слишком велика.");
    }
    return result;
  }
}
