import billing.CyberArena;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import model.CorporateGamingSeat;
import model.GamingSeat;
import model.UserGamingSeat;
import model.VRGlasses;
import service.NotificationService;
import service.impl.EmailNotification;
import service.impl.SmsNotification;

/** Проверки расчётов, уведомлений и учёта оплат для варианта 27. */
public final class Lab27Test {
  private static int checks;

  private Lab27Test() {}

  public static void main(String[] args) {
    int initialOrders = CyberArena.getTotalOrdersCount();
    double initialRevenue = CyberArena.getTotalRevenue();
    testBillingAndOptions();
    testCorrections();
    testChannels();
    check(CyberArena.getTotalOrdersCount() == initialOrders, "Расчёт не регистрирует заказ");
    money(initialRevenue, CyberArena.getTotalRevenue(), "Расчёт не меняет выручку");
    testPayments();
    System.out.println("OK: " + checks + " checks passed.");
  }

  private static void testBillingAndOptions() {
    GamingSeat seat = new UserGamingSeat("Место 1", 100);
    check("Standard Zone".equals(seat.getZone()), "Стандартная зона по умолчанию");
    money(100, seat.calculateBillingPrice(), "Стандартный тариф");
    seat.setZone("BootCamp");
    money(200, seat.calculateBillingPrice(), "Тариф BootCamp");
    seat.setZone("VIP Zone");
    money(140, seat.calculateBillingPrice(), "Тариф VIP");

    VRGlasses vr = new VRGlasses("VR", 2.50);
    money(142.50, seat.addVR(vr), "VR на одну минуту");
    money(165, seat.addVR(vr, 10), "VR на десять минут");
    money(140, seat.calculateFinalPrice(), "Цена места без VR после привязки очков");
    VRGlasses other = new VRGlasses("Другие VR", 1);
    rejects(() -> seat.calculateFinalPrice(other, 0), "Нулевая длительность VR");
    rejects(() -> seat.calculateFinalPrice(null), "Пустая опция VR");
    rejects(() -> seat.calculateFinalPrice(new VRGlasses("Дорогие", Double.MAX_VALUE), 2),
        "Переполнение стоимости VR");
    check(seat.getAppliedOption() == vr, "Ошибка расчёта сохранила прежние очки");
    rejects(() -> vr.setPricePerMinute(-1), "Отрицательная цена минуты");
    vr.setPricePerMinute(2.345);
    money(2.35, vr.getPricePerMinute(), "Округление цены минуты до копейки");

    GamingSeat team = new CorporateGamingSeat("Команда", 10.01, "VIP Zone", 5);
    money(70.07, team.calculateBillingPrice(), "Округление корпоративной суммы после умножения");
    String warning = outputOf(() -> money(70.07, team.addVR(vr, 10), "Корпоративный тариф без VR"));
    check(!warning.isBlank(), "Предупреждение об ограничении корпоративного тарифа");
    money(70.07, team.calculateFinalPrice(), "Перегрузка использует корпоративный расчёт");
    check(team.getAppliedOption() == null, "Корпоративному заказу очки не назначены");
  }

  private static void testCorrections() {
    GamingSeat seat = new UserGamingSeat("Место 2", 50);
    for (double invalid : new double[]{0, -5, 0.004, Double.NaN, Double.POSITIVE_INFINITY}) {
      String warning = outputOf(() -> seat.setBasePrice(invalid));
      money(100, seat.getBasePrice(), "Замена некорректной базовой цены");
      check(!warning.isBlank(), "Предупреждение об исправлении цены");
    }
    seat.setBasePrice(1.005);
    money(1.01, seat.calculateBillingPrice(), "Округление HALF_UP");
    for (String unknown : new String[]{null, "", "Deluxe"}) {
      String warning = outputOf(() -> {
        seat.setZone(unknown);
        money(1.01, seat.calculateBillingPrice(), "Стандартная стоимость для неизвестной зоны");
      });
      check(!warning.isBlank(), "Предупреждение о неизвестной зоне");
    }
    String warning = outputOf(() -> {
      CorporateGamingSeat team = new CorporateGamingSeat("Команда", 300, "VIP Zone", 0);
      check(team.getSeatCount() == 1, "Минимум одно место");
      money(420, team.calculateBillingPrice(), "Стоимость одного места после исправления");
    });
    check(!warning.isBlank(), "Предупреждение об исправлении количества мест");
  }

