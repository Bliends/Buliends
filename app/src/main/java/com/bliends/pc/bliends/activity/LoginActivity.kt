package com.bliends.pc.bliends.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import com.bliends.pc.bliends.R
import com.bliends.pc.bliends.util.ORMUtil
import com.bliends.pc.bliends.data.Sign
import com.bliends.pc.bliends.data.UserInfo
import com.bliends.pc.bliends.util.RetrofitUtil
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.jetbrains.anko.toast
import org.json.JSONObject
import android.view.View.OnFocusChangeListener
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import com.bliends.pc.bliends.data.User
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginId.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->

            if (hasFocus) {
                Handler().postDelayed(Runnable {
                    //흰색배경으로 덮어줌
                    loginIdView.setBackgroundResource(R.drawable.bg_login_select_et)
                    LoginHintId.visibility = View.VISIBLE
                }, 100)
            } else {
                //흰색 배경을 표시/숨기기
                loginIdView.setBackgroundResource(R.drawable.bg_login_et)
                if (loginId.text.isNotEmpty()) {
                    LoginHintId.visibility = View.VISIBLE
                } else {
                    LoginHintId.visibility = View.INVISIBLE
                }
            }
        }


        loginPasswd.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->

            if (hasFocus) {
                Handler().postDelayed(Runnable {
                    //흰색배경으로 덮어줌
                    loginPdView.setBackgroundResource(R.drawable.bg_login_select_et)
                    LoginHintPasswd.visibility = View.VISIBLE
                }, 100)
            } else {
                //흰색 배경을 표시/숨기기
                loginPdView.setBackgroundResource(R.drawable.bg_login_et)
                if (loginPasswd.text.isNotEmpty()) {
                    LoginHintPasswd.visibility = View.VISIBLE
                } else {
                    LoginHintPasswd.visibility = View.INVISIBLE
                }
            }
        }


        loginPasswd.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    loginSeeCheckPd.visibility = View.INVISIBLE
                } else if(s.isNotEmpty()){
                    loginSeeCheckPd.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {}
        })

        loginSeeCheckPd.onClick {
            Log.e("texttype",loginPasswd.inputType.toString())
            if(loginPasswd.inputType == 129){
                loginPasswd.inputType = InputType.TYPE_CLASS_TEXT
                loginSeeCheckPd.setBackgroundResource(R.drawable.rock)
            }else if(loginPasswd.inputType == InputType.TYPE_CLASS_TEXT){
                loginPasswd.inputType = 129
                loginSeeCheckPd.setBackgroundResource(R.drawable.unrock)
            }
        }

        loginIdDelete.onClick {
            loginId.text = null
        }

        loginId.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    loginIdDelete.visibility = View.INVISIBLE
                } else if(s.isNotEmpty()){
                    loginIdDelete.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {}
        })

            loginSignup.onClick {
                startActivity<SignupSelectActivity>()
                finish()
            }

            loginBtn.onLongClick {
                startActivity<UserMainActivity>()
            }

            loginBtn.onClick {
                Login()
            }
        }


        fun Login() {
            var res: Call<Sign> = RetrofitUtil.postService.Sign(loginId.text.toString(), loginPasswd.text.toString())

            res.enqueue(object : Callback<Sign> {

                override fun onResponse(call: Call<Sign>?, response: Response<Sign>?) {
//                Log.e("login" + response!!.code().toString(), response.body()!!.message)
                    when {
                        response!!.code() == 200 -> response.body()?.let {
                            toast(response.body()!!.message)
                            var token = response.body()!!.token
                            GetUser(token)
                            ORMUtil(this@LoginActivity).tokenORM.save(response.body()!!)
                            startActivity<MainActivity>()
                            finish()
                        }
                        else -> {
                            val ErrorObj = JSONObject(response.errorBody()!!.string())
                            toast(ErrorObj.getString("message"))
                        }

                    }
                }

                override fun onFailure(call: Call<Sign>?, t: Throwable?) {
                    toast("Sever Error")
                    Log.e("login Error", t!!.message)
                }
            })
        }

        fun GetUser(token: String) {
            var res: Call<User> = RetrofitUtil.postService.UserInfo(token)
            res.enqueue(object : Callback<User> {

                override fun onResponse(call: Call<User>?, response: Response<User>?) {
                    when {
                        response!!.code() == 200 -> response.body().let {
                            Log.e("name", Gson().toJson(response.body()!!))
                            ORMUtil(this@LoginActivity).userORM.save(response.body()!!)
                        }
                        else -> {
                            val ErrorObj = JSONObject(response.errorBody()!!.string())
                            toast(ErrorObj.getString("message"))
                        }
                    }
                }

                override fun onFailure(call: Call<User>?, t: Throwable?) {
                    Log.e("getuser Error", t!!.message)
                    toast("Sever Error")
                }
            })
        }

    }
