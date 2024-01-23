package com.example.carseatselector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.carseatselector.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), CarSeatSelectorView.CarSeatSelectorListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewCarSeatSelector.setCarSeatSelectorListener(this)

        binding.viewCarSeatSelector.setUnavailable(listOf(2))
    }

    override fun onSeatClick(selectedSeatPositions: List<Int>) {
        binding.textSelectedSeats.text = "Selected seats: $selectedSeatPositions"
    }
}