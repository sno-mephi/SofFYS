package ru.idfedorov09.telegram.bot;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
import java.net.ConnectException;

public class UpdatesSender {

    protected void sendUpdate(Update update, String botUrl) {
        Gson gson = new Gson();
        String jsonUpdate = gson.toJson(update);

        OkHttpClient httpClient = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(jsonUpdate, okhttp3.MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(botUrl)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {

        } catch (ConnectException e) {
            System.out.println(botUrl+" is offline.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    protected void exceptHandle(IOException e){
        e.printStackTrace();
    }

}
