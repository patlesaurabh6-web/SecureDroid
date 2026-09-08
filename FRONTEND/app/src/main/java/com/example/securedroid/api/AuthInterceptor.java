package com.example.securedroid.api;

import android.content.Context;
import com.example.securedroid.utils.SessionManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;

    public AuthInterceptor(Context context) {
        this.sessionManager = SessionManager.getInstance(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = sessionManager.getAuthToken();

        if (token != null && !token.isEmpty()) {
            Request modified = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(modified);
        }

        return chain.proceed(original);
    }
}
