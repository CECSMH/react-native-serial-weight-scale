package com.serialweightscale

import com.facebook.react.BaseReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import java.util.HashMap

class SerialWeightScalePackage : BaseReactPackage() {
  override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? {
    return if (name == SerialWeightScaleModule.NAME) {
      SerialWeightScaleModule(reactContext)
    } else {
      null
    }
  }

 override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        return listOf(SerialWeightScaleModule(reactContext))
    }



  override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
    return ReactModuleInfoProvider {
      val moduleInfos: MutableMap<String, ReactModuleInfo> = HashMap()
      moduleInfos[SerialWeightScaleModule.NAME] = ReactModuleInfo(
        SerialWeightScaleModule.NAME,
        SerialWeightScaleModule.NAME,
        false,  // canOverrideExistingModule
        false,  // needsEagerInit
        false,  // isCxxModule
        true // isTurboModule
      )
      moduleInfos
    }
  }
}
