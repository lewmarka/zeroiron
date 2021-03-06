/*
 * Copyright Martin Goulet 2012 - ZeroIron
 */

package com.mg.ZeroIron;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 * Activity used to display the list of course data to the user.
 */
public class ZeroIronCourseList extends ListActivity implements OnItemLongClickListener, EditDeletePopupInvoker {

	protected ZeroIronDbAdapter mDbAdapter;
	
	protected static final int NEW_ID = Menu.FIRST;
	//protected static final int GEN_ID = Menu.FIRST+1;
	//protected static final int DEL_ID = Menu.FIRST+2;
	
	protected static final int COURSE_EDIT_ACTIVITY_ID = 0;
	protected static final int GAME_EDIT_ACTIVITY_ID = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zeroiron_courselist);

		mDbAdapter = ZeroIronApplication.getDbAdapter();
		
		mDbAdapter.createCoursesTableIfRequired();
		
		ListView listView = this.getListView();
		listView.setOnItemLongClickListener(this);

	}
	
	/**
	 * updates the ListView in the activity wirh course data.
	 */
    private void fillData() {

        // Get all of the notes from the database and create the item list
        Cursor c = mDbAdapter.fetchAllCourses();
        startManagingCursor(c);

        String[] from = new String[] {  ZeroIronDbAdapter.KEY_COURSE_NAME,
        								ZeroIronDbAdapter.KEY_COURSE_LOCATION,
        								ZeroIronDbAdapter.KEY_COURSE_PAR,
        								ZeroIronDbAdapter.KEY_COURSE_SIZE};
        
        int[] to = new int[] { R.id.textCourseName,
        					   R.id.textLocationValue,
        					   R.id.textParValue,
        					   R.id.textSizeValue};
      
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter cursor =
            new SimpleCursorAdapter(this, R.layout.zeroiron_courselist_item, c, from, to);
        
        setListAdapter(cursor);

    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		//1 - retrieve parameters from user selection
		ListView listView = this.getListView();
		Cursor cursor = (Cursor) listView.getItemAtPosition(position);
		
		String courseName = cursor.getString(ZeroIronDbAdapter.COURSE_NAME_COLUMN);

		//2 - retrieve courseId from database
		int courseId = mDbAdapter.fetchCourseIdFromName(courseName);

		//3 - send to edit activity via intent
		Intent i = new Intent(ZeroIronCourseList.this, ZeroIronGameEdit.class);
		i.putExtra(ZeroIronDbAdapter.KEY_COURSE_NAME, courseName);
		i.putExtra(ZeroIronDbAdapter.KEY_COURSE_ID, courseId);
		startActivityForResult(i, GAME_EDIT_ACTIVITY_ID);
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK && requestCode == COURSE_EDIT_ACTIVITY_ID) {

			Bundle bundle = data.getExtras();
			
			//retrieve the data structure from bundle
			ZeroIronCourseStructure newCourseStructure = (ZeroIronCourseStructure) bundle.getSerializable(ZeroIronDbAdapter.NEW_RECORD);
			ZeroIronCourseStructure oldCourseStructure = null;
			
			//retrieve old values if applicable
			if (bundle.containsKey(ZeroIronDbAdapter.OLD_RECORD)) {
				oldCourseStructure = (ZeroIronCourseStructure) bundle.getSerializable(ZeroIronDbAdapter.OLD_RECORD);
			}
			
			//update existing record
			
			mDbAdapter.writeCourse(oldCourseStructure, newCourseStructure);
						
			fillData();
			
		} else if (resultCode == Activity.RESULT_OK && requestCode == GAME_EDIT_ACTIVITY_ID) {
			
			//retrieve game from intent
			Bundle bundle = data.getExtras();
			ZeroIronGameStructure newGame = (ZeroIronGameStructure) bundle.getSerializable(ZeroIronDbAdapter.NEW_RECORD);
			
			//store in DB
			mDbAdapter.writeGame(null, newGame);
			
			//2 - move to the games list...not sure how to do this
			int ert=0;
			ert++;
		}

	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	menu.add(0, NEW_ID, 0, R.string.menu_new_course);
        //menu.add(0, GEN_ID, 0, R.string.menu_gen_courses);
        //menu.add(0, DEL_ID, 0, R.string.menu_clear_courses);
        return true;
    }	

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case NEW_ID:

    		//build intent for edit screen
    		Intent i = new Intent(ZeroIronCourseList.this, ZeroIronCourseEdit.class);
    		
    		//launch intent
    		startActivityForResult(i, COURSE_EDIT_ACTIVITY_ID);
  
        	break;
        	/*
        case GEN_ID:
           	generateCourses();
           	break;
           	*/
        	/*
        case DEL_ID:
           	mDbAdapter.deleteAllCourses();
           	mDbAdapter.dropCoursesTable();
           	mDbAdapter.createCoursesTableIfRequired();
           	break;
           	*/
        }
        
        fillData();

        return super.onMenuItemSelected(featureId, item);
        
    }
    
    /**
	 * Generates sample courses in the database.
	 */
    private void generateCourses() {

		//create course structures and send over to the dbAdapter.
		
		ZeroIronCourseStructure course0 = new ZeroIronCourseStructure();
		course0.setCourseName("The Marshes");
		course0.setCourseLocation("Kanata, ON");
		course0.setCoursePar(5);
		course0.setCourseSize(18);    	
    	
    	ZeroIronCourseStructure course1 = new ZeroIronCourseStructure();
		course1.setCourseName("Pineview Golf - Executive Course");
		course1.setCourseLocation("Ottawa, ON");
		course1.setCoursePar(5);
		course1.setCourseSize(18);

		ZeroIronCourseStructure course2 = new ZeroIronCourseStructure();
		course2.setCourseName("Pineview Golf - Pro Course");
		course2.setCourseLocation("Ottawa, ON");
		course2.setCoursePar(5);
		course2.setCourseSize(18);		
		
		ZeroIronCourseStructure course3 = new ZeroIronCourseStructure();
		course3.setCourseName("White Sands");
		course3.setCourseLocation("Orleans, ON");
		course3.setCoursePar(3);
		course3.setCourseSize(9);
		
		boolean result = mDbAdapter.writeCourse(null, course0) && mDbAdapter.writeCourse(null, course1) &&
						 mDbAdapter.writeCourse(null, course2) && mDbAdapter.writeCourse(null, course3);

    }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		//Display current data from database
		fillData();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {

		ZeroIronEditDeletePopup popup = new ZeroIronEditDeletePopup(arg1, arg2); 
		popup.showLikeQuickAction();

		return true;
	}
	
	/**
	 * Invoked by the EditDeletePopupInvoker, this method retrieves the selected item
	 * and loads up a ZeroIronCourseEdit activity with the selected course via intent.
	 * @param rowId The row id of the selected course in the ListView
	 */
	public void editButtonClicked(int rowId) {
		
		//1 - retrieve parameters from user selection
		ListView listView = this.getListView();
		Cursor cursor = (Cursor) listView.getItemAtPosition(rowId); 
		String courseName = cursor.getString(ZeroIronDbAdapter.COURSE_NAME_COLUMN);
		String courseLocation = cursor.getString(ZeroIronDbAdapter.COURSE_LOCATION_COLUMN);
		int coursePar = cursor.getInt(ZeroIronDbAdapter.COURSE_PAR_COLUMN);
		int courseSize = cursor.getInt(ZeroIronDbAdapter.COURSE_SIZE_COLUMN);

		//2 - build structure		
		ZeroIronCourseStructure course =
				new ZeroIronCourseStructure(courseName, courseLocation, coursePar, courseSize); 

		//3 - send to edit activity via intent		
		Intent i = new Intent(ZeroIronCourseList.this, ZeroIronCourseEdit.class);
		i.putExtra(ZeroIronDbAdapter.OLD_RECORD, course);
		startActivityForResult(i, COURSE_EDIT_ACTIVITY_ID);
	}
	
	/**
	 * Invoked by the EditDeletePopupInvoker, this method attempts to delete the course entry
	 * from the database. The function will return unsuccessful if there exists a game record
	 * in the database that references the course.
	 * 
	 * @param rowId The row id of the selected course in the ListView
	 */
	public void deleteButtonClicked(int rowId) {
		//get a handle on the name of the course at location 'rowId'
		ListView listView = this.getListView();
		Cursor cursor = (Cursor) listView.getItemAtPosition(rowId); 
		String courseName = cursor.getString(ZeroIronDbAdapter.COURSE_NAME_COLUMN);
		
		//check if a game references the course first
		int courseId = mDbAdapter.fetchCourseIdFromName(courseName);
		Cursor c = mDbAdapter.fetchAllGames();
		
		int numReferences = 0;
		
		if (!c.moveToFirst()) {
			mDbAdapter.deleteCourse(courseName);
		} else {
	
			do {
				if (c.getInt(ZeroIronDbAdapter.GAME_COURSE_ID_COLUMN) == courseId) {
					numReferences++;
				}
			} while (c.moveToNext());
			
			if (numReferences == 0) {
				mDbAdapter.deleteCourse(courseName);
			} else {
				Toast.makeText(this.getApplicationContext(), "Cannot delete due to game reference.", Toast.LENGTH_SHORT).show();
			}
		}
		
		fillData();
	}
	
}


