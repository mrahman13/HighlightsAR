package com.nayeem.augumentedimages

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment

import java.io.IOException
import java.io.InputStream

class CustomArFragment : ArFragment() {
    override fun getSessionConfiguration(session: Session): Config {
        planeDiscoveryController.setInstructionView(null)
        val config = Config(session)
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        session.configure(config)
        arSceneView.setupSession(session)
        config.focusMode = Config.FocusMode.AUTO

        if ((activity as MainActivity).setupAugmentedImagesDb(config, session)) {
            Log.d("SetupAugImgDb", "Success")
        } else {
            Log.e("SetupAugImgDb", "Faliure setting up db")
        }

        return config
    }


}
