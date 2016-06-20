package www.yuanbenshengxian.com.yuanbenshengxian.utils;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.PreferencesCookieStore;
import com.orhanobut.logger.Logger;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;

import www.yuanbenshengxian.com.yuanbenshengxian.LoginActivity;
import www.yuanbenshengxian.com.yuanbenshengxian.MainActivity;
import www.yuanbenshengxian.com.yuanbenshengxian.common.AppContext;
import www.yuanbenshengxian.com.yuanbenshengxian.common.SystemState;
import www.yuanbenshengxian.com.yuanbenshengxian.diyview.LoadingDialog;

/**
 * Created by HJ on 2015/12/26.
 */
public  class XUtilsHelper<T> {

    private Context context;
    private Handler handler;
    private String url;
    AppContext mApp;
    /**
     * 返回文本的编码， 默认编码UTF-8
     */
    private HttpUtils httpUtils;

    private LoadingDialog loadingDialog;
    private LoadingDialog alertDialog;
    private ProgressDialog progressDialog;
    /**
     * 请求参数，默认编码UTF-8
     */
    private RequestParams requestParams;

    private String filename;
    String[][] param = new String[][]{};


    /**
     *  是否遇到error
     */
    boolean isError = false;


    int TAG_NOTHING = 0;
    public static int TAG_FAILURE = -1;
    public static int TAG_SUCCESS = 200;
    public static int TAG_NET_ERROR = 0;


