package com.hexon.androidtest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hexon.androidtest.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    ActivityMainBinding mDataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mDataBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration divier = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        divier.setDrawable(getDrawable(R.drawable.divider));
        mDataBinding.recyclerView.addItemDecoration(divier);
        mDataBinding.recyclerView.setAdapter(new TestListAdapter(this));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private static class TestListAdapter extends RecyclerView.Adapter<TestViewHolder> {
        ArrayList<TestItemData> mTestItemList = new ArrayList<>();
        static final String META_DATA_KEY = "test_item";
        Context mContext;

        public TestListAdapter(Context context) {
            mContext = context;
            loadTestList();
        }

        private void loadTestList() {
            PackageManager manager = mContext.getPackageManager();
            try {
                PackageInfo packageInfo = manager.getPackageInfo(
                        mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
                for (ActivityInfo info : packageInfo.activities) {
                    if (info.metaData != null) {
                        Log.d(TAG, info.metaData.get(META_DATA_KEY).toString());
                        Log.d(TAG, info.toString());
                        mTestItemList.add(new TestItemData(
                                info.metaData.get(META_DATA_KEY).toString(), info.name));
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.list_item_test, parent, false);
            return new TestViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
            holder.setItemPosition(position);
            holder.setItemData(mTestItemList.get(position));
        }

        @Override
        public int getItemCount() {
            Log.d(TAG, "getItemCount:" + mTestItemList.size());
            return mTestItemList.size();
        }
    }

    private static class TestItemData {
        String mTitleName;
        String mActivity;

        public TestItemData(String title, String activity) {
            mTitleName = title;
            mActivity = activity;
        }
    }

    private static class TestViewHolder extends RecyclerView.ViewHolder {
        TestItemData mData;

        public TestViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClassName(v.getContext(), mData.mActivity);
                    v.getContext().startActivity(intent);
                }
            });
        }

        public void setItemData(TestItemData data) {
            mData = data;
            TextView textView = itemView.findViewById(R.id.tv_title);
            textView.setText(mData.mTitleName);
        }

        public void setItemPosition(int position) {
            TextView textView = itemView.findViewById(R.id.tv_number);
            textView.setText(Integer.toString(position));
        }
    }
}