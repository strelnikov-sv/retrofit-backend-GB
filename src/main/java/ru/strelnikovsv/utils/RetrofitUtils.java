package ru.strelnikovsv.utils;

import lombok.experimental.UtilityClass;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import ru.strelnikovsv.dto.ErrorMessage;
import ru.strelnikovsv.dto.Product;

import java.io.IOException;
import java.lang.annotation.Annotation;

@UtilityClass
public class RetrofitUtils {
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    OkHttpClient client = new OkHttpClient
            .Builder()
            .addInterceptor(loggingInterceptor)
            .build();

    public Retrofit getRetrofit() {
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return new Retrofit.Builder()
                .client(client)
                .baseUrl("http://80.78.248.82:8189/market/api/v1/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }

    public static ErrorMessage convertBody(Response<Product> response, Class<ErrorMessage> errorBodyClass) throws IOException {
        if (response != null && !response.isSuccessful() && response.errorBody() != null) {
            ResponseBody body = response.errorBody();
            Converter<ResponseBody, ErrorMessage> converter = RetrofitUtils.getRetrofit().responseBodyConverter(errorBodyClass, new Annotation[0]);
            return converter.convert(body);
        }
        return null;
    }

}
