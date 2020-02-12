package com.huawei.codelabpush.push;

import com.huawei.hms.push.RemoteMessage;

public abstract class MessageReceiver {
    abstract void onReceive(RemoteMessage message);
}
