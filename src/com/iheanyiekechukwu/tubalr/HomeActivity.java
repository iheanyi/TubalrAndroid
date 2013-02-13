
package com.iheanyiekechukwu.tubalr;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.MediaController;

public class HomeActivity extends Activity {
    MediaController controller;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        controller = (MediaController) findViewById(R.id.mediaController);
        
        //controller = new MediaController(this);
        controller.show(50000);
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
        controller.hide();
        //mediaPlayer.stop();
        //mediaPlayer.release();
    }

}
