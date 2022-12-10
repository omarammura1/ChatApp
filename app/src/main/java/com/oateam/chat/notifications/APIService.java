package com.oateam.chat.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAebef_-o:APA91bG3hZAyZfbw3l4uCJYtfu8suvoIM-xJzLddf9P-qoH_z2I4Ckocsg4vhsoCkGKLyZQEgXzllRUKjGSsw5DwTnA3J3wWbW3c7AclS4Xyhnd-F62v8821Yf0IMoy-NvtLZVnnqcmL\t\n"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
