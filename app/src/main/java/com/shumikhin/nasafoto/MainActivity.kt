package com.shumikhin.nasafoto

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import coil.api.load
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : AppCompatActivity() {

    private val RetrofitImpl = com.shumikhin.nasafoto.RetrofitImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sendServerRequest()
    }

    private fun sendServerRequest() {
        RetrofitImpl.getRetrofit().getPictureOfTheDay("DEMO_KEY")
            .enqueue(object : Callback<DataModel> {

                override fun onResponse(call: Call<DataModel>, response: Response<DataModel>) {
                    if (response.isSuccessful && response.body() != null) {
                        renderData(response.body(), null)
                    } else {
                        renderData(null, Throwable("Ответ от сервера пустой"))
                    }
                }

                override fun onFailure(call: Call<DataModel>, t: Throwable) {
                    renderData(null, t)
                }
            })
    }

    private fun renderData(dataModel: DataModel?, error: Throwable?) {
        if (dataModel == null || error != null) {
            //Ошибка
            Toast.makeText(this, error?.message, Toast.LENGTH_LONG).show()
        } else {
            val url = dataModel.url
            if (url.isNullOrEmpty()) {
                //ссылка на фото пустая
            } else {
                image_view.load(url)
            }
            val explanation = dataModel.explanation

            if (explanation.isNullOrEmpty()) {
                //Пустое описание
            } else {
                text_view.text = explanation
            }


        }
    }

}

data class DataModel(
    val explanation: String?,//Здесь хранится описание фотографии
    val url: String? //урл самой фотографии
)

interface PictureOfTheDayAPI {
    @GET("planetary/apod")
    fun getPictureOfTheDay(@Query("api_key") apiKey: String): Call<DataModel>
}

class RetrofitImpl {
    fun getRetrofit(): PictureOfTheDayAPI {
        val podRetrofit = Retrofit.Builder()
            .baseUrl("https://api.nasa.gov/")
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setLenient().create()
                )
            ).build()
        return podRetrofit.create(PictureOfTheDayAPI::class.java)
    }
}