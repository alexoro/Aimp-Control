/*
 * Copyright (c) 2013, Sorokin Alexander (uas.sorokin@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * 3. The names of the authors may not be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.uas.media.aimp.api;

import android.util.Log;

/**
 * User: uas.sorokin@gmail.com
 */
public class Logger {
	
	private static final boolean SHOW_DEBUG_INFO = true; 
	protected static final String TAG = "AimpControl/API";


	public static void d(String tag, String message) {
		if (SHOW_DEBUG_INFO) Log.d(tag, message);
	}

	public static void d(String tag, String message, Throwable t) {
		if (SHOW_DEBUG_INFO) Log.d(tag, message, t);
	}
	
	public static void d(String message) {
		d(TAG, message);
	}

	public static void d(String message, Throwable t) {
		d(TAG, message, t);
	}


	public static void e(String tag, String message) {
		Log.e(tag, message);
	}

	public static void e(String tag, String message, Throwable t) {
		Log.e(tag, message, t);
	}

	public static void e(String message) {
		e(TAG, message);
	}

	public static void e(String message, Throwable t) {
		e(TAG, message, t);
	}

	public static void i(String tag, String message) {
		Log.i(tag, message);
	}

	public static void v(String tag, String message) {
		Log.v(tag, message);
	}
	
	public static void w(String tag, String message, Throwable t) {
		Log.v(tag, message, t);
	}
	
}