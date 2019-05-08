package com.nayeem.highlightsar

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import com.google.ar.core.AugmentedImage
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import android.media.MediaMetadataRetriever.*
import java.io.IOException
import java.io.InputStream

open class ArVideoFragment : ArFragment() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var externalTexture: ExternalTexture
    private lateinit var videoRenderable: ModelRenderable
    private lateinit var videoAnchorNode: AnchorNode
    private var TAG = "ArVideoFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaPlayer = MediaPlayer()
    }

    private fun createArScene(){
        externalTexture = ExternalTexture().also {
            mediaPlayer.setSurface(it.surface)
        }

        ModelRenderable.builder()
            .setSource(requireContext(), R.raw.augmented_video_model)
            .build()
            .thenAccept{ renderable ->
                videoRenderable = renderable
                renderable.isShadowCaster = false
                renderable.isShadowReceiver = false
                renderable.material.setExternalTexture("videoTexture", externalTexture)
            }
            .exceptionally { throwable ->
                Log.e(TAG, "Could not create ModelRenderable", throwable)
                return@exceptionally null
            }

        videoAnchorNode = AnchorNode().apply {
            setParent(arSceneView.scene)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    override fun onUpdate(frameTime: FrameTime) {
        val frame = arSceneView.arFrame
        if(frame == null || frame.camera.trackingState != TrackingState.TRACKING){
            return
        }

        val updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)
        for(augmentedImage in updatedAugmentedImages) {
            if(augmentedImage.trackingState == TrackingState.TRACKING){
                dismissArVideo()
                playbackVideo(augmentedImage)
                break
            }
        }
    }

    private fun dismissArVideo() {
        videoAnchorNode.anchor?.detach()
        videoAnchorNode.renderable = null
        //activeAugmentedImage = null
        mediaPlayer.reset()
    }

    private fun playbackVideo(augmentedImage: AugmentedImage){
        requireContext().assets.openFd(augmentedImage.name)
            .use{ descriptor ->

                val metadataRetriever = MediaMetadataRetriever()
                metadataRetriever.setDataSource(
                    descriptor.fileDescriptor,
                    descriptor.startOffset,
                    descriptor.length
                )

                val videoWidth = metadataRetriever.extractMetadata(METADATA_KEY_VIDEO_WIDTH).toFloatOrNull() ?: 0f
                val videoHeight = metadataRetriever.extractMetadata(METADATA_KEY_VIDEO_HEIGHT).toFloatOrNull() ?: 0f
                val videoRotation = metadataRetriever.extractMetadata(METADATA_KEY_VIDEO_ROTATION).toFloatOrNull() ?: 0f

                mediaPlayer.setDataSource(descriptor)            }.also {
                mediaPlayer.prepare()
                mediaPlayer.start()
            }

        videoAnchorNode.anchor = augmentedImage.createAnchor(augmentedImage.centerPose)
        videoAnchorNode.localScale = Vector3(
            augmentedImage.extentX,
            1.0f,
            augmentedImage.extentZ
        )

        externalTexture.surfaceTexture.setOnFrameAvailableListener {
            it.setOnFrameAvailableListener(null)
            videoAnchorNode.renderable = videoRenderable
        }
    }


}
