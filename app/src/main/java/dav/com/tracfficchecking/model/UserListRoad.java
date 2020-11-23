package dav.com.tracfficchecking.model;

public class UserListRoad {
    private String fullAddress;
    private String keyItem;

    public UserListRoad() {
    }

    public UserListRoad(String fullAddress, String keyItem) {
        this.fullAddress = fullAddress;
        this.keyItem = keyItem;
    }

    public String getKeyItem() {
        return keyItem;
    }

    public void setKeyItem(String keyItem) {
        this.keyItem = keyItem;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }
}
