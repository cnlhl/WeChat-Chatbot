package com.example.xposed_wechatrobot;

import android.content.ContentValues;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Scanner;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class hook implements IXposedHookLoadPackage{

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (loadPackageParam.packageName.equals("com.tencent.mm")) {

            Class<?> b$1Class = XposedHelpers.findClass("com.tencent.mm.booter.notification.c$1", loadPackageParam.classLoader);
            XposedHelpers.findAndHookMethod(b$1Class, "handleMessage", Message.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Message message = (Message) param.args[0];
                    final String string = message.getData().getString("notification.show.talker");
                    String string2 = message.getData().getString("notification.show.message.content");
                    int i = message.getData().getInt("notification.show.message.type");
                    int i2 = message.getData().getInt("notification.show.tipsflag");
                    XposedBridge.log("senderinfo"+string+'-'+ string2+ '-'+i+ '-'+i2);
                    Class<?> gClass = XposedHelpers.findClass("com.tencent.mm.kernel.h",loadPackageParam.classLoader);
                    Object g = XposedHelpers.callStaticMethod(gClass, "aGF");
                    Object filedA = XposedHelpers.getObjectField(g, "hHG");

                    Class<?> tClass = XposedHelpers.findClass("com.tencent.mm.al.t", loadPackageParam.classLoader);
                    Class<?> qClass = XposedHelpers.findClass("com.tencent.mm.al.q", loadPackageParam.classLoader);
                    Method methodA = XposedHelpers.findMethodExact(tClass, "a", qClass, int.class);
                    Object o = XposedHelpers.callStaticMethod(tClass, "a", filedA);

                    // HTTP请求
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            String res=robot.getansewer(string2);
                            Class<?> iClass = XposedHelpers.findClass("com.tencent.mm.modelmulti.i", loadPackageParam.classLoader);
                            Object io = XposedHelpers.newInstance(
                                    iClass,
                                    new Class[]{String.class, String.class, int.class, int.class, Object.class},
                                    string,
                                    res,
                                    1,
                                    1,
                                    new HashMap<String, String>() {{ put(string, string); }}
                            );
                            Object[] pp = new Object[]{io, 0};
                            try {
                                XposedBridge.invokeOriginalMethod(methodA, o, pp);
                            } catch (Exception e) {
                                e.printStackTrace();
                                XposedBridge.log(e.getLocalizedMessage());
                            }
                            XposedBridge.log("send ok");
                        }
                    }).start();

                }
            });

        }

    }


}

class robot{
    public static String getansewer(String question) {
        String answer = "";
        try {
            String info = URLEncoder.encode(question, "utf-8");//处理字符串
            String getURL = "http://api.qingyunke.com/api.php?key=free&appid=0&msg=" + info;//网址拼接
            URL getUrl = new URL(getURL);
            HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            StringBuffer last = new StringBuffer();
            String line = "";
            while ((line = reader.readLine()) != null)
            {
                last.append(line);
            }
            reader.close();
            connection.disconnect();//获取结束，得到返回的json

            JSONObject object = new JSONObject(last.toString());
            answer = object.getString("content");
        } catch(IOException e) {
            e.printStackTrace();
        } catch(JSONException e) {
            e.printStackTrace();
        }//当遇到异常就抛出异常
        return answer;//返回结果
    }
}
