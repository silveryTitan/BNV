package com.silverytitan.bnv

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import com.silverytitan.bnv.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    val bind by viewBinding<ActivityMainBinding>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bind.rvBm.setViewPager(bind.vpPage)
                .setItemData("首页", R.mipmap.home_pressed, R.mipmap.home_normal, MainFragment())
                .setItemData("动态", R.mipmap.home_pressed, R.mipmap.home_normal, MainFragment())
    }
}
