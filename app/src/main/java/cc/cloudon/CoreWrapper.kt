package cc.cloudon

import android.widget.Toast


class CoreWrapper(var activity: MainActivity) {

    init {
//        System.loadLibrary("native-lib")
        System.loadLibrary("core-lib")
    }
    external fun setupEnvironment(path: String): Int
    external fun connectToServer(ip: String, port:Int): Int
    external fun runCloud(): Int

    fun onFileUploaded(filename: String){
        activity.onFileUploaded(filename)
    }

}