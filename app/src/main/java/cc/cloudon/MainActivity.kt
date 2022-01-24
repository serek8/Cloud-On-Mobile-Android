package cc.cloudon

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), OnTouchListener {

    var dX = 0f
    var dY = 0f
    var lastAction = 0
    var listViewDefaultPosY = 0.0f
    var listViewDefaultHeight = 0

    external fun stringFromJNI(): String
    external fun setupEnvironment(path: String): Int
    external fun connectToServer(ip: String, port:Int): Int
    external fun runCloud(): Int

    init {
        System.loadLibrary("native-lib")
        System.loadLibrary("core-lib")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dataDir = getApplicationInfo().dataDir
        if(setupEnvironment(dataDir) < 0){
            // error
        }

        addFragmentToActivity()
        val floatingLayout = findViewById<FrameLayout>(R.id.frameLayout);
        floatingLayout.setOnTouchListener(this);

        val buttonGeneratePasscode = findViewById<Button>(R.id.buttonGeneratePasscode)
        buttonGeneratePasscode.setOnClickListener(View.OnClickListener {
            thread {
                val code = connectToServer("cloudon.cc", 9293)
                runOnUiThread {
                    val myTextView = findViewById<TextView>(R.id.textViewPasscode)
                    myTextView.text = code.toString()
                }
                runCloud()
            }
        })

        listViewDefaultPosY = findViewById<FrameLayout>(R.id.frameLayout).y
        listViewDefaultHeight = findViewById<FrameLayout>(R.id.frameLayout).height
//        listViewDefaultPosY = 400
//        listViewDefaultHeight = findViewById<FrameLayout>(R.id.frameLayout).height400
    }

    @Override
    fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);
    }

    private fun addFragmentToActivity(){
        val fragment = FileListFragment()
        val fm = supportFragmentManager
        val tr = fm.beginTransaction()
        tr.add(R.id.frameLayout, fragment)
        tr.addToBackStack(null);
        tr.commit();
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val activityView = findViewById<LinearLayout>(R.id.main_layout)
        when (event.getActionMasked()) {
            MotionEvent.ACTION_DOWN -> {
                dX = view.x - event.getRawX()
                dY = view.y - event.getRawY()
                lastAction = MotionEvent.ACTION_DOWN
            }
            MotionEvent.ACTION_MOVE -> {
                view.y = event.getRawY() + dY

                val params = view.getLayoutParams()

                params.height = findViewById<LinearLayout>(R.id.main_layout).height - view.y.toInt()
                Log.e("gggg", "height= "+ params.height)
                view.setLayoutParams(params);

                lastAction = MotionEvent.ACTION_MOVE
            }
            MotionEvent.ACTION_UP -> {
                if(view.height > activityView.height/2){
                    animetePosY(view, view.y, 100.0f)
                    animeteHeight(view, view.height, activityView.height - 100)
                }
                else{
                    animetePosY(view, view.y, findViewById<LinearLayout>(R.id.main_layout).height - 400.0f)
                    animeteHeight(view, view.height, 400)
                }

                return false
            }

        }
        return true
    }

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
//            Log.e("gggg", "height= "+ params.height)
            view.setLayoutParams(params);
        }

        slideAnimator.interpolator = LinearInterpolator()
        slideAnimator.start()
    }
}