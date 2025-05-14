package com.example.safelink;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ModelInterpreter {


    private final Context context;
    private static final String API_URL = "http://152.53.84.199:5000/predict"; // تأكد من صحة عنوان الـ API

    public ModelInterpreter(Context context) {
        this.context = context;
    }

    // طريقة لفحص URL
    public void checkUrl(String url, ModelCallback callback) {
        new CheckUrlTask(callback).execute(url);
    }

    // المهمة التي يتم تنفيذها في الخلفية لفحص URL
    private class CheckUrlTask extends AsyncTask<String, Void, String> {
        private final ModelCallback callback;

        public CheckUrlTask(ModelCallback callback) {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                // إنشاء الاتصال بالـ API
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // إرسال البيانات إلى الـ API (URL المراد فحصه)
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("url", params[0]);

                OutputStream os = conn.getOutputStream();
                os.write(jsonRequest.toString().getBytes());
                os.flush();
                os.close();

                // قراءة استجابة الـ API
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                conn.disconnect();

                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null; // إعادة القيمة null إذا حدث خطأ
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                // في حال حدوث خطأ في الاتصال، إظهار رسالة للمستخدم
                Toast.makeText(context, "Connection error!", Toast.LENGTH_SHORT).show();
                callback.onResult("Connection error");
                return;
            }

            try {
                // محاولة تحويل الاستجابة إلى JSON
                JSONObject jsonResponse = new JSONObject(result);

                // الحصول على قيمة "is_spam" من الاستجابة
                boolean isSpam = jsonResponse.optBoolean("is_spam", false);

                // تحديد النتيجة بناءً على قيمة "is_spam"
                String resultText = isSpam ? "Safe" : "Suspicious";

                // استدعاء الواجهة لإظهار النتيجة
                callback.onResult(resultText);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onResult("Error parsing response");
            }
        }
    }

    // واجهة استرجاع النتيجة من الـ API
    public interface ModelCallback {
        void onResult(String result);
    }
}
