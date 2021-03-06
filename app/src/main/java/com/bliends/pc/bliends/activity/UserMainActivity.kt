package com.bliends.pc.bliends.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.bliends.pc.bliends.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.WindowManager
import com.bliends.pc.bliends.service.MoneyService
import com.bliends.pc.bliends.data.Help
import com.bliends.pc.bliends.util.RetrofitUtil
import com.bliends.pc.bliends.data.Sign
import com.bliends.pc.bliends.data.User
import com.bliends.pc.bliends.service.UserLockService
import com.bliends.pc.bliends.util.GPSUtil
import com.bliends.pc.bliends.util.ORMUtil
import com.bliends.pc.bliends.util.TTSUtil
import kotlinx.android.synthetic.main.activity_user_main.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColor
import org.jetbrains.anko.toast
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class UserMainActivity : AppCompatActivity() {
    var latitude: Double? = null
    var longitude: Double? = null
    var time = timmer()
    var path = ""
    var file: File? = null
    var recorder: MediaRecorder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)
        setSupportActionBar(userToolbar)
        val intent = Intent(
                applicationContext, //현재제어권자
                MoneyService::class.java) // 이동할 컴포넌트
        startService(intent) // 서비스 시작

        val Lockintent = Intent(
                applicationContext, //현재제어권자
                UserLockService::class.java) // 이동할 컴포넌트
        startService(Lockintent) // 서비스 시작

        userCall.onClick {
            var intent = Intent(this@UserMainActivity, UserSelectActivity::class.java)
            intent.putExtra("bl", true)
            startActivityForResult(intent, 0)
            tts("전화하기\n 경찰서에 전화하시려면 왼쪽을 클릭하시고 보호자에게 전화하시려면 오른쪽을 클릭해주세요")
            userCall.backgroundColor = ContextCompat.getColor(this@UserMainActivity, R.color.colorSub)
            callText.textColor = ContextCompat.getColor(this@UserMainActivity, R.color.colorMain)
        }

        userCallHelp.onClick {
            time = timmer()
            Timer().schedule(time, 0, 1000)
            recordstart()
            userCallHelp.backgroundColor = ContextCompat.getColor(this@UserMainActivity, R.color.colorSub)
            helpText.textColor = ContextCompat.getColor(this@UserMainActivity, R.color.colorMain)
        }


        userWayLose.onClick {
            var intent = Intent(this@UserMainActivity, UserSelectActivity::class.java)
            intent.putExtra("bl", false)
            startActivityForResult(intent, 2)
            tts("길을 잃어버렸을때 보내는 요청입니다.\n 오른쪽 클릭은 보내기 왼쪽을릭은 취소입니다.")
            userWayLose.backgroundColor = ContextCompat.getColor(this@UserMainActivity, R.color.colorSub)
            loseText.textColor = ContextCompat.getColor(this@UserMainActivity, R.color.colorMain)
        }

        userMoney.onClick {
            var intent = Intent(this@UserMainActivity, UserSelectActivity::class.java)
            intent.putExtra("bl", false)
            startActivityForResult(intent, 3)
            tts("돈이 부족할때 보내는 요청입니다.\n 오른쪽 클릭은 보내기 왼쪽을릭은 취소입니다.")
            userMoney.backgroundColor = ContextCompat.getColor(this@UserMainActivity, R.color.colorSub)
            moneyText.textColor = ContextCompat.getColor(this@UserMainActivity, R.color.colorMain)
        }
    }

    fun tts(message: String) {
        TTSUtil.usingTTS(this@UserMainActivity, message)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        loseText.textColor = ContextCompat.getColor(this@UserMainActivity, R.color.colorbottomNav)
        callText.textColor = ContextCompat.getColor(this@UserMainActivity, R.color.colorbottomNav)
        moneyText.textColor = ContextCompat.getColor(this@UserMainActivity, R.color.colorbottomNav)

        userCall.backgroundColor = ContextCompat.getColor(this@UserMainActivity, R.color.colorUserMain)
        userWayLose.backgroundColor = ContextCompat.getColor(this@UserMainActivity, R.color.colorUserMain)
        userMoney.backgroundColor = ContextCompat.getColor(this@UserMainActivity, R.color.colorUserMain)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                0 -> {
                    var bl = data!!.getBooleanExtra("bl", false)
                    if (bl) {
                        val pref = getSharedPreferences("UserphoneNum", Context.MODE_PRIVATE)
                        var phone = pref.getString("phoneNum", "")
                        if (phone.isEmpty()) {
                            tts("설정에서 보호자의 휴대폰번호를 등록해주세요")
                            toast("설정에서 보호자의 휴대폰번호를 등록해주세요")
                        } else {
                            startActivity(Intent("android.intent.action.CALL", Uri.parse("tel:$phone")))
                        }
                    } else {
                        startActivity(Intent("android.intent.action.CALL", Uri.parse("tel:112")))
                    }
                }

                2 -> {
                    Callhelp('L'.toString())
                }
                3 -> {
                    Callhelp('M'.toString())
                }
            }
        }
    }

    fun location() {
        var gps = GPSUtil(applicationContext)
        gps!!.getLocation()
        longitude = gps!!.longitude
        latitude = gps!!.latitude
        gps!!.stopUsingGPS()
    }

    fun recordstart() {
        try {
            var recodePath = "Bliends" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            var dirPath = File(Environment.getExternalStorageDirectory().absolutePath, "Bliends")
            if (!dirPath.exists()) dirPath.mkdirs()
            dirPath.mkdir()
            file = File.createTempFile(recodePath, ".3gp", dirPath)//파일생성
            path = file!!.path
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(path)
                prepare()
                start()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun recordstop() {
        try {
            recorder!!.stop()// setAudioSource () 단계로 돌아가서 객체를 재사용 할 수 있습니다.
            recorder!!.release()
        } catch (e: KotlinNullPointerException) {
            Log.e("e", e.toString())
        }
    }

    fun timmer(): TimerTask {
        var s = 0

        var tt = object : TimerTask() {

            val handler = @SuppressLint("HandlerLeak")
            object : Handler() {
                @SuppressLint("SetTextI18n")
                override fun handleMessage(msg: Message) {
                    recordstop()
                    Callhelp("E")
                    time.cancel()
                }
            }

            override fun run() {
                Log.e("s", s.toString())
                if (s >= 10) {
                    s = 0
                    val msg = handler.obtainMessage()
                    handler.sendMessage(msg)
                }
                s++
            }
        }
        return tt
    }

    override fun onDestroy() {
        super.onDestroy()
        TTSUtil.speakStop()
    }


    fun Callhelp(situation_: String) {

        helpText.textColor = ContextCompat.getColor(this@UserMainActivity, R.color.colorbottomNav)
        userCallHelp.backgroundColor = ContextCompat.getColor(this@UserMainActivity, R.color.colorUserMain)
        location()
        var list: List<Any> = ORMUtil(this@UserMainActivity).tokenORM.find(Sign())
        var sign = list[list.size - 1] as Sign
        var attachments: MultipartBody.Part? = null
        attachments = if (situation_ == "E") {
            val mFile = RequestBody.create(MediaType.parse("audio/*"), file)
            MultipartBody.Part.createFormData("attachments", file!!.path, mFile)
        } else {
            null
        }

        Log.e("situation", situation_)
        var res = RetrofitUtil.postService.Help(
                sign.token,
                latitude!!,
                longitude!!,
                situation_,
                attachments
        )

        res.enqueue(object : Callback<Help> {
            override fun onResponse(call: Call<Help>, response: Response<Help>) {
                when (response.code()) {
                    201 -> {
                        Log.e("help", "ok")
                        if (situation_ != "E") {
                            toast("정상적으로 발송이 완료되었습니다.")
                        }
                    }

                    else -> {
                        Log.e(response.code().toString(), response.message())
                        val ErrorObj = JSONObject(response.errorBody()!!.string())
                        toast(ErrorObj.getString("message"))
                    }
                }
            }

            override fun onFailure(call: Call<Help>, t: Throwable) {
                Log.e("Help Error", t!!.message)
                toast("Sever Error")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.userSetting -> {
                //테스트로 로그인 부분으로 넘어가게 해놨음
                tts("설정으로 이동합니다.")
                toast("설정으로 이동합니다.")
                startActivity<UserSettingActivity>()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
