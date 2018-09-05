
 package com.example.loraapplication

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log

 class MainActivity : AppCompatActivity() {
    var spi: SerialPortInterface?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        spi = SerialPortInterface(this.application, "/dev/ttyUSB0")
        spi?.readTHData()
        TimeThread().start()

    }
     internal inner class TimeThread : Thread() {
         override fun run() {
             do {
                 try {
                     var str = spi!!.readTHData()
                     if( str[0]!="")
                     {
                         val msg = Message()
                         msg.what = 3  //消息(一个整型值)
                         msg.obj = str[0]
                         mHandler.sendMessage(msg)
                     }
                     Thread.sleep(1000)// 每隔1秒发送一个msg给mHandler
                 } catch (e: InterruptedException) {
                     e.printStackTrace()
                 }

             } while (true)
         }
     }
     var mHandler: Handler = object : Handler() {
         override fun handleMessage(msg: Message) {
             super.handleMessage(msg)
             when (msg.what) {
                 3 -> {
                     //在这里得到数据，并且可以直接更新UI

//                     Toast.makeText(this@MainActivity,"6666666", Toast.LENGTH_SHORT).show()
                     var str = msg.obj
                     Log.d("wzj",str.toString())
                 }



             }
         }
     }
}
