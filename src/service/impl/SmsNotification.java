package service.impl;

import java.util.regex.Pattern;
import service.NotificationService;

public final class SmsNotification implements NotificationService {
  private static final Pattern PHONE = Pattern.compile("(?:\\+7|8)[0-9]{10}");

  @Override
  public void sendNotification(String message, String recipient) {
    if (recipient == null || !PHONE.matcher(recipient).matches()) {
      System.out.println("Предупреждение: неверный номер телефона. Чек не отправлен.");
      return;
    }
    System.out.printf("[SMS] На номер %s отправлено сообщение: %s%n", recipient, message);
  }
}
