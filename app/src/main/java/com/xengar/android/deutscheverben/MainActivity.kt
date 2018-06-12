/*
 * Copyright (C) 2018 Angel Garcia
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
package com.xengar.android.deutscheverben

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*

/**
 * MainActivity
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_change_group -> {
                //changeVerbGroup()
                return true
            }

            R.id.action_sort -> {
                //sortVerbs()
                return true
            }

            R.id.action_most_common -> {
                //showMostCommon()
                return true
            }

            R.id.action_search -> {
                //ActivityUtils.launchSearchActivity(applicationContext)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        when (id) {
            R.id.nav_verbs -> {
                //page = PAGE_VERBS

                supportActionBar!!.setTitle(R.string.verbs)
                //ActivityUtils.saveStringToPreferences(applicationContext, CURRENT_PAGE, PAGE_VERBS)
                //launchFragment(PAGE_VERBS)
            }
            R.id.nav_cards -> {
                //page = PAGE_CARDS

                supportActionBar!!.setTitle(R.string.cards)
                //ActivityUtils.saveStringToPreferences(applicationContext, CURRENT_PAGE, PAGE_CARDS)
                //launchFragment(PAGE_CARDS)
            }
            R.id.nav_favorites -> {
                //page = PAGE_FAVORITES

                supportActionBar!!.setTitle(R.string.favorites)
                //ActivityUtils.saveStringToPreferences(applicationContext, CURRENT_PAGE, PAGE_FAVORITES)
                //launchFragment(PAGE_FAVORITES)
            }
            R.id.nav_settings -> {}//ActivityUtils.launchSettingsActivity(applicationContext)
            R.id.nav_help -> {}//ActivityUtils.launchHelpActivity(applicationContext)
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}
