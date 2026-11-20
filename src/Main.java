import billing.CyberArena;
import model.CorporateGamingSeat;
import model.GamingSeat;
import model.UserGamingSeat;
import model.VRGlasses;
import service.impl.EmailNotification;
import service.impl.SmsNotification;

/** Демонстрация двух типов заказов компьютерного клуба. */
public final class Main {
  private Main() {}

  public static void main(String[] args) {
    System.out.println("Компьютерный клуб. Блок 2, часть 2. Вариант 27");

    CyberArena personalCheckout = new CyberArena(new SmsNotification());
    GamingSeat personal = new UserGamingSeat("VIP-место", 1200, "VIP Zone");
    VRGlasses glasses = new VRGlasses("VR-очки клуба", 25);
    double personalPrice = personal.addVR(glasses, 10);
    personalCheckout.processPayment(personal, "+70000000000", personalPrice);

    CyberArena corporateCheckout = new CyberArena(new EmailNotification());
    GamingSeat corporate = new CorporateGamingSeat("Командная бронь на 5 мест", 1200, "BootCamp", 5);
    corporateCheckout.processPayment(corporate, "team@example.com", corporate.calculateBillingPrice());

    CyberArena.printGlobalStats();
  }
}
