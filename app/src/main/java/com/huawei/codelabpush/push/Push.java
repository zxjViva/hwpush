package com.huawei.codelabpush.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * https://developer.huawei.com/consumer/cn/doc/development/HMS-3-Guides/push-basic-client
 */
public class Push {
    private static Push push = new Push();
    public static final String ACTION_ON_NEW_TOKEN = "com.huawei.phoneservice.onnewtoken";
    private Push() {

    }

    public static Push getInstance() {
        return push;
    }

    private Set<MessageReceiver> receivers = new HashSet<>();

    //是个耗时方法，需要放在子线程

    /**
     * 2、EMUI10.0及以上版本的华为设备上，getToken接口直接返回token。如果当次调用失败PUSH会自动重试申请，成功后则以onNewToken接口返回。
     *
     * 3、低于EMUI10.0的设备上，getToken接口如果返回为空，结果后续以onNewToken接口返回。
     * @param context
     * @return
     * @throws ApiException
     */
    public String getToken(final Context context) throws ApiException, InterruptedException, ExecutionException, TimeoutException {
        FutureTask<String> task = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final StringBuffer buffer = new StringBuffer();
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ACTION_ON_NEW_TOKEN);
                LocalBroadcastManager.getInstance(context).registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (intent != null && intent.hasExtra("token")){
                            buffer.append(intent.getStringExtra("token"));
                        }
                        countDownLatch.countDown();
                    }
                },intentFilter);
                countDownLatch.await();
                return buffer.toString();
            }
        });
        new Thread(task).start();
        String appId = AGConnectServicesConfig.fromContext(context).getString("client/app_id");
        String token = HmsInstanceId.getInstance(context).getToken(appId, "HCM");
        //如果正常token拿不到，就等10s 看看onNewToken 能不能拿到token
        if (TextUtils.isEmpty(token)){
            token = task.get(10, TimeUnit.SECONDS);
        }
        return token;
    }

    //是个耗时方法，需要放在子线程
    public void deletToken(Context context) throws ApiException {
        String appId = AGConnectServicesConfig.fromContext(context).getString("client/app_id");
        HmsInstanceId.getInstance(context).deleteToken(appId, "HCM");
    }

    public void registReceiver(MessageReceiver receiver) {
        receivers.add(receiver);
    }

    public void unregistReceiver(MessageReceiver receiver) {
        receivers.remove(receiver);
    }

    protected Set<MessageReceiver> getReceivers() {
        return receivers;
    }

    protected void refreshedTokenToServer(String token){

    }

}
