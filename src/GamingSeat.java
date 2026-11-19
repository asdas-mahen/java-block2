import java.math.BigDecimal;
import java.math.RoundingMode;

/** Игровое место с тарифом и необязательной арендой VR-очков. */
public final class GamingSeat {
  private static final String DEFAULT_ZONE = "Standard Zone";
  private static final double STANDARD_MULTIPLIER = 1.0;
  private static final double VIP_MULTIPLIER = 1.4;
  private static final double BOOTCAMP_MULTIPLIER = 2.0;
  private static final int DEFAULT_VR_MINUTES = 1;

  private final String name;
  private double basePrice;
  private String zone;
  private VRGlasses appliedOption = null;

  public GamingSeat(String name, double basePrice) {
    this(name, basePrice, DEFAULT_ZONE);
  }

  public GamingSeat(String name, double basePrice, String zone) {
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

  public void setBasePrice(double basePrice) {
    if (!Double.isFinite(basePrice) || basePrice < 0) {
      throw new IllegalArgumentException("Базовая цена должна быть конечной и неотрицательной.");
    }
    this.basePrice = toMoney(BigDecimal.valueOf(basePrice));
  }

  public void setZone(String zone) {
    // Сначала проверяем тариф, чтобы ошибка не изменила состояние объекта.
    getZoneMultiplier(zone);
    this.zone = zone;
  }

  /** Возвращает цену места с учетом тарифа, без аренды VR-очков. */
  public double calculateFinalPrice() {
    BigDecimal price = BigDecimal.valueOf(basePrice)
        .multiply(BigDecimal.valueOf(getZoneMultiplier(zone)));
    return toMoney(price);
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

  private static double getZoneMultiplier(String zone) {
    if (zone == null) {
      throw new IllegalArgumentException("Тариф не должен быть null.");
    }
    return switch (zone) {
      case "Standard Zone" -> STANDARD_MULTIPLIER;
      case "VIP Zone" -> VIP_MULTIPLIER;
      case "BootCamp" -> BOOTCAMP_MULTIPLIER;
      default -> throw new IllegalArgumentException("Неизвестный тариф: " + zone);
    };
  }

  private static double toMoney(BigDecimal price) {
    double result = price.setScale(2, RoundingMode.HALF_UP).doubleValue();
    if (!Double.isFinite(result)) {
      throw new IllegalArgumentException("Рассчитанная цена слишком велика.");
    }
    return result;
  }
}
