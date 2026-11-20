/** Групповая бронь игровых мест организацией. */
public final class CorporateGamingSeat extends GamingSeat {
  private final int seatCount;

  public CorporateGamingSeat(String name, double basePrice, String zone, int seatCount) {
    super(name, basePrice, zone);
    if (seatCount < 1) {
      System.out.println("Предупреждение: для корпоративной брони требуется хотя бы одно место."
          + " Установлено одно место.");
      this.seatCount = 1;
    } else {
      this.seatCount = seatCount;
    }
  }

  public int getSeatCount() {
    return seatCount;
  }

  @Override
  public double calculateBillingPrice() {
    return calculateSeatsPrice(seatCount);
  }

  @Override
  public double calculateFinalPrice(VRGlasses option, int minutes) {
    System.out.println("Предупреждение: аренда VR для корпоративной брони недоступна."
        + " Применена стоимость игровых мест.");
    return calculateBillingPrice();
  }
}
