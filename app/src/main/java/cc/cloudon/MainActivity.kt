package cc.cloudon

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), OnTouchListener {
    var dY = 0f
    var listViewDefaultPosY = 0.0f
    var listViewDefaultHeight = 0

    // Use to find out the animation direction
    var listViewPreviousPosY = listViewDefaultPosY;
    private var listViewPreviousPosYMoveTimestamp : Long = 0;

    external fun stringFromJNI(): String
    external fun setupEnvironment(path: String): Int
    external fun connectToServer(ip: String, port:Int): Int
    external fun runCloud(): Int

    init {
        System.loadLibrary("native-lib")
        System.loadLibrary("core-lib")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(setupEnvironment(applicationInfo.dataDir) < 0){
            // error
            return;
        }
        addFragmentToActivity()
        val floatingLayout = findViewById<FrameLayout>(R.id.frameLayout);
        floatingLayout.setOnTouchListener(this);
        floatingLayout.y =- BOTTOM_MARGIN_FILE_LIST;


        val buttonGeneratePasscode = findViewById<Button>(R.id.buttonGeneratePasscode)
        buttonGeneratePasscode.setOnClickListener {
            buttonGeneratePasscode.visibility = View.GONE
            findViewById<Button>(R.id.buttonGeneratePasscode2).visibility = View.VISIBLE
            onNewPasscodeClick()
        }
        findViewById<Button>(R.id.buttonGeneratePasscode2).setOnClickListener { onNewPasscodeClick() }


        listViewDefaultPosY = floatingLayout.y
        listViewDefaultHeight = floatingLayout.height

        when {
            intent?.action == Intent.ACTION_SEND -> {
                    handleSendImage(intent) // Handle single image being sent
            }
            intent?.action == Intent.ACTION_SEND_MULTIPLE -> {
                handleSendMultipleImages(intent) // Handle multiple images being sent
            }
        }
    }

    fun onFileUploaded(filename: String){
        runOnUiThread {
            Toast.makeText(this, "Added new file: $filename", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSendImage(intent: Intent) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
            val destinationDir = applicationInfo.dataDir + "/hosted_files"
            val filesrc = uriToFile(it)
            if (filesrc == null){
                Toast.makeText(this, "Can't open file", Toast.LENGTH_LONG)
                return
            }
            filesrc.copyTo(File(destinationDir, filesrc.name), overwrite = true)

        }
    }

    fun uriToFile(uri: Uri): File? {
        val context = applicationContext
        val scheme = uri.scheme

        if (ContentResolver.SCHEME_FILE == scheme) {
            // Direct file path
            return File(uri.path!!)
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            // Content URI (e.g., media content)
            val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)
            val cursor = context.contentResolver.query(uri, filePathColumn, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    return File(it.getString(columnIndex))
                }
            }
        }

        return null
    }

    private fun copyFile(sourceFile: File, destFile: File) {
        FileInputStream(sourceFile).channel.use { source ->
            FileOutputStream(destFile).channel.use { destination ->
                destination.transferFrom(source, 0, source.size())
            }
        }
    }

    private fun handleSendMultipleImages(intent: Intent) {
        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let {
            // Update UI to reflect multiple images being shared
        }
    }

    fun onNewPasscodeClick(){
            thread {
                val code = connectToServer("cloudon.cc", 9293)
                runOnUiThread {
                    val myTextView = findViewById<TextView>(R.id.textViewPasscode)
                    if(code > 0){
                        myTextView.text = code.toString()
                        findViewById<ImageView>(R.id.imageViewLocker).setImageResource(R.drawable.ic_unlocked)
                    }
                    else{
                        myTextView.text = "Error"
                    }

                }
                runCloud()
            }
    }


    private fun addFragmentToActivity(){
        val fragment = FileListFragment()
        val fm = supportFragmentManager
        val tr = fm.beginTransaction()
        tr.add(R.id.frameLayout, fragment)
        tr.addToBackStack(null);
        tr.commit();
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                listViewPreviousPosYMoveTimestamp = event.eventTime;
                dY = view.y - event.getRawY()
                listViewPreviousPosY = view.y
            }
            MotionEvent.ACTION_MOVE -> {
                Log.e("TEST", "view.y="+view.y);
                if(event.eventTime - listViewPreviousPosYMoveTimestamp > 50){
                    listViewPreviousPosY = view.y
                }
                view.y = event.getRawY() + dY
                listViewPreviousPosYMoveTimestamp = event.eventTime;
            }
            MotionEvent.ACTION_UP -> {
                Log.e("TEST", "listViewPreviousPosY < view.y= "+ (listViewPreviousPosY))
                if(listViewPreviousPosY < view.y){
                    animetePosY(view, view.y, findViewById<ConstraintLayout>(R.id.main_layout).height - 400.0f)
                }
                else{
                    animetePosY(view, view.y, 100.0f)
                }
                return false
            }
        }
        return true
    }

    private val filesFragmentHeight get() = findViewById<RelativeLayout>(R.id.main_layout).height - 400.0f


    fun animetePosY(view:View, currentHeight:Float, newHeight:Float) {
        val slideAnimator = ValueAnimator.ofFloat(currentHeight, newHeight).setDuration(500);
        /* We use an update listener which listens to each tick
         * and manually updates the height of the view  */
        slideAnimator.addUpdateListener {
            val value = it.animatedValue as Float
            view.y = value
        }
        slideAnimator.interpolator = LinearInterpolator()
        slideAnimator.start()
    }

    fun animeteHeight(view:View, currentHeight:Int, newHeight:Int) {
        val slideAnimator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(500);
        slideAnimator.addUpdateListener {
            val value = it.animatedValue as Int
            val params = view.getLayoutParams()
            params.height = value
            view.setLayoutParams(params);
        }

        slideAnimator.interpolator = LinearInterpolator()
        slideAnimator.start()
    }

    companion object {
        const val BOTTOM_MARGIN_FILE_LIST = 400.0f
    }
}