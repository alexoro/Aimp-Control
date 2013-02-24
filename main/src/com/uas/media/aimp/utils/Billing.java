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

package com.uas.media.aimp.utils;

import net.robotmedia.billing.BillingController;

/**
 * User: uas.sorokin@gmail.com
 */
public class Billing {

    private Billing() {
    }

    public static final String DONATE_1_99 = "com.uas.media.aimp.vending.donate_1_99";


    public static void init() {
        BillingController.setDebug(true);
        BillingController.setConfiguration(new BillingController.IConfiguration() {
            public byte[] getObfuscationSalt() {
                return new byte[] {37, -110, 112, -41, 99, -77, 114, -1, -127, -98, 28, -77, -127, -115, 13, 73, 57, 55, -24, 57};
            }

            public String getPublicKey() {
                return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwhdCY8vDyuwNuzscXCaE1gdfL5nEo/hGF/It8eNK9GxyO4PK5wu587p7vemHCWou5RGWk7i6fG31jYC64C1lq6kaez45oboOj3a8siciQqOMkl9pQZgNl9VKYb7zgoCJuR6KpNibTgM5NJNQrEzS6pGZI55Zohh0ElHft3Edcyjs2lh8R/ANs0fxO0c0RBadOWeH0N9jIqO+TggcyaQqPTcAekW0oiDjYzdtyevmsBPv65QHOx0w0RQ+iJj6pxec3EHW1du8OYXRccrlPcxKcdJEScyGW3NHws+sHX14duxbwDUIR9jhW23QGCwlqwSTkA9L4cjdDcssx5qbAjssuwIDAQAB";
            }
        });
    }

}