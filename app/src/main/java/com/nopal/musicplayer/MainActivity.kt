package com.nopal.musicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.mtechviral.mplaylib.MusicFinder
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.*


class MainActivity : AppCompatActivity() {

    var albumart: ImageView? = null

    var playbutton: ImageButton? = null
    var  shufflebutton: ImageButton? = null

    var songtitle: TextView? = null
    var songartist: TextView? = null

    private var mediaPlayer : MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),0)
        }else{
            createplayer()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
            createplayer()
        }else{
            longToast("Permission not granted. shutting down,")
            finish()
        }
    }

    private fun createplayer(){
        var songsjob = async {
            val songfinder = MusicFinder(contentResolver)
            songfinder.prepare()
            songfinder.allSongs
        }

        launch(kotlinx.coroutines.experimental.android.UI){
            val songs = songsjob.await()

            val playerUI = object:AnkoComponent<MainActivity>{
                override fun createView(ui: AnkoContext<MainActivity>)= with(ui) {
                    relativeLayout {
                        backgroundColor = Color.BLACK

                        albumart = imageView {
                            scaleType = ImageView.ScaleType.FIT_CENTER
                        }.lparams(matchParent, matchParent)

                        verticalLayout {
                            backgroundColor = Color.parseColor("#990000")
                            songtitle = textView {
                                textColor = Color.WHITE
                                typeface = Typeface.DEFAULT_BOLD
                                textSize = 18f
                            }

                            songartist = textView {
                                textColor = Color.WHITE
                            }

                            linearLayout {
                                playbutton = imageButton {
                                    imageResource = R.drawable.ic_play_arrow_black_24dp
                                    onClick {
                                        playorpause()
                                    }
                                }.lparams(0, wrapContent, 0.5f)

                                shufflebutton = imageButton {
                                    imageResource = R.drawable.ic_shuffle_black_24dp
                                    onClick {
                                        playrandom()
                                    }
                                }.lparams(0, wrapContent, 0.5f)
                            }.lparams(matchParent, wrapContent){
                                topMargin = dip(5)
                            }


                        }.lparams(matchParent, wrapContent){
                            alignParentBottom()
                        }

                    }
                }

                fun playrandom(){
                    Collections.shuffle(songs)
                    val song = songs[0]
                    mediaPlayer?.reset()
                    mediaPlayer = MediaPlayer.create(ctx,song.uri)
                    mediaPlayer?.setOnCompletionListener {
                        playrandom()
                    }
                    albumart?.imageURI =song.albumArt
                    songtitle?.text = song.title
                    songartist?.text = song.artist
                    mediaPlayer?.start()
                    playbutton?.imageResource = R.drawable.ic_pause_black_24dp
                }

                fun playorpause(){
                    var songplaying:Boolean? = mediaPlayer?.isPlaying

                    if(songplaying == true){
                        mediaPlayer?.pause()
                        playbutton?.imageResource = R.drawable.ic_play_arrow_black_24dp
                    }else{
                        mediaPlayer?.start()
                        playbutton?.imageResource = R.drawable.ic_pause_black_24dp
                    }
                }
            }
            playerUI.setContentView(this@MainActivity)
            playerUI.playrandom()

        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }
}
