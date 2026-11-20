package service.impl;

import java.util.regex.Pattern;
import service.NotificationService;

public final class EmailNotification implements NotificationService {
  private static final Pattern EMAIL = Pattern.compile(
      "[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+", Pattern.UNICODE_CHARACTER_CLASS);

  @Override
  public void sendNotification(String message, String recipient) {
    if (recipient == null || !EMAIL.matcher(recipient).matches()) {
      System.out.println("Предупреждение: неверный адрес электронной почты. Чек не отправлен.");
      return;
    }
    System.out.printf("[EMAIL] На адрес %s отправлено письмо: %s%n", recipient, message);
  }
}
