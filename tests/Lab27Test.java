/** Независимые проверки лабораторной № 27 без JUnit. */
public final class Lab27Test {
  private static int checks;

  private Lab27Test() {
  }

  public static void main(String[] args) {
    long initialOrders = CyberArena.getTotalOrdersCount();
    double initialRevenue = CyberArena.getTotalRevenue();

    testConstructorsAndZones();
    testOverloadsAndOptions();
    testValidationAndState();
    testRounding();

    check(CyberArena.getTotalOrdersCount() == initialOrders,
        "Расчеты не регистрируют заказы");
    money(initialRevenue, CyberArena.getTotalRevenue(),
        "Расчеты не меняют выручку");
    testPayments();

    System.out.println("OK: " + checks + " checks passed.");
  }

  private static void testConstructorsAndZones() {
    GamingSeat standard = new GamingSeat("PC-1", 100.0);
    check("PC-1".equals(standard.getName()), "Название места");
    check("Standard Zone".equals(standard.getZone()), "Зона по умолчанию");
    check(standard.getAppliedOption() == null, "Исходная опция отсутствует");
    money(100.0, standard.getBasePrice(), "Базовая цена");
    money(100.0, standard.calculateFinalPrice(), "Standard Zone");

    GamingSeat vip = new GamingSeat("PC-2", 100.0, "VIP Zone");
    GamingSeat bootCamp = new GamingSeat("PC-3", 100.0, "BootCamp");
    money(140.0, vip.calculateFinalPrice(), "VIP Zone");
    money(200.0, bootCamp.calculateFinalPrice(), "BootCamp");

    standard.setBasePrice(250.0);
    standard.setZone("VIP Zone");
    money(350.0, standard.calculateFinalPrice(), "Изменение цены и зоны");
    money(140.0, vip.calculateFinalPrice(), "Независимость разных мест");
    standard.setZone("BootCamp");
    money(500.0, standard.calculateFinalPrice(), "Смена зоны на BootCamp");
    standard.setZone("Standard Zone");
    money(250.0, standard.calculateFinalPrice(), "Смена зоны на Standard Zone");

    VRGlasses glasses = new VRGlasses("VR-1", 2.5);
    check("VR-1".equals(glasses.getName()), "Название очков");
    money(2.5, glasses.getPricePerMinute(), "Цена минуты VR");
    glasses.setPricePerMinute(3.75);
    money(3.75, glasses.getPricePerMinute(), "Изменение цены минуты VR");
    money(0.0, new GamingSeat("Free", 0.0).calculateFinalPrice(), "Нулевая цена места");
    money(0.0, new VRGlasses("Free VR", 0.0).getPricePerMinute(), "Нулевая цена VR");
  }

  private static void testOverloadsAndOptions() {
    GamingSeat seat = new GamingSeat("VIP", 100.0, "VIP Zone");
    VRGlasses first = new VRGlasses("First", 2.5);
    VRGlasses second = new VRGlasses("Second", 4.0);

    money(142.5, seat.calculateFinalPrice(first), "VR по умолчанию на одну минуту");
    check(seat.getAppliedOption() == first, "Привязка первых очков");
    money(165.0, seat.calculateFinalPrice(first, 10), "VR на десять минут");
    money(140.0, seat.calculateFinalPrice(), "Расчет без аргументов исключает VR");
    check(seat.getAppliedOption() == first, "Расчет без VR сохраняет опцию");

    money(144.0, seat.addVR(second), "Стоимость addVR без длительности");
    check(seat.getAppliedOption() == second, "addVR с одной минутой привязывает опцию");
    money(152.5, seat.addVR(first, 5), "Стоимость addVR с длительностью");
    check(seat.getAppliedOption() == first, "addVR с длительностью привязывает опцию");
    money(144.0, seat.calculateFinalPrice(second, 1), "Замена очков и одна минута");

    first.setPricePerMinute(7.0);
    money(144.0, seat.calculateFinalPrice(second), "Цены разных очков независимы");
    money(154.0, seat.calculateFinalPrice(first, 2), "Обновленная цена VR");

    GamingSeat cheap = new GamingSeat("Large duration", 0.0);
    money(2147483647.0, cheap.calculateFinalPrice(new VRGlasses("One", 1.0),
        Integer.MAX_VALUE), "Максимальная положительная длительность");
  }

