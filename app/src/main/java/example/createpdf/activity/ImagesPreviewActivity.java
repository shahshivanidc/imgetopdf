package example.createpdf.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.eftimoff.viewpagertransformers.DepthPageTransformer;

import java.util.ArrayList;

import butterknife.ButterKnife;
import example.createpdf.R;
import example.createpdf.util.ThemeUtils;

import static example.createpdf.util.Constants.PREVIEW_IMAGES;

public class ImagesPreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.setThemeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_images);

        ButterKnife.bind(this);
        Intent intent = getIntent();
        ArrayList<String> mImagesArrayList = intent.getStringArrayListExtra(PREVIEW_IMAGES);

        ViewPager mViewPager = findViewById(R.id.viewpager);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    public static Intent getStartIntent(Context context, ArrayList<String>  uris) {
        Intent intent = new Intent(context, ImagesPreviewActivity.class);
        intent.putExtra(PREVIEW_IMAGES, uris);
        return intent;
    }
}