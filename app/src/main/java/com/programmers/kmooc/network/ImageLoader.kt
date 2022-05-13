package com.programmers.kmooc.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.URL

object ImageLoader {
    private val imageCache = mutableMapOf<String,Bitmap>()

    fun loadImage(url: String, completed: (Bitmap?) -> Unit) {

        // 이미지가 없을때 null을 반환
        if(url.isEmpty()){
            completed(null)
            return
        }

        // 이미지 캐시 : 한번 사용한 이미지를 저장해서 다음에 불러 올때 같은 이미지가 있으면 바로 캐시에서 이미지 사용.
        if(imageCache.containsKey(url)){
            completed(imageCache[url])
            return
        }

        // 비동기식 이미지 처리
      GlobalScope.launch(Dispatchers.IO) {
            try {
                val bitmap = BitmapFactory.decodeStream(URL(url).openStream())
                //이미지 캐시에 저장
                imageCache[url] = bitmap

                withContext(Dispatchers.Main){
                    completed(bitmap)
                }

            } catch (e : Exception){
                withContext(Dispatchers.Main) {
                    completed(null)
                }
            }
        }
    }
}