  private static void testValidationAndState() {
    for (String invalidName : new String[]{null, "", "   "}) {
      rejects(() -> new GamingSeat(invalidName, 100.0), "Пустое название места");
      rejects(() -> new GamingSeat(invalidName, 100.0, "VIP Zone"),
          "Пустое название в полном конструкторе");
      rejects(() -> new VRGlasses(invalidName, 1.0), "Пустое название очков");
    }

    GamingSeat seat = new GamingSeat("Seat", 100.0, "VIP Zone");
    VRGlasses oldOption = new VRGlasses("Old", 2.0);
    VRGlasses newOption = new VRGlasses("New", 3.0);
    seat.calculateFinalPrice(oldOption, 3);

    for (double invalidPrice : new double[]{-1.0, -0.001, Double.NaN,
        Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
      rejects(() -> new GamingSeat("Bad", invalidPrice), "Невалидная цена места");
      rejects(() -> new GamingSeat("Bad", invalidPrice, "VIP Zone"),
          "Невалидная цена в полном конструкторе");
      rejects(() -> new VRGlasses("Bad", invalidPrice), "Невалидная цена VR");
      rejects(() -> seat.setBasePrice(invalidPrice), "Невалидный сеттер цены места");
      money(100.0, seat.getBasePrice(), "Цена места сохранена после ошибки");
      rejects(() -> oldOption.setPricePerMinute(invalidPrice), "Невалидный сеттер цены VR");
      money(2.0, oldOption.getPricePerMinute(), "Цена VR сохранена после ошибки");
      check(seat.getAppliedOption() == oldOption, "Опция сохранена после ошибки цены");
    }

    for (String invalidZone : new String[]{null, "", "   ", "Unknown", "vip zone"}) {
      rejects(() -> new GamingSeat("Bad", 100.0, invalidZone), "Невалидная зона");
      rejects(() -> seat.setZone(invalidZone), "Невалидный сеттер зоны");
      check("VIP Zone".equals(seat.getZone()), "Зона сохранена после ошибки");
      check(seat.getAppliedOption() == oldOption, "Опция сохранена после ошибки зоны");
    }

    for (int invalidMinutes : new int[]{0, -1, Integer.MIN_VALUE}) {
      rejects(() -> seat.calculateFinalPrice(newOption, invalidMinutes),
          "Неположительная длительность расчета");
      check(seat.getAppliedOption() == oldOption, "Расчет с ошибкой не заменяет опцию");
      rejects(() -> seat.addVR(newOption, invalidMinutes), "Неположительная длительность addVR");
      check(seat.getAppliedOption() == oldOption, "addVR с ошибкой не заменяет опцию");
    }

    rejects(() -> seat.calculateFinalPrice(null), "Расчет с null вместо очков");
    rejects(() -> seat.calculateFinalPrice(null, 1), "Расчет с null и длительностью");
    rejects(() -> seat.addVR(null), "addVR с null");
    rejects(() -> seat.addVR(null, 1), "addVR с null и длительностью");
    check(seat.getAppliedOption() == oldOption, "Ошибки null сохраняют опцию");
    VRGlasses hugePrice = new VRGlasses("Huge", Double.MAX_VALUE);
    rejects(() -> seat.calculateFinalPrice(hugePrice, 2), "Переполнение стоимости VR");
    check(seat.getAppliedOption() == oldOption, "Переполнение не заменяет опцию");
    money(140.0, seat.calculateFinalPrice(), "Ошибки не меняют стоимость места");
    money(146.0, seat.calculateFinalPrice(oldOption, 3), "После ошибок расчет корректен");
  }

  private static void testRounding() {
    money(1.01, new GamingSeat("Half cent", 1.005).calculateFinalPrice(),
        "Округление HALF_UP в стандартной зоне");
    money(14.01, new GamingSeat("VIP rounding", 10.01, "VIP Zone").calculateFinalPrice(),
        "Округление после применения коэффициента зоны");
    money(0.0, new GamingSeat("Zero rounding", 0.004).calculateFinalPrice(),
        "Округление вниз до нуля");

    GamingSeat seat = new GamingSeat("Rounded fields", 10.005, "VIP Zone");
    VRGlasses glasses = new VRGlasses("Rounded rate", 0.005);
    money(10.01, seat.getBasePrice(), "Конструктор округляет базовую цену");
    money(0.01, glasses.getPricePerMinute(), "Конструктор округляет тариф VR");
    money(14.04, seat.calculateFinalPrice(glasses, 3), "Расчет по округленным тарифам");
    seat.setBasePrice(2.345);
    glasses.setPricePerMinute(2.345);
    money(2.35, seat.getBasePrice(), "Сеттер округляет базовую цену");
    money(2.35, glasses.getPricePerMinute(), "Сеттер округляет тариф VR");
  }

  private static void testPayments() {
    long ordersBefore = CyberArena.getTotalOrdersCount();
    double revenueBefore = CyberArena.getTotalRevenue();
    CyberArena.registerPayment(10.0);
    CyberArena.registerPayment(2.345);
    CyberArena.registerPayment(0.005);
    CyberArena.registerPayment(0.0);
    check(CyberArena.getTotalOrdersCount() == ordersBefore + 4,
        "Каждая успешная оплата учитывается, включая нулевую");
    money(revenueBefore + 12.36, CyberArena.getTotalRevenue(), "Суммирование округленных оплат");

    long ordersAfter = CyberArena.getTotalOrdersCount();
    double revenueAfter = CyberArena.getTotalRevenue();
    for (double invalidPayment : new double[]{-1.0, -0.001, Double.NaN,
        Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
      rejects(() -> CyberArena.registerPayment(invalidPayment), "Невалидная оплата");
      check(CyberArena.getTotalOrdersCount() == ordersAfter,
          "Неуспешная оплата не увеличивает число заказов");
      money(revenueAfter, CyberArena.getTotalRevenue(), "Неуспешная оплата не меняет выручку");
    }
  }

  private static void money(double expected, double actual, String message) {
    check(Double.isFinite(actual) && Math.abs(expected - actual) < 0.0000001,
        message + ": expected " + expected + ", actual " + actual);
  }

  private static void rejects(Runnable action, String message) {
    try {
      action.run();
    } catch (IllegalArgumentException expected) {
      checks++;
      return;
    }
    throw new AssertionError(message + ": expected IllegalArgumentException");
  }

  private static void check(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
    checks++;
  }
}
