package ru.netology.app_manager.ui

import android.view.LayoutInflater
import ru.netology.app_manager.core.templates.BaseActivity
import ru.netology.app_manager.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate


}