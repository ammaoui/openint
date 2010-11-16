/*
 * Copyright (C) 2010 Karl Ostmo
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

package org.openintents.calendarpicker.activity;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.openintents.calendarpicker.MiniMonthDrawable;
import org.openintents.calendarpicker.R;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.LinearLayout.LayoutParams;


public class YearsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.years);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.titlebar_icon);

        LayoutParams parms = new LayoutParams(
        		TableRow.LayoutParams.FILL_PARENT,
        		TableRow.LayoutParams.FILL_PARENT,
        		1);
        
        LinearLayout vertical_layout = (LinearLayout) findViewById(R.id.years_table);
        

		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.MONTH, cal.getMinimum(Calendar.MONTH));
        
        boolean horizontal = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        int rows = horizontal ? 3 : 4;
        int cols = horizontal ? 4 : 3;
        
        for (int i=0; i<rows; i++) {
	        LinearLayout row = new LinearLayout(this);
	        vertical_layout.addView(row, parms);
	
	        for (int j=0; j<cols; j++) {
		        
		        ImageView iv = new ImageView(this);
		        iv.setImageDrawable(new MiniMonthDrawable(this, iv, (Calendar) cal.clone()));
		        row.addView(iv, parms);

	        	cal.add(Calendar.MONTH, 1);
	        }
        }
    }
}