package com.modoobel.modoobelllauncher.custom_obj;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.Gson;
import com.modoobel.modoobelllauncher.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by luckyleeis on 2017. 1. 5..
 */

public class MBResponseArray {

    Bundle bundleData  = new Bundle();
    Bundle bundleOrder = new Bundle();


    public void setAutoResponse()
    {
        for (int i = 0; i < getSize(); i++)
        {
            String id = getId(i);
            if (id.equals("more"))
            {
                if (bundleData.containsKey(id))
                {
                    bundleData.remove(id);
                }else {
                    bundleData.remove("more2");
                }
                bundleOrder.remove(String.valueOf(i));
            }
        }

        Bundle bundle = (Bundle) bundleOrder.clone();
        bundleOrder.clear();

        int i = 0;

        for (String key : bundle.keySet())
        {
            bundleOrder.putString(String.valueOf(i),bundle.getString(key));
            i++;
        }


    }


    public Set<String> getIdList()
    {
        return bundleData.keySet();
    }

    public int getSize()
    {
        return bundleData.size();
    }

    public String getResponseTitle(String id)
    {
        return getMBResponse(id).name;
    }

    public String getId(int order) {

        return bundleOrder.getString(String.valueOf(order));
    }

    public MBResponse getMBResponse(String id)
    {
        MBResponse mbResponse = bundleData.getParcelable(id);
        return mbResponse;
    }

    public MBResponse getMBResponse(int order)
    {
        try {

            String key = bundleOrder.getString(String.valueOf(order));
            MBResponse mbResponse = bundleData.getParcelable(key);
            return mbResponse;

        }catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 응대배열 초기화
     * @param context
     * @return
     */
    public static MBResponseArray initMBMessageData(Context context)
    {
        String commData = Common.getRawDataToString(context, R.raw.communicate_data);
        MBResponseArray self = new MBResponseArray();

        Gson gson = new Gson();

        try {
            JSONObject jsonObject = new JSONObject(commData);
            Iterator i = jsonObject.keys();
            while (i.hasNext()) {
                String key = i.next().toString();
                JSONObject resData = jsonObject.getJSONObject(key);

                MBResponse mbResponse = gson.fromJson(resData.toString(),MBResponse.class);
                mbResponse.setMessageData(resData.getJSONObject("msg_data"));

                self.bundleData.putParcelable(mbResponse.id,mbResponse);
                self.bundleOrder.putString(resData.getString("order"),mbResponse.id);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return self;
    }




}

