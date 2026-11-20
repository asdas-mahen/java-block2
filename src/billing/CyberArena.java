package billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;
import model.GamingSeat;
import service.NotificationService;

/** Принимает оплату и передаёт чек выбранной службе уведомлений. */
public final class CyberArena {
  private static int totalOrdersCount = 0;
  private static double totalRevenue = 0.0;
  private final NotificationService notificationService;

  public CyberArena(NotificationService notificationService) {
    this.notificationService = Objects.requireNonNull(notificationService, "Нужна служба уведомлений.");
  }

  public void processPayment(GamingSeat seat, String contact, double finalPrice) {
    Objects.requireNonNull(seat, "Нужно указать заказ.");
    double paid = registerPayment(finalPrice);
    String receipt = String.format(Locale.ROOT, "Заказ «%s» оплачен. Сумма: %.2f руб.",
        seat.getName(), paid);
    notificationService.sendNotification(receipt, contact);
  }

  public static int getTotalOrdersCount() {
    return totalOrdersCount;
  }

  public static double getTotalRevenue() {
    return totalRevenue;
  }

  /** Фиксирует один оплаченный заказ; обычный расчет цены статистику не меняет. */
  private static double registerPayment(double amount) {
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
    return payment.doubleValue();
  }

  public static void printGlobalStats() {
    System.out.println("\n=== Статистика компьютерного клуба ===");
    System.out.printf("Оплачено заказов: %d%n", getTotalOrdersCount());
    System.out.printf(Locale.ROOT, "Общая выручка: %.2f руб.%n", getTotalRevenue());
  }

}
