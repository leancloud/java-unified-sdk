package cn.leancloud.im;

public interface SystemReporter {
  final class SystemInfo {
    String brand;
    String manufacturer;
    String model;
    String osCodeName;
    int osAPILevel;
    boolean runOnEmulator;

    public String getBrand() {
      return brand;
    }

    public void setBrand(String brand) {
      this.brand = brand;
    }

    public String getManufacturer() {
      return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
      this.manufacturer = manufacturer;
    }

    public String getModel() {
      return model;
    }

    public void setModel(String model) {
      this.model = model;
    }

    public String getOsCodeName() {
      return osCodeName;
    }

    public void setOsCodeName(String osCodeName) {
      this.osCodeName = osCodeName;
    }

    public int getOsAPILevel() {
      return osAPILevel;
    }

    public void setOsAPILevel(int osAPILevel) {
      this.osAPILevel = osAPILevel;
    }

    public boolean isRunOnEmulator() {
      return runOnEmulator;
    }

    public void setRunOnEmulator(boolean runOnEmulator) {
      this.runOnEmulator = runOnEmulator;
    }
  }
  SystemInfo getInfo();
}
