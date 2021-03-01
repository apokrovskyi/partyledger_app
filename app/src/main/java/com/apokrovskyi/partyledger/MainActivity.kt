package com.apokrovskyi.partyledger

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.apokrovskyi.partyledger.models.Ledger
import com.apokrovskyi.partyledger.models.Ledger.Global
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    OnItemSelectedListener {
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mToggle: ActionBarDrawerToggle
    private lateinit var mNavigationView: NavigationView
    private lateinit var mSpinner: Spinner
    private lateinit var mHeader: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mHeader = findViewById(R.id.header_title)
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mToggle = ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close)
        mNavigationView = findViewById(R.id.navigation_view)

        mDrawerLayout.addDrawerListener(mToggle)
        mToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mNavigationView.setNavigationItemSelectedListener(this)

        Global.load(applicationContext)

        mSpinner = findViewById(R.id.spinner)
        updateSpinner()
        mSpinner.onItemSelectedListener = this
        val button = findViewById<ImageButton>(R.id.delete_button)
        button.setOnClickListener {
            Ledger.ledgers.removeAt(mSpinner.selectedItemPosition)
            if (Ledger.ledgers.isEmpty())
                Ledger.ledgers.add(Ledger("Ledger"))
            Ledger.Current = Global.ledgers[0]
            updateSpinner()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position in Ledger.ledgers.indices) {
            val newCurrent = Global.ledgers[position]

            if (newCurrent != Ledger.Current) {
                Ledger.Current = newCurrent
                selectFragment(R.id.total)
            }
            return
        }

        mSpinner.setSelection(Ledger.ledgers.indexOf(Ledger.Current))

        val dialogView = Util.inflateDialog(layoutInflater, R.layout.textbox_dialog)

        AlertDialog.Builder(this)
            .setTitle("New ledger")
            .setView(dialogView)
            .setPositiveButton("Add")
            { dialog, _ ->
                dialog.dismiss()
                val name = dialogView.findViewById<EditText>(R.id.textEdit).text.toString()
                if (name.isBlank()) {
                    Toast.makeText(this, "No ledger name specified", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newLedger = Ledger(name)
                Ledger.ledgers.add(newLedger)
                Ledger.Current = newLedger
                updateSpinner()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()
            .show()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    private fun updateSpinner() {
        mSpinner.adapter = ArrayAdapter<Ledger>(
            applicationContext,
            android.R.layout.simple_spinner_dropdown_item,
            Global.ledgers.plus(Ledger("[new ledger]")).toTypedArray()
        )
        mSpinner.setSelection(Ledger.ledgers.indexOf(Ledger.Current))
        selectFragment(R.id.total)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mToggle.onOptionsItemSelected(item))
            return true

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        selectFragment(item.itemId)
        return true
    }

    private fun selectFragment(id: Int) {
        val fragment: TitledFragment = when (id) {
            R.id.total -> Total()
            R.id.members -> MemberList()
            R.id.groups -> GroupList()
            R.id.purchases -> PurchaseList()
            R.id.payments -> PaymentList()
            else -> throw IllegalArgumentException("Wrong item id")
        }

        mNavigationView.menu.findItem(id).isChecked = true
        supportFragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit()
        mHeader.text = fragment.title
        mDrawerLayout.closeDrawers()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val action: Int = event.action
        val keyCode: Int = event.keyCode
        if (action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                val data = (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager)
                    .primaryClip?.getItemAt(0)?.text.toString()
                Global.load(applicationContext, data)
                Ledger.Current = Global.ledgers[0]
                updateSpinner()
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                    ClipData.newPlainText(
                        "text",
                        Global.save(applicationContext)
                    )
                )
            else if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
                    mDrawerLayout.closeDrawer(GravityCompat.START)
                else return false
            }
            return true
        }
        return false
    }
}