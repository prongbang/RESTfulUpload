package com.prongbang.file;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by prongbang on 9/7/2015.
 */
public class ManageFile {
    public String uploadFiletoServer(String strSDPath, String strUrlServer) {

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        int resCode = 0;
        String resMessage = "";

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        try {
            /** Check file on SD Card ***/
            File file = new File(strSDPath);
            if (!file.exists()) {
                return "{\"StatusID\":\"0\",\"Error\":\"Please check path on SD Card\"}";
            }

            FileInputStream fileInputStream = new FileInputStream(new File(strSDPath));

            URL url = new URL(strUrlServer);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);

            DataOutputStream outputStream = new DataOutputStream(conn
                    .getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream
                    .writeBytes("Content-Disposition: form-data; name=\"filUpload\";filename=\""
                            + strSDPath + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // Read file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Response Code and  Message
            resCode = conn.getResponseCode();
            if (resCode == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                int read = 0;
                while ((read = is.read()) != -1) {
                    bos.write(read);
                }
                byte[] result = bos.toByteArray();
                bos.close();

                resMessage = new String(result);

            }

            Log.d("resCode=", Integer.toString(resCode));
            Log.d("resMessage=", resMessage.toString());

            fileInputStream.close();
            outputStream.flush();
            outputStream.close();

            return resMessage.toString();

        } catch (Exception ex) {
            // Exception handling
            return null;
        }
    }
}
