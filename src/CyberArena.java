import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/** Учет оплаченных заказов компьютерного клуба и демонстрация варианта 27. */
public final class CyberArena {
  private static int totalOrdersCount = 0;
  private static double totalRevenue = 0.0;

  private CyberArena() {}

  public static int getTotalOrdersCount() {
    return totalOrdersCount;
  }

  public static double getTotalRevenue() {
    return totalRevenue;
  }

  /** Фиксирует один оплаченный заказ; обычный расчет цены статистику не меняет. */
  public static void registerPayment(double amount) {
    if (!Double.isFinite(amount) || amount < 0) {
      throw new IllegalArgumentException("Сумма оплаты должна быть конечной и неотрицательной.");
    }
    BigDecimal payment = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    double updatedRevenue = BigDecimal.valueOf(totalRevenue).add(payment).doubleValue();
    if (!Double.isFinite(updatedRevenue) || totalOrdersCount == Integer.MAX_VALUE) {
      throw new IllegalStateException("Достигнут предел значений глобальной статистики.");
    }
    totalRevenue = updatedRevenue;
    totalOrdersCount++;
  }

  public static void printGlobalStats() {
    System.out.println("\n=== Статистика компьютерного клуба ===");
    System.out.printf("Оплачено заказов: %d%n", getTotalOrdersCount());
    System.out.printf(Locale.ROOT, "Общая выручка: %.2f руб.%n", getTotalRevenue());
  }

  public static void main(String[] args) {
    VRGlasses vr = new VRGlasses("VR-очки клуба", 25.00);

    // Сокращенный конструктор выбирает Standard Zone, полный задает тариф явно.
    GamingSeat standardSeat = new GamingSeat("Место 1", 1200.00);
    GamingSeat vipSeat = new GamingSeat("Место 2", 1200.00, "VIP Zone");
    GamingSeat bootcampSeat = new GamingSeat("Место 3", 1200.00, "BootCamp");

    System.out.println("Лабораторная работа. Блок 2, часть 1. Вариант 27");
    System.out.println("Компьютерный клуб: игровое место и аренда VR-очков");
    System.out.printf(Locale.ROOT, "Цена VR за минуту: %.2f руб.%n%n", vr.getPricePerMinute());

    // Последовательная демонстрация всех трех перегруженных методов.
    double standardPrice = standardSeat.calculateFinalPrice();
    printOrder(standardSeat, "без VR", standardPrice);
    registerPayment(standardPrice);

    double vipPrice = vipSeat.calculateFinalPrice(vr);
    printOrder(vipSeat, "VR на 1 минуту", vipPrice);
    registerPayment(vipPrice);

    double bootcampPrice = bootcampSeat.calculateFinalPrice(vr, 30);
    printOrder(bootcampSeat, "VR на 30 минут", bootcampPrice);
    registerPayment(bootcampPrice);

    System.out.println("\nМетоды addVR из таблицы варианта (повторный расчет без оплаты):");
    System.out.printf(Locale.ROOT, "addVR(vr): %.2f руб.%n", vipSeat.addVR(vr));
    System.out.printf(Locale.ROOT, "addVR(vr, 30): %.2f руб.%n", bootcampSeat.addVR(vr, 30));

    printGlobalStats();
  }

  private static void printOrder(GamingSeat seat, String details, double price) {
    System.out.printf(Locale.ROOT, "%s | %s | %s | %.2f руб.%n",
        seat.getName(), seat.getZone(), details, price);
  }
}
