package com.duyh.navigationtest;

import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.IBinder;

import java.lang.reflect.Method;

public class InstallPackageUtils {

    public static void installApk(){
        try
        {
            //反射拿到Service Manager，然后调用getService获取IBinder对象
            Class<?> clazz = Class.forName("android.os.ServiceManager");
            Method method_getService = clazz.getMethod("getService",
                    String.class);
            IBinder bind = (IBinder) method_getService.invoke(null, "package");
//            PackageInstaller

//            IPackageManager iPm = IPackageManager.Stub.asInterface(bind);
//            //调用安装函数
//            iPm.installPackage(Uri.fromFile(apkFile), null, 2,apkFile.getName());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
