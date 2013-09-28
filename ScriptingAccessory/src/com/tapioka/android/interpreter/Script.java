// Copyright 2010 Google Inc. All Rights Reserved.

package com.tapioka.android.interpreter;


import com.tapioka.android.R;

import android.content.Context;
//import android.content.res.Resources;

public class Script {

    // load file name is described here
    // public final static int ID = R.raw.script;
    public final static int ID = R.raw.weather_light;

  public static String sFileName;

  public static String getFileName(Context context) {
    if (sFileName == null) {
//      sFileName = "intent_test.py";
      sFileName = "script.py";
      /*
      Resources resources = context.getResources();
      String name = resources.getText(ID).toString();
      sFileName = name.substring(name.lastIndexOf('/') + 1, name.length());
      */
    }
    return sFileName;
  }

  public static String getFileExtension(Context context) {
    if (sFileName == null) {
      getFileName(context);
    }
    int dotIndex = sFileName.lastIndexOf('.');
    if (dotIndex == -1) {
      return null;
    }
    return sFileName.substring(dotIndex);
  }

}