    //当前请求标记
    int SEND_POSTWITHKEY = 1;
    int SEND_POSTWITHKEYAUTO = 2;
    int tagNow;
    /**
     * 构造方法
     *
     * @param context
     *            用于程序上下文，必须用当前Activity的this对象，否则报错
     * @param url
     *            网络资源地址
     * @param handler
     *            消息处理对象，用于请求完成后的怎么处理返回的结果数据
     */
    public XUtilsHelper(Context context, String url, Handler handler) {

        this.context = context;

        try {
            // 保存网络资源文件名，要在转码之前保存，否则是乱码
            filename = url.substring(url.lastIndexOf("/") + 1, url.length());
            // 解决中文乱码问题，地址中有中文字符造成乱码问题
            String old_url = URLEncoder.encode(url, "UTF-8");
            // 替换地址中的特殊字符
            String new_url = old_url.replace("%3A", ":").replace("%2F", "/")
                    .replace("%3F", "?").replace("%3D", "=")
                    .replace("%26", "&").replace("%2C", ",")
                    .replace("%20", " ").replace("+", "%20")
                    .replace("%2B", "+").replace("%23", "#")
                    .replace("#", "%23");
            this.url = new_url;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        this.httpUtils = XutilsHttpClient.getInstence(context);
        httpUtils.configCookieStore(NetworkTools.cookieStore);
        this.handler = handler;
        this.loadingDialog = new LoadingDialog(context);
        this.loadingDialog.getTextView().setText("正在加载");
        this.progressDialog = new ProgressDialog(context);
        this.alertDialog = new LoadingDialog(context);
        this.requestParams = new RequestParams(); // 编码与服务器端字符编码一致为utf-8
    }


    /**
     * get方法请求网络
     */
    public void sendGet() {
//        loadingDialog.show();
        httpUtils.configCurrentHttpCacheExpiry(20000);//设置请求缓存时间为5秒
        httpUtils.configCookieStore(NetworkTools.cookieStore);
        httpUtils.send(HttpRequest.HttpMethod.GET, url, requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {
                        Message msg = Message.obtain();

                        String resultStr = arg0.result;

                        if (arg0.statusCode == 200 || arg0.statusCode == 201) {
                            msg.what = TAG_SUCCESS;
                            msg.obj = resultStr;
                        } else {
                            msg.what = TAG_FAILURE;
                            msg.obj = resultStr;
                        }
                        loadingDialog.dismiss();
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        loadingDialog.dismiss();
                        arg0.printStackTrace();
                        Message msg = Message.obtain();
                        msg.what = TAG_NET_ERROR;
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onStart() {
                        super.onStart();
                    }

                    @Override
                    public void onCancelled() {
                        super.onCancelled();
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        super.onLoading(total, current, isUploading);
                    }
                });

    }




    public void  setUrlParams() {
        url = url + "?sessionKey="+getLocalKey.getLocalKey(context)+"&";

        for (int i = 0; i < param.length; i++){
            url += param[i][0] + "=" + param[i][1]+"&";
        }

        url = url.substring(0,url.length() -1);
    }

    /**
     * 设置请求参数
     */
    public void setRequestParams(String[][] params){
        param = params;


        String sessionKey = getLocalKey.getLocalKey(context);
        if (sessionKey == null || "".equals(sessionKey)){
            Utils.toast(context,"需要重新登录");
            clearData();
            context.startActivity(new Intent(context, LoginActivity.class));
            isError = true;
        }else {
            url = url + "?sessionKey="+getLocalKey.getLocalKey(context)+"&";
        }

        for (int i = 0; i < param.length; i++){
            url += param[i][0] + "=" + param[i][1]+"&";
        }

        url = url.substring(0,url.length() -1);
        /*try {
            String old_url = URLEncoder.encode(url, "UTF-8");

            // 替换地址中的特殊字符
            String new_url = old_url.replace("%3A", ":").replace("%2F", "/")
                    .replace("%3F", "?").replace("%3D", "=")
                    .replace("%26", "&").replace("%2C", ",")
                    .replace("%20", " ").replace("+", "%20")
                    .replace("%2B", "+").replace("%23", "#")
                    .replace("#", "%23");
            this.url = new_url;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
    }


    /**
     * 设置文件参数
     */
    public void setFileRequestParames(ArrayList<File> files){

        for (int i = 0;i < files.size() ;i ++){
            if (i == 0) {
                requestParams.addBodyParameter("imags", files.get(i));
            } else {
                requestParams.addBodyParameter("imags" + i, files.get(i));
            }
        }
    }

    /**
     * 设置请求参数
     */
    public void setRequestParamsRealPost(String[][] params){
        param = params;
        String sessionKey = getLocalKey.getLocalKey(context);
        if (sessionKey == null || "".equals(sessionKey)){
            Utils.toast(context,"需要重新登录");
            clearData();
            context.startActivity(new Intent(context, LoginActivity.class));
            isError = true;
        }else {
            url = url;
        }

        requestParams.addBodyParameter("sessionKey", sessionKey);
        for (int i = 0; i < param.length; i++){
            requestParams.addBodyParameter(param[i][0],param[i][1]);
        }
    }


    /**
     * POST方式请求服务器资源 不用传,不自动解析
     */
    public void sendPostWithKey(){

        tagNow = SEND_POSTWITHKEY;
//        loadingDialog.show();
        httpUtils.configCurrentHttpCacheExpiry(20000);//设置请求缓存时间为5秒
        httpUtils.configCookieStore(NetworkTools.cookieStore);

        if (!isError){

            httpUtils.send(HttpRequest.HttpMethod.POST, url,
                new RequestCallBack<String>() {

                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {
                        Message msg = Message.obtain();
                        Logger.i("网络请求:url--->" + url + "\nresultStr---->" + arg0.result);

                        if (arg0.statusCode == 200 || arg0.statusCode == 201) {
                            String resultStr = arg0.result;
                            try {
                                JSONObject object = new JSONObject(resultStr);
                                if (object.has("errorCode")){
                                    if (object.getInt("errorCode")==904){//表示key过期了,重新获得

                                        Log.i("keyupdate","更新>>>>>>");
                                        //得到新的key存入本地
                                        getNewKey(null,null);
                                    }else if (object.getInt("errorCode")==901 || object.getInt("errorCode")==903){//需要重新登录
                                        Utils.toast(context,"需要重新登录");
                                        clearData();
                                        context.startActivity(new Intent(context, LoginActivity.class));
                                    }else if (object.getInt("errorCode") == 902){
                                        Utils.toast(context, "您没有访问权限");
                                        context.startActivity(new Intent(context,MainActivity.class));
                                        if (context instanceof Activity){
                                            ((Activity) context).finish();
                                        }
                                    }
                                }else if (object.getBoolean("state")){
                                    msg.obj = resultStr;
                                    msg.what = TAG_SUCCESS;
                                    handler.sendMessage(msg);
                                }else {
                                    Utils.toast(context,object.getString("msg"));
                                    loadingDialog.dismiss();
                                    msg.what = TAG_FAILURE;
                                    handler.sendMessage(msg);
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            loadingDialog.dismiss();


                        } else {
                            loadingDialog.dismiss();
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }

                    }

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        loadingDialog.dismiss();
                        if (arg0.getExceptionCode() == 904){//key不正确
                            getNewKey(null,null);
                        }else if (arg0.getExceptionCode()==901 || arg0.getExceptionCode()==903){//需要重新登录
                            Utils.toast(context,"需要重新登录");
                            clearData();
                            context.startActivity(new Intent(context, LoginActivity.class));
                        }else if (arg0.getExceptionCode() == 902){
                            Utils.toast(context, "您没有访问权限");
                            context.startActivity(new Intent(context,MainActivity.class));
                            if (context instanceof Activity){
                                ((Activity) context).finish();
                            }
                        }else {
                            Utils.toast(context, "网络信号较差，请稍后再试");
                            arg0.printStackTrace();
                            Message msg = Message.obtain();
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }

                    }
                });
    }}

    /**
     * POST方式请求服务器资源 不用传,不自动解析
     */
    public void sendPostWithKeyRealPost() {

        tagNow = SEND_POSTWITHKEY;
        loadingDialog.show();
        loadingDialog.getTextView().setText("正在加载");
        httpUtils.configCurrentHttpCacheExpiry(20000);//设置请求缓存时间为5秒
        httpUtils.configCookieStore(NetworkTools.cookieStore);

        if (!isError){
            httpUtils.send(HttpRequest.HttpMethod.POST, url,requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {

                        Message msg = Message.obtain();
                        Logger.i("网络请求:url--->" + url + "\nresultStr---->" + arg0.result);
                        if (arg0.statusCode == 200 || arg0.statusCode == 201) {
                            String resultStr = arg0.result;
                            try {
                                JSONObject object = new JSONObject(resultStr);
                                if (object.has("errorCode")){
                                    if (object.getInt("errorCode")==904){//表示key过期了,重新获得

                                        Log.i("keyupdate","更新>>>>>>");
                                        //得到新的key存入本地
                                        getNewKey(null,null);
                                    }else if (object.getInt("errorCode")==901 || object.getInt("errorCode")==903){//需要重新登录
                                        Utils.toast(context,"需要重新登录");
                                        clearData();
                                        context.startActivity(new Intent(context, LoginActivity.class));
                                    }else if (object.getInt("errorCode") == 902){
                                        Utils.toast(context, "您没有访问权限");
                                        context.startActivity(new Intent(context,MainActivity.class));
                                        if (context instanceof Activity){
                                            ((Activity) context).finish();
                                        }
                                    }
                                }else if (object.getBoolean("state")){
                                    msg.obj = resultStr;
                                    msg.what = TAG_SUCCESS;
                                    handler.sendMessage(msg);
                                }else {
                                    Utils.toast(context,object.getString("msg"));
                                    loadingDialog.dismiss();
                                    msg.what = TAG_FAILURE;
                                    handler.sendMessage(msg);
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            loadingDialog.dismiss();


                        } else {
                            loadingDialog.dismiss();
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }

                    }

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        loadingDialog.dismiss();
                        if (arg0.getExceptionCode() == 904){//key不正确
                            getNewKey(null,null);
                        }else if (arg0.getExceptionCode()==901 || arg0.getExceptionCode()==903){//需要重新登录
                            Utils.toast(context,"需要重新登录");
                            clearData();
                            context.startActivity(new Intent(context, LoginActivity.class));
                        }else if (arg0.getExceptionCode() == 902){
                            Utils.toast(context, "您没有访问权限");
                            context.startActivity(new Intent(context,MainActivity.class));
                            if (context instanceof Activity){
                                ((Activity) context).finish();
                            }
                        }else {
                            Utils.toast(context, "网络信号较差，请稍后再试");
                            arg0.printStackTrace();
                            Message msg = Message.obtain();
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }

                    }
                });
    }}

    /**
     * POST方式请求服务器资源 不用传,不自动解析
     */
    public void sendPostWithKeyRealPostUpload() {

        tagNow = SEND_POSTWITHKEY;
        loadingDialog.show();
        loadingDialog.getTextView().setText("正在提交");
        httpUtils.configCurrentHttpCacheExpiry(20000);//设置请求缓存时间为5秒
        httpUtils.configCookieStore(NetworkTools.cookieStore);

        if (!isError){
            httpUtils.send(HttpRequest.HttpMethod.POST, url,requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {

                        Message msg = Message.obtain();
                        Logger.i("网络请求:url--->" + url + "\nresultStr---->" + arg0.result);
                        if (arg0.statusCode == 200 || arg0.statusCode == 201) {
                            String resultStr = arg0.result;
                            try {
                                JSONObject object = new JSONObject(resultStr);
                                if (object.has("errorCode")){
                                    if (object.getInt("errorCode")==904){//表示key过期了,重新获得

                                        Log.i("keyupdate","更新>>>>>>");
                                        //得到新的key存入本地
                                        getNewKey(null,null);
                                    }else if (object.getInt("errorCode")==901 || object.getInt("errorCode")==903){//需要重新登录
                                        Utils.toast(context,"需要重新登录");
                                        clearData();
                                        context.startActivity(new Intent(context, LoginActivity.class));
                                    }else if (object.getInt("errorCode") == 902){
                                        Utils.toast(context, "您没有访问权限");
                                        context.startActivity(new Intent(context,MainActivity.class));
                                        if (context instanceof Activity){
                                            ((Activity) context).finish();
                                        }
                                    }
                                }else if (object.getBoolean("state")){
                                    msg.obj = resultStr;
                                    msg.what = TAG_SUCCESS;
                                    handler.sendMessage(msg);
                                }else {
                                    Utils.toast(context,object.getString("msg"));
                                    loadingDialog.dismiss();
                                    msg.what = TAG_FAILURE;
                                    handler.sendMessage(msg);
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            loadingDialog.dismiss();


                        } else {
                            loadingDialog.dismiss();
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }

                    }

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        loadingDialog.dismiss();
                        if (arg0.getExceptionCode() == 904){//key不正确
                            getNewKey(null,null);
                        }else if (arg0.getExceptionCode()==901 || arg0.getExceptionCode()==903){//需要重新登录
                            Utils.toast(context,"需要重新登录");
                            clearData();
                            context.startActivity(new Intent(context, LoginActivity.class));
                        }else if (arg0.getExceptionCode() == 902){
                            Utils.toast(context, "您没有访问权限");
                            context.startActivity(new Intent(context,MainActivity.class));
                            if (context instanceof Activity){
                                ((Activity) context).finish();
                            }
                        }else {
                            Utils.toast(context, "网络信号较差，请稍后再试");
                            arg0.printStackTrace();
                            Message msg = Message.obtain();
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }

                    }
                });
    }}
    /**
     * POST方式请求服务器资源 不用传,不自动解析
     */
    public void sendPostWithKeyRealPostNoDialog() {

        tagNow = SEND_POSTWITHKEY;
        httpUtils.configCurrentHttpCacheExpiry(20000);//设置请求缓存时间为5秒
        httpUtils.configCookieStore(NetworkTools.cookieStore);

        if (!isError){
            httpUtils.send(HttpRequest.HttpMethod.POST, url,requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {
                        Message msg = Message.obtain();
                        Logger.i("网络请求:url--->" + url + "\nresultStr---->" + arg0.result);

                        if (arg0.statusCode == 200 || arg0.statusCode == 201) {
                            String resultStr = arg0.result;
                            try {
                                JSONObject object = new JSONObject(resultStr);
                                if (object.has("errorCode")){
                                    if (object.getInt("errorCode")==904){//表示key过期了,重新获得

                                        Log.i("keyupdate","更新>>>>>>");
                                        //得到新的key存入本地
                                        getNewKey(null,null);
                                    }else if (object.getInt("errorCode")==901 || object.getInt("errorCode")==903){//需要重新登录
                                        Utils.toast(context,"需要重新登录");
                                        clearData();
                                        context.startActivity(new Intent(context, LoginActivity.class));
                                    }else if (object.getInt("errorCode") == 902){
                                        Utils.toast(context, "您没有访问权限");
                                        context.startActivity(new Intent(context,MainActivity.class));
                                        if (context instanceof Activity){
                                            ((Activity) context).finish();
                                        }
                                    }
                                }else if (object.getBoolean("state")){
                                    msg.obj = resultStr;
                                    msg.what = TAG_SUCCESS;
                                    handler.sendMessage(msg);
                                }else {
                                    Utils.toast(context, object.getString("msg"));
                                    msg.what = TAG_FAILURE;
                                    handler.sendMessage(msg);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        } else {
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }

                    }

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {

                        if (arg0.getExceptionCode() == 904){//key不正确
                            getNewKey(null,null);
                        }else if (arg0.getExceptionCode()==901 || arg0.getExceptionCode()==903){//需要重新登录
                            Utils.toast(context,"需要重新登录");
                            clearData();
                            context.startActivity(new Intent(context, LoginActivity.class));
                        }else if (arg0.getExceptionCode() == 902){
                            Utils.toast(context, "您没有访问权限");
                            context.startActivity(new Intent(context,MainActivity.class));
                            if (context instanceof Activity){
                                ((Activity) context).finish();
                            }
                        }else {
                            Utils.toast(context, "网络信号较差，请稍后再试");
                            arg0.printStackTrace();
                            Message msg = Message.obtain();
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }

                    }
                });
    }}


    /**
     * POST方式请求服务器资源 不用传,不自动解析
     */
    public void sendPostWithKeyNoDialog() {
        tagNow = SEND_POSTWITHKEY;
        httpUtils.configCurrentHttpCacheExpiry(20000);//设置请求缓存时间为15秒
        httpUtils.configCookieStore(NetworkTools.cookieStore);

        if (!isError){
        httpUtils.send(HttpRequest.HttpMethod.POST, url,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {
                        Message msg = Message.obtain();
                        Logger.i("网络请求:url--->" + url + "\nresultStr---->" + arg0.result);

                        if (arg0.statusCode == 200 || arg0.statusCode == 201) {
                            String resultStr = arg0.result;
                            try {
                                JSONObject object = new JSONObject(resultStr);
                                if (object.has("errorCode")){
                                    if (object.getInt("errorCode")==904){//表示key过期了,重新获得

                                        Log.i("keyupdate","更新>>>>>>");
                                        //得到新的key存入本地
                                        getNewKey(null,null);
                                    }else if (object.getInt("errorCode")==901 || object.getInt("errorCode")==903){//需要重新登录
                                        Utils.toast(context, "需要重新登录");
                                        clearData();
                                        //
                                        context.startActivity(new Intent(context, LoginActivity.class));
                                    }else if (object.getInt("errorCode") == 902){
                                        Utils.toast(context, "您没有访问权限");
                                        context.startActivity(new Intent(context,MainActivity.class));
                                        if (context instanceof Activity){
                                            ((Activity) context).finish();
                                        }
                                    }
                                }else if (object.getBoolean("state")){
                                    msg.obj = resultStr;
                                    msg.what = TAG_SUCCESS;
                                    handler.sendMessage(msg);
                                }else {
                                    Utils.toast(context, object.getString("msg"));
                                    msg.what = TAG_FAILURE;
                                    handler.sendMessage(msg);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }

                    }

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        if (arg0.getExceptionCode() == 904){//key不正确
                            getNewKey(null,null);
                        }else if (arg0.getExceptionCode()==901 || arg0.getExceptionCode()==903){//需要重新登录
                            Utils.toast(context,"需要重新登录");
                            clearData();
                            context.startActivity(new Intent(context, LoginActivity.class));
                        }else if (arg0.getExceptionCode() == 902){
                            Utils.toast(context, "您没有访问权限");
                            context.startActivity(new Intent(context,MainActivity.class));
                            if (context instanceof Activity){
                                ((Activity) context).finish();
                            }
                        }else {
                            Utils.toast(context, "网络信号较差，请稍后再试");
                            arg0.printStackTrace();
                            Message msg = Message.obtain();
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }

                    }
                });
    }}

    //清空SharedPrefrenced里面的数据
    public void clearData() {


        SharedPreferences sharedPreferences = context.getSharedPreferences("UserIF",
                Activity.MODE_PRIVATE);
        //清空
        sharedPreferences.edit().clear().commit();

    }



    /**
     * POST方式请求服务器资源 不用传,自动解析
     */
    public void sendPostAutoPaser(final T data, final Class<T> tClass) {
        tagNow = SEND_POSTWITHKEYAUTO;
        httpUtils.configCurrentHttpCacheExpiry(20000);//设置请求缓存时间为5秒
        httpUtils.configCookieStore(NetworkTools.cookieStore);

        if (!isError){
        httpUtils.send(HttpRequest.HttpMethod.POST, url,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {
                        Logger.i("网络请求:url--->" + url + "\nresultStr---->" + arg0.result);
                        Message msg = Message.obtain();
                        if (arg0.statusCode == 200 || arg0.statusCode == 201) {
                            String resultStr = arg0.result;
                            try {
                                JSONObject object = new JSONObject(resultStr);
                                if (object.has("errorCode")){
                                    if (object.getInt("errorCode")==904){//表示key过期了,重新获得
                                        Log.i("keyupdate","更新>>>>>>");
                                        //得到新的key存入本地
                                        getNewKey(data,tClass);
                                    }else if (object.getInt("errorCode")==901 || object.getInt("errorCode")==903){//需要重新登录
                                        Utils.toast(context,"需要重新登录");
                                        clearData();
                                        context.startActivity(new Intent(context, LoginActivity.class));
                                    }else if (object.getInt("errorCode") == 902){
                                        Utils.toast(context, "您没有访问权限");
                                        context.startActivity(new Intent(context,MainActivity.class));
                                        if (context instanceof Activity){
                                            ((Activity) context).finish();
                                        }
                                    }
                                }else if (object.getBoolean("state")){
                                    loadingDialog.dismiss();
                                    msg.obj = resultStr;
                                    msg.what = TAG_SUCCESS;
                                    loadingDialog.dismiss();
                                    new PaserDataTask<>(handler, resultStr,data,tClass,TAG_SUCCESS).execute();
                                }else {
                                    loadingDialog.dismiss();
                                    msg.what = TAG_FAILURE;
                                    handler.sendMessage(msg);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            loadingDialog.dismiss();
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }

                    }

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        loadingDialog.dismiss();
                        if (arg0.getExceptionCode() == 904){
                            getNewKey(data, tClass);
                        }else if (arg0.getExceptionCode()==901 || arg0.getExceptionCode()==903){//需要重新登录
                            Utils.toast(context,"需要重新登录");
                            clearData();
                            context.startActivity(new Intent(context, LoginActivity.class));
                        }else if (arg0.getExceptionCode() == 902){
                            Utils.toast(context, "您没有访问权限");
                            context.startActivity(new Intent(context,MainActivity.class));
                            if (context instanceof Activity){
                                ((Activity) context).finish();
                            }
                        }else {
                            Utils.toast(context, "网络信号较差，请稍后再试");
                            arg0.printStackTrace();
                            Message msg = Message.obtain();
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }


                    }
                });
    }}
    /**
     * RealPOST方式请求服务器资源 不用传,自动解析
     */
    public void sendPostAutoPaserReal(final T data, final Class<T> tClass) {
        tagNow = SEND_POSTWITHKEYAUTO;
        loadingDialog.show();
        httpUtils.configCurrentHttpCacheExpiry(20000);//设置请求缓存时间为5秒
        httpUtils.configCookieStore(NetworkTools.cookieStore);

        if (!isError){

        httpUtils.send(HttpRequest.HttpMethod.POST, url,requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {
                        Logger.i("网络请求:url--->" + url + "\nresultStr---->" + arg0.result);
                        Message msg = Message.obtain();
                        if (arg0.statusCode == 200 || arg0.statusCode == 201) {
                            String resultStr = arg0.result;
                            try {
                                JSONObject object = new JSONObject(resultStr);
                                if (object.has("errorCode")){
                                    if (object.getInt("errorCode")==904){//表示key过期了,重新获得
                                        Log.i("keyupdate","更新>>>>>>");
                                        //得到新的key存入本地
                                        getNewKey(data,tClass);
                                    }else if (object.getInt("errorCode")==901 || object.getInt("errorCode")==903){//需要重新登录
                                        Utils.toast(context,"需要重新登录");
                                        clearData();
                                        context.startActivity(new Intent(context, LoginActivity.class));
                                    }else if (object.getInt("errorCode") == 902){
                                        Utils.toast(context, "您没有访问权限");
                                        context.startActivity(new Intent(context,MainActivity.class));
                                        if (context instanceof Activity){
                                            ((Activity) context).finish();
                                        }
                                    }
                                }else if (object.getBoolean("state")){
                                    loadingDialog.dismiss();
                                    msg.obj = resultStr;
                                    msg.what = TAG_SUCCESS;
                                    loadingDialog.dismiss();
                                    new PaserDataTask<>(handler, resultStr,data,tClass,TAG_SUCCESS).execute();
                                }else {
                                    loadingDialog.dismiss();
                                    msg.what = TAG_FAILURE;
                                    handler.sendMessage(msg);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            loadingDialog.dismiss();
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }

                    }

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        loadingDialog.dismiss();
                        if (arg0.getExceptionCode() == 904){
                            getNewKey(data, tClass);
                        }else if (arg0.getExceptionCode()==901 || arg0.getExceptionCode()==903){//需要重新登录
                            Utils.toast(context,"需要重新登录");
                            clearData();
                            context.startActivity(new Intent(context, LoginActivity.class));
                        }else if (arg0.getExceptionCode() == 902){
                            Utils.toast(context, "您没有访问权限");
                            context.startActivity(new Intent(context,MainActivity.class));
                            if (context instanceof Activity){
                                ((Activity) context).finish();
                            }
                        }else {
                            Utils.toast(context, "网络信号较差，请稍后再试");
                            arg0.printStackTrace();
                            Message msg = Message.obtain();
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }


                    }
                });
    }}


    /**
     * POST方式请求服务器资源 不用传,自动解析
     */
    public void sendPostAutoPaserNoDialog(final T data, final Class<T> tClass) {
        tagNow = SEND_POSTWITHKEYAUTO;
        httpUtils.configCurrentHttpCacheExpiry(20000);//设置请求缓存时间为5秒
        httpUtils.configCookieStore(NetworkTools.cookieStore);
        if (!isError){

        httpUtils.send(HttpRequest.HttpMethod.POST, url,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {
                        Logger.i("网络请求:url--->" + url + "\nresultStr---->" + arg0.result);
                        Message msg = Message.obtain();
                        if (arg0.statusCode == 200 || arg0.statusCode == 201) {
                            String resultStr = arg0.result;
                            try {
                                JSONObject object = new JSONObject(resultStr);
                                if (object.has("errorCode")){
                                    if (object.getInt("errorCode")==904){//表示key过期了,重新获得
                                        Log.i("keyupdate","更新>>>>>>");
                                        //得到新的key存入本地
                                        getNewKey(data,tClass);
                                    }else if (object.getInt("errorCode")==901 || object.getInt("errorCode")==903){//需要重新登录
                                        Utils.toast(context,"需要重新登录");
                                        clearData();
                                        context.startActivity(new Intent(context, LoginActivity.class));
                                    }else if (object.getInt("errorCode") == 902){
                                        Utils.toast(context, "您没有访问权限");
                                        context.startActivity(new Intent(context,MainActivity.class));
                                        if (context instanceof Activity){
                                            ((Activity) context).finish();
                                        }
                                    }
                                }else if (object.getBoolean("state")){
                                    msg.obj = resultStr;
                                    msg.what = TAG_SUCCESS;
                                    new PaserDataTask<>(handler, resultStr,data,tClass,TAG_SUCCESS).execute();
                                }else {
                                    msg.what = TAG_FAILURE;
                                    handler.sendMessage(msg);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }

                    }

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        if (arg0.getExceptionCode() == 904){
                            getNewKey(data, tClass);
                        }else if (arg0.getExceptionCode()==901 || arg0.getExceptionCode()==903){//需要重新登录
                            Utils.toast(context,"需要重新登录");
                            clearData();
                            context.startActivity(new Intent(context, LoginActivity.class));
                        }else if (arg0.getExceptionCode() == 902){
                            Utils.toast(context, "您没有访问权限");
                            context.startActivity(new Intent(context,MainActivity.class));
                            if (context instanceof Activity){
                                ((Activity) context).finish();
                            }
                        }else {
                            Utils.toast(context, "网络信号较差，请稍后再试");
                            arg0.printStackTrace();
                            Message msg = Message.obtain();
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }


                    }
                });
    }}

    /**
     * Real POST方式请求服务器资源 不用传,自动解析
     */
    public void sendPostAutoPaserNoDialogReal(final T data, final Class<T> tClass) {
        tagNow = SEND_POSTWITHKEYAUTO;
        httpUtils.configCurrentHttpCacheExpiry(20000);//设置请求缓存时间为5秒
        httpUtils.configCookieStore(NetworkTools.cookieStore);

        if (!isError){
        httpUtils.send(HttpRequest.HttpMethod.POST, url,requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {
                        Logger.i("网络请求:url--->" + url + "\nresultStr---->" + arg0.result);
                        Message msg = Message.obtain();
                        if (arg0.statusCode == 200 || arg0.statusCode == 201) {
                            String resultStr = arg0.result;
                            try {
                                JSONObject object = new JSONObject(resultStr);
                                if (object.has("errorCode")){
                                    if (object.getInt("errorCode")==904){//表示key过期了,重新获得
                                        Log.i("keyupdate","更新>>>>>>");
                                        //得到新的key存入本地
                                        getNewKey(data,tClass);
                                    }else if (object.getInt("errorCode")==901 || object.getInt("errorCode")==903){//需要重新登录
                                        Utils.toast(context,"需要重新登录");
                                        clearData();
                                        context.startActivity(new Intent(context, LoginActivity.class));
                                    }else if (object.getInt("errorCode") == 922){
                                        Utils.toast(context, "您没有访问权限");
                                        context.startActivity(new Intent(context,MainActivity.class));
                                        if (context instanceof Activity){
                                            ((Activity) context).finish();
                                        }
                                    }
                                }else if (object.getBoolean("state")){
                                    msg.obj = resultStr;
                                    msg.what = TAG_SUCCESS;
                                    new PaserDataTask<>(handler, resultStr,data,tClass,TAG_SUCCESS).execute();

                                    //保存cookie
                                    DefaultHttpClient httpClient = (DefaultHttpClient) httpUtils.getHttpClient();
                                    NetworkTools.cookieStore =  httpClient.getCookieStore();
                                }else {
                                    Utils.toast(context,object.optString("msg"));
                                    msg.what = TAG_FAILURE;
                                    handler.sendMessage(msg);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }

                    }

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {

                        if (arg0.getExceptionCode() == 904){
                            getNewKey(data, tClass);
                        }else if (arg0.getExceptionCode()==901 || arg0.getExceptionCode()==903){//需要重新登录
                            Utils.toast(context,"需要重新登录");
                            clearData();
                            context.startActivity(new Intent(context, LoginActivity.class));
                        }else if (arg0.getExceptionCode() == 902){
                            Utils.toast(context, "您没有访问权限");
                            context.startActivity(new Intent(context,MainActivity.class));
                            if (context instanceof Activity){
                                ((Activity) context).finish();
                            }
                        }else {
                            Utils.toast(context, "网络信号较差，请稍后再试");
                            arg0.printStackTrace();
                            Message msg = Message.obtain();
                            msg.what = TAG_FAILURE;
                            handler.sendMessage(msg);
                        }


                    }
                });
    }
    }


    /**
     * 设置登录注册参数
     */
    public void setParamsLogin(String[][] paramsLogin){
        requestParams.addBodyParameter(paramsLogin[0][0], paramsLogin[0][1]);
        requestParams.addBodyParameter(paramsLogin[1][0], paramsLogin[1][1]);
//        url += "?"  + paramsLogin[0][0] + "=" + paramsLogin[0][1] + "&" + paramsLogin[1][0] + "=" + paramsLogin[1][1];
    }


    /**
     * POST方式请求服务器资源
     */
    public void sendPostLogin() {
        loadingDialog.show();
        httpUtils.configCurrentHttpCacheExpiry(20000);//设置请求缓存时间为15秒


        httpUtils.send(HttpRequest.HttpMethod.POST, url, requestParams,
                new RequestCallBack<String>() {

                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {
                        Message msg = Message.obtain();
                        Log.i("url", "url>>" + url);
                        Logger.i("登录返回Str--->" + arg0.result);
                        if (arg0.statusCode == 200 || arg0.statusCode == 201) {
                            String resultStr = arg0.result;
                            try {
                                JSONObject object = new JSONObject(resultStr);
                                if (object.has("errorCode")) {
                                    if (object.getInt("errorCode") == 904) {//表示key过期了,重新获得


                                        //得到新的key存入本地
//                                        getNewKey(null,null);

                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //保存cookie
                            DefaultHttpClient httpClient = (DefaultHttpClient) httpUtils.getHttpClient();
                            NetworkTools.cookieStore =  httpClient.getCookieStore();

                            msg.obj = resultStr;
                            msg.what = TAG_SUCCESS;

                            loadingDialog.dismiss();


                        } else {
                            loadingDialog.dismiss();
                            msg.what = TAG_FAILURE;
                        }
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        loadingDialog.dismiss();
                        Utils.toast(context, "网络信号较差，请稍后再试");
                        arg0.printStackTrace();
                        Message msg = Message.obtain();
                        msg.what = TAG_FAILURE;
                        handler.sendMessage(msg);
                    }
                });

    }


    /**
     * 得到新的key
     * @return
     */
    public void getNewKey(final T Result, final Class<T> tClass) {

        Logger.v("刷新新key：oldkey------------>" + getLocalKey.getLocalKey(context));
        String url1 = SystemState.Root_Url + SystemState.update_key + "?sessionKey=" + getLocalKey.getLocalKey(context);
        httpUtils.configCookieStore(NetworkTools.cookieStore);
        httpUtils.send(HttpRequest.HttpMethod.POST, url1,
                new RequestCallBack<String>() {

                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {
                        Message msg = Message.obtain();
                        if (arg0.statusCode == 200 || arg0.statusCode == 201) {
                            String data = arg0.result;
                            Log.i("i", "正在更新更新>>data>>" + data);
                            try {
                                JSONObject object = new JSONObject(data);
                                int errorCode = object.optInt("errorCode");
                                if (errorCode == 903 || errorCode == 901){
                                    Utils.toast(context,"需要重新登录");
                                    clearData();
                                    context.startActivity(new Intent(context, LoginActivity.class));
                                }else {
                                    String key = object.getJSONObject("data").getString("key");
                                    //存到本地sharedprenced
                                    //实例化SharedPreferences对象（第一步）
                                    key = DES.decryptDES(key);

                                    SharedPreferences mySharedPreferences= context.getSharedPreferences("UserIF",
                                            Activity.MODE_PRIVATE);
                                    //实例化SharedPreferences.Editor对象（第二步）
                                    SharedPreferences.Editor editor = mySharedPreferences.edit();
                                    //用putString的方法保存数据
                                    editor.putString("Key", key);
                                    Logger.v("刷新新key：newkey------------>" + getLocalKey.getLocalKey(context));

                                    //提交当前数据
                                    editor.commit();

                                    if (tagNow == SEND_POSTWITHKEY){
                                        url = url.split("[?]")[0];
                                        setUrlParams();
                                        sendPostWithKey();
                                    }else if (tagNow == SEND_POSTWITHKEYAUTO){
                                        url = url.split("[?]")[0];
                                        setUrlParams();
                                        sendPostAutoPaser(Result,tClass);
                                    }
                                }


                            } catch (JSONException e) {
                                Utils.toast(context,"需要重新登录");
                                clearData();
                                context.startActivity(new Intent(context, LoginActivity.class));
                                Logger.i(e.getMessage());
                            }


                        } else if (arg0.statusCode == 904){
                           getNewKey(null,null);
                        }
                    }

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        loadingDialog.dismiss();
                        if (arg0.getExceptionCode() == 904){//key不正确
                            getNewKey(null,null);
                        }else if (arg0.getExceptionCode()==901 || arg0.getExceptionCode()==903){//需要重新登录
                            Utils.toast(context,"需要重新登录");
                            clearData();
                            context.startActivity(new Intent(context, LoginActivity.class));
                        }else {
                            Utils.toast(context, "网络信号较差，请稍后再试");
                            arg0.printStackTrace();
                            Message msg = Message.obtain();
                        }

                    }
                });

        /*XUtilsHelper helper = new XUtilsHelper(context,url1 ,new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == XUtilsHelper.TAG_SUCCESS){

                    String data = (String) msg.obj;
                    Log.i("i","正在更新更新>>data>>"+data);
                    try {
                        JSONObject object = new JSONObject(data);

                        String key = object.getJSONObject("data").getString("key");

                        //存到本地sharedprenced
                        //实例化SharedPreferences对象（第一步）
                        key = DES.decryptDES(key);

                        SharedPreferences mySharedPreferences= context.getSharedPreferences("UserIF",
                                Activity.MODE_PRIVATE);
                        //实例化SharedPreferences.Editor对象（第二步）
                        SharedPreferences.Editor editor = mySharedPreferences.edit();
                        //用putString的方法保存数据
                        editor.putString("Key", key);

                        //提交当前数据
                        editor.commit();

                    } catch (JSONException e) {

                    }
                    if (tagNow == SEND_POSTWITHKEY){
                        url = url.split("[？]")[0];
                        setUrlParams();
                        sendPostWithKey();
                    }else if (tagNow == SEND_POSTWITHKEYAUTO){
                        url = url.split("[？]")[0];
                        setUrlParams();
                        sendPostAutoPaser(Result,tClass);
                    }
                }else if (msg.what == XUtilsHelper.TAG_FAILURE){


                }else if (msg.what == XUtilsHelper.TAG_NET_ERROR){

                }
                return false;
            }
        }));
        helper.sendPostWithKey();*/

    }


    /**
     * delet方式请求服务器资源
     */
    public void sendDelete() {


        loadingDialog.show();
        httpUtils.configCurrentHttpCacheExpiry(20000);//设置请求缓存时间为5秒

        httpUtils.send(HttpRequest.HttpMethod.DELETE, url,
                new RequestCallBack<String>() {

                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {
                        Message msg = Message.obtain();
                        if (arg0.statusCode == 200) {

                            String resultStr = arg0.result;
                            try {
                                JSONObject jsonObject = new JSONObject(resultStr);
                                if (jsonObject.getBoolean("result")){
                                    msg.obj = resultStr;
                                    msg.what = TAG_SUCCESS;
                                }else {

                                    msg.what = TAG_FAILURE;
                                }

                                loadingDialog.dismiss();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            loadingDialog.dismiss();
                            msg.what = TAG_FAILURE;
                        }
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        loadingDialog.dismiss();

                        arg0.printStackTrace();
                        Message msg = Message.obtain();
                        msg.what = TAG_FAILURE;
                        handler.sendMessage(msg);
                    }
                });
    }

    public void addHeader(){
        requestParams.addHeader("apikey", SystemState.weatherApiKey);
    }

    /**
     * 上传文件到服务器
     *
     * @param param
     *            提交参数名称
     * @param file
     *            要上传的文件对象
     */
    public void uploadFile(String param, File file) {
        progressDialog.setTitle("上传文件中，请稍等...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        // 设置进度条风格，风格为水平进度条
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        requestParams.addBodyParameter(param, file);
        httpUtils.configCurrentHttpCacheExpiry(20000);//设置请求缓存时间为5秒

        httpUtils.send(HttpRequest.HttpMethod.POST, url, requestParams,
                new RequestCallBack<String>() {

                    @Override
                    public void onStart() {
                        progressDialog.show();
                    }

                    @Override
                    public void onLoading(long total, long current,
                                          boolean isUploading) {
                        // 设置ProgressDialog 的进度条是否不明确 false 就是不设置为不明确
                        progressDialog.setIndeterminate(false);
                        progressDialog.setProgress((int) current);
                        progressDialog.setMax((int) total);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {
                        System.out.println(arg0.statusCode);
                        System.out.println(arg0.result);
                        progressDialog.dismiss();
                        Message msg = Message.obtain();
                        msg.obj = arg0.result;
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        progressDialog.dismiss();
                        arg0.printStackTrace();
                        Message msg = Message.obtain();
                        msg.what = TAG_NET_ERROR;
                        handler.sendMessage(msg);
                    }
                });
    }

    /**
     * 从服务器上下载文件保存到系统磁盘上
     *
     * @param saveLocation
     *            下载的文件保存路径
     * @param downloadBtn
     *            触发下载操作的控件按钮，用于设置下载进度情况
     */
    public void downloadFile(String saveLocation, final Button downloadBtn) {
        httpUtils.configCurrentHttpCacheExpiry(20000);//设置请求缓存时间为5秒

        httpUtils.download(url, saveLocation + filename,
                new RequestCallBack<File>() {

                    @Override
                    public void onStart() {
                        downloadBtn.setText("连接服务器中...");
                    }

                    @Override
                    public void onLoading(long total, long current,
                                          boolean isUploading) {
                        DecimalFormat df = new DecimalFormat("#.##");
                        downloadBtn.setText("下载中... "
                                + df.format((double) current / 1024 / 1024)
                                + "MB/"
                                + df.format((double) total / 1024 / 1024)
                                + "MB");
                    }

                    @Override
                    public void onSuccess(ResponseInfo<File> arg0) {
                        downloadBtn.setText("打开文件");
                        Toast.makeText(context, "下载成功！文件（"
                                + arg0.result.getAbsolutePath()
                                + "）保存在内部存储的Educ文件夹下。",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        progressDialog.dismiss();
                        arg0.printStackTrace();
                        Toast.makeText(context, "下载失败，请重试！",Toast.LENGTH_SHORT).show();
                        downloadBtn.setText("下载附件");
                    }
                });
    }

    /**
     * 从服务器上下载文件保存到系统磁盘上，此方法会弹出进度对话框显示下载进度信息（
     * 有的需要知道文件是否下载完成，如果下载完成返回的是改文件在磁盘中的完整路径）
     *
     * @param saveLocation
     *            下载的文件保存路径
     */
    public void downloadFile(String saveLocation) {
        progressDialog.setTitle("下载中，请稍等...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        // 设置进度条风格，风格为水平进度条
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // 设置ProgressDialog 的进度条是否不明确 false 就是不设置为不明确
        progressDialog.setIndeterminate(false);
        httpUtils.configCurrentHttpCacheExpiry(20000);//设置请求缓存时间为5秒

        httpUtils.download(url, saveLocation + filename,
                new RequestCallBack<File>() {

                    @Override
                    public void onStart() {
                        progressDialog.show();
                    }

                    @Override
                    public void onLoading(long total, long current,
                                          boolean isUploading) {
                        progressDialog.setProgress((int) current);
                        progressDialog.setMax((int) total);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<File> arg0) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "下载成功！文件（"
                                + arg0.result.getAbsolutePath()
                                + "）保存在内部存储的Educ文件夹下。",Toast.LENGTH_LONG).show();
                        if (handler != null) {
                            Message msg = Message.obtain();
                            msg.obj = arg0.result.getAbsoluteFile();
                            handler.sendMessage(msg);
                        }
                    }

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        progressDialog.dismiss();
                        arg0.printStackTrace();
                        Toast.makeText(context, "下载失败，请重试！",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private class Configs {



    }
}

/**
 * 单例模式获取HttpUtils对象
 *
 * @author Shyky
 * @date 2014-11-19
 */
class XutilsHttpClient {

    private static HttpUtils client;

    /**
     * 单例模式获取实例对象
     *
     * @param context
     *            应用程序上下文
     * @return HttpUtils对象实例
     */
    public synchronized static HttpUtils getInstence(Context context) {
        if (client == null) {
            // 设置请求超时时间为10秒
            client = new HttpUtils(1000 * 20);
            client.configSoTimeout(1000 * 20);
            client.configResponseTextCharset("UTF-8");
            // 保存服务器端(Session)的Cookie
            PreferencesCookieStore cookieStore = new PreferencesCookieStore(
                    context);
            cookieStore.clear(); // 清除原来的cookie
            client.configCookieStore(cookieStore);
        }
        return client;
    }

}