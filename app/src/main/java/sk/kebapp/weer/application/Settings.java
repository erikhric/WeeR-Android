/*
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sk.kebapp.weer.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

    public boolean getEnableBackgroundPlay() {
        return false;
    }

    public boolean getUsingMediaCodec() {
        return true;
    }

    public boolean getUsingMediaCodecAutoRotate() {

        return false;
    }

    public boolean getUsingOpenSLES() {

        return true;
    }

    public String getPixelFormat() {
        return null;
    }

    public boolean getEnableNoView() {
        return false;
    }

    public boolean getEnableSurfaceView() {
//        String key = mAppContext.getString(R.string.pref_key_enable_surface_view);
        return false;
    }

    public boolean getEnableTextureView() {
//        String key = mAppContext.getString(R.string.pref_key_enable_texture_view);
        return true;
    }
}
