package com.hexon.androidtest;

import android.app.Activity;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hexon.androidtest.databinding.ActivityFeatureBinding;


public class FeatureActivity extends Activity {
    private static final String TAG =  FeatureActivity.class.getSimpleName();
    ActivityFeatureBinding mDataBinding;
    PackageManager mPm;
    FeatureInfo[] mFeartureInfos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_feature);
        mDataBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration divier = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        divier.setDrawable(getDrawable(R.drawable.divider));
        mDataBinding.recyclerView.addItemDecoration(divier);
        mDataBinding.recyclerView.setAdapter(new FeatureListAdapter(this));
        mPm = getPackageManager();
        mFeartureInfos = mPm.getSystemAvailableFeatures();
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        boolean hasVib = vibrator.hasVibrator();
        Log.d(TAG, "hasVib:" + hasVib);
    }

    private class FeatureListAdapter extends RecyclerView.Adapter<FeatureViewHolder> {
        public FeatureListAdapter(Activity activity) {
        }

        @NonNull
        @Override
        public FeatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    android.R.layout.simple_list_item_1, parent, false);
            return new FeatureViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull FeatureViewHolder holder, int position) {
            holder.setItemData(mFeartureInfos[position]);
        }

        @Override
        public int getItemCount() {
            return mFeartureInfos.length;
        }
    }

    private static class FeatureViewHolder extends RecyclerView.ViewHolder {
        FeatureInfo mFeatrueInfo;
        TextView mTvFeature;
        public FeatureViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvFeature = itemView.findViewById(android.R.id.text1);
        }

        public void setItemData(FeatureInfo feartureInfo) {
            mFeatrueInfo = feartureInfo;
            mTvFeature.setText(mFeatrueInfo.name);
        }
    }
}
