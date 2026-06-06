/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Sacred Pixel Dungeon
 * Copyright (C) 2026 AI SOFT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.sacredpixel.sacredpixeldungeon.teavm;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * TeaVM-specific interstitial ad bridge for Appsintoss.
 * Communicates with parent window via window.parent calls.
 *
 * All calls are defensive - if the API is not available, they will
 * silently fail and call the completion callback.
 */
public class TeaVMInterstitialAd {

    /**
     * Check if interstitial ad API is available.
     * Returns true if the bridge function exists in parent window.
     */
    @JSBody(script =
        "try {" +
        "  return window.parent && " +
        "         window.parent.__INTERSTITIAL_AD_AVAILABLE__ === true && " +
        "         typeof window.parent.__showInterstitialAd__ === 'function';" +
        "} catch(e) {" +
        "  return false;" +
        "}")
    public static native boolean isAvailable();

    /**
     * Preload an interstitial ad (call when boss dies).
     * This loads the ad in the background so it's ready to show immediately.
     */
    @JSBody(script =
        "try {" +
        "  if (window.parent && typeof window.parent.__preloadInterstitialAd__ === 'function') {" +
        "    console.log('TeaVMInterstitialAd: preloading ad');" +
        "    window.parent.__preloadInterstitialAd__();" +
        "  } else {" +
        "    console.log('TeaVMInterstitialAd: preload not available');" +
        "  }" +
        "} catch(e) {" +
        "  console.warn('TeaVMInterstitialAd preload error', e);" +
        "}")
    public static native void preload();

    /**
     * Check if an ad is preloaded and ready to show.
     */
    @JSBody(script =
        "try {" +
        "  if (window.parent && typeof window.parent.__isAdPreloaded__ === 'function') {" +
        "    return window.parent.__isAdPreloaded__();" +
        "  }" +
        "  return false;" +
        "} catch(e) {" +
        "  return false;" +
        "}")
    public static native boolean isPreloaded();

    /**
     * Show an interstitial ad.
     * @param callback Called when the ad is dismissed or fails to show.
     */
    public static void show(AdCallback callback) {
        showAsync(new JSAdCallback() {
            @Override
            public void onComplete() {
                callback.onComplete();
            }
        });
    }

    /**
     * Callback interface for ad completion.
     */
    public interface AdCallback {
        void onComplete();
    }

    /**
     * Internal JS callback interface.
     */
    @JSFunctor
    private interface JSAdCallback extends JSObject {
        void onComplete();
    }

    @JSBody(params = {"callback"}, script =
        "try {" +
        "  var called = false;" +
        "  var doComplete = function() {" +
        "    if (called) return;" +
        "    called = true;" +
        "    console.log('TeaVMInterstitialAd: invoking Java callback');" +
        "    try { callback.onComplete(); } catch(e) { console.error('TeaVMInterstitialAd callback error', e); }" +
        "    window.__onInterstitialAdComplete__ = null;" +
        "    window.removeEventListener('message', msgHandler);" +
        "  };" +
        "  window.__onInterstitialAdComplete__ = doComplete;" +
        "  var msgHandler = function(e) {" +
        "    if (e.data && e.data.type === 'adComplete') {" +
        "      console.log('TeaVMInterstitialAd: received postMessage fallback');" +
        "      doComplete();" +
        "    }" +
        "  };" +
        "  window.addEventListener('message', msgHandler);" +
        "  if (window.parent && typeof window.parent.__showInterstitialAd__ === 'function') {" +
        "    console.log('TeaVMInterstitialAd: calling parent');" +
        "    window.parent.__showInterstitialAd__();" +
        "  } else {" +
        "    console.warn('TeaVMInterstitialAd: not available, completing immediately');" +
        "    doComplete();" +
        "  }" +
        "} catch(e) {" +
        "  console.error('TeaVMInterstitialAd error', e);" +
        "  try { callback.onComplete(); } catch(e2) {}" +
        "}")
    private static native void showAsync(JSAdCallback callback);
}
