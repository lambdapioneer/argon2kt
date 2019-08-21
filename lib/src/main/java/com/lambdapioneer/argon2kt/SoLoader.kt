// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.lambdapioneer.argon2kt

interface SoLoaderShim {
    fun loadLibrary(libname: String)
}

class SystemSoLoader : SoLoaderShim {
    override fun loadLibrary(libname: String) = System.loadLibrary(libname)
}
