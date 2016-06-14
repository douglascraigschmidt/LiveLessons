package example.imagetaskgang;

import java.io.File;
import java.util.ArrayList;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * @class ResultsActivity
 *
 * @brief Allows the user to browse the processed images stored by the
 *        ImageTaskGang.
 */
public class ResultsActivity extends Activity {
    /**
     * The names of the filters used in the ImageTaskGang, which is
     * used to organize the results into groups.
     */
    private String[] mFilterNames;

    /**
     * The layout that contains the buttons that are responsible for
     * loading the images into the GridView.
     */
    private LinearLayout mLayout;
    
    /**
     * The column width to use for the GridView.
     */
    private int mColWidth;
    
    /**
     * The number of columns to use in the GridView.
     */
    private int mNumCols;
    
    /**
     * A reasonable column width.
     */
    private final int COL_WIDTH = 300;

    /**
     * The adapter responsible for loading the results into the
     * GridView.
     */
    private ImageAdapter imageAdapter;
	
    /**
     * Creates the activity and generates a button for each filter
     * applied to the images. These buttons load change the
     * imageAdapter's source to a new directory, from which it will
     * load images into the GridView.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        
        // Retrieve the Layout that buttons will be added to.
        mLayout = (LinearLayout) findViewById(R.id.buttonList);
		
        // Configure the GridView adapter and dimensions.
        imageAdapter = new ImageAdapter(this);
        GridView imageGrid = (GridView) findViewById(R.id.imageGrid);
        imageGrid.setAdapter(imageAdapter);
        configureGridView(imageGrid);
        
        // Retrieves the names of the filters applied to this set of
        // downloads.
        mFilterNames =
            getIntent().getStringArrayExtra(MainActivity.FILTER_EXTRA);

        // Iterate over the filter names and generate a button for
        // each filter.
        for (String filterName : mFilterNames) 
            addResultButton(filterName);
    }
    
    /**
     * Add a button with the given filterName as its text.  This
     * button will load the results of the given filter into the
     * GridView
     */
    @SuppressLint("InflateParams")
    private void addResultButton(String filterName) {
    	// Create a new button with the layout of "result_button".
        Button resultButton = 
            (Button) LayoutInflater.from(this).inflate 
            (R.layout.result_button,
             null);
        
        // Set the new button's text and tag to the filter name.
        resultButton.setText(filterName);
        resultButton.setTag(filterName);
        
        // When the button is clicked, change the imageAdapter source
        // to the appropriate filter directory.
        resultButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button button =
                        (Button) view;
                    
                    // Find the filter directory and load the
                    // directory as the source of the imageAdapter.
                    imageAdapter.setBitmaps
                        (new File(PlatformStrategy.instance().getDirectoryPath(),
                                  button.getText().toString()).getAbsolutePath());
				
                }
            });
        
        // Add the button to the layout.
        mLayout.addView(resultButton);
    }
    
    /**
     * Configures the GridView with an appropriate column number and
     * width based on the screen size.
     */
    private void configureGridView(GridView imageGrid) {
    	// Retrieve the Screen dimensions.
        Display display = getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	display.getSize(size);
    	
    	// Calculate appropriate values.
    	mNumCols = size.x/COL_WIDTH;
    	mColWidth = size.x/mNumCols;
    	
    	// Configure the GridView with dynamic values.
    	imageGrid.setColumnWidth(mColWidth);
    	imageGrid.setNumColumns(mNumCols);
    }

    /**
     * @class ImageAdapter
     *
     * @brief The Adapter that loads the Images into the Layout's
     *        GridView.
     */
    public class ImageAdapter extends BaseAdapter {
        /**
         * The Context of the application
         */
        private Context mContext;
        
        /**
         * The padding each image will have around it
         */
        private int mPadding = 8;

        /**
         * The ArrayList of bitmaps that hold the thumbnail images.
         */
        private ArrayList<Bitmap> mBitmaps;

        /**
         * Creates the ImageAdapter in the given context.
         */
        public ImageAdapter(Context c) {
            mContext = c;
            mBitmaps = new ArrayList<Bitmap>();
        }

        /**
         * Returns the count of bitmaps in the list.
         */
        @Override
            public int getCount() {
            return mBitmaps.size();
        }

        /**
         * Returns the bitmap at the given position.
         */
        @Override
            public Object getItem(int position) {
            return mBitmaps.get(position);
        }

        /**
         * Returns the given position as the Id of the bitmap.  This
         * works because the bitmaps are stored in a sequential
         * manner.
         */
        @Override
            public long getItemId(int position) {
            return position;
        }

        /**
         * Returns the view. This method is necessary for filling the
         * GridView appropriately.
         */
        @Override
        public View getView(int position,
                            View convertView,
                            ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                
                // Set configuration properties of the ImageView
                imageView.setLayoutParams(
                		new GridView.LayoutParams(mColWidth, mColWidth));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(mPadding, mPadding, mPadding, mPadding);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageBitmap(mBitmaps.get(position));
            return imageView;
        }

        /**
         * Resets the bitmaps of the GridView to the ones found at the
         * given filterPath.
         */
        private void setBitmaps(String filterPath) {
            File[] bitmaps = new File(filterPath).listFiles();
            mBitmaps = new ArrayList<Bitmap>();

            for (File bitmap : bitmaps){
                if (bitmap != null) {
                    mBitmaps.add
                        (BitmapFactory.decodeFile(bitmap.getAbsolutePath()));
                }
            }
            notifyDataSetChanged();
        }
    }

}
