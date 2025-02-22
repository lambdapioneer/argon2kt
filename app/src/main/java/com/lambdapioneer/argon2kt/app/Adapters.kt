// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.lambdapioneer.argon2kt.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.lambdapioneer.argon2kt.Argon2Mode
import com.lambdapioneer.argon2kt.Argon2Version

class ArgonModeAdapter(private val layoutInflater: LayoutInflater) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: layoutInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
        view.findViewById<TextView>(android.R.id.text1).text = getItem(position).toString()
        return view
    }

    override fun getItem(position: Int): Any = Argon2Mode.entries[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getCount(): Int = Argon2Mode.entries.size
}

class ArgonVersionAdapter(private val layoutInflater: LayoutInflater) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: layoutInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
        view.findViewById<TextView>(android.R.id.text1).text = getItem(position).toString()
        return view
    }

    override fun getItem(position: Int): Any = Argon2Version.entries[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getCount(): Int = Argon2Version.entries.size
}
