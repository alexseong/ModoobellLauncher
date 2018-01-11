package com.modoobel.modoobelllauncher.custom_obj;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.modoobel.modoobelllauncher.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by luckyleeis on 2017. 1. 7..
 */

public class Common {

    public static int MAIN_BUTTON_COUNT = 6; // 메인화면에서 한페이지에 나오는 응대버튼 갯수

    public static String RESPONSE_ID = "response_id";
    public static String MESSAGE_ID = "msg_id";
    public static String DIALOG_ID = "dialog_id";
    public static String IS_ID_DATA = "is_id_data";
    public static String IS_START = "is_start";
    public static String FROM_BELL = "from_bell";
    public static String NAME = "from_name";
    public static String IS_AUTO = "is_auto";
    public static String IS_CLOSE_DIALOG = "is_close_dialog";
    public static String IS_SEND_DEVICE = "is_send_device"; // 푸시메시지에서 디바이스 등록 메시지 일때 적용
    public static String DEVICE_REG_OK = "device_reg_ok";
    public static String VIDEO_CALL_CHANEL = "video_call_chanel";
    public static String SEND_AUTO_RESPONSE_DATA = "send_auto_response_data";
    public static String REQUEST_AUTO_RESPONSE_DATA = "request_auto_response_data";
    public static String NOTIFICATION_KEY = "notification_key";
    public static String DEVICE_TOKEN = "device_token";
    public static String FROM = "from_token";

    public static String fcbUrl = "https://fcm.googleapis.com/fcm/send";
    public static String fcbKey = "AAAAx-bZZFs:APA91bG5pYvmAupfm5NLsr_RWlFA7H9xU5YdV7E_SqM29HkfPRStlJE5fJMXSNPUL40dyKUfsT140wvvHsRTv_51r5vAOD6KtiPcqxVVcg8vnELVTzYl3UjdVr83oLV9nWEkDqe77uyM";
    public static String playRtcKey = "60ba608a-e228-4530-8711-fa38004719c1";

    public static String BUNDLE_DEVICE_TOKEN = "token";
    public static String BUNDLE_DEVICE_NAME = "name";
    public static String BUNDLE_KEY_NAME = "key";

    public static int PASS_CODE_CHECK_OK = 1001;
    public static int PASS_CODE_CHECK_WRONG = 1002;


    public static String getRawDataToString(Context context, int raw)
    {
        InputStream inputStream = context.getResources().openRawResource(raw);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            int i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toString();
    }

    public static int getResourceId(Context context, String pVariableName, String pResourcename)
    {
        try {
            String pPackageName = context.getPackageName();
            return context.getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     * 대화를 시작한다.
     * @param context
     * @param dialogId 대화 아이디
     */

    public static void startDialog(Context context, long dialogId)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();
        ePref.putLong("status",dialogId);
        ePref.commit();

        Log.d("Tag","" + dialogId);
    }

    /**
     * 영상통화 채널 아이디를 저장한다.
     * @param context
     * @param chanelId
     */

    public static void setPlayRtcChanelId(Context context, String chanelId)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();
        ePref.putString(VIDEO_CALL_CHANEL,chanelId);
        ePref.commit();
    }


    /**
     * 영상통화 채널아이디를 가져온다.
     * @param context
     * @return
     */

    public static String getPlayRtcChanelId(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        return pref.getString(VIDEO_CALL_CHANEL,"");
    }


    /**
     * 대화를 종료 한다.
     * @param context
     */

    public static void closeDialog(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();
        ePref.remove("status");
        ePref.remove(VIDEO_CALL_CHANEL);
        ePref.commit();
    }


    /**
     * 현재 상태를 리턴한다.
     * @return 대화중 - (현재 대화중인 dialogId), 대화중이 아닐때 -1
     */

    public static long getCurrentStatus(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        return pref.getLong("status",-1);
    }

    /**
     * dialogId를 이용하여 대화상대 기기ID를 리턴한다.
     * @param context
     * @param dialogId
     * @return
     */

