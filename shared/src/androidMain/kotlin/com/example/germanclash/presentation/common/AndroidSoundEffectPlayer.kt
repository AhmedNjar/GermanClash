package com.example.germanclash.presentation.common

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.germanclash.presentation.feature.game.SoundEffect

/**
 * Loads short game SFX from res/raw. Wire each SoundEffect to a real
 * raw resource once the audio assets are added to the project, e.g.:
 *   soundIds[SoundEffect.CORRECT] = soundPool.load(context, R.raw.correct, 1)
 */
class AndroidSoundEffectPlayer(
    @Suppress("UNUSED_PARAMETER") context: Context,
) : SoundEffectPlayer {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val soundIds = mutableMapOf<SoundEffect, Int>()

    override fun play(sound: SoundEffect) {
        soundIds[sound]?.let { id -> soundPool.play(id, 1f, 1f, 1, 0, 1f) }
    }
}
