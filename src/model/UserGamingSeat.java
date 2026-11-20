package model;

/** Бронирование одного места частным клиентом компьютерного клуба. */
public final class UserGamingSeat extends GamingSeat {
  public UserGamingSeat(String name, double basePrice) {
    super(name, basePrice);
  }

  public UserGamingSeat(String name, double basePrice, String zone) {
    super(name, basePrice, zone);
  }

  @Override
  public double calculateBillingPrice() {
    return calculateSeatsPrice(1);
  }
}
