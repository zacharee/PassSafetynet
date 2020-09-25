package tk.zwander.passsafetynet.util

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.json.JSONObject
import java.io.File

class XposedHooks : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        fixSafetynet(lpparam)
    }

    private fun fixSafetynet(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == "android") {
            XposedHelpers.findAndHookMethod(
                File::class.java,
                "exists",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val file = param.thisObject as File?

                        val enforceFile = File("/sys/fs/selinux/enforce")
                        val binSu = File("/system/bin/su")
                        val xbinSu = File("/system/xbin/su")

                        when (file) {
                            enforceFile -> param.result = true
                            binSu, xbinSu -> param.result = false
                        }
                    }
                }
            )
        }

        XposedHelpers.findAndHookMethod(
            JSONObject::class.java,
            "getBoolean",
            String::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    when (param.args[0] as String?) {
                        "ctsProfileMatch",
                        "basicIntegrity",
                        "isValidSignature" -> {
                            param.result = true
                        }
                    }
                }
            }
        )
    }
}