    public static String getPriviousDialogToID(Context context, String dialogId)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        try {
            JSONObject jObj = new JSONObject(pref.getString(dialogId,""));
            return jObj.getString("to_id");

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * dialogId를 이용하여 ResposeID(방문타입)를 리턴한다.
     * @param context
     * @param dialogId
     * @return
     */

    public static String getPriviousDialogResponseID(Context context, String dialogId)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        try {
            JSONObject jObj = new JSONObject(pref.getString(dialogId,""));
            return jObj.getString("responseId");

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * dialogId를 이용하여 대화를 리턴한다
     * @param dialogId
     * @return
     */

    public static ArrayList<MBDialogData> getPriviousDialogData(Context context, String dialogId)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        Gson gson = new Gson();

        try {
            JSONObject jObj = new JSONObject(pref.getString(dialogId,""));

            Type type = new TypeToken<List<MBDialogData>>() {}.getType();
            ArrayList<MBDialogData> arrayList = gson.fromJson(jObj.getString("dialod_data"),type);
            return arrayList;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Bundle getDialog(Context context, long dialogId)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);

        String data = pref.getString(String.valueOf(dialogId),"");

        Bundle bundle = new Bundle();
        try {
            JSONObject jObj = new JSONObject(data);

            bundle.putString(RESPONSE_ID,jObj.getString(RESPONSE_ID));
            bundle.putString("dialod_data",jObj.getString("dialod_data"));

//            try {
//                bundle.putString(FROM,jObj.getString(FROM));
//            } catch (JSONException e) {}



        }catch (JSONException e) {
            e.printStackTrace();
            return null;
        }


        return bundle;

    }



    /**
     * 대화내역 추가
     * @param context
     * @param dialogId 대화 아이디
     * @param mbDialogData
     * @param responseId
     */

    public static void addDialog(Context context, String dialogId, MBDialogData mbDialogData, String responseId, boolean isAuto)
    {

        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();

        Gson gson = new Gson();
        ArrayList<MBDialogData> arrayList;
        JSONObject jObj;

        try {
            jObj = new JSONObject(pref.getString(dialogId,""));
            Type type = new TypeToken<List<MBDialogData>>() {}.getType();
            arrayList = gson.fromJson(jObj.getString("dialod_data"),type);
            arrayList.add(mbDialogData);

        } catch (JSONException e) {
            e.printStackTrace();

            arrayList = new ArrayList<>();
            String content = context.getString(R.string.purpose_visit);
            MBDialogData firstDialogData = new MBDialogData(null,content,true,false);
            arrayList.add(firstDialogData);
            arrayList.add(mbDialogData);

        }

        try {

            jObj = new JSONObject();

            if (!jObj.has(RESPONSE_ID)) jObj.put(RESPONSE_ID, responseId);
            if (!jObj.has(IS_AUTO)) jObj.put(IS_AUTO,isAuto);
            jObj.put("dialod_data",gson.toJson(arrayList));

            ePref.putString(dialogId,jObj.toString());
            ePref.commit();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 대화내역 추가
     * @param context
     * @param dialogId 대화 아이디
     * @param mbDialogData
     * @param responseId
     */

    public static void addDialog(Context context, String dialogId, MBDialogData mbDialogData, String responseId)
    {
        addDialog(context,dialogId,mbDialogData,responseId,Common.isAutoResponse(context,responseId));
    }



    /**
     * 대화내역 저장
     * @param context
     * @param dialogId
     * @param arrayList
     */

    public static void saveDialog(Context context, String dialogId, ArrayList<MBDialogData> arrayList, String responseId)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();

        Gson gson = new Gson();
        try {

            JSONObject jObj = new JSONObject();
            jObj.put(RESPONSE_ID, responseId);
            jObj.put("dialod_data",gson.toJson(arrayList));

            ePref.putString(dialogId,jObj.toString());
            ePref.commit();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 벨에 등록된 기기 토큰 리스트를 반환
     * @param context
     * @return
     */

    public static ArrayList<String> getDeviceTokenList(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("device", Activity.MODE_PRIVATE);

        ArrayList<String> arr = new ArrayList<>();

        for (String key : pref.getAll().keySet())
        {
            arr.add(pref.getString(key,""));
        }

        return arr;
    }




    /**
     * 벨에 기기를 등록한다.
     * @param context
     * @param deviceToken
     */

    public static boolean saveDevice(Context context, String deviceToken, String name)
    {
        SharedPreferences pref = context.getSharedPreferences("device", Activity.MODE_PRIVATE);

        JSONObject jObj = new JSONObject();
        try {
            jObj.put(BUNDLE_DEVICE_NAME,name);
            jObj.put(BUNDLE_DEVICE_TOKEN,deviceToken);
        } catch (JSONException e) {
            return false;
        }

        String value = jObj.toString();


        for (String key : pref.getAll().keySet())
        {
            if (pref.getString(key,"").equals(value))
            {
                return false;
            }
        }

        SharedPreferences.Editor ePref = pref.edit();
        String key = "device_" + pref.getAll().size();
        ePref.putString(key,value);
        ePref.commit();

        return true;
    }

//    /**
//     * 기기에 벨을 등록한다.
//     * @param context
//     * @param bellKey
//     */
//
//    public static void saveBellDevice(Context context, String bellKey)
//    {
//        SharedPreferences pref = context.getSharedPreferences("device", Activity.MODE_PRIVATE);
//        SharedPreferences.Editor ePref = pref.edit();
//        ePref.putString("bell",bellKey);
//        ePref.commit();
//    }
//




    /**
     * 상대방에게 대화종료 메시지를 보낸다.
     * @param handler
     */

    public static void sendCloseDialogMessage(Context context, long dialogId, String responseId, final Handler handler)
    {
        final JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            data.put(IS_CLOSE_DIALOG,true);
            data.put(DIALOG_ID,dialogId);
            data.put(RESPONSE_ID,responseId);
            data.put(FROM,FirebaseInstanceId.getInstance().getToken());

            json.put("data",data);
            json.put("to",getNotificationKey(context));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        send(json,handler);

    }


    /**
     * 대화 내역을 보낸다
     * @param resId MBResponse ID (배달, 택배, 예약 등의 방문 목적)
     * @param msgId MBMessageData ID (대화 내용)
     * @param dialogId 대화의 ID (처음 대화 시작시의 시간)
     * @param isIdData 템플릿 대화일 경우 true, 직접 입력시 false
     * @param isStart 대화의 시작일 경우 true
     * @param handler
     */

    public static void sendToMessage(Context context, String resId, final String msgId, long dialogId, boolean isIdData, boolean isStart, final Handler handler) {

        final JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            data.put(RESPONSE_ID,resId);
            data.put(MESSAGE_ID,msgId);
            data.put(DIALOG_ID, dialogId);
            data.put(IS_ID_DATA,isIdData);
            data.put(IS_START,isStart);
            data.put(FROM,FirebaseInstanceId.getInstance().getToken());
            data.put(FROM_BELL, false);

            json.put("data",data);
            json.put("to",getNotificationKey(context));
            json.put("priority","high");
            json.put("time_to_live",0);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("Tag",json.toString());
        send(json,handler);

    }

    /**
     * 기기정보를 보낸다.
     * @param to
     * @param handler
     */

    public static void sendDeviceInfo(String to, String name, Handler handler)
    {
        final JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            data.put(IS_SEND_DEVICE,true);
            data.put(DEVICE_TOKEN, FirebaseInstanceId.getInstance().getToken());
            data.put(NAME, name);
            json.put("data",data);
            json.put("to",to);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        send(json,handler);

    }

    public static void send(final JSONObject json, final Handler handler)
    {
        Thread myThread = new Thread(new Runnable() {
            public void run() {

                try {

                    OkHttpClient client = new OkHttpClient();
                    MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                    RequestBody body = RequestBody.create(mediaType, json.toString());
                    Request request = new Request.Builder()
                            .url(fcbUrl)
                            .post(body)
                            .addHeader("Content-Type","application/json")
                            .addHeader("Authorization","key=" + fcbKey)
                            .build();

                    Response response = client.newCall(request).execute();

                    if (handler != null)
                        handler.sendEmptyMessage(0);


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        myThread.start();
    }


    public static void showMessage(Context context, String message)
    {

        LayoutInflater inflater = (LayoutInflater)context.getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.alert_normal, null);
        TextView customTitle = (TextView)view.findViewById(R.id.title);
        customTitle.setText(message);

        view.findViewById(R.id.message).setVisibility(View.GONE);

        final AlertDialog alert = new AlertDialog.Builder(context).setView(view).create();


        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alert.dismiss();     //닫기
            }
        });


        view.findViewById(R.id.btn_cancel).setVisibility(View.GONE);

        alert.show();
    }


    /**
     * 자동응답 모드 세팅 리스트를 불러온다.
     * @param context
     * @return
     */

    public static Bundle getAutoResponseSetting(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("setting_response", Activity.MODE_PRIVATE);
        MBResponseArray arr = MBResponseArray.initMBMessageData(context);

        Bundle bundle = new Bundle();
        for (String key : arr.getIdList())
        {
            bundle.putBoolean(key,pref.getBoolean(key,false));
        }

        return bundle;
    }



    /**
     *  자동응답 세팅 데이터 셋
     * @param context
     */

    public static void setAutoResponseSetting(Context context, Bundle bundle)
    {
        SharedPreferences pref = context.getSharedPreferences("setting_response", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();

        for (String key : bundle.keySet())
        {
            ePref.putBoolean(key,bundle.getBoolean(key,false));
        }

        ePref.commit();
    }

    /**
     *  자동응답 세팅 온 오프
     * @param context
     */

    public static void setAutoResponse(Context context, String key, boolean isOn)
    {
        SharedPreferences pref = context.getSharedPreferences("setting_response", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();
        ePref.putBoolean(key,isOn);
        ePref.commit();
    }

    /**
     *
     * @param context
     * @param to
     * @param handler
     */

    public static void requestAutoResponseSettingData(Context context, String to, Handler handler)
    {
        final JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            data.put(REQUEST_AUTO_RESPONSE_DATA,true);
            data.put(DEVICE_TOKEN, FirebaseInstanceId.getInstance().getToken());

            json.put("data",data);
            json.put("to",to);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        send(json,handler);
    }


    /**
     * 자동응답 세팅내역을 전송한다.
     * @param context
     * @param handler
     */

    public static void sendAutoResponseSettingData(Context context, Handler handler)
    {

        String notiKey = getNotificationKey(context);

        final JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();
        Bundle bunSetting = getAutoResponseSetting(context);

        try {

            for (String key : bunSetting.keySet())
            {
                data.put(key,bunSetting.getBoolean(key,false));
            }
            data.put(SEND_AUTO_RESPONSE_DATA,true);
            json.put("data",data);
            json.put("time_to_live",241900);
            json.put("to",notiKey);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        send(json,handler);
    }

    public static boolean isAutoResponse(Context context, String resId)
    {
        SharedPreferences pref = context.getSharedPreferences("setting_response", Activity.MODE_PRIVATE);
        return pref.getBoolean(resId,false);
    }


    /**
     * 메시지 키를 저장한다.
     * @param notiKey
     */

    public static void saveNotificationKey(Context context, String notiKey)
    {
        SharedPreferences pref = context.getSharedPreferences("notiKey", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();
        ePref.putString("key",notiKey);

        ePref.commit();

    }


    public static String getNotificationKey(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("notiKey", Activity.MODE_PRIVATE);
        return pref.getString("key","");
    }

    public static String getNotificationName(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("notiKey", Activity.MODE_PRIVATE);
        return pref.getString("name","");
    }



    public static boolean isSaveVisitorPicture(Context context, long dialogId)
    {
        //*************************************************************************************
        // 폴더생성
        File file = new File(context.getApplicationContext().getFilesDir() + "/visitor/" + dialogId);
        if (file.exists()) {
            return true;
        }else {
            return false;
        }
        //*************************************************************************************
    }


    public static Bitmap cropFaceFromBitmap(Bitmap original, Face face) {

        int x = face.getPosition().x < 0 ? 0:(int)face.getPosition().x;
        int y = face.getPosition().y < 0 ? 0:(int)face.getPosition().y;
        int width = (int)face.getWidth();
        int height = (int)face.getHeight();

        if (width + x > original.getWidth()) {
            width = original.getWidth() - x;
        }

        if (height + y > original.getHeight()) {
            height = original.getHeight() - y;
        }

        Bitmap result = Bitmap.createBitmap(original
                , x
                , y
                , width
                , height);
        return result;
    }


    public static void saveVisitorPicture(Context context, Bitmap bitmap, long dialogId, Runnable saveVisitorPicture)
    {
        //*************************************************************************************
        // 폴더생성
        File dirName = new File(context.getApplicationContext().getFilesDir() + "/visitor");
        if (!dirName.exists()) {
            dirName.mkdirs();
        }
        //*************************************************************************************

        if (isSaveVisitorPicture(context,dialogId)) return;

        FaceDetector fd = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .build();

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> mFaces = fd.detect(frame);
        fd.release();

        Bitmap face;

        String file_name = dialogId+".jpg";
        String file_name_face = dialogId+"_face.jpg";
        String string_path = dirName.getAbsolutePath() + "/" + file_name;
        String string_path_face = dirName.getAbsolutePath() + "/" + file_name_face;

        if (mFaces.size() > 0)
        {
            face = Common.cropFaceFromBitmap(bitmap,mFaces.valueAt(0));
        }else {

            savePicture(string_path,bitmap);
            Handler handler = new Handler();
            handler.postDelayed(saveVisitorPicture,1000);
            return;
        }

        try{
            savePicture(string_path,bitmap);
            FileOutputStream out_face = new FileOutputStream(string_path_face);
            face.compress(Bitmap.CompressFormat.JPEG, 100, out_face);
            out_face.close();

        }catch(FileNotFoundException exception){
            Log.d("Tag", exception.getMessage());
        }catch(IOException exception){
            Log.d("Tag", exception.getMessage());
        }
    }

    public static void savePicture(String path, Bitmap bitmap)
    {
        try{
            FileOutputStream out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

        }catch(FileNotFoundException exception){
            Log.d("Tag", exception.getMessage());
        }catch(IOException exception){
            Log.d("Tag", exception.getMessage());
        }

    }

    public static String getSavedFile(Context context, String dialogId, boolean isFace)
    {
        File dirName = new File(context.getApplicationContext().getFilesDir() + "/visitor");
        String file_name;

        if (isFace)
        {
            file_name = dialogId+"_face.jpg";
        }else {

            file_name = dialogId+".jpg";
        }

        return dirName.getAbsolutePath() + "/" + file_name;
    }

}
