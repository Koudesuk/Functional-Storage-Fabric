package com.koudesuk.functionalstorage.inventory;

public interface ILockable {
    boolean isLocked();

    void setLocked(boolean locked);
}
