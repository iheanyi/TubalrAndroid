
package com.iheanyiekechukwu.tubalr;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.MediaController;

public class HomeActivity extends Activity implements OnItemClickListener {
    MediaController controller;
    
    private ListView menuListView;
    private ArrayAdapter<String> menuAdapter;
    private Resources res;
    private String[] menuNames;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        //controller = (MediaController) findViewById(R.id.mediaController);
        
        menuListView = (ListView) findViewById(R.id.homeFixedListView);
        
        res = getResources();
        
        menuNames = res.getStringArray(R.array.home_menu);
        
        menuAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menuNames);
        menuListView.setAdapter(menuAdapter);
        menuListView.setOnItemClickListener(this);
        
        
        //controller = new MediaController(this);
        //controller.show(50000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_home, menu);
        return true;
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        //controller.hide();
        //mediaPlayer.stop();
        //mediaPlayer.release();
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View v, int id, long arg3) {
        // TODO Auto-generated method stub
        
    }

}
