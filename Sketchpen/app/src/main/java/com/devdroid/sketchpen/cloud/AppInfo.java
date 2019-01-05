package com.devdroid.sketchpen.cloud;

public class AppInfo {
    private int storeVersion;
    private int outdatedVersion;
    private boolean forceUpdate;
    private String message;

    @Override
    public String toString() {
        return "AppInfo{" +
                "storeVersion=" + storeVersion +
                ", outdatedVersion=" + outdatedVersion +
                ", forceUpdate=" + forceUpdate +
                ", message='" + message + '\'' +
                '}';
    }

    public int getStoreVersion() {
        return storeVersion;
    }

    public void setStoreVersion(int storeVersion) {
        this.storeVersion = storeVersion;
    }

    public int getOutdatedVersion() {
        return outdatedVersion;
    }

    public void setOutdatedVersion(int outdatedVersion) {
        this.outdatedVersion = outdatedVersion;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
