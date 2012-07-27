package in.uncod.android.droidbooru;

import android.util.Log;
import android.os.Bundle;
import android.accounts.AccountManager;
import android.accounts.Account;

import com.actionbarsherlock.app.SherlockActivity;

public class LoginActivity extends SherlockActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AccountManager am = AccountManager.get(this);

        Account[] accounts = am.getAccountsByType("com.google");
        for (Account account : accounts) {
            if (account.name.contains("@ironclad.mobi"))
                Log.d("", account.name + ", " + account.type);
        }
    }
}
