package example.createpdf.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.lottie.LottieAnimationView;
import com.dd.morphingbutton.MorphingButton;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import example.createpdf.R;
import example.createpdf.activity.ImagesPreviewActivity;
import example.createpdf.adapter.ExtractImagesAdapter;
import example.createpdf.adapter.MergeFilesAdapter;
import example.createpdf.interfaces.BottomSheetPopulate;
import example.createpdf.interfaces.ExtractImagesListener;
import example.createpdf.util.BottomSheetCallback;
import example.createpdf.util.BottomSheetUtils;
import example.createpdf.util.CommonCodeUtils;
import example.createpdf.util.FileUtils;
import example.createpdf.util.MorphButtonUtility;
import example.createpdf.util.PdfToImages;

import static android.app.Activity.RESULT_OK;
import static example.createpdf.util.CommonCodeUtils.populateUtil;
import static example.createpdf.util.Constants.BUNDLE_DATA;
import static example.createpdf.util.Constants.PDF_TO_IMAGES;
import static example.createpdf.util.DialogUtils.createAnimationDialog;
import static example.createpdf.util.FileUriUtils.getFilePath;

public class PdfToImageFragment extends Fragment implements BottomSheetPopulate, MergeFilesAdapter.OnClickListener,
        ExtractImagesListener, ExtractImagesAdapter.OnFileItemClickedListener {

    private static final int INTENT_REQUEST_PICKFILE_CODE = 10;
    private Activity mActivity;
    private String mPath;
    private Uri mUri;
    private MorphButtonUtility mMorphButtonUtility;
    private FileUtils mFileUtils;
    private BottomSheetBehavior mSheetBehavior;
    private BottomSheetUtils mBottomSheetUtils;
    private ArrayList<String> mOutputFilePaths;
    private MaterialDialog mMaterialDialog;
    private String mOperation;

    @BindView(R.id.lottie_progress)
    LottieAnimationView mLottieProgress;
    @BindView(R.id.bottom_sheet)
    LinearLayout mLayoutBottomSheet;
    @BindView(R.id.upArrow)
    ImageView mUpArrow;
    @BindView(R.id.selectFile)
    MorphingButton mSelectFileButton;
    @BindView(R.id.createImages)
    MorphingButton mCreateImagesButton;
    @BindView(R.id.created_images)
    RecyclerView mCreatedImages;
    @BindView(R.id.pdfToImagesText)
    TextView mCreateImagesSuccessText;
    @BindView(R.id.options)
    LinearLayout options;
    @BindView(R.id.layout)
    RelativeLayout mLayout;
    @BindView(R.id.recyclerViewFiles)
    RecyclerView mRecyclerViewFiles;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pdf_to_image, container, false);
        ButterKnife.bind(this, rootView);
        mOperation = getArguments().getString(BUNDLE_DATA);
        mSheetBehavior = BottomSheetBehavior.from(mLayoutBottomSheet);
        mSheetBehavior.setBottomSheetCallback(new BottomSheetCallback(mUpArrow, isAdded()));
        mLottieProgress.setVisibility(View.VISIBLE);
        mBottomSheetUtils.populateBottomSheetWithPDFs(this);
        resetView();
        return rootView;
    }

    @OnClick(R.id.shareImages)
    void onShareFilesClick() {
        if (mOutputFilePaths != null) {
            ArrayList<File> fileArrayList = new ArrayList<>();
            for (String path : mOutputFilePaths) {
                fileArrayList.add(new File(path));
            }
            mFileUtils.shareMultipleFiles(fileArrayList);
        }
    }

    @OnClick(R.id.viewFiles)
    void onViewFilesClick() {
        mBottomSheetUtils.showHideSheet(mSheetBehavior);
    }

  @OnClick(R.id.viewImages)
    void onViewImagesClicked() {
        mActivity.startActivity(ImagesPreviewActivity.getStartIntent(mActivity, mOutputFilePaths));
    }

    @OnClick(R.id.selectFile)
    public void showFileChooser() {
        startActivityForResult(mFileUtils.getFileChooser(),
                INTENT_REQUEST_PICKFILE_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) throws NullPointerException {
        if (data == null || resultCode != RESULT_OK || data.getData() == null)
            return;
        if (requestCode == INTENT_REQUEST_PICKFILE_CODE) {
            mUri = data.getData();
            setTextAndActivateButtons(getFilePath(data.getData()));
        }
    }

    @OnClick(R.id.createImages)
    public void parse() {
        if (mOperation.equals(PDF_TO_IMAGES))
            new PdfToImages(mPath, mUri, this).execute();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        mMorphButtonUtility = new MorphButtonUtility(mActivity);
        mFileUtils = new FileUtils(mActivity);
        mBottomSheetUtils = new BottomSheetUtils(mActivity);
    }

    @Override
    public void onItemClick(String path) {
        mUri = null;
        mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        setTextAndActivateButtons(path);
    }

    private void setTextAndActivateButtons(String path) {
        mCreatedImages.setVisibility(View.GONE);
        options.setVisibility(View.GONE);
        mCreateImagesSuccessText.setVisibility(View.GONE);
        mPath = path;
        mMorphButtonUtility.setTextAndActivateButtons(path,
                mSelectFileButton, mCreateImagesButton);
    }

    @Override
    public void onFileItemClick(String path) {
        mFileUtils.openImage(path);
    }

    @Override
    public void resetView() {
        mPath = null;
        mMorphButtonUtility.initializeButton(mSelectFileButton, mCreateImagesButton);
    }

    @Override
    public void extractionStarted() {
        mMaterialDialog = createAnimationDialog(mActivity);
        mMaterialDialog.show();
    }

    @Override
    public void updateView(int imageCount, ArrayList<String> outputFilePaths) {

        mMaterialDialog.dismiss();
        resetView();
        mOutputFilePaths = outputFilePaths;

        CommonCodeUtils.updateView(mActivity, imageCount, outputFilePaths,
                mCreateImagesSuccessText, options, mCreatedImages, this);
    }

    @Override
    public void onPopulate(ArrayList<String> paths) {
        populateUtil(mActivity, paths, this, mLayout, mLottieProgress, mRecyclerViewFiles);
    }
}
