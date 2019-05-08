 package com.nayeem.highlightsar

 import android.app.Activity;
 import android.app.ActivityManager;
 import android.content.Context;
 import android.graphics.ColorSpace
 import android.graphics.SurfaceTexture;
 import android.media.MediaPlayer;
 import android.os.Build;
 import android.os.Build.VERSION_CODES;
 import android.os.Bundle;
 import android.os.PersistableBundle
 import android.support.annotation.Nullable;
 import android.support.v4.app.FragmentActivity
 import android.support.v7.app.AppCompatActivity
 import android.util.Log;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.widget.Toast;
 import com.google.ar.core.Anchor;
 import com.google.ar.core.HitResult;
 import com.google.ar.core.Plane;
 import com.google.ar.sceneform.AnchorNode;
 import com.google.ar.sceneform.Node;
 import com.google.ar.sceneform.math.Vector3;
 import com.google.ar.sceneform.rendering.Color;
 import com.google.ar.sceneform.rendering.ExternalTexture;
 import com.google.ar.sceneform.rendering.ModelRenderable;
 import com.google.ar.sceneform.rendering.Renderable
 import com.google.ar.sceneform.ux.ArFragment;
 import java.util.*

 class VideoActivity : AppCompatActivity() {

 private val TAG = VideoActivity::class.simpleName
 private val MIN_OPENGL_VERSION = 3.0

 private lateinit var arFragment : ArFragment

 @Nullable private lateinit var videoRenderable: ModelRenderable
 private lateinit var mediaPlayer: MediaPlayer

 private final var CHROMA_KEY_COLOR: Color = Color(0.1843f, 1.0f, 0.098f)

 private val VIDEO_HEIGHT_METERS = 0.85f;

 override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {

 super.onCreate(savedInstanceState, persistentState)

 if(!checkIsSupportedDeviceOrFinish(this)){
 return;
 }

 setContentView(R.layout.activity_video)

 arFragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment

 var texture = ExternalTexture()

 mediaPlayer = MediaPlayer.create(this, R.raw.giannis_dunk)
 mediaPlayer.setSurface(texture.surface)
 mediaPlayer.isLooping = true

 ModelRenderable.builder()
 .setSource(this, R.raw.giannis_dunk)
 .build()
 .thenAccept {
 renderable -> videoRenderable = renderable
 renderable.material.setExternalTexture("videoTexture", texture)
 renderable.material.setFloat4("keyColor", CHROMA_KEY_COLOR);
 }
 .exceptionally {
 throwable ->
 val toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
 toast.setGravity(Gravity.CENTER, 0, 0)
 toast.show()
 null
 }

 arFragment.setOnTapArPlaneListener{
 hitResult, plane, motionEvent ->
 if(videoRenderable == null){
 return@setOnTapArPlaneListener
 }

 var anchor = hitResult.createAnchor()
 var anchorNode = AnchorNode(anchor)
 anchorNode.setParent(arFragment.arSceneView.scene)

 var videoNode = Node()
 videoNode.setParent(anchorNode)

 val videoWidth = mediaPlayer.videoWidth
 val videoHeight = mediaPlayer.videoHeight

 videoNode.localScale = Vector3(
 VIDEO_HEIGHT_METERS * (videoWidth / videoHeight), VIDEO_HEIGHT_METERS, 1.0f)

 if(!mediaPlayer.isPlaying){
 mediaPlayer.start()

 texture.surfaceTexture.setOnFrameAvailableListener {
 videoNode.renderable = videoRenderable
 texture.surfaceTexture.setOnFrameAvailableListener { null }
 }
 } else{
 videoNode.renderable = videoRenderable
 }



 }


 }

 public override fun onDestroy() {
 super.onDestroy()

 if (mediaPlayer != null) {
 mediaPlayer.run {
 release()
 reset()
 }
 }
 }

 fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
 if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
 Log.e(TAG, "Sceneform " +
         "requires Android N or later")
 Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show()
 activity.finish()
 return false
 }
 val openGlVersionString = (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
 .deviceConfigurationInfo
 .glEsVersion
 if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
 Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
 Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
 .show()
 activity.finish()
 return false
 }
 return true
 }
 }

 //public inline fun String.toDouble(): Double = java.lang.Double.parseDouble(this)
