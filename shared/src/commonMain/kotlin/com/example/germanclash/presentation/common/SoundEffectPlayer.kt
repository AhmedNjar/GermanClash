package com.example.germanclash.presentation.common

import com.example.germanclash.presentation.feature.game.SoundEffect

interface SoundEffectPlayer {
    fun play(sound: SoundEffect)
}