/*
 *  Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.huawei.codelabpush.push;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;
import com.huawei.hms.push.SendException;

public class PushService extends HmsMessageService {
    private static final String TAG = "PushService";


    @Override
    public void onNewToken(String token) {
        //会存在token 上报两次的情况，因为没法区分出是因为失效走的onnewToken 还是主动去拿的token
//        if (!TextUtils.isEmpty(token)) {
//            refreshedTokenToServer(token);
//        }
        Intent intent = new Intent();
        intent.setAction(Push.ACTION_ON_NEW_TOKEN);
        intent.putExtra("token", token);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
    }
//    private void refreshedTokenToServer(String token) {
//        Push.getInstance().refreshedTokenToServer(token);
//    }


    @Override
    public void onMessageReceived(RemoteMessage message) {
      if (message != null){
          for (MessageReceiver receiver : Push.getInstance().getReceivers()) {
              receiver.onReceive(message);
          }
      }
    }


    @Override
    public void onMessageSent(String msgId) {

    }

    @Override
    public void onSendError(String msgId, Exception exception) {

    }
}