  private static void testChannels() {
    NotificationService email = new EmailNotification();
    NotificationService sms = new SmsNotification();
    String receipt = "Тестовый чек";
    String address = "billing@example.org";
    String emailLog = outputOf(() -> email.sendNotification(receipt, address));
    check(emailLog.contains("[EMAIL]") && emailLog.contains(address) && emailLog.contains(receipt),
        "Email содержит адрес и текст чека");
    for (String phone : new String[]{"+70000000000", "80000000000"}) {
      String smsLog = outputOf(() -> sms.sendNotification(receipt, phone));
      check(smsLog.contains("[SMS]") && smsLog.contains(phone) && smsLog.contains(receipt),
          "SMS для обоих допустимых префиксов");
    }
    for (String invalid : new String[]{null, "", "billing.example.org", "billing@example"}) {
      String log = outputOf(() -> email.sendNotification(receipt, invalid));
      check(!log.isBlank() && !log.contains(receipt), "Неверный адрес: предупреждение без чека");
    }
    for (String invalid : new String[]{null, "+70000", "90000000000", "8abcdefghij"}) {
      String log = outputOf(() -> sms.sendNotification(receipt, invalid));
      check(!log.isBlank() && !log.contains(receipt), "Неверный номер: предупреждение без чека");
    }
  }

  private static void testPayments() {
    int initialOrders = CyberArena.getTotalOrdersCount();
    double initialRevenue = CyberArena.getTotalRevenue();
    List<String> receipts = new ArrayList<>();
    CyberArena desk = new CyberArena((message, recipient) -> receipts.add(recipient + " " + message));
    GamingSeat seat = new UserGamingSeat("Место 3", 100);
    desk.processPayment(seat, "guest", 112.35);
    check(receipts.size() == 1 && receipts.get(0).startsWith("guest ")
        && receipts.get(0).replace(',', '.').contains("112.35"), "Сервис получил чек и сумму оплаты");
    desk.processPayment(seat, "guest", 0);
    for (double invalid : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY}) {
      rejects(() -> desk.processPayment(seat, "guest", invalid), "Недопустимая сумма оплаты");
    }
    check(receipts.size() == 2, "Чек создаётся только для принятой оплаты");

    CyberArena otherDesk = new CyberArena(new EmailNotification());
    GamingSeat team = new CorporateGamingSeat("Команда", 100, "BootCamp", 5);
    String warning = outputOf(() -> otherDesk.processPayment(team, "wrong", team.calculateBillingPrice()));
    check(!warning.isBlank(), "Касса сообщила об ошибке адреса");
    check(CyberArena.getTotalOrdersCount() == initialOrders + 3, "Общий счётчик двух касс");
    money(initialRevenue + 1112.35, CyberArena.getTotalRevenue(),
        "Ошибка доставки не отменяет оплату, недопустимая сумма не учитывается");
  }

  private static String outputOf(Runnable action) {
    PrintStream originalOut = System.out;
    PrintStream originalErr = System.err;
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (PrintStream capture = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
      System.setOut(capture);
      System.setErr(capture);
      action.run();
    } finally {
      System.setOut(originalOut);
      System.setErr(originalErr);
    }
    return bytes.toString(StandardCharsets.UTF_8);
  }

  private static void money(double expected, double actual, String label) {
    check(Math.abs(expected - actual) < 0.0000001,
        label + ": ожидалось " + expected + ", получено " + actual);
  }

  private static void rejects(Runnable action, String label) {
    try {
      action.run();
    } catch (IllegalArgumentException expected) {
      checks++;
      return;
    }
    throw new AssertionError(label);
  }

  private static void check(boolean condition, String label) {
    if (!condition) {
      throw new AssertionError(label);
    }
    checks++;
  }
}
