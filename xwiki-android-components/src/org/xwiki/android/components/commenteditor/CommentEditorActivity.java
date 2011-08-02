package org.xwiki.android.components.commenteditor;

import java.util.List;

import org.xwiki.android.components.R;
import org.xwiki.android.components.login.LoginActivity;
import org.xwiki.android.resources.Attachment;
import org.xwiki.android.resources.Attachments;
import org.xwiki.android.resources.Comment;
import org.xwiki.android.resources.Comments;
import org.xwiki.android.rest.HttpConnector;
import org.xwiki.android.rest.Requests;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CommentEditorActivity extends ListActivity
{
    private String[] data;
    
    private List<Comment> commentList;

    private String wikiName, spaceName, pageName, username, password, url;

    private boolean isSecured = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        wikiName = getIntent().getExtras().getString("wikiname");
        spaceName = getIntent().getExtras().getString("spacename");
        pageName = getIntent().getExtras().getString("pagename");
        url = getIntent().getExtras().getString("url");

        if (getIntent().getExtras().getString("password") != null
            && getIntent().getExtras().getString("username") != null) {
            username = getIntent().getExtras().getString("username");
            password = getIntent().getExtras().getString("password");
            isSecured = true;
        }

      

        setupListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.commenteditor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.add_comment:
                addComment();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // shows add comment dialog and do update
    private void addComment()
    {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);

        alert.setView(input);
        alert.setTitle("Enter new comment");
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                dialog.dismiss();
                
                String value = input.getText().toString().trim();

                Comment comment = new Comment();
                comment.setText(value);
                Requests requests = new Requests(url);
                if(isSecured){
                    requests.setAuthentication(username, password);
                }
                
                String s = requests.addPageComment(wikiName, spaceName, pageName, comment);
              
                setupListView();
                
                
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                dialog.cancel();
            }
        });
        alert.show();
    }

    private void initDataArray()
    {
        final ProgressDialog myProgressDialog;
        myProgressDialog = ProgressDialog.show(CommentEditorActivity.this,
                "Comment", "Loading comments...", true);
        
        Requests request = new Requests(url);
        if (isSecured) {
            request.setAuthentication(username, password);
        }

        Comments comments = request.requestPageComments(wikiName, spaceName, pageName);

        commentList = comments.getComments();

        data = new String[commentList.size()];

        for (int i = 0; i < data.length; i++) {
            data[i] = commentList.get(i).getAuthor() + "\n" + commentList.get(i).getText();
        }
        
        myProgressDialog.dismiss();
    }
    
    private void setupListView(){
        
        initDataArray();
        setListAdapter(new ArrayAdapter<String>(this, R.layout.attachment_list_item, data));

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView< ? > arg0, View view, int arg2, long arg3)
            {
                Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
