/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.xwiki.android.components.listnavigator;

import org.xwiki.android.components.IntentExtra;
import org.xwiki.android.components.R;
import org.xwiki.android.components.search.SearchActivity;
import org.xwiki.android.resources.Pages;
import org.xwiki.android.resources.Spaces;
import org.xwiki.android.resources.Wikis;
import org.xwiki.android.rest.Requests;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * XWiki List Navigator Activity to select a page.
 */
public class XWikiListNavigatorActivity extends Activity
{
    public static final String INTENT_EXTRA_PUT_URL = IntentExtra.URL;

    public static final String INTENT_EXTRA_PUT_USERNAME = IntentExtra.USERNAME;

    public static final String INTENT_EXTRA_PUT_PASSWORD = IntentExtra.PASSWORD;

    public static final String INTENT_EXTRA_GET_WIKI_NAME = IntentExtra.WIKI_NAME;

    public static final String INTENT_EXTRA_GET_SPACE_NAME = IntentExtra.SPACE_NAME;

    public static final String INTENT_EXTRA_GET_PAGE_NAME = IntentExtra.PAGE_NAME;

    private static final int SEARCH_ACTIVITY_REQUEST_CODE = 100;

    private String url, wikiName, spaceName, pageName, username, password, cachedWikiName, cachedSpaceName;

    private Requests request;

    // state 0 = wiki 1=space 2= pages
    private int state;

    private boolean isSecured;

    private String[] data;

    private String[] wikiList, spaceList, pageList;

    private Button backButton;

    private TextView titleTextView;

    ProgressDialog myProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xwikilistnavigator);

        titleTextView = (TextView) findViewById(R.id.textView_listnavigator_title);

        state = 0;

        url = getIntent().getExtras().getString(INTENT_EXTRA_PUT_URL);

        if (getIntent().getExtras().getString(INTENT_EXTRA_PUT_PASSWORD) != null
            && getIntent().getExtras().getString(INTENT_EXTRA_PUT_USERNAME) != null) {
            username = getIntent().getExtras().getString(INTENT_EXTRA_PUT_USERNAME);
            password = getIntent().getExtras().getString(INTENT_EXTRA_PUT_PASSWORD);
            isSecured = true;
        }

        request = new Requests(url);

        if (isSecured) {
            request.setAuthentication(username, password);
        }

        myProgressDialog = new ProgressDialog(XWikiListNavigatorActivity.this);
        myProgressDialog.setTitle("Loading");
        myProgressDialog.setMessage("Please wait while connecting...");

        backButton = (Button) findViewById(R.id.button_listnavigator_back);

        backButton.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View arg0)
            {
                state--;
                addItemsToList(state, null);
            }
        });

        wikiName = "";
        spaceName = "";
        pageName = "";

        addItemsToList(state, null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.xwikilistnavigator_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.do_search:
                showSearch();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == SEARCH_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                wikiName = data.getExtras().getString(SearchActivity.INTENT_EXTRA_GET_WIKI_NAME);
                spaceName = data.getExtras().getString(SearchActivity.INTENT_EXTRA_GET_SPACE_NAME);
                pageName = data.getExtras().getString(SearchActivity.INTENT_EXTRA_GET_PAGE_NAME);
                completeProcess();
            }
        }
    }

    private void showSearch()
    {
        Intent intent = new Intent(this, SearchActivity.class);

        if (isSecured) {
            intent.putExtra(SearchActivity.INTENT_EXTRA_PUT_USERNAME, username);
            intent.putExtra(SearchActivity.INTENT_EXTRA_PUT_PASSWORD, password);
        }

        intent.putExtra(SearchActivity.INTENT_EXTRA_PUT_URL, url);

        startActivityForResult(intent, SEARCH_ACTIVITY_REQUEST_CODE);
    }

    private void loadWikis()
    {
        if (wikiList == null) {
            Wikis wikis = request.requestWikis();

            data = new String[wikis.getWikis().size()];

            for (int i = 0; i < data.length; i++) {
                data[i] = wikis.getWikis().get(i).getName();
            }

            wikiList = data;
        } else {
            data = wikiList;
            Log.d("Optimize", "optimized wikiList");
        }

    }

    private void loadSpaces()
    {
        if (wikiName.equals(cachedWikiName)) {
            data = spaceList;
            Log.d("Optimize", "optimized spaceList");
        } else {
            Spaces spaces = request.requestSpaces(wikiName);

            data = new String[spaces.getSpaces().size()];

            for (int i = 0; i < data.length; i++) {
                data[i] = spaces.getSpaces().get(i).getName();
            }

            spaceList = data;
            cachedWikiName = wikiName;

        }

    }

    private void loadPages()
    {
        if (wikiName.equals(cachedWikiName) && spaceName.equals(cachedSpaceName)) {
            data = pageList;
            Log.d("Optimize", "optimized pageList");
        } else {
            Pages pages = request.requestAllPages(wikiName, spaceName);

            data = new String[pages.getPageSummaries().size()];

            for (int i = 0; i < data.length; i++) {
                data[i] = pages.getPageSummaries().get(i).getName();
            }

            pageList = data;
            cachedWikiName = wikiName;
            cachedSpaceName = spaceName;
        }

    }

    private void addItemsToList(final int stateID, final String selectedItem)
    {
        myProgressDialog.show();

        if (stateID == 0) {
            titleTextView.setText("");
            loadWikis();

        } else if (stateID == 1) {

            if (selectedItem != null && !wikiName.equals(selectedItem)) {
                wikiName = selectedItem;
                spaceName = "";
                pageName = "";
            }

            titleTextView.setText(wikiName);
            loadSpaces();

        } else if (stateID == 2) {
            if (selectedItem != null && !spaceName.equals(selectedItem)) {
                spaceName = selectedItem;
                pageName = "";
            }
            titleTextView.setText(wikiName + " > " + spaceName);
            loadPages();
        } else if (stateID < 0) {
            finishProcess();
        } else {
            pageName = selectedItem;
            completeProcess();
        }

        myProgressDialog.dismiss();

        setUIElements();
        Log.d("Variables", "wikiName,spaceName,pagename=" + wikiName + "," + spaceName + "," + pageName);
    }

    private void setUIElements()
    {
        ArrayAdapter<String> adapt =
            new ArrayAdapter<String>(getApplicationContext(), R.layout.xwikilistnavigator_list_item, data);

        ListView lv = (ListView) findViewById(R.id.listView_xwikilistnavigator);
        lv.setAdapter(adapt);
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView< ? > arg0, View view, int arg2, long arg3)
            {
                state++;
                addItemsToList(state, ((TextView) view).getText().toString());
            }
        });
    }

    private void completeProcess()
    {
        getIntent().putExtra(INTENT_EXTRA_GET_WIKI_NAME, wikiName);
        getIntent().putExtra(INTENT_EXTRA_GET_SPACE_NAME, spaceName);
        getIntent().putExtra(INTENT_EXTRA_GET_PAGE_NAME, pageName);
        setResult(Activity.RESULT_OK, getIntent());
        finish();
    }

    private void finishProcess()
    {
        setResult(Activity.RESULT_CANCELED, getIntent());
        finish();
    }
}
